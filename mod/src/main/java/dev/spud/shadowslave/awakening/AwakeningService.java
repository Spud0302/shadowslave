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
 * Rising a rank (DESIGN): once the rank's requirement is met, the player picks
 * one of their Nature's abilities for that rank. Dreamers Awaken, the Awakened
 * Ascend to Master and Masters Transcend to Saint (ch. 17, 459). The soul and
 * Aspect rise, the core empties into its larger shape, and the essence pool
 * grows and refills. A Nature whose rung is not built yet still rises; its
 * owed ability is offered in the Soul Sea once it exists.
 */
public final class AwakeningService {
    public enum Result {
        /** Rose a rank, or took an ability owed from an earlier rise. */
        AWAKENED,
        NO_ASPECT,
        /** No rank is open to this soul yet. */
        NOTHING_OPEN,
        NOT_A_CHOICE,
        NOT_READY
    }

    private AwakeningService() {
    }

    /** The rank this player could rise to next, if it is built yet. */
    public static Optional<SoulRank> nextRank(ServerPlayer player) {
        return nextRank(SoulService.get(player).spellState(), SoulIdentityService.get(player).isRevealed());
    }

    static Optional<SoulRank> nextRank(SpellState state, boolean revealed) {
        if (!revealed) {
            return Optional.empty();
        }
        return switch (state) {
            case DREAMER -> Optional.of(SoulRank.AWAKENED);
            case AWAKENED -> Optional.of(SoulRank.ASCENDED);
            case MASTER -> Optional.of(SoulRank.TRANSCENDENT);
            default -> Optional.empty();
        };
    }

    /**
     * A rank the player has already reached whose abilities they hold none of,
     * because the rung was built after they rose. The Soul Sea offers it first.
     */
    public static Optional<SoulRank> owedRank(ServerPlayer player) {
        Optional<SoulRank> reached = SoulService.get(player).soulRank();
        Optional<AspectInstanceData> aspect = SoulIdentityService.get(player).aspect();
        if (reached.isEmpty() || aspect.isEmpty()) {
            return Optional.empty();
        }
        for (SoulRank rank : SoulRank.values()) {
            if (rank == SoulRank.DORMANT || rank.compareTo(reached.get()) > 0) {
                continue;
            }
            List<AbilityVariant> rung = KitLadder.variants(aspect.get().natureId(), rank);
            if (!rung.isEmpty() && rung.stream().noneMatch(variant -> aspect.get().abilitySet().contains(variant.abilityId()))) {
                return Optional.of(rank);
            }
        }
        return Optional.empty();
    }

    /** Whether the requirement for the next rank is met; an owed ability is always ready to take. */
    public static boolean ready(ServerPlayer player) {
        if (owedRank(player).isPresent()) {
            return true;
        }
        return nextRank(player).flatMap(RankUpRequirements::forRank).map(requirement -> requirement.met(player)).orElse(false);
    }

    /** The abilities the player can choose between now: an owed rung first, else the next rank's. */
    public static List<AbilityVariant> choices(ServerPlayer player) {
        Optional<SoulRank> rank = owedRank(player).or(() -> nextRank(player));
        Optional<AspectInstanceData> aspect = SoulIdentityService.get(player).aspect();
        if (rank.isEmpty() || aspect.isEmpty()) {
            return List.of();
        }
        return KitLadder.variants(aspect.get().natureId(), rank.get());
    }

    /** Rises (or takes an owed ability) with the chosen ability. */
    public static Result awaken(ServerPlayer player, ResourceLocation abilityId) {
        return rise(player, Optional.of(abilityId));
    }

    /**
     * Rises one rank. {@code abilityId} must be one of {@link #choices}; it may
     * be empty only when the Nature has no abilities built for that rank yet.
     */
    public static Result rise(ServerPlayer player, Optional<ResourceLocation> abilityId) {
        SoulIdentityData identity = SoulIdentityService.get(player);
        if (identity.aspect().isEmpty()) {
            return Result.NO_ASPECT;
        }
        List<AbilityVariant> choices = choices(player);
        Optional<AbilityVariant> choice = abilityId.flatMap(id -> choices.stream()
                .filter(variant -> variant.abilityId().equals(id))
                .findFirst());
        if (owedRank(player).isPresent()) {
            if (choice.isEmpty()) {
                return Result.NOT_A_CHOICE;
            }
            AbilityGrants.grant(player, choice.get());
            celebrate(player, Optional.empty(), choice);
            return Result.AWAKENED;
        }
        Optional<SoulRank> next = nextRank(player);
        if (next.isEmpty()) {
            return Result.NOTHING_OPEN;
        }
        if (choice.isEmpty() && !choices.isEmpty()) {
            return Result.NOT_A_CHOICE;
        }
        if (!ready(player)) {
            return Result.NOT_READY;
        }

        if (next.get() == SoulRank.AWAKENED) {
            SoulService.awaken(player);
        } else {
            SoulService.ascend(player);
        }
        AspectInstanceData aspect = identity.aspect().get().withAspectRank(next.get());
        SoulIdentityService.replace(player, new SoulIdentityData(Optional.of(aspect), identity.flaw()));
        choice.ifPresent(variant -> AbilityGrants.grant(player, variant));
        CoreService.set(player, 0);
        EssenceService.fill(player);
        AbilityService.syncStatus(player);
        celebrate(player, next, choice);
        ShadowSlaveMod.LOGGER.info("{} rose to {} and chose {}", player.getScoreboardName(), next.get().serializedName(),
                choice.map(AbilityVariant::name).orElse("nothing yet"));
        return Result.AWAKENED;
    }

    /** What the Spell calls the rank's holders. */
    public static String title(SoulRank rank) {
        return switch (rank) {
            case DORMANT -> "Sleeper";
            case AWAKENED -> "Awakened";
            case ASCENDED -> "Master";
            case TRANSCENDENT -> "Saint";
            case SUPREME -> "Sovereign";
            case SACRED -> "Spirit";
            case DIVINE -> "God";
        };
    }

    private static void celebrate(ServerPlayer player, Optional<SoulRank> rank, Optional<AbilityVariant> choice) {
        ServerLevel level = player.serverLevel();
        int scale = rank.map(value -> value.ordinal()).orElse(1);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 40 + 20 * scale, 0.6, 1.0, 0.6, 0.08);
        level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.2, player.getZ(), 20 + 10 * scale, 1.2, 0.2, 1.2, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 0.8F);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.6F);
        if (rank.filter(value -> value.compareTo(SoulRank.ASCENDED) >= 0).isPresent()) {
            level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.7F, 0.7F);
        }
        String grows = choice.map(variant -> " Your Aspect grows: " + variant.name() + ".").orElse("");
        String text = rank.map(value -> switch (value) {
            case AWAKENED -> "You have Awakened.";
            case ASCENDED -> "You have Ascended. You are a Master now.";
            case TRANSCENDENT -> "You have Transcended. You are a Saint now.";
            default -> "Your soul rises.";
        }).orElse("Your Aspect has grown.");
        player.sendSystemMessage(Component.literal(text + grows).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
