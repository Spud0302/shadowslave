package dev.spud.shadowslave.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Minecraft executor for the Java-owned Ash Burrower Nightmare Creature.
 *
 * <p>GeckoLib owns only visual animation execution. Creature identity, authored senses/pressures,
 * combat authority, progression and rewards remain Java-owned outside GeckoLib.</p>
 */
public final class AshBurrowerEntity extends Silverfish implements GeoEntity {
    private static final int VIBRATION_SAMPLE_INTERVAL_TICKS = 4;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, Vec3> sampledPlayerPositions = new HashMap<>();
    private int vibrationPursuitTicks;
    private UUID vibrationTargetId;

    public AshBurrowerEntity(EntityType<? extends Silverfish> type, Level level) {
        super(type, level);
        // Empty loot is not enough: vanilla Silverfish still award XP independently of loot tables.
        // This bounded encounter must not create any gameplay reward authority.
        this.xpReward = 0;
    }

    @Override
    protected void registerGoals() {
        // Deliberately do not call Silverfish.registerGoals(): it installs merge-with-stone and
        // wake-friends goals that can replace blocks, discard this executor, and later spawn a
        // vanilla Silverfish. Keep only generic removable movement/combat execution here.
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.7D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // There is intentionally no generic nearest-player target goal. Player acquisition is driven
        // by the authored VIBRATION sense through the bounded executor below.
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // These controllers only present already-existing movement/swing state.
        // They never feed back into creature identity, AI, combat resolution or progression.
        controllers.add(
                DefaultAnimations.genericWalkIdleController(this),
                DefaultAnimations.genericAttackAnimation(this, DefaultAnimations.ATTACK_STRIKE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        if (this.tickCount % VIBRATION_SAMPLE_INTERVAL_TICKS == 0) {
            refreshVibrationTarget();
        }

        if (this.vibrationPursuitTicks > 0
                && this.getTarget() instanceof Player target
                && target.getUUID().equals(this.vibrationTargetId)
                && isAttackablePlayer(target)) {
            this.vibrationPursuitTicks--;
            this.getNavigation().moveTo(target, AshBurrowerVibrationBehavior.PURSUIT_SPEED);
        }
    }

    private void refreshVibrationTarget() {
        double range = AshBurrowerVibrationBehavior.DETECTION_RANGE;
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(range),
                this::isAttackablePlayer);

        Set<UUID> nearbyIds = new HashSet<>();
        Player detected = null;
        double detectedDistanceSquared = Double.MAX_VALUE;

        for (Player player : nearbyPlayers) {
            UUID playerId = player.getUUID();
            nearbyIds.add(playerId);
            Vec3 current = player.position();
            Vec3 previous = this.sampledPlayerPositions.put(playerId, current);
            double sampledDisplacementSquared = previous == null ? 0.0D : current.distanceToSqr(previous);
            double distanceSquared = this.distanceToSqr(player);

            if (AshBurrowerVibrationBehavior.detects(
                    distanceSquared,
                    sampledDisplacementSquared,
                    player.isShiftKeyDown())
                    && distanceSquared < detectedDistanceSquared) {
                detected = player;
                detectedDistanceSquared = distanceSquared;
            }
        }

        this.sampledPlayerPositions.keySet().retainAll(nearbyIds);

        if (detected != null) {
            this.setTarget(detected);
            this.vibrationTargetId = detected.getUUID();
            this.vibrationPursuitTicks = AshBurrowerVibrationBehavior.PURSUIT_TICKS;
            this.getNavigation().moveTo(detected, AshBurrowerVibrationBehavior.PURSUIT_SPEED);
            return;
        }

        if (this.getTarget() instanceof Player player
                && AshBurrowerVibrationBehavior.shouldReleaseVibrationTarget(
                        this.vibrationTargetId,
                        player.getUUID(),
                        this.vibrationPursuitTicks,
                        isAttackablePlayer(player),
                        this.distanceToSqr(player))) {
            this.setTarget(null);
            this.vibrationTargetId = null;
            this.getNavigation().stop();
        }
    }

    private boolean isAttackablePlayer(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.isCreative();
    }
}
