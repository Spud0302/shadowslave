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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Verdant's Dormant ability: everything growing within {@link #RADIUS}
 * blocks surges as if bone-mealed twice (crops, saplings, stems, berries and
 * cocoa; not grass, so a field isn't buried in weeds), and animals nearby are
 * mended.
 */
public final class BloomAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.VERDANT.abilityId();
    static final int ESSENCE_COST = 25;
    static final int RADIUS = 4;
    static final int SURGES = 2;
    static final double ANIMAL_RADIUS = 6.0;
    static final float ANIMAL_HEAL = 4.0F;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.VERDANT.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        int grown = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -2, -RADIUS), center.offset(RADIUS, 2, RADIUS))) {
            if (surge(level, pos.immutable())) {
                grown++;
            }
        }
        int mended = 0;
        for (Animal animal : level.getEntitiesOfClass(Animal.class, player.getBoundingBox().inflate(ANIMAL_RADIUS))) {
            animal.heal(ANIMAL_HEAL);
            level.sendParticles(ParticleTypes.HEART, animal.getX(), animal.getY() + animal.getBbHeight() + 0.2, animal.getZ(),
                    2, 0.2, 0.1, 0.2, 0.0);
            mended++;
        }
        if (grown == 0 && mended == 0) {
            AbilityService.feedback(player, "Nothing here is growing.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 0.3, player.getZ(), 40, 2.5, 0.3, 2.5, 0.0);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX(), player.getY() + 2.0, player.getZ(), 30, 2.0, 0.5, 2.0, 0.0);
        level.playSound(null, center, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0F, 0.8F);
        level.playSound(null, center, SoundEvents.AZALEA_LEAVES_PLACE, SoundSource.PLAYERS, 1.0F, 1.0F);
        return Activation.USED;
    }

    /** Bone-meals one growing block {@link #SURGES} times; true if it took. */
    static boolean surge(ServerLevel level, BlockPos pos) {
        boolean grew = false;
        for (int i = 0; i < SURGES; i++) {
            BlockState state = level.getBlockState(pos);
            if (!growing(state) || !(state.getBlock() instanceof BonemealableBlock block)
                    || !block.isValidBonemealTarget(level, pos, state)) {
                break;
            }
            block.performBonemeal(level, level.random, pos, state);
            grew = true;
        }
        if (grew) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.0);
        }
        return grew;
    }

    /** Crops and other things a farmer grows on purpose, as opposed to grass and moss. */
    static boolean growing(BlockState state) {
        return state.is(BlockTags.CROPS) || state.is(BlockTags.SAPLINGS) || state.getBlock() instanceof StemBlock
                || state.getBlock() instanceof SweetBerryBushBlock || state.getBlock() instanceof CocoaBlock;
    }
}
