package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ability.combat.AbilityCombat;
import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * Stone, Ascended (force): a line of stone spikes bursts from the ground
 * toward where you look, hurting and throwing up every hostile in its path.
 */
public final class StoneSpikesAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.STONE_SPIKES.abilityId();
    static final int ESSENCE_COST = 50;
    static final int LENGTH = 10;
    static final double WIDTH = 1.5;
    static final float DAMAGE = 6.0F;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.STONE_SPIKES.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle().multiply(1, 0, 1);
        Vec3 dir = look.lengthSqr() < 1.0e-4 ? Vec3.directionFromRotation(0, player.getYRot()) : look.normalize();
        AbilityCombat combat = AbilityCombat.get();
        Set<LivingEntity> struck = new HashSet<>();
        BlockParticleOption stone = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DRIPSTONE_BLOCK.defaultBlockState());
        for (int step = 1; step <= LENGTH; step++) {
            Vec3 at = player.position().add(dir.scale(step));
            level.sendParticles(stone, at.x, at.y + 0.5, at.z, 12, 0.2, 0.6, 0.2, 0.15);
            for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(WIDTH, 2.0, WIDTH),
                    entity -> entity instanceof Enemy && entity.isAlive())) {
                if (struck.add(mob)) {
                    combat.damage(player, mob, DAMAGE);
                    mob.setDeltaMovement(mob.getDeltaMovement().x * 0.2, 0.8, mob.getDeltaMovement().z * 0.2);
                    mob.hurtMarked = true;
                }
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.PLAYERS, 1.0F, 0.5F);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.4F, 0.7F);
        return Activation.USED;
    }
}
