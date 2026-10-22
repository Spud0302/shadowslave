package dev.spud.shadowslave.client;

import dev.spud.shadowslave.world.entity.AshenTyrantEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.render_types.LodestoneWorldParticleRenderType;

import java.awt.Color;

/**
 * Lodestone effects for Nightmare Creatures, played on the client when the
 * server broadcasts an entity event: the Ashen Tyrant's slam (an ember ring
 * along the ground and a screen shake that fades with distance) and its
 * brood call (embers drawn up from its chest).
 */
public final class CreatureEffects {
    static final Color EMBER = new Color(255, 120, 40);
    static final Color ASH = new Color(70, 50, 45);
    static final double SHAKE_RANGE = 16.0;

    private CreatureEffects() {
    }

    public static void play(Entity entity, byte event) {
        Level level = entity.level();
        if (!level.isClientSide) {
            return;
        }
        if (event == AshenTyrantEntity.SLAM_EVENT) {
            slam(level, entity, level.getRandom());
        } else if (event == AshenTyrantEntity.BROOD_EVENT) {
            brood(level, entity, level.getRandom());
        }
    }

    private static void slam(Level level, Entity entity, RandomSource random) {
        for (int ring = 0; ring < 2; ring++) {
            int count = 40;
            double speed = ring == 0 ? 0.28 : 0.18;
            for (int i = 0; i < count; i++) {
                double angle = Mth.TWO_PI * i / count;
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                        .setColorData(ColorParticleData.create(EMBER, ASH).setEasing(Easing.QUAD_IN).build())
                        .setScaleData(GenericParticleData.create(0.6F, 0.1F).setEasing(Easing.SINE_OUT).build())
                        .setTransparencyData(GenericParticleData.create(0.9F, 0F).setEasing(Easing.QUAD_IN).build())
                        .setLifetime(20 + random.nextInt(6))
                        .addMotion(Math.cos(angle) * speed, 0.02, Math.sin(angle) * speed)
                        .enableNoClip()
                        .spawn(level, entity.getX() + Math.cos(angle), entity.getY() + 0.15, entity.getZ() + Math.sin(angle));
            }
        }
        WorldParticleBuilder.create(LodestoneParticleTypes.SMOKE_PARTICLE)
                .setColorData(ColorParticleData.create(ASH, ASH).build())
                .setScaleData(GenericParticleData.create(1.4F, 2.4F).build())
                .setTransparencyData(GenericParticleData.create(0.5F, 0F).build())
                .setLifetime(40)
                .setRandomMotion(0.12, 0.05)
                .repeat(level, entity.getX(), entity.getY() + 0.4, entity.getZ(), 16);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            double distance = player.distanceTo(entity);
            if (distance < SHAKE_RANGE) {
                float strength = (float) (0.9 * (1.0 - distance / SHAKE_RANGE));
                ScreenshakeHandler.addScreenshake(builder -> builder
                        .setDuration(18)
                        .setStrength(strength, 0F)
                        .setEasing(Easing.QUAD_OUT));
            }
        }
    }

    private static void brood(Level level, Entity entity, RandomSource random) {
        double chest = entity.getY() + entity.getBbHeight() * 0.6;
        for (int i = 0; i < 30; i++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double radius = 1.5 + random.nextDouble() * 1.5;
            WorldParticleBuilder.create(LodestoneParticleTypes.SPARKLE_PARTICLE)
                    .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                    .setColorData(ColorParticleData.create(EMBER, new Color(255, 220, 120)).build())
                    .setScaleData(GenericParticleData.create(0.25F, 0F).build())
                    .setTransparencyData(GenericParticleData.create(0.9F, 0F).build())
                    .setLifetime(28)
                    .addMotion(-Math.cos(angle) * radius / 28, (chest - entity.getY()) / 28, -Math.sin(angle) * radius / 28)
                    .enableNoClip()
                    .spawn(level, entity.getX() + Math.cos(angle) * radius, entity.getY() + 0.1, entity.getZ() + Math.sin(angle) * radius);
        }
    }
}
