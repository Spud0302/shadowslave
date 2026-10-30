package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.echoes.EchoService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Masque, Awakened (control): throw a creature's own call to the spot you
 * look at. The creature nearest that spot names the kind; every one of that
 * kind nearby forgets what it hunts and walks to the call for eight seconds.
 */
public final class StolenVoiceAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.STOLEN_VOICE.abilityId();
    static final int ESSENCE_COST = 30;
    static final double REACH = 32.0;
    static final double RADIUS = 24.0;
    static final int LURE_TICKS = 20 * 8;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.STOLEN_VOICE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        HitResult hit = level.clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(REACH)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            AbilityService.feedback(player, "Look at the ground where the call should come from.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Vec3 point = hit.getLocation();
        Optional<Mob> voice = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RADIUS),
                        mob -> mob.isAlive() && EchoService.bond(mob).isEmpty())
                .stream().min(Comparator.comparingDouble(mob -> mob.distanceToSqr(point)));
        if (voice.isEmpty()) {
            AbilityService.feedback(player, "No creature near enough to steal a voice from.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        List<Mob> kind = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RADIUS),
                mob -> mob.isAlive() && mob.getType() == voice.get().getType() && EchoService.bond(mob).isEmpty());
        for (Mob mob : kind) {
            MasqueFace.lure(mob, point, LURE_TICKS);
            level.sendParticles(ParticleTypes.NOTE, mob.getX(), mob.getEyeY() + 0.5, mob.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
        voice.get().playAmbientSound();
        level.sendParticles(ParticleTypes.SCULK_SOUL, point.x, point.y + 0.3, point.z, 25, 0.4, 0.3, 0.4, 0.02);
        level.sendParticles(ParticleTypes.NOTE, point.x, point.y + 1.0, point.z, 6, 0.5, 0.3, 0.5, 0.0);
        level.playSound(null, point.x, point.y, point.z, SoundEvents.ALLAY_THROW, SoundSource.PLAYERS, 1.0F, 0.6F);
        AbilityService.feedback(player, "You call with the voice of a " + voice.get().getType().getDescription().getString() + ".",
                ChatFormatting.LIGHT_PURPLE);
        return Activation.USED;
    }
}
