package dev.spud.shadowslave.world.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Physical execution adapter for the Java-owned {@code drowned_listener} creature profile.
 *
 * <p>The stable content profile remains authoritative for creature identity. Vanilla Drowned
 * water/ground locomotion, navigation and melee remain temporary execution behavior, while
 * GeckoLib owns only the replaceable model/animation presentation lane. Player acquisition is
 * narrowed to a bounded VIBRATION executor instead of vanilla generic nearest-target authority.</p>
 */
public final class DrownedListenerEntity extends Drowned implements GeoEntity {
    private static final RawAnimation SWIM_ANIMATION = RawAnimation.begin().thenLoop("move.swim");
    private static final int VIBRATION_SAMPLE_INTERVAL_TICKS = 4;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, Vec3> sampledPlayerPositions = new HashMap<>();
    private int vibrationPursuitTicks;
    private UUID vibrationTargetId;

    public DrownedListenerEntity(EntityType<? extends DrownedListenerEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Drowned installs generic nearest-player/villager/golem target goals. Those would bypass
        // the authored sensory identity and make dry-ground/crouch counterplay meaningless.
        // Retaliation goals remain, so attacking the creature can still provoke ordinary combat.
        this.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal<?>);
    }

    /** Prevent inherited Drowned trident/fishing-rod/shell equipment from becoming placeholder rewards. */
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // Intentionally empty. Reward/equipment rules remain Java-owned and UNKNOWN for this adapter.
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "amphibious_locomotion", 5, this::locomotionAnimation),
                DefaultAnimations.genericAttackAnimation(this, DefaultAnimations.ATTACK_STRIKE));
    }

    private PlayState locomotionAnimation(AnimationState<DrownedListenerEntity> state) {
        if (this.isInWaterOrBubble() && state.isMoving()) {
            return state.setAndContinue(SWIM_ANIMATION);
        }
        if (state.isMoving()) {
            return state.setAndContinue(DefaultAnimations.WALK);
        }
        return state.setAndContinue(DefaultAnimations.IDLE);
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
            this.getNavigation().moveTo(target, DrownedListenerVibrationBehavior.PURSUIT_SPEED);
        }
    }

    private void refreshVibrationTarget() {
        double range = DrownedListenerVibrationBehavior.WATER_DETECTION_RANGE;
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(range),
                this::isAttackablePlayer);

        Set<UUID> nearbyIds = new HashSet<>();
        Player detected = null;
        double detectedDistanceSquared = Double.MAX_VALUE;
        boolean listenerInWater = this.isInWaterOrBubble();

        for (Player player : nearbyPlayers) {
            UUID playerId = player.getUUID();
            nearbyIds.add(playerId);
            Vec3 current = player.position();
            Vec3 previous = this.sampledPlayerPositions.put(playerId, current);
            double sampledDisplacementSquared = previous == null ? 0.0D : current.distanceToSqr(previous);
            double distanceSquared = this.distanceToSqr(player);

            if (DrownedListenerVibrationBehavior.detects(
                    distanceSquared,
                    sampledDisplacementSquared,
                    player.isShiftKeyDown(),
                    listenerInWater)
                    && distanceSquared < detectedDistanceSquared) {
                detected = player;
                detectedDistanceSquared = distanceSquared;
            }
        }

        this.sampledPlayerPositions.keySet().retainAll(nearbyIds);

        if (detected != null) {
            this.setTarget(detected);
            this.vibrationTargetId = detected.getUUID();
            this.vibrationPursuitTicks = DrownedListenerVibrationBehavior.PURSUIT_TICKS;
            this.getNavigation().moveTo(detected, DrownedListenerVibrationBehavior.PURSUIT_SPEED);
            return;
        }

        if (this.getTarget() instanceof Player player
                && DrownedListenerVibrationBehavior.shouldReleaseVibrationTarget(
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
