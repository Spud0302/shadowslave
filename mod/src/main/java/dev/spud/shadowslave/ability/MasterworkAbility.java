package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Forge, Awakened (craft): for a minute, the next billet you fuse at a Forge
 * Hearth may reach one Tier above your Forging level, and that piece gets a
 * hammer mark twice as wide and one Quality grade higher. Fusing the billet
 * ends it (SmithingService), as does the minute running out.
 */
public final class MasterworkAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.MASTERWORK.abilityId();
    static final int ESSENCE_COST = 60;
    static final int DURATION_TICKS = 20 * 60;
    /** How many Tiers above the branch level a Masterwork may reach. */
    public static final int TIER_BONUS = 1;

    private static final Map<UUID, Long> ACTIVE_UNTIL = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.MASTERWORK.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (active(player)) {
            AbilityService.feedback(player, "Your hands are already set for a masterwork.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        ACTIVE_UNTIL.put(player.getUUID(), level.getGameTime() + DURATION_TICKS);
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.1, player.getZ(), 20, 0.4, 0.4, 0.4, 0.01);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.1, player.getZ(), 30, 0.5, 0.5, 0.5, 0.2);
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.8F, 1.3F);
        AbilityService.feedback(player, "Masterwork: your next forged piece reaches higher", ChatFormatting.GOLD);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        if (until != null && until <= player.serverLevel().getGameTime()) {
            ACTIVE_UNTIL.remove(player.getUUID());
            AbilityService.feedback(player, "The masterwork moment passes.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        ACTIVE_UNTIL.remove(player.getUUID());
    }

    /** Lights the HUD slot while the masterwork is waiting. */
    @Override
    public boolean primed(ServerPlayer player) {
        return active(player);
    }

    public static boolean active(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        return until != null && until > player.serverLevel().getGameTime();
    }

    /** Called by the Forge Hearth once a billet has used the masterwork. */
    public static void spend(ServerPlayer player) {
        if (ACTIVE_UNTIL.remove(player.getUUID()) != null) {
            AbilityService.syncStatus(player);
        }
    }
}
