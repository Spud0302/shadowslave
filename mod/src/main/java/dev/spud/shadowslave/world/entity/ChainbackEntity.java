package dev.spud.shadowslave.world.entity;

import dev.spud.combatcore.api.CombatActionDefinition;
import dev.spud.combatcore.api.CombatPhase;
import dev.spud.combatcore.api.MobActionExecutor;
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
 * <p>Creature identity, target eligibility, displacement, cooldown, and presentation remain Shadow
 * Slave-owned. Combat Core owns only the generic commitment and wind-up/active/recovery clock for
 * the displacement action.</p>
 */
public final class ChainbackEntity extends Spider implements GeoEntity {
    private static final CombatActionDefinition DISPLACEMENT_ACTION = new CombatActionDefinition(
            "shadowslave:chainback_displacement", 12, 1, 8, 4.0D);
    private static final double MAX_VERTICAL_DELTA = 1.5D;
    private static final int COOLDOWN_TICKS = 50;
    private static final int EVADED_EXTRA_PENALTY_TICKS = 10;
    private static final double HORIZONTAL_PULL = 0.55D;
    private static final double LIFT = 0.10D;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final MobActionExecutor<LivingEntity> displacementAction = new MobActionExecutor<>();
    private int displacementCooldown;
    private int evadedPenaltyTicks;

    public ChainbackEntity(EntityType<? extends ChainbackEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new DisplacementReservationGoal(this));
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

        long nowTick = this.level().getGameTime();
        if (this.displacementAction.actionReserved(nowTick)) {
            this.displacementAction.publishPhase(nowTick, this::presentDisplacementPhase);
            this.displacementAction.resolveActiveWindow(nowTick, (target, ignored) -> this.resolveDisplacement(target));
            return;
        }
        if (this.evadedPenaltyTicks > 0) {
            this.evadedPenaltyTicks--;
            return;
        }

        LivingEntity target = this.getTarget();
        if (!canPull(target, this.displacementCooldown)) {
            return;
        }
        if (this.displacementAction.start(DISPLACEMENT_ACTION, target, nowTick)) {
            this.displacementAction.publishPhase(nowTick, this::presentDisplacementPhase);
        }
    }

    private void resolveDisplacement(LivingEntity target) {
        this.displacementCooldown = COOLDOWN_TICKS;
        if (!canPull(target, 0)) {
            this.evadedPenaltyTicks = EVADED_EXTRA_PENALTY_TICKS;
            this.playSound(SoundEvents.CHAIN_BREAK, 0.9F, 0.75F);
            return;
        }

        double deltaX = this.getX() - target.getX();
        double deltaZ = this.getZ() - target.getZ();
        double lengthSquared = deltaX * deltaX + deltaZ * deltaZ;
        if (lengthSquared <= 1.0E-8D) {
            this.evadedPenaltyTicks = EVADED_EXTRA_PENALTY_TICKS;
            return;
        }
        double inverseLength = 1.0D / Math.sqrt(lengthSquared);
        target.push(
                deltaX * inverseLength * HORIZONTAL_PULL,
                LIFT,
                deltaZ * inverseLength * HORIZONTAL_PULL);
    }

    private boolean canPull(LivingEntity target, int cooldownTicksRemaining) {
        if (target == null || !target.isAlive() || !this.getSensing().hasLineOfSight(target)) {
            return false;
        }
        double deltaX = this.getX() - target.getX();
        double deltaZ = this.getZ() - target.getZ();
        double verticalDelta = this.getY() - target.getY();
        double horizontalDistanceSquared = deltaX * deltaX + deltaZ * deltaZ;
        return cooldownTicksRemaining <= 0
                && horizontalDistanceSquared > 0.0D
                && horizontalDistanceSquared <= DISPLACEMENT_ACTION.reach() * DISPLACEMENT_ACTION.reach()
                && Math.abs(verticalDelta) <= MAX_VERTICAL_DELTA;
    }

    private void presentDisplacementPhase(
            LivingEntity target,
            CombatActionDefinition ignored,
            CombatPhase phase) {
        if (phase == CombatPhase.WINDUP) {
            this.playSound(SoundEvents.CHAIN_PLACE, 0.8F, 0.7F);
            this.emitDisplacementTelegraph(target);
        } else if (phase == CombatPhase.ACTIVE) {
            this.emitDisplacementTelegraph(target);
        }
    }

    private void emitDisplacementTelegraph(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel) || target == null) {
            return;
        }
        double targetHeight = target.getY() + target.getBbHeight() * 0.55D;
        double sourceHeight = this.getY() + this.getBbHeight() * 0.55D;
        for (int step = 1; step <= 4; step++) {
            double progress = step / 5.0D;
            double x = target.getX() + (this.getX() - target.getX()) * progress;
            double y = targetHeight + (sourceHeight - targetHeight) * progress;
            double z = target.getZ() + (this.getZ() - target.getZ()) * progress;
            serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 1, 0.03D, 0.03D, 0.03D, 0.03D, 0.0D);
        }
    }

    private boolean hasDisplacementReservation() {
        long nowTick = this.level().getGameTime();
        return this.evadedPenaltyTicks > 0 || this.displacementAction.movementReserved(nowTick);
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

    private static final class DisplacementReservationGoal extends Goal {
        private final ChainbackEntity chainback;

        private DisplacementReservationGoal(ChainbackEntity chainback) {
            this.chainback = chainback;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.chainback.hasDisplacementReservation();
        }

        @Override
        public boolean canContinueToUse() {
            return this.chainback.hasDisplacementReservation();
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
