package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SpellState;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;
import java.util.Optional;

/**
 * Rising from Dormant to Awakened (DESIGN): once the rank's requirement is met,
 * the player picks one of their Nature's two Awakened abilities. The soul and
 * Aspect rise, the core empties into its larger Awakened shape, and the essence
 * pool grows and refills.
 */
public final class AwakeningService {
    public enum Result {
        AWAKENED,
        NO_ASPECT,
        ALREADY_AWAKENED,
        NOT_A_CHOICE,
        NOT_READY
    }

    private AwakeningService() {
    }

    /** The rank this player could rise to next, if it is built yet. */
    public static Optional<SoulRank> nextRank(ServerPlayer player) {
        SpellState state = SoulService.get(player).spellState();
        return state == SpellState.DREAMER && SoulIdentityService.get(player).isRevealed()
                ? Optional.of(SoulRank.AWAKENED)
                : Optional.empty();
    }

    /** Whether the requirement for the next rank is met. */
    public static boolean ready(ServerPlayer player) {
        return nextRank(player).flatMap(RankUpRequirements::forRank).map(requirement -> requirement.met(player)).orElse(false);
    }

    /** The abilities the player can choose between on rising; empty when no rank-up is open. */
    public static List<AbilityVariant> choices(ServerPlayer player) {
        Optional<SoulRank> next = nextRank(player);
        Optional<AspectInstanceData> aspect = SoulIdentityService.get(player).aspect();
        if (next.isEmpty() || aspect.isEmpty()) {
            return List.of();
        }
        return KitLadder.variants(aspect.get().natureId(), next.get());
    }

    public static Result awaken(ServerPlayer player, ResourceLocation abilityId) {
        SoulIdentityData identity = SoulIdentityService.get(player);
        if (identity.aspect().isEmpty()) {
            return Result.NO_ASPECT;
        }
        if (nextRank(player).isEmpty()) {
            return Result.ALREADY_AWAKENED;
        }
        Optional<AbilityVariant> choice = choices(player).stream()
                .filter(variant -> variant.abilityId().equals(abilityId))
                .findFirst();
        if (choice.isEmpty()) {
            return Result.NOT_A_CHOICE;
        }
        if (!ready(player)) {
            return Result.NOT_READY;
        }

        SoulService.awaken(player);
        AspectInstanceData aspect = identity.aspect().get().withAspectRank(SoulRank.AWAKENED);
        SoulIdentityService.replace(player, new SoulIdentityData(Optional.of(aspect), identity.flaw()));
        AbilityGrants.grant(player, choice.get());
        CoreService.set(player, 0);
        EssenceService.fill(player);
        AbilityService.syncStatus(player);
        celebrate(player, choice.get());
        ShadowSlaveMod.LOGGER.info("{} Awakened and chose {}", player.getScoreboardName(), choice.get().name());
        return Result.AWAKENED;
    }

    private static void celebrate(ServerPlayer player, AbilityVariant choice) {
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 60, 0.6, 1.0, 0.6, 0.08);
        level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.2, player.getZ(), 30, 1.2, 0.2, 1.2, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 0.8F);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.6F);
        player.sendSystemMessage(Component.literal("You have Awakened. Your Aspect grows: " + choice.name() + ".")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
