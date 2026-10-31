package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.fieldguide.FieldGuideService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Surveyor, Awakened: take a bearing on the nearest open Gate, or in the Dream
 * Realm the nearest Nightmare Seed if it is nearer, and pin its exact spot in
 * your Field Guide, however far it is.
 */
public final class TriangulateAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.TRIANGULATE.abilityId();
    static final int ESSENCE_COST = 40;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.TRIANGULATE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        Optional<SurveyorCraft.Fix> fix = SurveyorCraft.triangulate(player);
        if (fix.isEmpty()) {
            AbilityService.feedback(player, "No Gate or Seed answers your bearing in this world.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        SurveyorCraft.Fix found = fix.get();
        SurveyorCraft.pin(player, found);
        FieldGuideService.sendStatus(player, false);
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 toward = Vec3.atCenterOf(found.pos()).subtract(eye).multiply(1.0, 0.0, 1.0);
        if (toward.lengthSqr() > 1.0E-4) {
            AbilityTargets.beam(level, ParticleTypes.END_ROD, eye, eye.add(toward.normalize().scale(8.0)));
        }
        level.playSound(null, player.blockPosition(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
        int distance = (int) Math.sqrt(found.pos().distSqr(player.blockPosition()));
        AbilityService.feedback(player, "Triangulated: " + found.label() + " at " + found.pos().getX() + ", " + found.pos().getZ()
                + ", " + distance + " blocks off. Pinned in your guide.", ChatFormatting.GOLD);
        return Activation.USED;
    }
}
