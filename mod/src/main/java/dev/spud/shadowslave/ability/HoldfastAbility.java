package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Stone's Dormant ability: brace for a few seconds of heavy resistance and no knockback, but no sprinting. */
public final class HoldfastAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.STONE.abilityId();
    static final int DURATION_TICKS = 20 * 6;
    static final int COOLDOWN_TICKS = 20 * 30;

    /** Game time each bracing player's Holdfast ends. Short-lived, so it is not saved. */
    private static final Map<UUID, Long> BRACED_UNTIL = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Holdfast";
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BRACED_UNTIL.put(player.getUUID(), level.getGameTime() + DURATION_TICKS);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, DURATION_TICKS, 1, false, true));
        player.setSprinting(false);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                player.getX(), player.getY() + 0.1, player.getZ(), 40, 0.6, 0.1, 0.6, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5F, 0.6F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        if (!isBraced(player)) {
            return;
        }
        if (player.isSprinting()) {
            player.setSprinting(false);
        }
        ServerLevel level = player.serverLevel();
        if (level.getGameTime() % 10 == 0) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    player.getX(), player.getY() + 0.05, player.getZ(), 6, 0.35, 0.0, 0.35, 0.0);
        }
    }

    public static boolean isBraced(ServerPlayer player) {
        Long until = BRACED_UNTIL.get(player.getUUID());
        if (until == null) {
            return false;
        }
        if (until <= player.serverLevel().getGameTime()) {
            BRACED_UNTIL.remove(player.getUUID());
            return false;
        }
        return true;
    }
}
