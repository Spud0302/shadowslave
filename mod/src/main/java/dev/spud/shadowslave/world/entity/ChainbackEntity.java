package dev.spud.shadowslave.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Physical execution adapter for the Java-owned {@code chainback} creature profile.
 *
 * <p>Creature identity remains authoritative Java content. Vanilla Spider climbing/pathfinding and
 * hostile AI are still temporary execution behavior, GeckoLib owns only replaceable presentation,
 * and the bounded pull below executes the profile's already-authored DISPLACEMENT pressure.</p>
 */
public final class ChainbackEntity extends Spider implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int displacementCooldown;

    public ChainbackEntity(EntityType<? extends ChainbackEntity> type, Level level) {
        super(type, level);
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

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || !this.getSensing().hasLineOfSight(target)) {
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
            return;
        }

        ChainbackDisplacementBehavior.PullVector pull = ChainbackDisplacementBehavior.pullToward(deltaX, deltaZ);
        if (pull.isZero()) {
            return;
        }
        target.push(pull.x(), pull.y(), pull.z());
        this.displacementCooldown = ChainbackDisplacementBehavior.COOLDOWN_TICKS;
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
}
