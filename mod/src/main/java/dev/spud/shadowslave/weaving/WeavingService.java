package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.KitRules;
import dev.spud.shadowslave.ability.MasterworkAbility;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import dev.spud.shadowslave.memory.MemoryInstanceData;
import dev.spud.shadowslave.network.payload.WeaveTracePayload;
import dev.spud.shadowslave.network.payload.WeavingStatusPayload;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server side of Weaving: the table's buttons, Spell-granted patterns, and keeping the client's view current. */
public final class WeavingService {
    public static final ResourceLocation FORGING = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "forging");
    /** Thrifty Hands (Forge): Forging XP earned, as a percentage of the usual. */
    static final int FORGE_XP_PERCENT = 150;
    /** What each player's last press of Weave did, for the table screen. Not saved. */
    private static final Map<UUID, String> LAST_RESULT = new ConcurrentHashMap<>();

    private WeavingService() {
    }

    public static WeavingData get(ServerPlayer player) {
        return player.getData(WeavingRegistry.WEAVING);
    }

    public static void set(ServerPlayer player, WeavingData data) {
        player.setData(WeavingRegistry.WEAVING, data);
    }

    private static Optional<SoulRank> soulRank(ServerPlayer player) {
        return SoulService.get(player).soulRank();
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        LAST_RESULT.remove(player.getUUID());
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, opener) -> new WeavingMenu(id, inventory, ContainerLevelAccess.create(player.level(), pos)),
                Component.translatable("container.shadowslave.weaving_table")));
        sync(player);
    }

    static void handleButton(ServerPlayer player, WeavingMenu menu, int button) {
        WeavingContent content = WeavingContent.current();
        if (button == WeavingMenu.BUTTON_RESPEC) {
            respec(player);
        } else if (button == WeavingMenu.BUTTON_LAY) {
            StrandService.layThread(player, menu);
        } else if (button >= WeavingMenu.BUTTON_RAISE && button < WeavingMenu.BUTTON_RAISE + content.branches().size()) {
            raise(player, content.branchOrder().get(button - WeavingMenu.BUTTON_RAISE));
        } else if (button >= WeavingMenu.BUTTON_STUDY && button < WeavingMenu.BUTTON_STUDY + WeavingMenu.MAX_LISTED) {
            StrandService.startStudy(player, menu, button - WeavingMenu.BUTTON_STUDY);
        } else if (button >= WeavingMenu.BUTTON_STUDIED && button < WeavingMenu.BUTTON_STUDIED + WeavingMenu.MAX_LISTED) {
            StrandService.finishStudy(player, menu, button - WeavingMenu.BUTTON_STUDIED);
        } else if (button >= WeavingMenu.BUTTON_UNRAVEL && button < WeavingMenu.BUTTON_UNRAVEL + WeavingMenu.MAX_LISTED) {
            StrandService.unravel(player, menu, button - WeavingMenu.BUTTON_UNRAVEL);
        }
    }

    /** A traced weave from the table's screen. The table must be open; the server judges the trace. */
    public static void weaveTrace(ServerPlayer player, WeaveTracePayload payload) {
        if (!(player.containerMenu instanceof WeavingMenu menu) || !menu.stillValid(player)) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(payload.strand());
        Optional<StrandRestriction> restriction = java.util.Arrays.stream(StrandRestriction.values())
                .filter(value -> value.getSerializedName().equals(payload.restriction())).findFirst();
        if (id == null || (!payload.restriction().isEmpty() && restriction.isEmpty())) {
            return;
        }
        List<Float> samples = payload.trace();
        float[] trace = new float[Math.min(samples.size(), StrandRules.TRACE_LIMIT * 2) & ~1];
        for (int i = 0; i < trace.length; i++) {
            float value = samples.get(i);
            trace[i] = Float.isFinite(value) ? net.minecraft.util.Mth.clamp(value, -2.0F, 2.0F) : 0.0F;
        }
        StrandService.weave(player, menu, id, payload.tier(), restriction, trace);
    }

    /** Forge's gifts at the hearth: Masterwork's extra Tier while it lasts, and Thrifty Hands' quicker Forging. */
    public static WeavingRules.Boons boons(ServerPlayer player) {
        boolean forge = KitRules.hasInnate(player, AspectKitCatalog.FORGE.innateId());
        return new WeavingRules.Boons(MasterworkAbility.active(player) ? MasterworkAbility.TIER_BONUS : 0,
                forge ? Optional.of(FORGING) : Optional.empty(), forge ? FORGE_XP_PERCENT : 100);
    }

    public static boolean raise(ServerPlayer player, ResourceLocation branch) {
        WeavingContent content = WeavingContent.current();
        Optional<WeavingData> next = WeavingRules.raise(content, get(player), branch);
        if (next.isEmpty()) {
            report(player, "You can't raise " + content.branchName(branch) + " yet.");
            return false;
        }
        set(player, next.get());
        report(player, content.branchName(branch) + " rises to level " + WeavingRules.heldLevel(content, next.get(), branch) + ".");
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.4F);
        return true;
    }

    public static boolean respec(ServerPlayer player) {
        WeavingContent content = WeavingContent.current();
        WeavingData before = get(player);
        Optional<WeavingData> next = WeavingRules.respec(before);
        if (next.isEmpty()) {
            report(player, "No points to take back.");
            return false;
        }
        int refunded = WeavingRules.pointsSpent(content, before);
        set(player, next.get());
        report(player, refunded + (refunded == 1 ? " point returns" : " points return") + " to you. Deeds you earned stay earned.");
        return true;
    }

    /** The Spell shows a pattern the first time a player slays the creature it names. */
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide || event.isCanceled()) {
            return;
        }
        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer player ? player
                : victim.getKillCredit() instanceof ServerPlayer player ? player : null;
        if (killer != null && killer != victim) {
            grantForKill(killer, BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()));
        }
    }

    /** Teaches any patterns slaying this entity grants. Returns the ones learned. */
    public static List<ResourceLocation> grantForKill(ServerPlayer player, ResourceLocation entity) {
        WeavingContent content = WeavingContent.current();
        WeavingData data = get(player);
        List<ResourceLocation> granted = WeavingRules.grantsForKill(content, data, entity);
        if (granted.isEmpty()) {
            return granted;
        }
        for (ResourceLocation id : granted) {
            data = data.withLearned(id);
            String name = content.name(content.recipes().get(id));
            player.sendSystemMessage(Component.literal("The Spell shows you a Weaving pattern: " + name + ".")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        set(player, data);
        sync(player);
        return granted;
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_RESULT.remove(event.getEntity().getUUID());
        StrandService.forget(event.getEntity().getUUID());
    }

    public static void report(ServerPlayer player, String message) {
        LAST_RESULT.put(player.getUUID(), message);
        sync(player);
    }

    public static void sync(ServerPlayer player) {
        // Fake and game-test players have no negotiated mod channel to send on.
        if (player.connection == null || !player.connection.hasChannel(WeavingStatusPayload.TYPE)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, status(player));
    }

    public static String lastResult(ServerPlayer player) {
        return LAST_RESULT.getOrDefault(player.getUUID(), "");
    }

    public static WeavingStatusPayload status(ServerPlayer player) {
        WeavingContent content = WeavingContent.current();
        List<ResourceLocation> lessons = player.containerMenu instanceof WeavingMenu menu ? StrandService.teaches(content, menu.centre()) : List.of();
        return status(content, get(player), soulRank(player), lastResult(player), lessons, StrandService.essencePercent(player));
    }

    static WeavingStatusPayload status(WeavingContent content, WeavingData data, Optional<SoulRank> soulRank, String lastResult) {
        return status(content, data, soulRank, lastResult, List.of());
    }

    /** {@code lessons} are the strands the piece on an open Weaving Table teaches. */
    static WeavingStatusPayload status(WeavingContent content, WeavingData data, Optional<SoulRank> soulRank, String lastResult,
                                       List<ResourceLocation> lessons) {
        return status(content, data, soulRank, lastResult, lessons, 100);
    }

    /** {@code essencePercent} is the share of each essence cost the player pays (Wellspring pays less). */
    static WeavingStatusPayload status(WeavingContent content, WeavingData data, Optional<SoulRank> soulRank, String lastResult,
                                       List<ResourceLocation> lessons, int essencePercent) {
        List<WeavingStatusPayload.Branch> branches = new ArrayList<>();
        content.branches().forEach((id, branch) -> branches.add(new WeavingStatusPayload.Branch(
                branch.name(), WeavingRules.heldLevel(content, data, id), branch.maxLevel(), data.xp(id),
                nextText(content, data, id), WeavingRules.canRaise(content, data, id))));
        List<WeavingStatusPayload.Pattern> patterns = new ArrayList<>();
        List<WeavingStatusPayload.Hint> hints = new ArrayList<>();
        content.recipes().forEach((id, recipe) -> {
            if (WeavingRules.knows(data, id, recipe)) {
                patterns.add(new WeavingStatusPayload.Pattern(content.name(recipe), recipe.result().toString(),
                        content.branchName(recipe.branch()), content.tier(recipe), WeavingRules.rankName(content.rank(recipe)),
                        recipe.materials().stream().map(material -> new WeavingStatusPayload.Material(material.item().toString(), material.count())).toList(),
                        data.woven(id), WeavingRules.lock(content, data, recipe, soulRank).orElse("")));
            } else if (!data.hint(id).isEmpty()) {
                hints.add(new WeavingStatusPayload.Hint(content.branchName(recipe.branch()), content.tier(recipe), recipe.materials().size(),
                        data.hint(id).stream().map(ResourceLocation::toString).toList(), recipe.hint()));
            }
        });
        int patternsLevel = WeavingRules.heldLevel(content, data, StrandService.PATTERNS);
        List<WeavingStatusPayload.Strand> strands = new ArrayList<>();
        content.strands().forEach((id, strand) -> {
            if (!StrandRules.knows(data.studied(), id, strand)) {
                return;
            }
            List<Float> points = new ArrayList<>();
            StrandRules.points(id, strand).forEach(point -> {
                points.add(point.x());
                points.add(point.y());
            });
            String lock = strand.minRank() != SoulRank.DORMANT && soulRank.map(rank -> rank.ordinal() < strand.minRank().ordinal()).orElse(true)
                    ? "Needs " + WeavingRules.rankName(strand.minRank()) + " soul" : "";
            strands.add(new WeavingStatusPayload.Strand(id.toString(), strand.name(), strand.description(),
                    String.join(", ", strand.fits().stream().map(StrandFit::plural).toList()), strand.tunable(),
                    strand.material().item().toString(), strand.material().counts(),
                    strand.essence().stream().map(cost -> StrandRules.discounted(cost, essencePercent)).toList(), points, data.laid(id), lock));
        });
        List<WeavingStatusPayload.Lesson> table = lessons.stream()
                .map(id -> new WeavingStatusPayload.Lesson(id.toString(), content.strand(id).map(StrandPattern::name).orElse(id.getPath()),
                        content.strand(id).map(strand -> StrandRules.knows(data.studied(), id, strand)).orElse(false)))
                .toList();
        return new WeavingStatusPayload(WeavingRules.unspent(content, data), WeavingRules.xpTowardPoint(content, data),
                content.settings().xpPerPoint(), WeavingRules.canRespec(data), branches, patterns, hints, lastResult,
                strands, table, Math.min(StrandRules.soulCap(soulRank), patternsLevel), patternsLevel,
                WeavingRules.heldLevel(content, data, StrandService.FORGERY));
    }

    static String nextText(WeavingContent content, WeavingData data, ResourceLocation branch) {
        WeavingRules.NextStep next = WeavingRules.next(content, data, branch);
        if (next.maxed()) {
            return "Every level is yours.";
        }
        if (next.deed().isPresent()) {
            WeavingBranch.Deed deed = next.deed().get();
            return "Level " + next.level() + " is a deed: " + deed.description() + " (" + next.progress() + "/" + deed.count() + ")";
        }
        return "Level " + next.level() + " costs " + next.cost() + (next.cost() == 1 ? " point" : " points");
    }
}
