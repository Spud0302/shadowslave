package dev.spud.shadowslave.world.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Physical execution adapter for the Java-owned {@code drowned_listener} creature profile.
 *
 * <p>The stable content profile remains authoritative for creature identity. Vanilla Drowned
 * water/ground locomotion, navigation and hostile AI remain temporary execution behavior, while
 * GeckoLib owns only the replaceable model/animation presentation lane. The authored
 * sound/vibration senses are not inferred from this renderer.</p>
 */
public final class DrownedListenerEntity extends Drowned implements GeoEntity {
    private static final RawAnimation SWIM_ANIMATION = RawAnimation.begin().thenLoop("move.swim");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public DrownedListenerEntity(EntityType<? extends DrownedListenerEntity> type, Level level) {
        super(type, level);
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
}
