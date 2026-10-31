package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.network.payload.AbilityCastPayload;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectAbilityKind;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import dev.spud.shadowslave.soul.refinement.RefinementRules;
import dev.spud.shadowslave.soul.refinement.RefinementService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Server-authoritative handling of the ability keys: pick the ability on that
 * key, check the essence pool, run it. Abilities have no cooldowns; every one
 * of them draws on the same pool (see {@link EssenceService}).
 */
public final class AbilityService {
    /** Blocks within which other players see a casting pose. */
    static final double CAST_VIEW_DISTANCE = 64.0;

    private AbilityService() {
    }

    public enum Outcome {
        USED,
        NO_ASPECT,
        NO_ABILITY,
        DRAINED,
        PRIMED,
        FAILED
    }

    public static Outcome useAbility(ServerPlayer player, int slot) {
        Objects.requireNonNull(player, "player");
        SoulIdentityData identity = SoulIdentityService.get(player);
        if (!identity.isRevealed()) {
            feedback(player, "Your Aspect has not been named. Sleep beside a lit Dream Candle.", ChatFormatting.GRAY);
            return Outcome.NO_ASPECT;
        }
        List<ResourceLocation> held = abilityIds(identity, AbilityRegistry::isActivatable);
        if (slot < 0 || slot >= held.size()) {
            feedback(player, held.isEmpty() ? "Your Aspect has no ability to call on yet."
                    : "That ability has not awakened yet.", ChatFormatting.GRAY);
            return Outcome.NO_ABILITY;
        }

        if (VesselBond.hollowed(player)) {
            feedback(player, "Hollowed: your own abilities are locked while you carry your Echo.", ChatFormatting.GRAY);
            return Outcome.FAILED;
        }
        AspectAbility ability = AbilityRegistry.get(held.get(slot)).orElseThrow();
        if (!ability.primed(player) && !ScribeLore.inked(player)) {
            ScribeLore.dry(player);
            return Outcome.FAILED;
        }
        int cost = costFor(player, ability);
        if (!ability.primed(player) && !EssenceService.covers(player, cost)) {
            feedback(player, ability.displayName() + " needs " + cost + " essence", ChatFormatting.GRAY);
            syncStatus(player);
            return Outcome.DRAINED;
        }
        Outcome outcome = switch (ability.activate(player)) {
            case USED -> {
                finish(player, ability);
                yield Outcome.USED;
            }
            case PRIMED -> Outcome.PRIMED;
            case FAILED -> Outcome.FAILED;
        };
        if (outcome != Outcome.FAILED) {
            broadcastCast(player, ability);
            CantorVoice.onAbilityUsed(player);
            MasqueFace.onAbilityUsed(player, ability);
        }
        syncStatus(player);
        return outcome;
    }

    /** Spends an ability's essence and confirms it on the action bar; abilities that finish on their own call this. */
    public static void finish(ServerPlayer player, AspectAbility ability) {
        EssenceService.spend(player, costFor(player, ability));
        ScribeLore.spendInk(player);
        feedback(player, ability.displayName(), ChatFormatting.LIGHT_PURPLE);
        syncStatus(player);
    }

    /** Essence this player pays for one use of the ability. */
    public static int costFor(ServerPlayer player, AspectAbility ability) {
        return SurveyorCraft.costFor(player,
                KeeperHold.costFor(player, MasonWorks.costFor(player, RefinementRules.cost(ability.essenceCost(), RefinementService.get(player)))));
    }

    /** Everyone nearby, the caster included, plays the casting pose. */
    private static void broadcastCast(ServerPlayer caster, AspectAbility ability) {
        AbilityCastPayload payload = new AbilityCastPayload(caster.getId(), ability.id().toString());
        for (ServerPlayer viewer : caster.serverLevel().players()) {
            if (viewer.distanceToSqr(caster) <= CAST_VIEW_DISTANCE * CAST_VIEW_DISTANCE
                    && viewer.connection != null && viewer.connection.hasChannel(AbilityCastPayload.TYPE)) {
                PacketDistributor.sendToPlayer(viewer, payload);
            }
        }
    }

    /** What the HUD should show right now for this player. */
    public static AbilityStatusPayload status(ServerPlayer player) {
        List<AbilityStatusPayload.Slot> slots = held(player).stream()
                .map(ability -> new AbilityStatusPayload.Slot(ability.id().toString(), ability.displayName(),
                        costFor(player, ability), ability.primed(player)))
                .toList();
        if (slots.isEmpty()) {
            return AbilityStatusPayload.NONE;
        }
        return new AbilityStatusPayload(slots, EssenceService.current(player), EssenceService.capacity(player),
                EssenceService.regenPerTick(player), ChannelService.strikePrimed(player), FuryRage.status(player));
    }

    /** Sends the HUD state to the owning client, if it has the mod's channel. */
    public static void syncStatus(ServerPlayer player) {
        if (player.connection != null && player.connection.hasChannel(AbilityStatusPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, status(player));
        }
    }

    /** Refills essence and lets held abilities keep any lingering state going (marks, stances). */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        List<AspectAbility> held = held(player);
        held.forEach(ability -> ability.tick(player));
        boolean strikeFaded = ChannelService.tick(player);
        if ((EssenceService.tick(player) || strikeFaded) && !held.isEmpty()) {
            syncStatus(player);
        }
    }

    /** Takes down anything an ability left in the world (walls, wards) when its owner leaves. */
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearLingering(player);
            EssenceService.forget(player.getUUID());
        }
    }

    /** Takes down what abilities left in the world and forgets every kit's running state, as when the Aspect changes. */
    public static void clearLingering(ServerPlayer player) {
        AbilityRegistry.all().forEach(ability -> ability.clear(player));
        ForewarningService.forget(player.getUUID());
        DreamKitPassives.forget(player.getUUID());
        SignatureKitPassives.forget(player.getUUID());
        FuryRage.forget(player.getUUID());
        AnchorWeight.forget(player.getUUID());
        Stasis.forget(player.getUUID());
        HeraldCommand.forget(player.getUUID());
        KeeperHold.forget(player.getUUID());
        FortuneLuck.forget(player.getUUID());
        CantorVoice.forget(player.getUUID());
        MasqueFace.forget(player.getUUID());
        UnbrokenWill.forget(player.getUUID());
        SaboteurCraft.forget(player.getUUID());
        ScribeLore.forget(player.getUUID());
        SurveyorCraft.forget(player.getUUID());
        StandardColours.lower(player.server, player.getUUID());
    }

    private static List<AspectAbility> held(ServerPlayer player) {
        return abilityIds(SoulIdentityService.get(player), AbilityRegistry::isActivatable).stream()
                .map(AbilityRegistry::get)
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * The abilities on the keys, in key order: granted abilities that have an
     * activatable implementation, the lowest rank first, so the Dormant
     * ability stays on the first key. Legacy unclassified abilities from older
     * saves come before everything. Innate rules are passive and never listed.
     */
    static List<ResourceLocation> abilityIds(SoulIdentityData identity, Predicate<ResourceLocation> activatable) {
        return identity.aspect()
                .map(aspect -> aspect.abilitySet().abilities().stream()
                        .filter(ability -> ability.kind() != AspectAbilityKind.INNATE)
                        .filter(ability -> activatable.test(ability.abilityId()))
                        .sorted(Comparator.comparingInt(AbilityService::rankOrder))
                        .map(AspectAbilityData::abilityId)
                        .toList())
                .orElseGet(List::of);
    }

    public static boolean possessesAspectAbility(SoulIdentityData identity, ResourceLocation abilityId) {
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(abilityId, "abilityId");
        return identity.aspect().map(aspect -> aspect.abilitySet().contains(abilityId)).orElse(false);
    }

    private static int rankOrder(AspectAbilityData ability) {
        return ability.acquisitionRank().map(Enum::ordinal).orElse(-1);
    }

    public static void feedback(ServerPlayer player, String text, ChatFormatting colour) {
        player.displayClientMessage(Component.literal(text).withStyle(colour), true);
    }
}
