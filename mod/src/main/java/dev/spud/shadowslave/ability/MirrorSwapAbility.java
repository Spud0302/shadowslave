package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Masque, Awakened (movement): trade places with the creature or player you look at. */
public final class MirrorSwapAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.MIRROR_SWAP.abilityId();
    static final int ESSENCE_COST = 30;
    static final double REACH = 24.0;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.MIRROR_SWAP.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity other = AbilityTargets.lookedAt(player, REACH, entity -> entity != player);
        if (other == null) {
            AbilityService.feedback(player, "Look at someone to trade places with.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        Vec3 mine = player.position();
        Vec3 theirs = other.position();
        float myYaw = player.getYRot();
        float theirYaw = other.getYRot();
        level.sendParticles(ParticleTypes.WITCH, mine.x, mine.y + 1.0, mine.z, 30, 0.3, 0.6, 0.3, 0.05);
        level.sendParticles(ParticleTypes.WITCH, theirs.x, theirs.y + other.getBbHeight() * 0.5, theirs.z, 30, 0.3, 0.6, 0.3, 0.05);
        if (other instanceof ServerPlayer swapped) {
            swapped.teleportTo(level, mine.x, mine.y, mine.z, theirYaw, swapped.getXRot());
        } else {
            other.teleportTo(mine.x, mine.y, mine.z);
        }
        // Each faces where the other was, as a reflection would.
        player.teleportTo(level, theirs.x, theirs.y, theirs.z, myYaw + 180.0F, player.getXRot());
        player.resetFallDistance();
        level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0F, 1.0F);
        return Activation.USED;
    }
}
