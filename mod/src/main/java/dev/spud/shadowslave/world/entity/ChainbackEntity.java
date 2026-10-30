package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.ability.ForewarningService;
import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.creature.CreatureStats;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.EnumSet;

/**
 * Chainback, an Awakened Monster (DESIGN): a heavy, shelled pursuer that
 * climbs cliffs and walls and drags prey off ledges with the chains grown
 * into its back. Its chain pull winds up for {@link ChainPullGoal#WINDUP_TICKS}
 * with a rattle and a line of rust in the air, then yanks the target toward
 * it.
 *
 * <p>Counterplay: break line of sight during the wind-up and the chain falls
 * short; it is too wide for one-block gaps, so a narrow passage beats it.</p>
 */
public final class ChainbackEntity extends NightmareCreature {
    public static final CreatureStats STATS = new CreatureStats(60.0, 7.0, 8.0, 0.27);
    private static final EntityDataAccessor<Boolean> CLIMBING = SynchedEntityData.defineId(ChainbackEntity.class, EntityDataSerializers.BOOLEAN);
    private static final DustParticleOptions RUST = new DustParticleOptions(new Vector3f(0.55F, 0.27F, 0.12F), 1.2F);

    static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    static final RawAnimation CLIMB = RawAnimation.begin().thenLoop("climb");
    static final RawAnimation SLAM = RawAnimation.begin().thenPlay("slam");
    static final RawAnimation CHAIN_THROW = RawAnimation.begin().thenPlay("chain_throw");

    public ChainbackEntity(EntityType<? extends ChainbackEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public CreatureRank nativeRank() {
        return CreatureRank.AWAKENED;
    }

    @Override
    public CreatureClass nativeClass() {
        return CreatureClass.MONSTER;
    }

    @Override
    public CreatureStats nativeStats() {
        return STATS;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CLIMBING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            this.entityData.set(CLIMBING, this.horizontalCollision);
        }
    }

    @Override
    public boolean onClimbable() {
        return this.entityData.get(CLIMBING);
    }

    @Override
    public void makeStuckInBlock(BlockState state, net.minecraft.world.phys.Vec3 motion) {
        if (!state.is(net.minecraft.world.level.block.Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motion);
        }
    }

    @Override
    protected void registerSignatureGoals() {
        this.goalSelector.addGoal(2, new ChainPullGoal(this));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            triggerAnim("action", "slam");
        }
        return hit;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.CHAIN_STEP, 0.6F, 0.6F);
        playSound(SoundEvents.SPIDER_STEP, 0.4F, 0.5F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CHAIN_HIT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.6F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "body", 4, state -> {
            if (onClimbable() && !onGround()) {
                return state.setAndContinue(CLIMB);
            }
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        }));
        controllers.add(new AnimationController<>(this, "action", 2, state -> PlayState.STOP)
                .triggerableAnim("slam", SLAM)
                .triggerableAnim("chain_throw", CHAIN_THROW));
    }

    /** Winds up, then drags the target toward it if the chain still has a clear line. */
    static final class ChainPullGoal extends Goal {
        static final int WINDUP_TICKS = 16;
        static final int COOLDOWN_TICKS = 100;
        static final double MIN_RANGE = 4.0;
        static final double MAX_RANGE = 12.0;
        static final double PULL = 1.1;

        private final ChainbackEntity chainback;
        private LivingEntity target;
        private int windup;
        private long readyAt;

        ChainPullGoal(ChainbackEntity chainback) {
            this.chainback = chainback;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity candidate = chainback.getTarget();
            if (candidate == null || chainback.silenced() || chainback.level().getGameTime() < readyAt) {
                return false;
            }
            double distance = chainback.distanceTo(candidate);
            return distance >= MIN_RANGE && distance <= MAX_RANGE && chainback.getSensing().hasLineOfSight(candidate);
        }

        @Override
        public void start() {
            target = chainback.getTarget();
            windup = WINDUP_TICKS;
            chainback.getNavigation().stop();
            chainback.playSound(SoundEvents.CHAIN_PLACE, 1.2F, 0.5F);
            chainback.triggerAnim("action", "chain_throw");
            ForewarningService.bigAttack(chainback, WINDUP_TICKS);
        }

        @Override
        public void tick() {
            chainback.getNavigation().stop();
            chainback.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (chainback.level() instanceof ServerLevel server && windup % 2 == 0) {
                double progress = 1.0 - (double) windup / WINDUP_TICKS;
                for (int step = 1; step <= 6; step++) {
                    double t = step / 7.0 * progress;
                    server.sendParticles(RUST,
                            chainback.getX() + (target.getX() - chainback.getX()) * t,
                            chainback.getY() + 1.0 + (target.getY() + 1.0 - chainback.getY() - 1.0) * t,
                            chainback.getZ() + (target.getZ() - chainback.getZ()) * t,
                            1, 0.02, 0.02, 0.02, 0.0);
                }
            }
            if (--windup > 0) {
                return;
            }
            readyAt = chainback.level().getGameTime() + COOLDOWN_TICKS;
            if (!target.isAlive() || !chainback.getSensing().hasLineOfSight(target)
                    || chainback.distanceTo(target) > MAX_RANGE + 2.0) {
                chainback.playSound(SoundEvents.CHAIN_BREAK, 1.0F, 0.7F);
                return;
            }
            var pull = chainback.position().subtract(target.position()).normalize();
            target.push(pull.x * PULL, 0.35, pull.z * PULL);
            target.hurtMarked = true;
            chainback.playSound(SoundEvents.CHAIN_HIT, 1.4F, 0.5F);
        }

        @Override
        public boolean canContinueToUse() {
            return windup > 0 && !chainback.silenced() && target != null && target.isAlive();
        }
    }
}
