package dev.spud.shadowslave.devtools;

import dev.spud.shadowslave.ability.AbilityRegistry;
import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.AspectRollService;
import dev.spud.shadowslave.awakening.AspectRoller;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.network.SoulSyncService;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SpellState;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.soul.core.ShardRank;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectAbilityKind;
import dev.spud.shadowslave.soul.identity.AspectAbilitySetData;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Testing shortcuts behind the dev menu and {@code /shadowslave dev}: swap a
 * player's Nature or the ability on either key in place, set their rank, and
 * top up what they would otherwise grind for. Only operators reach these
 * (see {@link DevCommands}); nothing here is part of normal play.
 */
public final class DevTools {
    /** Permission level the dev tools need: cheats on in single player, or an operator. */
    public static final int PERMISSION = 2;
    /** Player tag that makes abilities cost no essence. A tag, so it survives relogs without an attachment. */
    static final String FREE_CASTING_TAG = "shadowslave.dev_free_casting";

    private DevTools() {
    }

    /** The short name an ability goes by in commands: the last part of its id, such as {@code kindle}. */
    public static String abilityKey(ResourceLocation abilityId) {
        String path = abilityId.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static Optional<ResourceLocation> dormantAbility(String key) {
        return AspectKitCatalog.all().stream().map(AspectKit::abilityId).filter(id -> abilityKey(id).equals(key)).findFirst();
    }

    public static Optional<ResourceLocation> awakenedAbility(String key) {
        return KitLadder.all().stream().map(variant -> variant.abilityId()).filter(id -> abilityKey(id).equals(key)).findFirst();
    }

    public static boolean allowed(ServerPlayer player) {
        return player.hasPermissions(PERMISSION);
    }

    /**
     * Gives the player a fresh Aspect of {@code kit}, keeping the stage they had:
     * an Awakened, Master or Saint keeps their rank and gets the new Nature's first
     * ability of each rank they have reached.
     */
    public static AspectRoller.Roll swapNature(ServerPlayer player, AspectKit kit) {
        Optional<SoulRank> reached = SoulService.get(player).soulRank()
                .filter(rank -> rank.compareTo(SoulRank.AWAKENED) >= 0);
        AbilityService.clearLingering(player);
        AspectRoller.Roll roll = AspectRollService.roll(player, Optional.of(kit));
        reached.ifPresent(top -> {
            raiseAspect(player, top);
            for (SoulRank rank : List.of(SoulRank.AWAKENED, SoulRank.ASCENDED, SoulRank.TRANSCENDENT)) {
                if (rank.compareTo(top) <= 0) {
                    KitLadder.variants(kit.natureId(), rank).stream().findFirst()
                            .ifPresent(variant -> replaceRankAbility(player, rank, Optional.of(variant.abilityId())));
                }
            }
        });
        EssenceService.fill(player);
        refresh(player);
        return roll;
    }

    /** Puts any Nature's Dormant ability on the first key, in place of the one there. */
    public static boolean putDormant(ServerPlayer player, ResourceLocation abilityId) {
        if (AspectKitCatalog.all().stream().noneMatch(kit -> kit.abilityId().equals(abilityId))) {
            return false;
        }
        return replaceRankAbility(player, SoulRank.DORMANT, Optional.of(abilityId));
    }

    /**
     * Puts any Nature's Awakened ability on the second key, in place of the one
     * there, or clears the key when {@code abilityId} is empty. The soul does
     * not have to be Awakened.
     */
    public static boolean putAwakened(ServerPlayer player, Optional<ResourceLocation> abilityId) {
        if (abilityId.isPresent() && KitLadder.byAbility(abilityId.get()).isEmpty()) {
            return false;
        }
        return replaceRankAbility(player, SoulRank.AWAKENED, abilityId);
    }

    private static boolean putAwakened(ServerPlayer player, ResourceLocation abilityId) {
        return putAwakened(player, Optional.of(abilityId));
    }

    /**
     * Sets the soul to Dormant (a Dreamer) or Awakened. Rising grants the
     * Nature's first Awakened ability when the second key is empty; falling
     * takes the Awakened ability away.
     */
    public static boolean setRank(ServerPlayer player, SoulRank rank) {
        SoulIdentityData identity = SoulIdentityService.get(player);
        SoulData soul = SoulService.get(player);
        if (identity.aspect().isEmpty() || soul.aspectId().isEmpty() || soul.flawId().isEmpty()
                || !soul.spellState().isAtOrBeyondDreamer() || rank.compareTo(SoulRank.TRANSCENDENT) > 0) {
            return false;
        }
        // Abilities of ranks above the new one go; each rank up to it keeps or gets one.
        for (SoulRank above : List.of(SoulRank.AWAKENED, SoulRank.ASCENDED, SoulRank.TRANSCENDENT)) {
            if (above.compareTo(rank) > 0) {
                replaceRankAbility(player, above, Optional.empty());
            }
        }
        SoulService.replace(player, switch (rank) {
            case DORMANT -> soul.asDreamer(soul.aspectId().get(), SoulRank.DORMANT, soul.flawId().get());
            case AWAKENED -> soul.asAwakened();
            case ASCENDED -> soul.asRisen(SpellState.MASTER);
            default -> soul.asRisen(SpellState.SAINT);
        });
        raiseAspect(player, rank);
        for (SoulRank reached : List.of(SoulRank.AWAKENED, SoulRank.ASCENDED, SoulRank.TRANSCENDENT)) {
            if (reached.compareTo(rank) <= 0 && rankAbility(SoulIdentityService.get(player), reached).isEmpty()) {
                KitLadder.variants(identity.aspect().get().natureId(), reached).stream().findFirst()
                        .ifPresent(variant -> replaceRankAbility(player, reached, Optional.of(variant.abilityId())));
            }
        }
        CoreService.set(player, 0);
        EssenceService.fill(player);
        refresh(player);
        return true;
    }

    /** Hands over a stack of soul shards of {@code rank}. */
    public static void giveShards(ServerPlayer player, ShardRank rank, int count) {
        ItemStack stack = new ItemStack(ModItems.shardFor(rank), count);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    /** Full health, food and essence, and no lingering effects or fire. */
    public static void restore(ServerPlayer player) {
        player.removeAllEffects();
        player.clearFire();
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);
        EssenceService.fill(player);
        AbilityService.syncStatus(player);
    }

    public static boolean freeCasting(ServerPlayer player) {
        return player.getTags().contains(FREE_CASTING_TAG);
    }

    /** Turns free casting on or off; returns the new state. */
    public static boolean toggleFreeCasting(ServerPlayer player) {
        boolean next = !freeCasting(player);
        if (next) {
            player.addTag(FREE_CASTING_TAG);
            EssenceService.fill(player);
        } else {
            player.removeTag(FREE_CASTING_TAG);
        }
        AbilityService.syncStatus(player);
        return next;
    }

    /** The ability the soul holds for {@code rank}, if any. */
    static Optional<AspectAbilityData> rankAbility(SoulIdentityData identity, SoulRank rank) {
        return identity.aspect().flatMap(aspect -> aspect.abilitySet().abilities().stream()
                .filter(ability -> ability.kind() == AspectAbilityKind.RANK_GRANTED)
                .filter(ability -> ability.acquisitionRank().equals(Optional.of(rank)))
                .findFirst());
    }

    private static boolean replaceRankAbility(ServerPlayer player, SoulRank rank, Optional<ResourceLocation> abilityId) {
        Objects.requireNonNull(abilityId, "abilityId");
        SoulIdentityData identity = SoulIdentityService.get(player);
        Optional<AspectInstanceData> aspect = identity.aspect();
        if (aspect.isEmpty()) {
            return false;
        }
        List<AspectAbilityData> next = new ArrayList<>();
        for (AspectAbilityData ability : aspect.get().abilitySet().abilities()) {
            boolean onThisKey = ability.kind() == AspectAbilityKind.RANK_GRANTED
                    && ability.acquisitionRank().equals(Optional.of(rank));
            boolean duplicate = abilityId.isPresent() && ability.abilityId().equals(abilityId.get());
            if (onThisKey || duplicate) {
                AbilityRegistry.get(ability.abilityId()).ifPresent(held -> held.clear(player));
            } else {
                next.add(ability);
            }
        }
        abilityId.ifPresent(id -> next.add(AspectAbilityData.rankGranted(id, rank, "dev menu")));
        AspectInstanceData changed = aspect.get().withAbilitySet(new AspectAbilitySetData(next));
        SoulIdentityService.replace(player, new SoulIdentityData(Optional.of(changed), identity.flaw()));
        refresh(player);
        return true;
    }

    private static void raiseAspect(ServerPlayer player, SoulRank rank) {
        SoulIdentityData identity = SoulIdentityService.get(player);
        identity.aspect().ifPresent(aspect -> SoulIdentityService.replace(player,
                new SoulIdentityData(Optional.of(aspect.withAspectRank(rank)), identity.flaw())));
        SoulData soul = SoulService.get(player);
        if (soul.aspectId().isPresent() && soul.flawId().isPresent() && !soul.aspectRank().equals(Optional.of(rank))) {
            SoulService.replace(player, soul.withIdentity(soul.aspectId().get(), rank, soul.flawId().get()));
        }
    }

    private static void refresh(ServerPlayer player) {
        SoulSyncService.sync(player);
        CoreService.refresh(player);
        AbilityService.syncStatus(player);
    }
}
