package dev.spud.shadowslave.world.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Physical execution adapter for the Java-owned {@code drowned_listener} creature profile.
 *
 * <p>The stable content profile remains authoritative for creature identity. This entity still
 * reuses vanilla Drowned water/ground locomotion and navigation as a visible placeholder execution
 * layer, but player acquisition is narrowed to the authored VIBRATION identity and
 * {@code dry_ground} counterplay. Inherited instant melee resolution is replaced by one bounded
 * committed strike with a readable wind-up and an earned recovery window. Exact ranges, sampling,
 * strike timing and recovery remain DESIGN.</p>
 */
public final class DrownedListenerEntity extends Drowned {
    private static final int VIBRATION_SAMPLE_INTERVAL_TICKS = 4;

    private final Map<UUID, Vec3> sampledPlayerPositions = new HashMap<>();
    private int vibrationPursuitTicks;
    private int listeningRecoveryTicks;
    private int strikeWindupTicks;
    private int strikeRecoveryTicks;
    private LivingEntity committedStrikeTarget;
    private UUID vibrationTargetId;

    public DrownedListenerEntity(EntityType<? extends DrownedListenerEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Generic nearest-player targeting would bypass the authored VIBRATION identity and make
        // crouching / dry-ground counterplay meaningless. Retaliation remains intact.
        this.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal<?>);
        this.goalSelector.addGoal(0, new CommittedStrikeGoal());
        this.goalSelector.addGoal(0, new ListeningRecoveryGoal());
    }

    /** Prevent inherited Drowned trident/fishing-rod/shell equipment from becoming placeholder rewards. */
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // Intentionally empty. Reward/equipment rules remain Java-owned and UNKNOWN for this adapter.
    }

    /**
     * Converts inherited vanilla melee execution into a request to begin a readable committed
     * strike. The actual damage attempt is resolved later from {@link #tick()} after the wind-up.
     */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.level().isClientSide
                || this.strikeWindupTicks > 0
                || this.strikeRecoveryTicks > 0
                || this.listeningRecoveryTicks > 0
                || !(target instanceof LivingEntity livingTarget)
                || !livingTarget.isAlive()) {
            return false;
        }

        this.committedStrikeTarget = livingTarget;
        this.strikeWindupTicks = DrownedListenerStrikeBehavior.WINDUP_TICKS;
        this.getNavigation().stop();
        this.swing(InteractionHand.MAIN_HAND);
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        if (this.strikeWindupTicks > 0) {
            this.strikeWindupTicks--;
            this.getNavigation().stop();
            LivingEntity target = this.committedStrikeTarget;
            if (target != null && target.isAlive()) {
                this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            if (this.strikeWindupTicks == 0) {
                resolveCommittedStrike();
            }
            return;
        }

        if (this.strikeRecoveryTicks > 0) {
            this.strikeRecoveryTicks--;
            this.getNavigation().stop();
            return;
        }

        if (this.listeningRecoveryTicks > 0) {
            this.listeningRecoveryTicks--;
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

    private void resolveCommittedStrike() {
        LivingEntity target = this.committedStrikeTarget;
        boolean eligible = target != null
                && target.isAlive()
                && DrownedListenerStrikeBehavior.canConnect(
                        this.distanceToSqr(target),
                        Math.abs(target.getY() - this.getY()),
                        this.hasLineOfSight(target));
        boolean connected = eligible && super.doHurtTarget(target);
        this.committedStrikeTarget = null;
        this.strikeRecoveryTicks = DrownedListenerStrikeBehavior.recoveryTicks(connected);
        this.getNavigation().stop();
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

        UUID currentTargetId = this.getTarget() == null ? null : this.getTarget().getUUID();
        if (detected != null
                && DrownedListenerVibrationBehavior.canClaimVibrationTarget(this.vibrationTargetId, currentTargetId)) {
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
            // Do not let motion from before the earned recovery window count as fresh vibration
            // after the pause. The first post-recovery sample becomes a new baseline.
            this.sampledPlayerPositions.clear();
            this.listeningRecoveryTicks = DrownedListenerVibrationBehavior.recoveryTicksAfterReleasedVibrationTarget();
        }
    }

    private boolean isAttackablePlayer(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.isCreative();
    }

    private boolean isInCommittedStrikeState() {
        return this.strikeWindupTicks > 0 || this.strikeRecoveryTicks > 0;
    }

    /** Reserves movement/look controls while the committed strike winds up or recovers. */
    private final class CommittedStrikeGoal extends Goal {
        private CommittedStrikeGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return DrownedListenerEntity.this.isInCommittedStrikeState();
        }

        @Override
        public boolean canContinueToUse() {
            return DrownedListenerEntity.this.isInCommittedStrikeState();
        }

        @Override
        public void start() {
            DrownedListenerEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            DrownedListenerEntity.this.getNavigation().stop();
            LivingEntity target = DrownedListenerEntity.this.committedStrikeTarget;
            if (DrownedListenerEntity.this.strikeWindupTicks > 0 && target != null && target.isAlive()) {
                DrownedListenerEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }
    }

    /**
     * Reserves movement while the Listener reacquires its bearings after losing a vibration-owned
     * pursuit. Inherited Drowned stroll/pursuit goals therefore cannot erase the earned opening.
     */
    private final class ListeningRecoveryGoal extends Goal {
        private ListeningRecoveryGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return DrownedListenerEntity.this.listeningRecoveryTicks > 0;
        }

        @Override
        public boolean canContinueToUse() {
            return DrownedListenerEntity.this.listeningRecoveryTicks > 0;
        }

        @Override
        public void start() {
            DrownedListenerEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            DrownedListenerEntity.this.getNavigation().stop();
        }
    }
}
