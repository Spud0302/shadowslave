package dev.spud.shadowslave.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRetaliateTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtils;
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
 * <p>SmartBrainLib owns replaceable sensing/activity/path/combat scheduling and GeckoLib owns
 * replaceable visual animation execution. Creature identity, authored VIBRATION/AMBUSH rules,
 * progression and rewards remain Java-owned outside both libraries.</p>
 */
public final class AshBurrowerEntity extends Silverfish implements GeoEntity, SmartBrainOwner<AshBurrowerEntity> {
    private static final int VIBRATION_SAMPLE_INTERVAL_TICKS = 4;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, Vec3> sampledPlayerPositions = new HashMap<>();
    private int vibrationPursuitUntilTick;
    private UUID vibrationTargetId;

    public AshBurrowerEntity(EntityType<? extends Silverfish> type, Level level) {
        super(type, level);
        // Empty loot is not enough: vanilla Silverfish still award XP independently of loot tables.
        // This bounded encounter must not create any gameplay reward authority.
        this.xpReward = 0;
    }

    @Override
    protected void registerGoals() {
        // Do not call Silverfish.registerGoals(): merge-with-stone/wake-friends can mutate the world
        // and replace this executor. SmartBrainLib owns ordinary movement/combat scheduling now;
        // the one retained vanilla goal is the generic water-safety FloatGoal.
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    public List<? extends ExtendedSensor<? extends AshBurrowerEntity>> getSensors() {
        NearbyPlayersSensor<AshBurrowerEntity> vibrationPlayers = new NearbyPlayersSensor<>();
        vibrationPlayers.setRadius(AshBurrowerVibrationBehavior.DETECTION_RANGE);
        vibrationPlayers.setPredicate((player, owner) -> owner.isAttackablePlayer(player));
        vibrationPlayers.setScanRate(entity -> VIBRATION_SAMPLE_INTERVAL_TICKS);
        vibrationPlayers.afterScanning(AshBurrowerEntity::refreshVibrationTarget);

        return List.of(vibrationPlayers, new HurtBySensor<>());
    }

    @Override
    public BrainActivityGroup<? extends AshBurrowerEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),
                new MoveToWalkTarget<>());
    }

    @Override
    public BrainActivityGroup<? extends AshBurrowerEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<AshBurrowerEntity>(
                        new SetRetaliateTarget<>(),
                        new SetRandomLookTarget<>()),
                new OneRandomBehaviour<>(
                        new SetRandomWalkTarget<AshBurrowerEntity>().speedModifier(0.7F),
                        new Idle<AshBurrowerEntity>().runFor(entity -> entity.getRandom().nextInt(30, 60))));
    }

    @Override
    public BrainActivityGroup<? extends AshBurrowerEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<>(),
                new SetWalkTargetToAttackTarget<AshBurrowerEntity>()
                        .speedMod((owner, target) -> owner.isVibrationTarget(target)
                                ? (float)AshBurrowerVibrationBehavior.PURSUIT_SPEED
                                : 1.0F),
                new AnimatableMeleeAttack<AshBurrowerEntity>(0));
    }

    @Override
    protected void customServerAiStep() {
        tickBrain(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // These controllers only present already-existing movement/swing state.
        // They never feed back into creature identity, AI authority, combat resolution or progression.
        controllers.add(
                DefaultAnimations.genericWalkIdleController(this),
                DefaultAnimations.genericAttackAnimation(this, DefaultAnimations.ATTACK_STRIKE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    /**
     * Convert SmartBrainLib's generic nearby-player memory into the project-authored VIBRATION
     * acquisition rule. SmartBrainLib decides when to scan and later executes the selected target;
     * this method remains the Java-owned policy boundary deciding whether a player was detected.
     */
    private static void refreshVibrationTarget(AshBurrowerEntity entity) {
        List<Player> nearbyPlayers = BrainUtils.getMemory(entity, MemoryModuleType.NEAREST_PLAYERS);
        if (nearbyPlayers == null) {
            nearbyPlayers = List.of();
        }

        Set<UUID> nearbyIds = new HashSet<>();
        Player detected = null;
        double detectedDistanceSquared = Double.MAX_VALUE;

        for (Player player : nearbyPlayers) {
            UUID playerId = player.getUUID();
            nearbyIds.add(playerId);
            Vec3 current = player.position();
            Vec3 previous = entity.sampledPlayerPositions.put(playerId, current);
            double sampledDisplacementSquared = previous == null ? 0.0D : current.distanceToSqr(previous);
            double distanceSquared = entity.distanceToSqr(player);

            if (AshBurrowerVibrationBehavior.detects(
                    distanceSquared,
                    sampledDisplacementSquared,
                    player.isShiftKeyDown())
                    && distanceSquared < detectedDistanceSquared) {
                detected = player;
                detectedDistanceSquared = distanceSquared;
            }
        }

        entity.sampledPlayerPositions.keySet().retainAll(nearbyIds);

        if (detected != null) {
            BrainUtils.setTargetOfEntity(entity, detected);
            BrainUtils.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            entity.vibrationTargetId = detected.getUUID();
            entity.vibrationPursuitUntilTick = entity.tickCount + AshBurrowerVibrationBehavior.PURSUIT_TICKS;
            return;
        }

        LivingEntity currentTarget = BrainUtils.getTargetOfEntity(entity);
        if (!(currentTarget instanceof Player player) || entity.vibrationTargetId == null
                || !entity.vibrationTargetId.equals(player.getUUID())) {
            if (entity.tickCount >= entity.vibrationPursuitUntilTick) {
                entity.vibrationTargetId = null;
            }
            return;
        }

        int pursuitTicks = Math.max(0, entity.vibrationPursuitUntilTick - entity.tickCount);
        if (AshBurrowerVibrationBehavior.shouldReleaseVibrationTarget(
                entity.vibrationTargetId,
                player.getUUID(),
                pursuitTicks,
                entity.isAttackablePlayer(player),
                entity.distanceToSqr(player))) {
            BrainUtils.clearMemory(entity, MemoryModuleType.ATTACK_TARGET);
            BrainUtils.clearMemory(entity, MemoryModuleType.WALK_TARGET);
            entity.vibrationTargetId = null;
        }
    }

    private boolean isVibrationTarget(LivingEntity target) {
        return this.vibrationTargetId != null && this.vibrationTargetId.equals(target.getUUID());
    }

    private boolean isAttackablePlayer(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.isCreative();
    }
}
