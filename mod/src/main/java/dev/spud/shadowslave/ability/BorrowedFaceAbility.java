package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.echoes.EchoService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Masque, Dormant: take the face of the creature you look at. For a minute,
 * or until you strike or use another ability, creatures of your rank or lower
 * take you for one of their own and leave you be.
 */
public final class BorrowedFaceAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.MASQUE.abilityId();
    static final int ESSENCE_COST = 25;
    static final double REACH = 24.0;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.MASQUE.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity model = AbilityTargets.lookedAt(player, REACH,
                entity -> entity instanceof Mob && EchoService.bond(entity).isEmpty());
        if (model == null) {
            AbilityService.feedback(player, "Look at a creature to borrow its face.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        MasqueFace.wear(player, model.getType());
        ServerLevel level = player.serverLevel();
        AbilityTargets.beam(level, ParticleTypes.WITCH, model.position().add(0, model.getBbHeight() * 0.6, 0), player.position().add(0, 1.0, 0));
        level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 25, 0.3, 0.6, 0.3, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_PREPARE_MIRROR, SoundSource.PLAYERS, 1.0F, 1.0F);
        AbilityService.feedback(player, "You wear the face of a " + model.getType().getDescription().getString() + ".",
                ChatFormatting.LIGHT_PURPLE);
        return Activation.USED;
    }

    @Override
    public void clear(ServerPlayer player) {
        MasqueFace.drop(player, null);
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return MasqueFace.disguised(player);
    }
}
