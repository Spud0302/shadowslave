package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.fieldguide.FieldGuideService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Surveyor, Dormant: send a pulse through the ground. Your Field Guide maps
 * everything within 48 blocks, and for thirty seconds you see the outlines of
 * the structures and the cave hollows in it.
 */
public final class SurveyPulseAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.SURVEYOR.abilityId();
    static final int ESSENCE_COST = 30;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.SURVEYOR.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        SurveyorCraft.Survey survey = SurveyorCraft.pulse(player);
        ServerLevel level = player.serverLevel();
        for (int i = 0; i < 32; i++) {
            double angle = Math.PI * 2 * i / 32;
            level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, player.getX() + Math.cos(angle) * 2, player.getY() + 0.1,
                    player.getZ() + Math.sin(angle) * 2, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 1.2F, 0.6F);
        FieldGuideService.sendStatus(player, false);
        AbilityService.feedback(player, "Survey Pulse: " + survey.charted() + " stretches charted, "
                + survey.structures().size() + " " + (survey.structures().size() == 1 ? "structure" : "structures") + " and "
                + survey.hollows().size() + " hollows outlined.", ChatFormatting.GOLD);
        return Activation.USED;
    }

    @Override
    public void clear(ServerPlayer player) {
        SurveyorCraft.forget(player.getUUID());
    }
}
