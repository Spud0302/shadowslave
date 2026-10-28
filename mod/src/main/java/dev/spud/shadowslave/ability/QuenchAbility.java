package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.smithing.SmithingService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Forge's Dormant ability: smelt up to eight of whatever you hold, in your
 * bare hands, as a furnace would (Thrifty Hands applies). A shaped blade in
 * hand is quenched perfectly instead. With nothing
 * smeltable in hand the heat bursts outward instead, throwing back and
 * igniting hostile creatures close by, so a Forge is never defenceless in a
 * Gate.
 */
public final class QuenchAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.FORGE.abilityId();
    static final int ESSENCE_COST = 25;
    static final int MAX_ITEMS = 8;
    static final double BURST_RADIUS = 4.0;
    static final int BURST_FIRE_SECONDS = 4;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.FORGE.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (SmithingService.quenchInHand(player)) {
            level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getEyeY() - 0.4, player.getZ(), 20, 0.3, 0.2, 0.3, 0.02);
            level.playSound(null, player.blockPosition(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.9F, 1.0F);
            AbilityService.feedback(player, "Quenched in your hands, perfectly", ChatFormatting.GOLD);
            return Activation.USED;
        }
        ItemStack held = player.getMainHandItem();
        Optional<RecipeHolder<SmeltingRecipe>> recipe = held.isEmpty() ? Optional.empty()
                : level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(held), level);
        if (recipe.isPresent()) {
            smelt(player, level, held, recipe.get().value());
        } else {
            burst(player, level);
        }
        return Activation.USED;
    }

    private static void smelt(ServerPlayer player, ServerLevel level, ItemStack held, SmeltingRecipe recipe) {
        int count = Math.min(MAX_ITEMS, held.getCount());
        ItemStack result = recipe.getResultItem(level.registryAccess());
        int made = result.getCount() * count + KitPassives.thriftyExtra(player, count);
        held.shrink(count);
        give(player, result, made);
        int xp = Mth.floor(recipe.getExperience() * count + level.random.nextFloat());
        if (xp > 0) {
            ExperienceOrb.award(level, player.position(), xp);
        }
        Vec3 hand = player.getEyePosition().add(player.getLookAngle().scale(0.6)).add(0, -0.3, 0);
        level.sendParticles(ParticleTypes.FLAME, hand.x, hand.y, hand.z, 16, 0.15, 0.15, 0.15, 0.02);
        level.sendParticles(ParticleTypes.LAVA, hand.x, hand.y, hand.z, 4, 0.1, 0.1, 0.1, 0.0);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.7F, 1.2F);
        level.playSound(null, player.blockPosition(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.4F);
        AbilityService.feedback(player, "Quenched " + count + " into " + made + " " + result.getHoverName().getString(),
                ChatFormatting.GOLD);
    }

    /** Hands out {@code amount} of the item in full stacks; what doesn't fit drops at the player's feet. */
    static void give(ServerPlayer player, ItemStack template, int amount) {
        int max = Math.max(1, template.getMaxStackSize());
        while (amount > 0) {
            int take = Math.min(max, amount);
            player.getInventory().placeItemBackInInventory(template.copyWithCount(take));
            amount -= take;
        }
    }

    private static void burst(ServerPlayer player, ServerLevel level) {
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(BURST_RADIUS),
                AbilityTargets::isHostile)) {
            Vec3 away = mob.position().subtract(player.position()).multiply(1, 0, 1);
            Vec3 push = away.lengthSqr() < 1.0E-4 ? player.getLookAngle() : away.normalize();
            mob.push(push.x * 1.1, 0.35, push.z * 1.1);
            mob.hurtMarked = true;
            mob.igniteForSeconds(BURST_FIRE_SECONDS);
        }
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 60, 1.6, 0.4, 1.6, 0.05);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 0.5, player.getZ(), 20, 1.2, 0.2, 1.2, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.9F, 0.7F);
        AbilityService.feedback(player, "Nothing to smelt: the heat bursts outward", ChatFormatting.GOLD);
    }
}
