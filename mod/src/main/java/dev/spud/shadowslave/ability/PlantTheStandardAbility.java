package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

/**
 * Standard, Dormant: plant your banner for two minutes. Everyone of yours near
 * it is warded as if you stood there; at a Gateway being channelled it speeds
 * your clan's channel and slows the enemy's. Anyone can strike it down.
 */
public final class PlantTheStandardAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.STANDARD.abilityId();
    static final int ESSENCE_COST = 30;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.STANDARD.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (StandardColours.planted(player).isPresent()) {
            AbilityService.feedback(player, "Your Standard still stands.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        Optional<BlockPos> spot = WarBannerAbility.spot(level, player.blockPosition());
        if (spot.isEmpty()) {
            AbilityService.feedback(player, "There is no room here to plant your Standard.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        int facing = Math.floorMod(Math.round((player.getYRot() + 180.0F) * 16.0F / 360.0F), 16);
        level.setBlockAndUpdate(spot.get(), Blocks.PURPLE_BANNER.defaultBlockState().setValue(BannerBlock.ROTATION, facing));
        StandardColours.plant(player, spot.get());
        level.playSound(null, spot.get(), SoundEvents.ARMOR_EQUIP_CHAIN.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
        level.playSound(null, spot.get(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 0.6F, 0.7F);
        level.sendParticles(ParticleTypes.WITCH, spot.get().getX() + 0.5, spot.get().getY() + 1.6, spot.get().getZ() + 0.5,
                30, 0.3, 0.6, 0.3, 0.05);
        AbilityService.feedback(player, "Your Standard stands for " + StandardColours.BANNER_TICKS / 20 + " s.", ChatFormatting.LIGHT_PURPLE);
        return Activation.USED;
    }

    @Override
    public void clear(ServerPlayer player) {
        StandardColours.lower(player.server, player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return StandardColours.planted(player).isPresent();
    }
}
