package dev.spud.shadowslave.world.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
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
 * reuses vanilla Drowned water/ground locomotion, navigation and melee as a visible placeholder
 * execution layer, but player acquisition is narrowed to the authored VIBRATION identity and
 * {@code dry_ground} counterplay. Exact ranges, sampling and recovery timing remain DESIGN.</p>
 */
public final class DrownedListenerEntity extends Drowned {
    private static final int VIBRATION_SAMPLE_INTERVAL_TICKS = 4;

    private final Map<UUID, Vec3> sampledPlayerPositions = new HashMap<>();
    private int vibrationPursuitTicks;
    private int listeningRecoveryTicks;
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
        this.goalSelector.addGoal(0, new ListeningRecoveryGoal());
    }

    /** Prevent inherited Drowned trident/fishing-rod/shell equipment from becoming placeholder rewards. */
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // Intentionally empty. Reward/equipment rules remain Java-owned and UNKNOWN for this adapter.
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
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
            // Do not let motion from before the earned recovery window count as fresh vibration
            // after the pause. The first post-recovery sample becomes a new baseline.
            this.sampledPlayerPositions.clear();
            this.listeningRecoveryTicks = DrownedListenerVibrationBehavior.recoveryTicksAfterReleasedVibrationTarget();
        }
    }

    private boolean isAttackablePlayer(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.isCreative();
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
