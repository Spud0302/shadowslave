package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.network.payload.AbilityCastPayload;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectAbilityKind;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/** Server-authoritative handling of the ability key: pick the ability, check the cooldown, run it. */
public final class AbilityService {
    /** Blocks within which other players see a casting pose. */
    static final double CAST_VIEW_DISTANCE = 64.0;

    private AbilityService() {
    }

    public enum Outcome {
        USED,
        NO_ASPECT,
        NO_ABILITY,
        RECOVERING,
        PRIMED,
        FAILED
    }

    public static Outcome useAbility(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        SoulIdentityData identity = SoulIdentityService.get(player);
        if (!identity.isRevealed()) {
            feedback(player, "Your Aspect has not been named. Sleep beside a lit Dream Candle.", ChatFormatting.GRAY);
            return Outcome.NO_ASPECT;
        }
        Optional<AspectAbility> selected = activeAbilityId(identity, AbilityRegistry::isActivatable)
                .flatMap(AbilityRegistry::get);
        if (selected.isEmpty()) {
            feedback(player, "Your Aspect has no ability to call on yet.", ChatFormatting.GRAY);
            return Outcome.NO_ABILITY;
        }

        AspectAbility ability = selected.get();
        long now = player.serverLevel().getGameTime();
        AbilityCooldownData cooldowns = player.getData(ModAttachments.ABILITY_COOLDOWNS);
        long remaining = cooldowns.remainingTicks(ability.id(), now);
        if (remaining > 0) {
            feedback(player, ability.displayName() + " recovering: " + secondsCeil(remaining) + "s", ChatFormatting.GRAY);
            return Outcome.RECOVERING;
        }
        Outcome outcome = switch (ability.activate(player)) {
            case USED -> {
                startCooldown(player, ability);
                yield Outcome.USED;
            }
            case PRIMED -> Outcome.PRIMED;
            case FAILED -> Outcome.FAILED;
        };
        if (outcome != Outcome.FAILED) {
            broadcastCast(player, ability);
        }
        syncStatus(player);
        return outcome;
    }

    /** Starts an ability's cooldown and confirms it on the action bar; abilities that finish on their own call this. */
    public static void startCooldown(ServerPlayer player, AspectAbility ability) {
        long now = player.serverLevel().getGameTime();
        AbilityCooldownData cooldowns = player.getData(ModAttachments.ABILITY_COOLDOWNS);
        player.setData(ModAttachments.ABILITY_COOLDOWNS, cooldowns.start(ability.id(), now, ability.cooldownTicks()));
        feedback(player, ability.displayName(), ChatFormatting.LIGHT_PURPLE);
        syncStatus(player);
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

    /** What the HUD slot should show right now for this player. */
    public static AbilityStatusPayload status(ServerPlayer player) {
        Optional<AspectAbility> selected = activeAbilityId(SoulIdentityService.get(player), AbilityRegistry::isActivatable)
                .flatMap(AbilityRegistry::get);
        if (selected.isEmpty()) {
            return AbilityStatusPayload.NONE;
        }
        AspectAbility ability = selected.get();
        long remaining = player.getData(ModAttachments.ABILITY_COOLDOWNS)
                .remainingTicks(ability.id(), player.serverLevel().getGameTime());
        return new AbilityStatusPayload(ability.id().toString(), ability.displayName(),
                (int) Math.min(Integer.MAX_VALUE, remaining), ability.cooldownTicks(), ability.primed(player));
    }

    /** Sends the HUD slot state to the owning client, if it has the mod's channel. */
    public static void syncStatus(ServerPlayer player) {
        if (player.connection != null && player.connection.hasChannel(AbilityStatusPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, status(player));
        }
    }

    /** Lets the player's ability keep any lingering state going (marks, stances). */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        activeAbilityId(SoulIdentityService.get(player), AbilityRegistry::isActivatable)
                .flatMap(AbilityRegistry::get)
                .ifPresent(ability -> ability.tick(player));
    }

    /**
     * The ability the key triggers: the highest-rank granted ability that has an
     * activatable implementation, falling back to legacy unclassified ones from
     * older saves. Innate rules are passive and never chosen.
     */
    static Optional<ResourceLocation> activeAbilityId(SoulIdentityData identity, Predicate<ResourceLocation> activatable) {
        return identity.aspect().flatMap(aspect -> {
            Optional<AspectAbilityData> best = Optional.empty();
            for (AspectAbilityData ability : aspect.abilitySet().abilities()) {
                if (ability.kind() == AspectAbilityKind.INNATE || !activatable.test(ability.abilityId())) {
                    continue;
                }
                if (best.isEmpty() || outranks(ability, best.get())) {
                    best = Optional.of(ability);
                }
            }
            return best.map(AspectAbilityData::abilityId);
        });
    }

    public static boolean possessesAspectAbility(SoulIdentityData identity, ResourceLocation abilityId) {
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(abilityId, "abilityId");
        return identity.aspect().map(aspect -> aspect.abilitySet().contains(abilityId)).orElse(false);
    }

    static long secondsCeil(long ticks) {
        return (ticks + 19) / 20;
    }

    private static boolean outranks(AspectAbilityData candidate, AspectAbilityData current) {
        int candidateRank = candidate.acquisitionRank().map(Enum::ordinal).orElse(-1);
        int currentRank = current.acquisitionRank().map(Enum::ordinal).orElse(-1);
        return candidateRank > currentRank;
    }

    public static void feedback(ServerPlayer player, String text, ChatFormatting colour) {
        player.displayClientMessage(Component.literal(text).withStyle(colour), true);
    }
}
