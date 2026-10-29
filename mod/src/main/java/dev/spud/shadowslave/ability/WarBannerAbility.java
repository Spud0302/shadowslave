package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Herald's Dormant ability: plant a war banner at your feet. For fifteen
 * seconds you and your allies within {@link #RADIUS} blocks of it run faster
 * and hit harder. The banner is the Spell's, not yours: it cannot be broken
 * for its cloth, and it is gone when the call ends.
 */
public final class WarBannerAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.HERALD.abilityId();
    static final int ESSENCE_COST = 35;
    static final int DURATION_TICKS = 20 * 15;
    static final double RADIUS = 8.0;
    static final int BUFF_TICKS = 40;

    record Banner(ResourceKey<Level> dimension, BlockPos pos, long until) {
    }

    private static final Map<UUID, Banner> BANNERS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.HERALD.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (BANNERS.containsKey(player.getUUID())) {
            AbilityService.feedback(player, "Your banner still flies.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        Optional<BlockPos> spot = spot(level, player.blockPosition());
        if (spot.isEmpty()) {
            AbilityService.feedback(player, "There is no room here to plant a banner.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        int facing = Math.floorMod(Math.round((player.getYRot() + 180.0F) * 16.0F / 360.0F), 16);
        level.setBlockAndUpdate(spot.get(), Blocks.RED_BANNER.defaultBlockState().setValue(BannerBlock.ROTATION, facing));
        BANNERS.put(player.getUUID(), new Banner(level.dimension(), spot.get(), level.getGameTime() + DURATION_TICKS));
        level.playSound(null, spot.get(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 0.6F, 1.4F);
        level.sendParticles(ParticleTypes.FLAME, spot.get().getX() + 0.5, spot.get().getY() + 1.6, spot.get().getZ() + 0.5,
                20, 0.2, 0.4, 0.2, 0.02);
        rally(player, level, spot.get());
        return Activation.USED;
    }

    /** Open ground at your feet, or beside you if something already stands there. */
    static Optional<BlockPos> spot(ServerLevel level, BlockPos feet) {
        for (int dy = 0; dy <= 1; dy++) {
            for (int[] step : new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}}) {
                BlockPos pos = feet.offset(step[0], dy, step[1]);
                if (level.getBlockState(pos).canBeReplaced() && level.getBlockState(pos).getFluidState().isEmpty()
                        && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) {
                    return Optional.of(pos.immutable());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void tick(ServerPlayer player) {
        Banner banner = BANNERS.get(player.getUUID());
        if (banner == null) {
            return;
        }
        ServerLevel level = player.server.getLevel(banner.dimension());
        if (level == null || banner.until() <= level.getGameTime() || !level.getBlockState(banner.pos()).is(Blocks.RED_BANNER)) {
            lower(player.server, player.getUUID());
            AbilityService.feedback(player, "Your banner is furled.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
            return;
        }
        if (level.getGameTime() % 10 == 0) {
            rally(player, level, banner.pos());
        }
    }

    /** Everyone fighting under the banner: you and your allies near it. */
    private static void rally(ServerPlayer player, ServerLevel level, BlockPos pos) {
        for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(RADIUS),
                entity -> entity.isAlive() && (entity == player || CarryTheFlameAbility.isAlly(player, entity))
                        && entity.distanceToSqr(pos.getCenter()) <= RADIUS * RADIUS)) {
            ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BUFF_TICKS, 0, false, true, true));
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BUFF_TICKS, 0, false, true, true));
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                12, RADIUS / 2, 0.1, RADIUS / 2, 0.0);
    }

    /** Takes the banner down, if it still stands. */
    static void lower(MinecraftServer server, UUID owner) {
        Banner banner = BANNERS.remove(owner);
        if (banner == null) {
            return;
        }
        ServerLevel level = server.getLevel(banner.dimension());
        if (level != null && level.isLoaded(banner.pos()) && level.getBlockState(banner.pos()).is(Blocks.RED_BANNER)) {
            level.removeBlock(banner.pos(), false);
            level.sendParticles(ParticleTypes.POOF, banner.pos().getX() + 0.5, banner.pos().getY() + 1.0,
                    banner.pos().getZ() + 0.5, 8, 0.2, 0.5, 0.2, 0.02);
        }
    }

    static Optional<BlockPos> planted(ServerPlayer player) {
        return Optional.ofNullable(BANNERS.get(player.getUUID())).map(Banner::pos);
    }

    /** The war banner is not cloth to be taken: breaking it only furls it early. */
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        for (Map.Entry<UUID, Banner> entry : BANNERS.entrySet()) {
            if (entry.getValue().pos().equals(event.getPos()) && entry.getValue().dimension().equals(level.dimension())) {
                event.setCanceled(true);
                lower(level.getServer(), entry.getKey());
                return;
            }
        }
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        for (UUID owner : java.util.List.copyOf(BANNERS.keySet())) {
            lower(event.getServer(), owner);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        lower(player.server, player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return BANNERS.containsKey(player.getUUID());
    }
}
