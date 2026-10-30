package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Cantor, Awakened (control): speak to the Nightmare Creature you look at and
 * for five seconds it cannot use its special abilities: no slam, no chain, no
 * burrow, no borrowed voice. It still walks and bites.
 */
public final class SilencingWordAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.SILENCING_WORD.abilityId();
    static final int ESSENCE_COST = 35;
    static final double REACH = 20.0;
    static final int SILENCE_TICKS = 20 * 5;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.SILENCING_WORD.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (!(AbilityTargets.lookedAt(player, REACH, entity -> entity instanceof NightmareCreature) instanceof NightmareCreature creature)) {
            AbilityService.feedback(player, "Look at a Nightmare Creature to silence.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        creature.silence(SILENCE_TICKS);
        ServerLevel level = player.serverLevel();
        // Start the beam a little ahead of the eyes: sculk souls are large and would fill the speaker's own view.
        AbilityTargets.beam(level, ParticleTypes.SCULK_SOUL, player.getEyePosition().add(player.getLookAngle().scale(2.0)),
                creature.position().add(0, creature.getBbHeight() * 0.6, 0));
        level.playSound(null, creature.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.5F, 1.8F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        // The silenced shed a thin grey haze, so the party can see who is quiet.
        if (player.serverLevel().getGameTime() % 10 != 0) {
            return;
        }
        for (NightmareCreature creature : player.serverLevel().getEntitiesOfClass(NightmareCreature.class,
                player.getBoundingBox().inflate(REACH * 2), NightmareCreature::silenced)) {
            player.serverLevel().sendParticles(ParticleTypes.ASH, creature.getX(), creature.getY() + creature.getBbHeight() + 0.2,
                    creature.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
        }
    }
}
