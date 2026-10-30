package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.echoes.EchoService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Shepherd, Awakened (command): your summoned Echoes leap to the creature you
 * look at, spread around it starting from the side away from you, and fight
 * with Strength for eight seconds.
 */
public final class PackTacticsAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.PACK_TACTICS.abilityId();
    static final int ESSENCE_COST = 35;
    static final double REACH = 24.0;
    static final double FLANK_DISTANCE = 1.4;
    static final int STRENGTH_TICKS = 20 * 8;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.PACK_TACTICS.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        List<Mob> pack = EchoService.summonedBodies(player).stream().filter(echo -> echo.level() == player.level()).toList();
        if (pack.isEmpty()) {
            AbilityService.feedback(player, "No Echo of yours is out to flank with.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        LivingEntity target = AbilityTargets.lookedAt(player, REACH, entity -> entity != player
                && EchoService.bond(entity).filter(bond -> bond.owner().equals(player.getUUID())).isEmpty()
                && !CarryTheFlameAbility.isAlly(player, entity));
        if (target == null) {
            AbilityService.feedback(player, "Look at a creature for your Echoes to flank.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        Vec3 away = target.position().subtract(player.position()).multiply(1, 0, 1);
        double start = away.lengthSqr() < 1.0E-4 ? player.getYRot() * Math.PI / 180.0 : Math.atan2(away.z, away.x);
        for (int i = 0; i < pack.size(); i++) {
            Mob echo = pack.get(i);
            Vec3 at = flank(level, target, start + Math.PI * 2 * i / pack.size());
            level.sendParticles(ParticleTypes.SOUL, echo.getX(), echo.getY() + 0.5, echo.getZ(), 10, 0.3, 0.4, 0.3, 0.02);
            echo.getNavigation().stop();
            echo.teleportTo(at.x, at.y, at.z);
            echo.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
            echo.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, STRENGTH_TICKS, 0, false, true, true));
            EchoService.command(echo, target);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, at.x, at.y + 0.5, at.z, 12, 0.3, 0.4, 0.3, 0.02);
        }
        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                20, 0.4, 0.4, 0.4, 0.2);
        level.playSound(null, target.blockPosition(), SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.PLAYERS, 0.8F, 1.4F);
        return Activation.USED;
    }

    /** A free spot beside the target at {@code angle}, else a quarter turn either way, else the target's own feet. */
    static Vec3 flank(ServerLevel level, LivingEntity target, double angle) {
        for (double turn : new double[] {0.0, Math.PI / 2, -Math.PI / 2}) {
            Vec3 at = target.position().add(Math.cos(angle + turn) * FLANK_DISTANCE, 0, Math.sin(angle + turn) * FLANK_DISTANCE);
            BlockPos pos = BlockPos.containing(at);
            if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                    && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
                return at;
            }
        }
        return target.position();
    }
}
