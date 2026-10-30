package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.fieldguide.FieldGuideService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Cantor's Dormant ability: speak the true name of the Nightmare Creature you
 * look at and it is stunned for three seconds. Only a kind whose Field Guide
 * page you have completed can be named: to command a thing, know it.
 */
public final class CommandWordAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.CANTOR.abilityId();
    static final int ESSENCE_COST = 30;
    static final double REACH = 20.0;
    static final int STUN_TICKS = 20 * 3;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.CANTOR.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (!(AbilityTargets.lookedAt(player, REACH, entity -> entity instanceof NightmareCreature) instanceof NightmareCreature creature)) {
            AbilityService.feedback(player, "Look at a Nightmare Creature to name it.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(creature.getType());
        String name = FieldGuideService.creatureName(type);
        if (!FieldGuideService.pageComplete(player, type)) {
            AbilityService.feedback(player, "You do not know the " + name + " well enough to name it. Complete its Field Guide page.",
                    ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        creature.stun(STUN_TICKS);
        ServerLevel level = player.serverLevel();
        AbilityTargets.beam(level, ParticleTypes.ENCHANT, player.getEyePosition(), creature.position().add(0, creature.getBbHeight() * 0.6, 0));
        level.sendParticles(ParticleTypes.ENCHANT, creature.getX(), creature.getY() + creature.getBbHeight() + 0.3, creature.getZ(),
                40, 0.4, 0.3, 0.4, 0.8);
        level.playSound(null, creature.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.3F);
        player.displayClientMessage(Component.literal("\"" + name + ".\" It cannot move.").withStyle(ChatFormatting.AQUA), true);
        return Activation.USED;
    }
}
