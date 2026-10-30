package dev.spud.shadowslave.spell.calling;

import dev.spud.shadowslave.ability.CantorVoice;
import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.citadel.CitadelData;
import dev.spud.shadowslave.citadel.CitadelService;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.dreamrealm.DreamRealmContent;
import dev.spud.shadowslave.dreamrealm.DreamRegion;
import dev.spud.shadowslave.dreamrealm.seed.SeedData;
import dev.spud.shadowslave.network.payload.SpellCallingsPayload;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.spell.SpellContentLoader;
import dev.spud.shadowslave.spell.SpellRecorder;
import dev.spud.shadowslave.spell.SpellRegistry;
import dev.spud.shadowslave.spell.SpellSettings;
import dev.spud.shadowslave.spell.SpellWords;
import dev.spud.shadowslave.spell.truename.TrueNames;
import dev.spud.shadowslave.spell.voice.SpellVoices;
import dev.spud.shadowslave.spell.voice.VoiceRequest;
import dev.spud.shadowslave.trial.TrialData;
import dev.spud.shadowslave.trial.content.TrialContent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The Spell's Callings (DESIGN, Spell Callings section): short lines that
 * nudge a player on, shown over its starry thread-web with a soft sound. The
 * server notices the moments; the voice phrases them; the client shows them and
 * keeps them on the Field Guide's Callings page. How often they come is the
 * player's choice (a client option), and each moment is called only once.
 */
public final class SpellCallings {
    private SpellCallings() {
    }

    public static CallingData data(ServerPlayer player) {
        return player.getData(SpellRegistry.CALLINGS);
    }

    // ---- noticing the moments ------------------------------------------------------------------------------------

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }
        SpellSettings settings = SpellContentLoader.settings();
        if (player.tickCount % settings.checkTicks() != 0) {
            return;
        }
        check(player, settings);
    }

    public static void check(ServerPlayer player, SpellSettings settings) {
        CallingData data = data(player);
        noticeRank(player, data);
        noticeCore(player, data, settings);
        data.idleTicks += settings.checkTicks();
        if (DreamRealm.is(player.level())) {
            noticeRegion(player, data);
            noticeSeeds(player, data, settings);
            noticeCitadels(player, data, settings);
        }
        if (settings.idleMinutes() > 0 && data.idleTicks >= settings.idleMinutes() * 1200
                && call(player, Calling.IDLE, "idle", facts(player), false)) {
            data.idleTicks = 0;
        }
    }

    private static void noticeRank(ServerPlayer player, CallingData data) {
        String rank = CoreService.rank(player).map(SoulRank::serializedName).orElse("");
        if (!rank.equals(data.lastRank)) {
            Optional<SoulRank> now = CoreService.rank(player);
            if (!data.lastRank.equals("?") && now.isPresent()) {
                SpellRecorder.rankUp(player, now.get());
            }
            data.lastRank = rank;
            data.coreCalled = false;
        }
    }

    private static void noticeCore(ServerPlayer player, CallingData data, SpellSettings settings) {
        int limit = CoreService.limit(player);
        int saturation = CoreService.saturation(player);
        if (saturation > data.lastSaturation) {
            progressed(player);
        }
        data.lastSaturation = saturation;
        if (limit <= 0) {
            return;
        }
        if (saturation < limit * settings.coreReset()) {
            data.coreCalled = false;
        } else if (!data.coreCalled && saturation >= limit * settings.coreNearlyFull()) {
            String key = saturation >= limit ? "core_full" : "core_nearly_full";
            data.coreCalled = call(player, Calling.CORE_NEARLY_FULL, key, facts(player), false);
        }
    }

    private static void noticeRegion(ServerPlayer player, CallingData data) {
        Optional<Map.Entry<ResourceLocation, DreamRegion>> region = DreamRealmContent.get().regionAt(player.blockPosition());
        if (region.isEmpty() || data.regions.contains(region.get().getKey().toString())) {
            return;
        }
        Map<String, String> facts = facts(player);
        facts.put("region", region.get().getValue().displayName());
        facts.put("region_id", region.get().getKey().getPath());
        facts.put("danger", region.get().getValue().dangerRank().displayName());
        if (call(player, Calling.NEW_REGION, "new_region", facts, false)) {
            data.regions.add(region.get().getKey().toString());
        }
    }

    private static void noticeSeeds(ServerPlayer player, CallingData data, SpellSettings settings) {
        int next = nextCategory(player);
        if (next < 1 || TrialData.get(player.server).find(player.getUUID()).filter(record -> record.cleared(next)).isPresent()) {
            return;
        }
        for (SeedData.Seed seed : SeedData.get(player.serverLevel()).seeds()) {
            if (data.seeds.contains(seed.id()) || !seed.pos().closerThan(player.blockPosition(), settings.seedRange() * TrueNames.callingReach(player))) {
                continue;
            }
            Optional<Integer> category = seed.echoSite().flatMap(TrialContent.get()::site).map(TrialContent.get()::category);
            if (category.filter(found -> found == next).isEmpty()) {
                continue;
            }
            Map<String, String> facts = facts(player);
            facts.put("category", SpellWords.ordinal(next));
            if (call(player, Calling.SEED_NEXT_CATEGORY, "seed_next_category", facts, false)) {
                data.remember(data.seeds, seed.id());
            }
            return;
        }
    }

    private static void noticeCitadels(ServerPlayer player, CallingData data, SpellSettings settings) {
        Optional<CitadelData.Citadel> near = CitadelService.near(player.serverLevel(), player.blockPosition(),
                        Math.round(settings.citadelRange() * TrueNames.callingReach(player)))
                .filter(citadel -> citadel.hold() != CitadelData.Hold.CLAN && !data.citadels.contains(citadel.id()));
        if (near.isEmpty()) {
            return;
        }
        Map<String, String> facts = facts(player);
        facts.put("citadel", near.get().name());
        facts.put("hold", near.get().hold().name().toLowerCase(java.util.Locale.ROOT));
        if (call(player, Calling.UNCLAIMED_CITADEL, "unclaimed_citadel", facts, false)) {
            data.remember(data.citadels, near.get().id());
        }
    }

    /** The Category of Nightmare a player's soul needs next: the First to be Awakened, the Second to Ascend, and so on. */
    public static int nextCategory(ServerPlayer player) {
        return CoreService.rank(player).map(rank -> rank.ordinal() + 1).orElse(0);
    }

    /** Something moved the player on, so the Spell has no need to call them for a while. */
    public static void progressed(ServerPlayer player) {
        data(player).idleTicks = 0;
    }

    // ---- calling -------------------------------------------------------------------------------------------------

    /** Facts every line may use: the player's Nature, rank and the Category they need next. Never their name. */
    public static Map<String, String> playerFacts(ServerPlayer player) {
        return facts(player);
    }

    static Map<String, String> facts(ServerPlayer player) {
        Map<String, String> facts = new HashMap<>();
        CoreService.rank(player).ifPresent(rank -> facts.put("rank", capitalised(rank.serializedName())));
        player.getData(ModAttachments.IDENTITY).aspect()
                .flatMap(aspect -> AspectKitCatalog.byNature(aspect.natureId()))
                .map(AspectKit::natureName)
                .ifPresent(nature -> facts.put("nature", nature));
        int next = nextCategory(player);
        if (next > 0) {
            facts.put("next_category", SpellWords.ordinal(next));
        }
        // A bearer is closer to the mystical side (ch. 1318): the Spell's lines for them may say a little more.
        TrueNames.nameOf(player).ifPresent(name -> {
            facts.put("named", "yes");
            facts.put("true_name", name);
        });
        return facts;
    }

    /**
     * Calls to the player if their frequency allows it now (or {@code force}):
     * the voice phrases it, it is kept for their Field Guide and shown to them.
     * Returns whether it was called, so the moment can be marked as spent.
     */
    public static boolean call(ServerPlayer player, Calling calling, String key, Map<String, String> facts, boolean force) {
        CallingData data = data(player);
        long now = player.server.overworld().getGameTime();
        if (!force) {
            if (data.frequency == CallingFrequency.OFF) {
                return false;
            }
            if (data.lastCalling > 0 && now - data.lastCalling < gapTicks(player, data.frequency)) {
                return false;
            }
        }
        long seed = player.getRandom().nextLong();
        Optional<String> line = SpellVoices.say(VoiceRequest.of("calling", key, facts, seed));
        if (line.isEmpty()) {
            return false;
        }
        if (CantorVoice.attuned(player)) {
            // Attuned: the Spell says a little more to a Cantor.
            Optional<String> more = SpellVoices.say(VoiceRequest.of("calling_attuned", key, facts, seed));
            if (more.isPresent()) {
                line = Optional.of(line.get() + " " + more.get());
            }
        }
        long day = player.server.overworld().getDayTime() / 24000L + 1;
        CallingData.Entry entry = new CallingData.Entry(calling.id(), line.get(), day);
        data.add(entry, SpellContentLoader.settings().callingsKept());
        data.lastCalling = now;
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.5F, 1.4F);
        send(player, List.of(entry), true);
        return true;
    }

    /** The least time between two Callings for this player: their chosen gap, halved for an Attuned Cantor. */
    public static long gapTicks(ServerPlayer player, CallingFrequency frequency) {
        long gap = Math.round(SpellContentLoader.settings().gapTicks(frequency) * TrueNames.callingGapFactor(player));
        return CantorVoice.attuned(player) ? Math.round(gap * CantorVoice.ATTUNED_GAP) : gap;
    }

    public static void setFrequency(ServerPlayer player, CallingFrequency frequency) {
        data(player).frequency = frequency;
    }

    // ---- syncing -------------------------------------------------------------------------------------------------

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            send(player, data(player).history(), false);
        }
    }

    static void send(ServerPlayer player, List<CallingData.Entry> entries, boolean fresh) {
        if (player.connection == null || !player.connection.hasChannel(SpellCallingsPayload.TYPE)) {
            return;
        }
        List<SpellCallingsPayload.Entry> out = entries.stream()
                .map(entry -> new SpellCallingsPayload.Entry(entry.calling(),
                        Calling.byId(entry.calling()).map(Calling::title).orElse(""), entry.text(), entry.day()))
                .toList();
        PacketDistributor.sendToPlayer(player, new SpellCallingsPayload(out, fresh));
    }

    private static String capitalised(String id) {
        return id.isEmpty() ? id : Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }
}
