package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stone, Ascended (defense): for fifteen seconds your skin is stone. Harm
 * that reaches you is halved, nothing knocks you back, and you move a little
 * slower.
 */
public final class StoneskinAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.STONESKIN.abilityId();
    static final int ESSENCE_COST = 50;
    static final int DURATION_TICKS = 20 * 15;
    static final float DAMAGE_TAKEN = 0.5F;
    private static final ResourceLocation KNOCKBACK = ResourceLocation.fromNamespaceAndPath("shadowslave", "stoneskin_knockback");
    private static final ResourceLocation SPEED = ResourceLocation.fromNamespaceAndPath("shadowslave", "stoneskin_speed");

    private static final Map<UUID, Long> ACTIVE_UNTIL = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.STONESKIN.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (active(player)) {
            AbilityService.feedback(player, "Your skin is already stone.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        ACTIVE_UNTIL.put(player.getUUID(), level.getGameTime() + DURATION_TICKS);
        hold(player, true);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.4, 0.8, 0.4, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 1.0F, 0.5F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        if (until == null) {
            return;
        }
        if (until <= player.serverLevel().getGameTime()) {
            clear(player);
            AbilityService.feedback(player, "Your skin softens.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
        } else {
            hold(player, true);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        ACTIVE_UNTIL.remove(player.getUUID());
        hold(player, false);
    }

    static boolean active(ServerPlayer player) {
        return ACTIVE_UNTIL.containsKey(player.getUUID());
    }

    private static void hold(ServerPlayer player, boolean on) {
        KitModifiers.keep(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK, on ? 1.0 : 0.0);
        KitModifiers.keep(player, Attributes.MOVEMENT_SPEED, SPEED, on ? -0.1 : 0.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player && active(player)) {
            event.setNewDamage(event.getNewDamage() * DAMAGE_TAKEN);
        }
    }
}
