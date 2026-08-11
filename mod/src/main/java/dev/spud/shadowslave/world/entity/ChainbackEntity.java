package dev.spud.shadowslave.world.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

/**
 * Physical execution adapter for the Java-owned {@code chainback} creature profile.
 *
 * <p>Creature identity remains authoritative Java content. Vanilla Spider climbing/pathfinding and
 * hostile AI are still temporary execution behavior, GeckoLib owns only replaceable presentation,
 * and the bounded displacement exchange below executes the profile's already-authored DISPLACEMENT
 * pressure without introducing a generic combat-engine dependency.</p>
 */
public final class ChainbackEntity extends Spider implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int displacementCooldown;
    private int displacementTelegraphTicks;
    private int displacementRecoveryTicks;

    public ChainbackEntity(EntityType<? extends ChainbackEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new DisplacementRecoveryGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.displacementCooldown > 0) {
            this.displacementCooldown--;
        }
        if (this.level().isClientSide) {
            return;
        }
        if (this.displacementRecoveryTicks > 0) {
            this.displacementRecoveryTicks--;
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || !this.getSensing().hasLineOfSight(target)) {
            if (this.displacementTelegraphTicks > 0) {
                this.beginDisplacementRecovery(false);
            }
            return;
        }

        double deltaX = this.getX() - target.getX();
        double deltaZ = this.getZ() - target.getZ();
        double verticalDelta = this.getY() - target.getY();
        double horizontalDistanceSquared = deltaX * deltaX + deltaZ * deltaZ;
        if (!ChainbackDisplacementBehavior.canPull(
                horizontalDistanceSquared,
                verticalDelta,
                this.displacementCooldown)) {
            if (this.displacementTelegraphTicks > 0) {
                this.beginDisplacementRecovery(false);
            }
            return;
        }

        if (this.displacementTelegraphTicks <= 0) {
            this.displacementTelegraphTicks = ChainbackDisplacementBehavior.TELEGRAPH_TICKS;
            this.playSound(SoundEvents.CHAIN_PLACE, 0.8F, 0.7F);
            this.emitDisplacementTelegraph(target);
            return;
        }

        if (ChainbackDisplacementBehavior.shouldTelegraphPulse(this.displacementTelegraphTicks)) {
            this.emitDisplacementTelegraph(target);
        }
        this.displacementTelegraphTicks--;
        if (this.displacementTelegraphTicks > 0) {
            return;
        }

        ChainbackDisplacementBehavior.PullVector pull = ChainbackDisplacementBehavior.pullToward(deltaX, deltaZ);
        if (pull.isZero()) {
            this.beginDisplacementRecovery(false);
            return;
        }
        target.push(pull.x(), pull.y(), pull.z());
        this.beginDisplacementRecovery(true);
    }

    private void beginDisplacementRecovery(boolean pullConnected) {
        this.displacementTelegraphTicks = 0;
        this.displacementRecoveryTicks = ChainbackDisplacementBehavior.recoveryTicks(pullConnected);
        this.displacementCooldown = Math.max(this.displacementCooldown, ChainbackDisplacementBehavior.COOLDOWN_TICKS);
        this.getNavigation().stop();
        if (!pullConnected) {
            this.playSound(SoundEvents.CHAIN_BREAK, 0.9F, 0.75F);
        }
    }

    boolean isInDisplacementRecovery() {
        return this.displacementRecoveryTicks > 0;
    }

    private void emitDisplacementTelegraph(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        double targetHeight = target.getY() + target.getBbHeight() * 0.55D;
        double sourceHeight = this.getY() + this.getBbHeight() * 0.55D;
        for (int step = 1; step <= 4; step++) {
            double progress = step / 5.0D;
            double x = target.getX() + (this.getX() - target.getX()) * progress;
            double y = targetHeight + (sourceHeight - targetHeight) * progress;
            double z = target.getZ() + (this.getZ() - target.getZ()) * progress;
            serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 1, 0.03D, 0.03D, 0.03D, 0.0D);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericWalkIdleController(this),
                DefaultAnimations.genericAttackAnimation(this, DefaultAnimations.ATTACK_STRIKE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    /**
     * Recovery reserves movement/look/jump controls at priority zero, preventing inherited Spider
     * pursuit/leap/melee goals from immediately erasing the player's earned punish window.
     */
    private static final class DisplacementRecoveryGoal extends Goal {
        private final ChainbackEntity chainback;

        private DisplacementRecoveryGoal(ChainbackEntity chainback) {
            this.chainback = chainback;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.chainback.isInDisplacementRecovery();
        }

        @Override
        public boolean canContinueToUse() {
            return this.chainback.isInDisplacementRecovery();
        }

        @Override
        public void start() {
            this.chainback.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.chainback.getNavigation().stop();
            var motion = this.chainback.getDeltaMovement();
            this.chainback.setDeltaMovement(0.0D, motion.y, 0.0D);
        }
    }
}
