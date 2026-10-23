package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gloam, Awakened (control): every torch and lantern within {@link #RADIUS}
 * blocks goes out, and every lit candle, campfire and furnace dims, for 30
 * seconds. Hostiles nearby lose sight of you for a moment. Each light comes
 * back as it was, unless someone has changed that block since.
 */
public final class SnuffAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.SNUFF.abilityId();
    static final int ESSENCE_COST = 35;
    static final int RADIUS = 10;
    static final int DURATION_TICKS = 20 * 30;
    static final int MAX_LIGHTS = 96;
    static final int BLIND_TICKS = 20 * 4;

    record Snuffed(BlockPos pos, BlockState was, BlockState now) {
    }

    record Dark(ResourceKey<Level> dimension, List<Snuffed> lights, long until) {
    }

    private static final Map<UUID, Dark> DARKS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.SNUFF.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (DARKS.containsKey(player.getUUID())) {
            AbilityService.feedback(player, "Your darkness still holds.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        BlockPos centre = player.blockPosition();
        List<Snuffed> lights = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-RADIUS, -RADIUS, -RADIUS), centre.offset(RADIUS, RADIUS, RADIUS))) {
            if (lights.size() >= MAX_LIGHTS) {
                break;
            }
            BlockState state = level.getBlockState(pos);
            BlockState out = snuffed(state);
            if (out == null) {
                continue;
            }
            // No neighbour updates: nothing pops off or drops while the light is out.
            level.setBlock(pos, out, Block.UPDATE_CLIENTS);
            level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 4, 0.1, 0.1, 0.1, 0.01);
            lights.add(new Snuffed(pos.immutable(), state, out));
        }
        int blinded = 0;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RADIUS), AbilityTargets::isHostile)) {
            if (mob.getTarget() == player) {
                mob.setTarget(null);
            }
            mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLIND_TICKS, 0, false, true));
            blinded++;
        }
        if (lights.isEmpty() && blinded == 0) {
            AbilityService.feedback(player, "There is no light here to put out.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        DARKS.put(player.getUUID(), new Dark(level.dimension(), List.copyOf(lights), level.getGameTime() + DURATION_TICKS));
        level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 0.2, player.getZ(), 30, 1.5, 0.1, 1.5, 0.01);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.6F);
        return Activation.USED;
    }

    /** What a light becomes while snuffed, or null if it is not one Snuff puts out. */
    static BlockState snuffed(BlockState state) {
        if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.is(Blocks.SOUL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH)
                || state.is(Blocks.LANTERN) || state.is(Blocks.SOUL_LANTERN)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT)
                && !state.is(Blocks.REDSTONE_TORCH) && !state.is(Blocks.REDSTONE_WALL_TORCH) && !state.is(Blocks.REDSTONE_LAMP)
                && !state.is(Blocks.REDSTONE_ORE) && !state.is(Blocks.DEEPSLATE_REDSTONE_ORE)) {
            return state.setValue(BlockStateProperties.LIT, false);
        }
        return null;
    }

    @Override
    public void tick(ServerPlayer player) {
        Dark dark = DARKS.get(player.getUUID());
        if (dark != null && dark.until() <= player.serverLevel().getGameTime()) {
            restore(player);
            AbilityService.feedback(player, "The lights return.", ChatFormatting.GRAY);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        restore(player);
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return DARKS.containsKey(player.getUUID());
    }

    private static void restore(ServerPlayer player) {
        Dark dark = DARKS.remove(player.getUUID());
        if (dark != null && player.getServer() != null) {
            relight(player.getServer(), dark);
        }
    }

    private static void relight(MinecraftServer server, Dark dark) {
        ServerLevel level = server.getLevel(dark.dimension());
        if (level == null) {
            return;
        }
        for (Snuffed light : dark.lights()) {
            if (level.isLoaded(light.pos()) && level.getBlockState(light.pos()).equals(light.now())) {
                level.setBlock(light.pos(), light.was(), Block.UPDATE_CLIENTS);
            }
        }
    }

    /** Relights everything, when the server stops. */
    static void restoreAll(MinecraftServer server) {
        DARKS.values().forEach(dark -> relight(server, dark));
        DARKS.clear();
    }

    static List<Snuffed> snuffedBy(ServerPlayer player) {
        Dark dark = DARKS.get(player.getUUID());
        return dark == null ? List.of() : dark.lights();
    }
}
