package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.creature.CreatureStats;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Ash Burrower, a Dormant Beast (DESIGN): a burrowing ambusher. On soft ground
 * (dirt, sand, gravel, ash and the like) it sinks under the surface and moves
 * as a furrow of dust, hunting by vibration: a player who moves without
 * sneaking within {@link AshBurrowerVibrationBehavior#DETECTION_RANGE} gives
 * themselves away. It bursts out under its prey.
 *
 * <p>Counterplay: stone floors stop it (it cannot burrow and is stunned when a
 * furrow runs onto stone), sneaking hides footsteps, arrows cannot reach it
 * under ground and a melee hit forces it up.</p>
 */
public final class AshBurrowerEntity extends NightmareCreature {
    public static final CreatureStats STATS = new CreatureStats(12.0, 3.0, 2.0, 0.28);
    private static final EntityDataAccessor<Boolean> BURROWED = SynchedEntityData.defineId(AshBurrowerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int VIBRATION_SAMPLE_INTERVAL_TICKS = 4;
    private static final double SURFACE_RANGE = 2.5;

    static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    static final RawAnimation BURROWED_LOOP = RawAnimation.begin().thenLoop("burrowed");
    static final RawAnimation EMERGE = RawAnimation.begin().thenPlay("emerge");
    static final RawAnimation BITE = RawAnimation.begin().thenPlay("bite");

    private final Map<UUID, Vec3> sampledPlayerPositions = new HashMap<>();
    private int vibrationPursuitUntilTick;
    private UUID vibrationTargetId;

    public AshBurrowerEntity(EntityType<? extends AshBurrowerEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public CreatureRank nativeRank() {
        return CreatureRank.DORMANT;
    }

    @Override
    public CreatureClass nativeClass() {
        return CreatureClass.BEAST;
    }

    @Override
    public CreatureStats nativeStats() {
        return STATS;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BURROWED, false);
    }

    public boolean isBurrowed() {
        return this.entityData.get(BURROWED);
    }

    private void setBurrowed(boolean burrowed) {
        if (burrowed == isBurrowed()) {
            return;
        }
        this.entityData.set(BURROWED, burrowed);
        BlockState ground = groundState();
        playSound(burrowed ? SoundEvents.ROOTED_DIRT_BREAK : SoundEvents.SNIFFER_DIGGING_STOP, 1.0F, burrowed ? 0.7F : 1.2F);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX(), getY() + 0.1, getZ(),
                    burrowed ? 18 : 30, 0.4, 0.2, 0.4, 0.15);
            if (!burrowed) {
                triggerAnim("action", "emerge");
            }
        }
    }

    private BlockState groundState() {
        return level().getBlockState(BlockPos.containing(getX(), getY() - 0.2, getZ()));
    }

    /** Soft ground it can swim through; stone, wood and the rest stop it. */
    public static boolean isSoftGround(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_SHOVEL) && !state.is(BlockTags.SNOW) || state.is(BlockTags.SAND);
    }

    @Override
    public boolean usesSightTargeting() {
        return false;
    }

    @Override
    protected void registerSignatureGoals() {
        this.goalSelector.addGoal(1, new BurrowGoal(this));
        this.targetSelector.addGoal(0, new VibrationTargetGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide || !isBurrowed()) {
            return;
        }
        BlockState ground = groundState();
        if (!isSoftGround(ground) && onGround()) {
            // A furrow that runs onto stone throws the Burrower out, dazed.
            setBurrowed(false);
            addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 3));
            playSound(SoundEvents.STONE_HIT, 1.0F, 0.6F);
            return;
        }
        if (getDeltaMovement().horizontalDistanceSqr() > 1.0E-4 && tickCount % 2 == 0 && level() instanceof ServerLevel server) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX(), getY() + 0.05, getZ(),
                    3, 0.25, 0.02, 0.25, 0.05);
            server.sendParticles(ParticleTypes.ASH, getX(), getY() + 0.1, getZ(), 2, 0.2, 0.05, 0.2, 0.01);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isBurrowed()) {
            if (source.is(DamageTypeTags.IS_PROJECTILE)) {
                return false;
            }
            if (source.getEntity() != null && !level().isClientSide) {
                setBurrowed(false);
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            triggerAnim("action", "bite");
        }
        return hit;
    }

    @Override
    public boolean isSilent() {
        return isBurrowed() || super.isSilent();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.55F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "body", 4, state -> {
            if (isBurrowed()) {
                return state.setAndContinue(BURROWED_LOOP);
            }
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        }));
        controllers.add(new AnimationController<>(this, "action", 2, state -> PlayState.STOP)
                .triggerableAnim("emerge", EMERGE)
                .triggerableAnim("bite", BITE));
    }

    /** Sinks into soft ground while hunting or idle and bursts out next to its prey. */
    static final class BurrowGoal extends Goal {
        private final AshBurrowerEntity burrower;

        BurrowGoal(AshBurrowerEntity burrower) {
            this.burrower = burrower;
            setFlags(EnumSet.noneOf(Flag.class));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            if (burrower.silenced()) {
                // A silenced burrower cannot stay under: it surfaces and walks.
                if (burrower.isBurrowed()) {
                    burrower.setBurrowed(false);
                }
                return;
            }
            LivingEntity target = burrower.getTarget();
            boolean soft = isSoftGround(burrower.groundState());
            if (!burrower.isBurrowed()) {
                boolean farFromPrey = target == null || burrower.distanceTo(target) > SURFACE_RANGE * 2;
                if (soft && burrower.onGround() && farFromPrey && !burrower.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                        && burrower.tickCount % 20 == 0) {
                    burrower.setBurrowed(true);
                }
                return;
            }
            if (target != null && burrower.distanceTo(target) <= SURFACE_RANGE) {
                burrower.setBurrowed(false);
                Vec3 toward = target.position().subtract(burrower.position()).normalize();
                burrower.setDeltaMovement(toward.x * 0.5, 0.45, toward.z * 0.5);
                burrower.playSound(SoundEvents.RAVAGER_ATTACK, 0.8F, 1.6F);
            }
        }
    }

    /** Hunts by vibration: moving, non-sneaking players nearby give themselves away. */
    static final class VibrationTargetGoal extends Goal {
        private final AshBurrowerEntity burrower;

        VibrationTargetGoal(AshBurrowerEntity burrower) {
            this.burrower = burrower;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            return burrower.tickCount % VIBRATION_SAMPLE_INTERVAL_TICKS == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            Player detected = null;
            double best = Double.MAX_VALUE;
            Map<UUID, Vec3> seen = new HashMap<>();
            for (Player player : burrower.level().getEntitiesOfClass(Player.class,
                    burrower.getBoundingBox().inflate(AshBurrowerVibrationBehavior.DETECTION_RANGE))) {
                if (!isAttackable(player)) {
                    continue;
                }
                Vec3 previous = burrower.sampledPlayerPositions.get(player.getUUID());
                seen.put(player.getUUID(), player.position());
                double moved = previous == null ? 0.0 : player.position().distanceToSqr(previous);
                double distance = burrower.distanceToSqr(player);
                if (AshBurrowerVibrationBehavior.detects(distance, moved, player.isShiftKeyDown()) && distance < best) {
                    detected = player;
                    best = distance;
                }
            }
            burrower.sampledPlayerPositions.clear();
            burrower.sampledPlayerPositions.putAll(seen);

            if (detected != null) {
                burrower.setTarget(detected);
                burrower.vibrationTargetId = detected.getUUID();
                burrower.vibrationPursuitUntilTick = burrower.tickCount + AshBurrowerVibrationBehavior.PURSUIT_TICKS;
                return;
            }
            LivingEntity current = burrower.getTarget();
            if (current instanceof Player player && burrower.vibrationTargetId != null) {
                int pursuit = Math.max(0, burrower.vibrationPursuitUntilTick - burrower.tickCount);
                if (AshBurrowerVibrationBehavior.shouldReleaseVibrationTarget(burrower.vibrationTargetId,
                        player.getUUID(), pursuit, isAttackable(player), burrower.distanceToSqr(player))) {
                    burrower.setTarget(null);
                    burrower.vibrationTargetId = null;
                }
            }
        }

        private static boolean isAttackable(Player player) {
            return player.isAlive() && !player.isSpectator() && !player.isCreative();
        }
    }
}
