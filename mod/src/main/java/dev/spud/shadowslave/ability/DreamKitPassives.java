package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.DreamCandleRitual;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.dreamrealm.seed.SeedData;
import dev.spud.shadowslave.dreamrealm.seed.SeedService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The always-on halves of the Dream Realm Natures: Wayfarer's Pathfinder and
 * Wanderlust, and Lull's Drowsy Aura and Insomnia. Kept apart from
 * {@link KitPassives} since they lean on the Dream Realm.
 */
public final class DreamKitPassives {
    /** Pathfinder: how often the pull shows, and how far it reaches. */
    static final int PATHFINDER_TICKS = 40;
    static final double PATHFINDER_RANGE = 512.0;
    /** Pull colours (rgb); the particle options are made per use so unit tests can load this class without a registry. */
    private static final Vector3f SEED_PULL = new Vector3f(0.85F, 0.2F, 0.3F);
    private static final Vector3f HOME_PULL = new Vector3f(1.0F, 0.8F, 0.35F);
    /** Wanderlust: staying within this many blocks counts as lingering. */
    static final double WANDER_RADIUS = 24.0;
    /** Wanderlust: full refill for the first two minutes, then down to a quarter over the next five. */
    static final long WANDER_GRACE_TICKS = 20L * 60 * 2;
    static final long WANDER_FADE_TICKS = 20L * 60 * 5;
    static final float WANDER_FLOOR = 0.25F;
    /** Drowsy Aura: every this-many blows a hostile aims at a Lull, one fizzles as it nods off. */
    static final int DROWSY_EVERY = 4;
    static final String DROWSY_TAG = "shadowslave_drowsy_blows";

    record Settle(ResourceKey<Level> dimension, Vec3 centre, long since) {
    }

    private static final Map<UUID, Settle> SETTLED = new ConcurrentHashMap<>();

    private DreamKitPassives() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        if (KitRules.hasFlaw(player, AspectKitCatalog.WAYFARER.flawEffectId())) {
            settle(player);
        } else {
            SETTLED.remove(player.getUUID());
        }
        if (player.tickCount % PATHFINDER_TICKS == 0 && KitRules.hasInnate(player, AspectKitCatalog.WAYFARER.innateId())) {
            pathfinder(player);
        }
    }

    // ---- Wayfarer ------------------------------------------------------------------------------------------------

    /** Pathfinder: faint trails toward the nearest Seed (red) and toward home (gold), seen only by the Wayfarer. */
    private static void pathfinder(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (DreamRealm.is(level)) {
            SeedService.nearest(level, player.blockPosition()).map(SeedData.Seed::pos)
                    .filter(pos -> pos.distSqr(player.blockPosition()) <= PATHFINDER_RANGE * PATHFINDER_RANGE)
                    .ifPresent(pos -> pull(player, pos, SEED_PULL));
        }
        AnchorRecallAbility.home(player).ifPresent(pos -> pull(player, pos, HOME_PULL));
    }

    private static void pull(ServerPlayer player, BlockPos target, Vector3f colour) {
        DustParticleOptions dust = new DustParticleOptions(colour, 1.5F);
        Vec3 eye = player.getEyePosition();
        Vec3 toward = Vec3.atCenterOf(target).subtract(eye);
        if (toward.lengthSqr() < 16.0) {
            return;
        }
        Vec3 step = toward.normalize();
        for (int i = 3; i <= 10; i++) {
            Vec3 at = eye.add(step.scale(i * 0.8)).add(0, -0.4, 0);
            player.serverLevel().sendParticles(player, dust, false, at.x, at.y, at.z, 2, 0.04, 0.04, 0.04, 0.0);
        }
    }

    /** Wanderlust: notes where the Wayfarer has settled, starting afresh once they move on. */
    private static void settle(ServerPlayer player) {
        Settle settled = SETTLED.get(player.getUUID());
        if (settled == null || !settled.dimension().equals(player.level().dimension())
                || settled.centre().distanceToSqr(player.position()) > WANDER_RADIUS * WANDER_RADIUS) {
            SETTLED.put(player.getUUID(), new Settle(player.level().dimension(), player.position(), player.serverLevel().getGameTime()));
        }
    }

    /** Wanderlust's share of the essence refill for a Wayfarer who has lingered this long. */
    static float wanderlustFactor(long lingered) {
        if (lingered <= WANDER_GRACE_TICKS) {
            return 1.0F;
        }
        float faded = Math.min(1.0F, (float) (lingered - WANDER_GRACE_TICKS) / WANDER_FADE_TICKS);
        return 1.0F - faded * (1.0F - WANDER_FLOOR);
    }

    /** The refill share for this player right now: 1 unless Wanderlust has caught up with them. */
    public static float wanderlust(ServerPlayer player) {
        Settle settled = SETTLED.get(player.getUUID());
        if (settled == null || !KitRules.hasFlaw(player, AspectKitCatalog.WAYFARER.flawEffectId())) {
            return 1.0F;
        }
        return wanderlustFactor(player.serverLevel().getGameTime() - settled.since());
    }

    /** Pretends the Wayfarer settled {@code ticks} ago, for tests. */
    static void lingered(ServerPlayer player, long ticks) {
        SETTLED.put(player.getUUID(), new Settle(player.level().dimension(), player.position(),
                player.serverLevel().getGameTime() - ticks));
    }

    public static void forget(UUID player) {
        SETTLED.remove(player);
    }

    // ---- Lull ----------------------------------------------------------------------------------------------------

    /**
     * Drowsy Aura: every fourth blow a hostile creature aims at a Lull fizzles
     * as it nods off, so they land a quarter fewer hits. Sleeping creatures
     * (Lullaby, Dreamfall) land none.
     */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Mob attacker && Slumber.asleep(attacker)) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player && event.getSource().getEntity() instanceof Mob attacker
                && AbilityTargets.isHostile(attacker) && KitRules.hasInnate(player, AspectKitCatalog.LULL.innateId())
                && drowsyBlow(attacker)) {
            event.setCanceled(true);
            player.serverLevel().sendParticles(ParticleTypes.CLOUD, attacker.getX(), attacker.getEyeY() + 0.3, attacker.getZ(),
                    3, 0.15, 0.1, 0.15, 0.0);
        }
    }

    /** Counts a hostile's blow; true when this one fizzles. */
    static boolean drowsyBlow(Mob attacker) {
        int blows = attacker.getPersistentData().getInt(DROWSY_TAG) + 1;
        attacker.getPersistentData().putInt(DROWSY_TAG, blows % DROWSY_EVERY);
        return blows % DROWSY_EVERY == 0;
    }

    /** Insomnia: a Lull can only lie down where a lit Dream Candle burns by the bed. */
    public static void onCanSleep(CanPlayerSleepEvent event) {
        ServerPlayer player = event.getEntity();
        if (event.getProblem() != null || !KitRules.hasFlaw(player, AspectKitCatalog.LULL.flawEffectId())) {
            return;
        }
        if (DreamCandleRitual.findLitCandle(player.serverLevel(), event.getPos()).isEmpty()) {
            event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
            player.displayClientMessage(Component.literal("Sleep will not come. Only a Dream Candle can carry you through the night.")
                    .withStyle(ChatFormatting.DARK_PURPLE), true);
        }
    }
}
