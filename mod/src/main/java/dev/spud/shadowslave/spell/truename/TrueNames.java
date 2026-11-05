package dev.spud.shadowslave.spell.truename;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.network.payload.TrueNamePayload;
import dev.spud.shadowslave.spell.SpellEvent;
import dev.spud.shadowslave.spell.SpellJudge;
import dev.spud.shadowslave.spell.SpellRecord;
import dev.spud.shadowslave.spell.SpellRecorder;
import dev.spud.shadowslave.spell.SpellRegistry;
import dev.spud.shadowslave.spell.calling.Calling;
import dev.spud.shadowslave.spell.calling.SpellCallings;
import dev.spud.shadowslave.spell.voice.SpellVoices;
import dev.spud.shadowslave.spell.voice.VoiceRequest;
import dev.spud.shadowslave.trial.TrialService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * True Names (DESIGN, Run B, approved by Jack 2026-10-30): a rare honorary
 * name the Spell grants its favourites (ch. 9), after a Glorious-or-better
 * appraisal or an extraordinary deed (ch. 154, 364).
 *
 * <ul>
 *   <li>Anchored Soul: a True Name anchors the soul (ch. 644, 1291), so soul and
 *       essence drains, and shards torn from the core, bite less deep.</li>
 *   <li>True-Name Binding: weaving the name into a Memory binds it to its owner
 *       without the higher Bound pattern (ch. 696); see {@link TrueNameBinding}.</li>
 *   <li>Stronger Callings: the bearer is closer to the mystical side (ch. 1318),
 *       so the Spell calls to them more often and from further away.</li>
 *   <li>Secrecy: a name can make its bearer vulnerable to those who know it
 *       (ch. 15, 689). Anyone who learns it deals a little more damage to them and
 *       can sense where they stand once a day. It is hidden unless its bearer
 *       shows it as a title.</li>
 * </ul>
 *
 * <p>Everything here is decided by game code. The voice only phrases: the
 * form a name takes, and the Spell's lines about it.</p>
 */
public final class TrueNames {
    /** Damage that wears at the soul itself, which an Anchored Soul resists. */
    public static final TagKey<DamageType> SOUL_DRAIN = TagKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "soul_drain"));
    static final int CHECK_TICKS = 40;
    private static final String[] WINDS = {"south", "south-west", "west", "north-west", "north", "north-east", "east", "south-east"};

    private TrueNames() {
    }

    public static TrueNameData data(ServerPlayer player) {
        return player.getData(SpellRegistry.TRUE_NAME);
    }

    public static Optional<String> nameOf(ServerPlayer player) {
        TrueNameData data = data(player);
        return data.named() ? Optional.of(data.name()) : Optional.empty();
    }

    public static TrueNameSettings settings() {
        return TrueNameContent.get().settings();
    }

    // ---- granting ------------------------------------------------------------------------------------------------

    /**
     * What the Spell says when it grants a name.
     *
     * @param name      the True Name
     * @param deedLine  what it was granted for
     * @param spellLine the voice's line on it
     */
    public record Grant(String name, String deedLine, String spellLine) {
    }

    /**
     * After a conquered Nightmare's appraisal: a challenger without a name may
     * receive one if the grade opens the chance (only Glorious) and the roll
     * falls their way; the chance is higher when they achieved the impossible. The appraisal screen shows it, so no screen of its own.
     */
    public static Optional<Grant> considerAppraisal(ServerPlayer player, SpellJudge.Judgement judgement) {
        if (data(player).named()) {
            return Optional.empty();
        }
        TrueNameContent content = TrueNameContent.get();
        float chance = content.settings().chanceFor(judgement.grade().name(), judgement.impossible());
        if (chance <= 0.0F) {
            return Optional.empty();
        }
        Optional<Map.Entry<ResourceLocation, TrueNameDeed>> deed = content.best(found -> found.matchesAppraisal(judgement.behaviour().id()));
        if (deed.isEmpty() || player.getRandom().nextFloat() >= chance * deed.get().getValue().chance()) {
            return Optional.empty();
        }
        return grant(player, deed.get().getKey().getPath(), deed.get().getValue(), false);
    }

    /** A server first: some firsts are extraordinary deeds (the first to slay a Great creature). */
    public static void considerFirst(ServerPlayer player, String key) {
        if (data(player).named()) {
            return;
        }
        TrueNameContent.get().best(deed -> deed.matchesFirst(key))
                .filter(deed -> player.getRandom().nextFloat() < deed.getValue().chance())
                .ifPresent(deed -> grant(player, deed.getKey().getPath(), deed.getValue(), true));
    }

    /** A kill outside a Nightmare that may be an extraordinary deed (a Tyrant above your rank, slain alone). */
    public static void considerKill(ServerPlayer player, NightmareCreature creature, boolean aboveRank) {
        if (data(player).named() || TrialService.inside(player)) {
            return;
        }
        TrueNameContent.get().best(deed -> deed.matchesKill(creature.rank(), creature.creatureClass(), aboveRank,
                        alone(player, deed.aloneRange())))
                .filter(deed -> player.getRandom().nextFloat() < deed.getValue().chance())
                .ifPresent(deed -> grant(player, deed.getKey().getPath(), deed.getValue(), true));
    }

    /** A Citadel held against a raid that may be an extraordinary deed (held by one defender alone). */
    public static void considerRaid(ServerPlayer player, boolean alone) {
        if (data(player).named() || TrialService.inside(player)) {
            return;
        }
        TrueNameContent.get().best(deed -> deed.matchesRaid(alone))
                .filter(deed -> player.getRandom().nextFloat() < deed.getValue().chance())
                .ifPresent(deed -> grant(player, deed.getKey().getPath(), deed.getValue(), true));
    }

    /** No other player stands within {@code range} of this one. */
    static boolean alone(ServerPlayer player, int range) {
        if (range <= 0) {
            return true;
        }
        double reach = (double) range * range;
        return player.serverLevel().players().stream()
                .noneMatch(other -> other != player && !other.isSpectator() && other.distanceToSqr(player) <= reach);
    }

    /**
     * Grants a name for a deed: builds one nobody else bears, records it, and
     * tells the player. With {@code reveal}, the Spell's reveal screen opens
     * (once they are out of any Nightmare).
     */
    public static Optional<Grant> grant(ServerPlayer player, String deedId, TrueNameDeed deed, boolean reveal) {
        TrueNameSettings settings = settings();
        SpellRecord record = SpellRecord.get(player.server);
        Map<String, String> facts = SpellCallings.playerFacts(player);
        Optional<String> built = TrueNameBuilder.build(deedId, deed, facts, player.getRandom().nextLong(), settings.nameAttempts(),
                SpellVoices::say, request -> SpellVoices.lines().pick(request), record::nameTaken);
        if (built.isEmpty()) {
            ShadowSlaveMod.LOGGER.warn("The Spell found no free True Name for {} (deed {})", player.getScoreboardName(), deedId);
            return Optional.empty();
        }
        String name = built.get();
        TrueNameData data = data(player);
        data.grant(name, deedId, deed.deed(), player.server.overworld().getDayTime() / 24000L + 1);
        record.setTrueName(player.getUUID(), name);
        SpellRecorder.record(player.server, SpellEvent.Kind.TRUE_NAME, Optional.of(player), deedId, "");
        Map<String, String> named = new HashMap<>(facts);
        named.put("true_name", name);
        named.put("deed", deedId);
        String spellLine = SpellVoices.say(VoiceRequest.of(TrueNameBuilder.TOPIC, "granted", named, player.getRandom().nextLong()),
                "You have received a True Name. Your True Name is " + name + ".");
        DreamCrossing.spell(player, Component.literal(spellLine).withStyle(ChatFormatting.GOLD));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.6F);
        SpellRecorder.first(player, "true_name", "a True Name", "true_name", "earn a True Name");
        ShadowSlaveMod.LOGGER.info("The Spell granted {} a True Name for deed {}", player.getScoreboardName(), deedId);
        if (reveal) {
            data.pendingReveal = true;
            showPendingReveal(player);
        }
        sync(player);
        return Optional.of(new Grant(name, deed.deed(), spellLine));
    }

    /** Takes a player's name away again (an operator's tool; the Spell itself never does). */
    public static void forget(ServerPlayer player) {
        TrueNameData data = data(player);
        data.clear();
        SpellRecord.get(player.server).setTrueName(player.getUUID(), "");
        refreshName(player);
        sync(player);
    }

    // ---- the title -----------------------------------------------------------------------------------------------

    /**
     * Shows or hides the name as a title. Showing it tells everyone who can
     * read it: every player on the server learns it, and so does anyone who
     * joins while it shows. A name once learned is not forgotten when hidden again.
     */
    public static boolean setShown(ServerPlayer player, boolean shown) {
        TrueNameData data = data(player);
        if (!data.named() || data.shown == shown) {
            return false;
        }
        data.shown = shown;
        refreshName(player);
        if (shown) {
            for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
                if (other != player) {
                    learn(other, player, "It is written over " + player.getGameProfile().getName() + " for all to read.");
                }
            }
        }
        sync(player);
        return true;
    }

    private static void refreshName(ServerPlayer player) {
        player.refreshDisplayName();
        player.refreshTabListName();
    }

    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            title(player).ifPresent(title -> event.setDisplayname(event.getDisplayname().copy().append(title)));
        }
    }

    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            title(player).ifPresent(title -> event.setDisplayName(
                    (event.getDisplayName() == null ? player.getName() : event.getDisplayName()).copy().append(title)));
        }
    }

    private static Optional<Component> title(ServerPlayer player) {
        TrueNameData data = data(player);
        return data.named() && data.shown ? Optional.of(Component.literal(", " + data.name()).withStyle(ChatFormatting.GOLD)) : Optional.empty();
    }

    // ---- learning ------------------------------------------------------------------------------------------------

    /** {@code knower} learns {@code bearer}'s name. Returns false if they had no name or it was already known. */
    public static boolean learn(ServerPlayer knower, ServerPlayer bearer, String how) {
        return nameOf(bearer).filter(name -> learn(knower, bearer.getUUID(), bearer.getGameProfile().getName(), name, how)).isPresent();
    }

    /** Learns a name as written down or spoken. Nobody learns their own. */
    public static boolean learn(ServerPlayer knower, UUID bearer, String bearerName, String name, String how) {
        if (knower.getUUID().equals(bearer) || !data(knower).learn(bearer, bearerName, name)) {
            return false;
        }
        SpellRecorder.record(knower.server, SpellEvent.Kind.NAME_LEARNED, Optional.of(knower), bearerName, "");
        Map<String, String> facts = new HashMap<>(SpellCallings.playerFacts(knower));
        facts.put("true_name", name);
        String line = SpellVoices.say(VoiceRequest.of(TrueNameBuilder.TOPIC, "learned", facts, knower.getRandom().nextLong()),
                "You know a True Name now: " + name + ".");
        DreamCrossing.spell(knower, Component.literal(line + " " + how).withStyle(ChatFormatting.LIGHT_PURPLE));
        knower.serverLevel().playSound(null, knower.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 0.8F);
        sync(knower);
        return true;
    }

    /** The bearer speaks their name to one player close by. */
    public static boolean reveal(ServerPlayer bearer, ServerPlayer listener) {
        Optional<String> name = nameOf(bearer);
        if (name.isEmpty()) {
            tell(bearer, "You have no True Name to speak.", ChatFormatting.RED);
            return false;
        }
        int range = settings().revealRange();
        if (listener == bearer || listener.level() != bearer.level() || listener.distanceToSqr(bearer) > (double) range * range) {
            tell(bearer, "Speak it close: they must stand within " + range + " blocks of you.", ChatFormatting.RED);
            return false;
        }
        if (!learn(listener, bearer, bearer.getGameProfile().getName() + " spoke it to you.")) {
            tell(bearer, listener.getGameProfile().getName() + " already knows your True Name.", ChatFormatting.GRAY);
            return false;
        }
        tell(bearer, "You speak your True Name to " + listener.getGameProfile().getName() + ". They will not forget it.", ChatFormatting.GOLD);
        return true;
    }

    // ---- sensing -------------------------------------------------------------------------------------------------

    public enum Sensed { SENSED, UNKNOWN, QUIET, ABSENT }

    /**
     * Speaks a known True Name to sense where its bearer stands: a direction
     * and rough distance, or the world they are in. Once a day for each name.
     * The bearer feels it, but not who spoke.
     */
    public static Sensed sense(ServerPlayer knower, String spoken) {
        TrueNameData data = data(knower);
        Optional<TrueNameData.Known> known = data.knowsName(spoken);
        if (known.isEmpty()) {
            tell(knower, "You know no True Name like that.", ChatFormatting.RED);
            return Sensed.UNKNOWN;
        }
        long now = knower.server.overworld().getGameTime();
        long ready = readyAt(known.get());
        if (now < ready) {
            tell(knower, known.get().name() + " is quiet. It will answer again in " + minutes(ready - now) + ".", ChatFormatting.GRAY);
            return Sensed.QUIET;
        }
        ServerPlayer bearer = knower.server.getPlayerList().getPlayer(known.get().bearer());
        if (bearer == null || nameOf(bearer).filter(name -> name.equals(known.get().name())).isEmpty()) {
            tell(knower, known.get().name() + " does not answer. Its bearer is not under the Spell now.", ChatFormatting.GRAY);
            return Sensed.ABSENT;
        }
        data.sensed(bearer.getUUID(), now);
        SpellRecorder.record(knower.server, SpellEvent.Kind.NAME_SENSED, Optional.of(knower), bearer.getGameProfile().getName(), "");
        DreamCrossing.spell(knower, Component.literal(where(knower, bearer, known.get().name())).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (bearer.level() == knower.level()) {
            trail(knower, bearer.position());
        }
        knower.serverLevel().playSound(null, knower.blockPosition(), SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 1.0F, 0.7F);
        Map<String, String> facts = new HashMap<>(SpellCallings.playerFacts(bearer));
        facts.put("true_name", known.get().name());
        SpellCallings.call(bearer, Calling.NAME, "name_spoken", facts, true);
        sync(knower);
        return Sensed.SENSED;
    }

    /** When a known name can be sensed again. */
    public static long readyAt(TrueNameData.Known known) {
        return known.lastSensed() == Long.MIN_VALUE ? Long.MIN_VALUE : known.lastSensed() + settings().senseCooldown();
    }

    static String where(ServerPlayer knower, ServerPlayer bearer, String name) {
        if (bearer.level() != knower.level()) {
            return name + " answers from far away, beyond " + dimensionName(knower) + ".";
        }
        Vec3 to = bearer.position().subtract(knower.position());
        double distance = Math.sqrt(to.x * to.x + to.z * to.z);
        if (distance < 16) {
            return name + " answers from very close. Its bearer is near enough to touch.";
        }
        long rounded = Math.max(10, Math.round(distance / 10.0) * 10);
        return name + " answers from the " + wind(to.x, to.z) + ", about " + rounded + " blocks away.";
    }

    /** The eight winds, from a direction on the ground (Minecraft's north is -z). */
    static String wind(double dx, double dz) {
        double degrees = Math.toDegrees(Math.atan2(-dx, dz));
        int index = Math.floorMod((int) Math.round(degrees / 45.0), 8);
        return WINDS[index];
    }

    private static String dimensionName(ServerPlayer player) {
        String path = player.level().dimension().location().getPath();
        return "overworld".equals(path) ? "the waking world" : "dream_realm".equals(path) ? "the Dream Realm" : "this world";
    }

    /** A short line of soul fire toward the bearer, seen only by the one who spoke. */
    private static void trail(ServerPlayer knower, Vec3 target) {
        Vec3 from = knower.getEyePosition().subtract(0, 0.4, 0);
        Vec3 step = target.add(0, 1.0, 0).subtract(from).normalize();
        for (int i = 2; i <= 24; i++) {
            Vec3 at = from.add(step.scale(i * 0.5));
            knower.serverLevel().sendParticles(knower, ParticleTypes.SOUL_FIRE_FLAME, true, at.x, at.y, at.z, 2, 0.04, 0.04, 0.04, 0.0);
        }
    }

    static String minutes(long ticks) {
        long minutes = Math.max(1, (ticks + 1199) / 1200);
        return minutes == 1 ? "a minute" : minutes + " minutes";
    }

    // ---- what a name does ----------------------------------------------------------------------------------------

    /** The share of a soul or essence drain that still lands: less for an Anchored Soul. */
    public static float drainFactor(ServerPlayer player) {
        return data(player).named() ? 1.0F - settings().drainResist() : 1.0F;
    }

    public static float callingGapFactor(ServerPlayer player) {
        return data(player).named() ? settings().callingGapFactor() : 1.0F;
    }

    public static float callingReach(ServerPlayer player) {
        return data(player).named() ? settings().callingReach() : 1.0F;
    }

    /** Anchored Soul against soul-draining damage, and the edge anyone who knows the name has against its bearer. */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim) || !data(victim).named()) {
            return;
        }
        event.setAmount(event.getAmount() * damageFactor(victim, event.getSource().is(SOUL_DRAIN),
                event.getSource().getEntity() instanceof ServerPlayer attacker ? Optional.of(attacker) : Optional.empty()));
    }

    static float damageFactor(ServerPlayer victim, boolean soulDrain, Optional<ServerPlayer> attacker) {
        float factor = soulDrain ? drainFactor(victim) : 1.0F;
        String name = data(victim).name();
        if (attacker.filter(found -> found != victim)
                .flatMap(found -> data(found).knows(victim.getUUID()))
                .filter(known -> known.name().equals(name)).isPresent()) {
            factor *= 1.0F + settings().knownDamageBonus();
        }
        return factor;
    }

    // ---- ticking, joining and syncing ----------------------------------------------------------------------------

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % CHECK_TICKS == 0 && data(player).pendingReveal) {
            showPendingReveal(player);
        }
    }

    /** Opens the reveal screen for a name granted earlier, once nothing is in the way. */
    static void showPendingReveal(ServerPlayer player) {
        TrueNameData data = data(player);
        if (!data.pendingReveal || !data.named() || TrialService.inside(player) || player.connection == null
                || !player.connection.hasChannel(TrueNamePayload.TYPE)) {
            return;
        }
        data.pendingReveal = false;
        PacketDistributor.sendToPlayer(player, payload(player, true));
    }

    /** A Nightmare's appraisal screen showed the name, so it needs no screen of its own. */
    public static void revealedByAppraisal(ServerPlayer player) {
        data(player).pendingReveal = false;
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
            if (other != player && data(other).named() && data(other).shown) {
                learn(player, other, "It is written over " + other.getGameProfile().getName() + " for all to read.");
            }
        }
        sync(player);
        refreshName(player);
    }

    public static void sync(ServerPlayer player) {
        if (player.connection != null && player.connection.hasChannel(TrueNamePayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, payload(player, false));
        }
    }

    static TrueNamePayload payload(ServerPlayer player, boolean reveal) {
        TrueNameData data = data(player);
        TrueNameSettings settings = settings();
        List<TrueNamePayload.Known> known = new ArrayList<>();
        for (TrueNameData.Known entry : data.known()) {
            known.add(new TrueNamePayload.Known(entry.name(), entry.bearerName(), readyAt(entry)));
        }
        String spellLine = "";
        if (reveal) {
            Map<String, String> facts = new HashMap<>(SpellCallings.playerFacts(player));
            facts.put("true_name", data.name());
            facts.put("deed", data.deed());
            spellLine = SpellVoices.say(VoiceRequest.of(TrueNameBuilder.TOPIC, "reveal", facts, player.getRandom().nextLong()), "");
        }
        return new TrueNamePayload(data.name(), data.deedLine(), spellLine, data.shown(),
                Math.round(settings.drainResist() * 100), Math.round(settings.knownDamageBonus() * 100),
                settings.bindEssence(), known, reveal);
    }

    private static void tell(Player player, String message, ChatFormatting colour) {
        player.displayClientMessage(Component.literal(message).withStyle(colour), false);
    }
}
