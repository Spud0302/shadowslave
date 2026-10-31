package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.echoes.EchoService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Standard (Run I, clan wars): Under the Colours wards your allies near you,
 * Plant the Standard sets a banner that wards them near it too and leans on a
 * Gateway's channel, Hold the Line and Last Rally strengthen it, and
 * Oathbound weakens you for walking away from it while your allies fight.
 */
public final class StandardColours {
    static final double WARD_RADIUS = 8.0;
    static final double WARD_SHARE = 0.1;
    static final int BANNER_TICKS = 20 * 120;
    static final double OATH_LEASH = 12.0;
    static final int FIGHT_HOLD_TICKS = 100;
    static final float RALLY_HEAL = 8.0F;
    static final int RALLY_ABSORB_TICKS = 200;

    static final class Banner {
        final ResourceKey<Level> level;
        final BlockPos pos;
        final long until;
        long fightUntil;
        boolean warned;

        Banner(ResourceKey<Level> level, BlockPos pos, long until) {
            this.level = level;
            this.pos = pos;
            this.until = until;
        }

        boolean near(Entity entity, double radius) {
            return entity.level().dimension().equals(level) && entity.distanceToSqr(pos.getCenter()) <= radius * radius;
        }
    }

    private static final Map<UUID, Banner> BANNERS = new ConcurrentHashMap<>();

    private StandardColours() {
    }

    static boolean isStandard(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.STANDARD.innateId());
    }

    /** Your allies: your clan, your Echoes and your tamed creatures. */
    static boolean ally(ServerPlayer player, Entity entity) {
        if (entity == player || !(entity instanceof LivingEntity living) || !living.isAlive()) {
            return false;
        }
        if (entity instanceof ServerPlayer other) {
            return ClanService.clanOf(player.server, player.getUUID()).filter(clan -> clan.has(other.getUUID())).isPresent();
        }
        return EchoService.isAlly(player, entity) || entity instanceof TamableAnimal pet && pet.isOwnedBy(player);
    }

    /** You or one of your allies. */
    static boolean covered(ServerPlayer player, Entity entity) {
        return entity == player || ally(player, entity);
    }

    /** Whether a Standard's colours fall on this creature: an ally near them, or anyone of theirs near their banner. */
    static boolean warded(LivingEntity victim) {
        if (!(victim.level() instanceof ServerLevel level)) {
            return false;
        }
        for (ServerPlayer standard : level.players()) {
            if (!isStandard(standard)) {
                continue;
            }
            if (ally(standard, victim) && standard.distanceToSqr(victim) <= WARD_RADIUS * WARD_RADIUS) {
                return true;
            }
        }
        for (Map.Entry<UUID, Banner> entry : BANNERS.entrySet()) {
            ServerPlayer owner = KitRules.online(level.getServer(), entry.getKey());
            if (owner != null && entry.getValue().near(victim, WARD_RADIUS) && covered(owner, victim)) {
                return true;
            }
        }
        return false;
    }

    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getNewDamage() > 0 && warded(event.getEntity())) {
            event.setNewDamage(event.getNewDamage() * (float) (1.0 - WARD_SHARE));
        }
    }

    /** Hold the Line: no one of yours near your banner is knocked back. */
    public static void onKnockBack(LivingKnockBackEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        for (Map.Entry<UUID, Banner> entry : BANNERS.entrySet()) {
            ServerPlayer owner = KitRules.online(level.getServer(), entry.getKey());
            if (owner != null && entry.getValue().near(victim, WARD_RADIUS) && covered(owner, victim)
                    && KitRules.hasInnate(owner, KitLadder.HOLD_THE_LINE.abilityId())) {
                event.setCanceled(true);
                return;
            }
        }
    }

    /** A blow near a banner, on or by one of its side, means its side is fighting there. */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (BANNERS.isEmpty() || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        long now = level.getServer().getTickCount();
        for (Map.Entry<UUID, Banner> entry : BANNERS.entrySet()) {
            ServerPlayer owner = KitRules.online(level.getServer(), entry.getKey());
            Banner banner = entry.getValue();
            if (owner == null || !banner.near(victim, WARD_RADIUS)) {
                continue;
            }
            if (ally(owner, victim) || attacker != null && ally(owner, attacker)) {
                banner.fightUntil = now + FIGHT_HOLD_TICKS;
            }
        }
    }

    // ---- the banner ------------------------------------------------------------------------------------------------

    static void plant(ServerPlayer player, BlockPos pos) {
        BANNERS.put(player.getUUID(), new Banner(player.level().dimension(), pos.immutable(), player.server.getTickCount() + BANNER_TICKS));
    }

    static Optional<BlockPos> planted(ServerPlayer player) {
        return Optional.ofNullable(BANNERS.get(player.getUUID())).map(banner -> banner.pos);
    }

    static Optional<Banner> banner(ServerPlayer player) {
        return Optional.ofNullable(BANNERS.get(player.getUUID()));
    }

    /** Whose banners stand within {@code radius} of a spot. */
    public static List<UUID> plantedNear(ServerLevel level, BlockPos at, double radius) {
        List<UUID> owners = new ArrayList<>();
        BANNERS.forEach((owner, banner) -> {
            if (banner.level.equals(level.dimension()) && banner.pos.distSqr(at) <= radius * radius) {
                owners.add(owner);
            }
        });
        return owners;
    }

    public static List<BlockPos> bannersNear(ServerLevel level, BlockPos at, double radius) {
        List<BlockPos> found = new ArrayList<>();
        BANNERS.values().forEach(banner -> {
            if (banner.level.equals(level.dimension()) && banner.pos.distSqr(at) <= radius * radius) {
                found.add(banner.pos);
            }
        });
        return found;
    }

    /** The banner comes down: struck down, or its time is up. Last Rally heals everyone of yours near it. */
    static void fall(MinecraftServer server, UUID owner, boolean struck) {
        Banner banner = BANNERS.remove(owner);
        if (banner == null) {
            return;
        }
        ServerLevel level = server.getLevel(banner.level);
        if (level == null) {
            return;
        }
        if (level.isLoaded(banner.pos) && level.getBlockState(banner.pos).is(Blocks.PURPLE_BANNER)) {
            level.removeBlock(banner.pos, false);
        }
        level.sendParticles(ParticleTypes.POOF, banner.pos.getX() + 0.5, banner.pos.getY() + 1.0, banner.pos.getZ() + 0.5, 10, 0.2, 0.5, 0.2, 0.02);
        ServerPlayer player = KitRules.online(server, owner);
        if (player == null) {
            return;
        }
        AbilityService.feedback(player, struck ? "Your Standard has fallen." : "Your Standard is furled.", ChatFormatting.GRAY);
        if (KitRules.hasInnate(player, KitLadder.LAST_RALLY.abilityId())) {
            rally(player, level, banner.pos);
        }
        AbilityService.syncStatus(player);
    }

    /** Last Rally: everyone of yours near the fallen banner is healed and steadied. */
    static void rally(ServerPlayer player, ServerLevel level, BlockPos pos) {
        int healed = 0;
        for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(WARD_RADIUS),
                entity -> covered(player, entity) && entity.distanceToSqr(pos.getCenter()) <= WARD_RADIUS * WARD_RADIUS)) {
            ally.heal(RALLY_HEAL);
            ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, RALLY_ABSORB_TICKS, 0, false, true, true));
            level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY() + ally.getBbHeight() + 0.3, ally.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
            healed++;
        }
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 60, 1.5, 0.8, 1.5, 0.3);
        level.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.7F, 1.3F);
        if (healed > 0) {
            AbilityService.feedback(player, "Last Rally: the fallen Standard lifts " + healed + " of yours.",
                    ChatFormatting.LIGHT_PURPLE);
        }
    }

    /** Watches every banner: down when it is gone or its time is up, and its oath on its bearer. */
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (!BANNERS.isEmpty() && server.getTickCount() % 10 == 0) {
            watch(server);
        }
    }

    static void watch(MinecraftServer server) {
        long now = server.getTickCount();
        for (Map.Entry<UUID, Banner> entry : List.copyOf(BANNERS.entrySet())) {
            Banner banner = entry.getValue();
            ServerLevel level = server.getLevel(banner.level);
            if (level == null || banner.until <= now) {
                fall(server, entry.getKey(), false);
                continue;
            }
            if (level.isLoaded(banner.pos) && !level.getBlockState(banner.pos).is(Blocks.PURPLE_BANNER)) {
                fall(server, entry.getKey(), true);
                continue;
            }
            if (now % 20 == 0) {
                level.sendParticles(ParticleTypes.WITCH, banner.pos.getX() + 0.5, banner.pos.getY() + 0.1, banner.pos.getZ() + 0.5,
                        10, WARD_RADIUS / 2, 0.05, WARD_RADIUS / 2, 0.0);
            }
            ServerPlayer owner = KitRules.online(server, entry.getKey());
            if (owner != null) {
                oath(owner, banner, now);
            }
        }
    }

    /** Oathbound: while your side fights near your banner, walking away from it weakens you. */
    static void oath(ServerPlayer player, Banner banner, long now) {
        if (!KitRules.hasFlaw(player, AspectKitCatalog.STANDARD.flawEffectId())) {
            return;
        }
        if (banner.fightUntil <= now) {
            banner.warned = false;
            return;
        }
        if (banner.near(player, OATH_LEASH)) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, true, true));
        if (!banner.warned) {
            banner.warned = true;
            AbilityService.feedback(player, "Oathbound: your side fights under your Standard, and you walk away.", ChatFormatting.RED);
        }
    }

    /** Striking the banner down is how it falls: it gives no cloth. */
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        for (Map.Entry<UUID, Banner> entry : BANNERS.entrySet()) {
            if (entry.getValue().pos.equals(event.getPos()) && entry.getValue().level.equals(level.dimension())) {
                event.setCanceled(true);
                fall(level.getServer(), entry.getKey(), true);
                return;
            }
        }
    }

    /** Takes the banner down quietly, with no rally: its bearer left or changed their soul. */
    static void lower(MinecraftServer server, UUID owner) {
        Banner banner = BANNERS.remove(owner);
        if (banner == null) {
            return;
        }
        ServerLevel level = server.getLevel(banner.level);
        if (level != null && level.isLoaded(banner.pos) && level.getBlockState(banner.pos).is(Blocks.PURPLE_BANNER)) {
            level.removeBlock(banner.pos, false);
        }
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        for (UUID owner : List.copyOf(BANNERS.keySet())) {
            lower(event.getServer(), owner);
        }
    }
}
