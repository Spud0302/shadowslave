package dev.spud.shadowslave.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Physical body for the Java-owned {@code chainback} creature profile: a spider
 * with the Chainback model. Its chain-pull attack was part of the advanced
 * combat proof and was dropped with Combat Core, for a later Advanced Combat
 * update to bring back.
 */
public final class ChainbackEntity extends Spider implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public ChainbackEntity(EntityType<? extends ChainbackEntity> type, Level level) {
        super(type, level);
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
