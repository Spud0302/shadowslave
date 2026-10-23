package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;

/** Anchor's Dormant ability: step to the next weight, Normal, Heavy, Light and round again. */
public final class ShiftWeightAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.ANCHOR.abilityId();
    static final int ESSENCE_COST = 10;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.ANCHOR.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        AnchorWeight.Weight next = AnchorWeight.weight(player).next();
        AnchorWeight.set(player, next);
        ServerLevel level = player.serverLevel();
        switch (next) {
            case HEAVY -> {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()),
                        player.getX(), player.getY() + 0.1, player.getZ(), 30, 0.5, 0.05, 0.5, 0.1);
                level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5F, 0.6F);
            }
            case LIGHT -> {
                level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.3, player.getZ(), 20, 0.4, 0.1, 0.4, 0.03);
                level.playSound(null, player.blockPosition(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.6F, 1.6F);
            }
            case NORMAL -> level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.PLAYERS,
                    0.6F, 1.0F);
        }
        AbilityService.feedback(player, "You are " + next.label + ".", ChatFormatting.LIGHT_PURPLE);
        return Activation.USED;
    }
}
