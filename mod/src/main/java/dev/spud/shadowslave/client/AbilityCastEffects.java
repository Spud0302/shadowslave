package dev.spud.shadowslave.client;

import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;
import team.lodestar.lodestone.systems.particle.render_types.LodestoneWorldParticleRenderType;

import java.awt.Color;
import java.util.Optional;

/**
 * Lodestone glow for an ability cast, played alongside the casting pose: a
 * ring of light rising from the caster's feet and a flare at the chest, in the
 * colours of the ability's Nature. Stone abilities also shake the caster's own
 * screen. The ability's own vanilla particles still play on top.
 */
public final class AbilityCastEffects {
    /** One look per Nature: the colour a particle starts at and fades to. */
    enum NatureGlow {
        EMBER(new Color(255, 170, 60), new Color(200, 40, 20)),
        MIST(new Color(235, 240, 255), new Color(140, 150, 175)),
        STONE(new Color(215, 190, 150), new Color(110, 100, 90)),
        TIDE(new Color(90, 220, 255), new Color(20, 70, 200)),
        WARDEN(new Color(170, 230, 120), new Color(40, 100, 40)),
        SEER(new Color(215, 170, 255), new Color(90, 50, 170)),
        REAPER(new Color(235, 60, 80), new Color(70, 0, 25)),
        WELLSPRING(new Color(255, 232, 150), new Color(215, 140, 40)),
        FORGE(new Color(255, 200, 90), new Color(150, 60, 20)),
        DELVE(new Color(190, 200, 215), new Color(60, 70, 90)),
        MASON(new Color(225, 205, 175), new Color(140, 90, 60)),
        VERDANT(new Color(160, 240, 120), new Color(30, 110, 40)),
        WAYFARER(new Color(255, 225, 140), new Color(120, 170, 230)),
        LULL(new Color(220, 200, 255), new Color(60, 50, 140)),
        GLOAM(new Color(90, 80, 120), new Color(10, 5, 20)),
        RIME(new Color(220, 245, 255), new Color(90, 160, 220)),
        FURY(new Color(255, 90, 50), new Color(120, 10, 10)),
        THRESHOLD(new Color(200, 140, 255), new Color(40, 20, 80)),
        ANCHOR(new Color(170, 175, 190), new Color(50, 55, 70)),
        HOURGLASS(new Color(250, 225, 160), new Color(150, 110, 50)),
        HERALD(new Color(255, 200, 80), new Color(160, 30, 30)),
        KEEPER(new Color(230, 235, 255), new Color(90, 110, 170));

        final Color start;
        final Color end;

        NatureGlow(Color start, Color end) {
            this.start = start;
            this.end = end;
        }
    }

    static final int RING_PARTICLES = 24;
    static final double RING_RADIUS = 0.9;

    private AbilityCastEffects() {
    }

    /** The Nature whose look an ability casts with, Dormant or Awakened; empty for anything else. */
    static Optional<NatureGlow> glowFor(ResourceLocation abilityId) {
        for (AspectKit kit : AspectKitCatalog.all()) {
            boolean ownsAbility = kit.abilityId().equals(abilityId)
                    || KitLadder.variants(kit.natureId(), SoulRank.AWAKENED).stream()
                            .anyMatch(variant -> variant.abilityId().equals(abilityId));
            if (ownsAbility) {
                return Optional.of(NatureGlow.valueOf(kit.natureName().toUpperCase(java.util.Locale.ROOT)));
            }
        }
        return Optional.empty();
    }

    public static void play(AbstractClientPlayer player, ResourceLocation abilityId) {
        glowFor(abilityId).ifPresent(glow -> {
            Level level = player.level();
            RandomSource random = level.getRandom();
            ring(level, player, glow, random);
            flare(level, player, glow, random);
            if (glow == NatureGlow.STONE && player == Minecraft.getInstance().player) {
                ScreenshakeHandler.addScreenshake(builder -> builder
                        .setDuration(12)
                        .setStrength(0.35F, 0F)
                        .setEasing(Easing.QUAD_OUT));
            }
        });
    }

    private static void ring(Level level, AbstractClientPlayer player, NatureGlow glow, RandomSource random) {
        for (int i = 0; i < RING_PARTICLES; i++) {
            double angle = Mth.TWO_PI * i / RING_PARTICLES;
            double x = player.getX() + Math.cos(angle) * RING_RADIUS;
            double z = player.getZ() + Math.sin(angle) * RING_RADIUS;
            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                    .setColorData(ColorParticleData.create(glow.start, glow.end).setEasing(Easing.QUAD_IN).build())
                    .setScaleData(GenericParticleData.create(0.35F, 0.05F).setEasing(Easing.SINE_OUT).build())
                    .setTransparencyData(GenericParticleData.create(0.8F, 0F).setEasing(Easing.QUAD_IN).build())
                    .setLifetime(18 + random.nextInt(8))
                    .addMotion(-Math.sin(angle) * 0.03, 0.06 + random.nextDouble() * 0.04, Math.cos(angle) * 0.03)
                    .enableNoClip()
                    .spawn(level, x, player.getY() + 0.1, z);
        }
    }

    private static void flare(Level level, AbstractClientPlayer player, NatureGlow glow, RandomSource random) {
        WorldParticleBuilder.create(LodestoneParticleTypes.STAR_PARTICLE)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .setColorData(ColorParticleData.create(glow.start, glow.end).build())
                .setScaleData(GenericParticleData.create(0.2F, 1.6F, 0F).setEasing(Easing.EXPO_OUT, Easing.QUAD_IN).build())
                .setTransparencyData(GenericParticleData.create(0.9F, 0F).setEasing(Easing.QUAD_IN).build())
                .setSpinData(SpinParticleData.create(0.2F, 0F).randomSpinOffset(random).build())
                .setLifetime(14)
                .enableNoClip()
                .spawn(level, player.getX(), player.getY() + player.getBbHeight() * 0.55, player.getZ());
        WorldParticleBuilder.create(LodestoneParticleTypes.SPARKLE_PARTICLE)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .setColorData(ColorParticleData.create(glow.start, glow.end).build())
                .setScaleData(GenericParticleData.create(0.15F, 0F).build())
                .setTransparencyData(GenericParticleData.create(0.9F, 0F).build())
                .setLifetime(20)
                .setRandomMotion(0.12, 0.12)
                .enableNoClip()
                .repeat(level, player.getX(), player.getY() + player.getBbHeight() * 0.55, player.getZ(), 14);
    }
}
