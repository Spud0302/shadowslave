package dev.spud.shadowslave.gate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The visible tear of a Nightmare Gate. Presentation only: it shows the
 * Gate's phase (a thin forming sliver during the warning, a full rift while
 * open, a closing seam at the end) and carries no state of its own; the
 * Gate lives in {@link GateData}.
 */
public final class GateRiftEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Byte> PHASE = SynchedEntityData.defineId(GateRiftEntity.class, EntityDataSerializers.BYTE);
    static final RawAnimation FORMING = RawAnimation.begin().thenLoop("forming");
    static final RawAnimation OPEN = RawAnimation.begin().thenPlay("opening").thenLoop("open");
    static final RawAnimation CLOSING = RawAnimation.begin().thenPlayAndHold("closing");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int closingTicks;

    public GateRiftEntity(EntityType<? extends GateRiftEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PHASE, (byte) GateEvent.Phase.WARNING.ordinal());
    }

    public GateEvent.Phase phase() {
        return GateEvent.Phase.values()[this.entityData.get(PHASE)];
    }

    void setPhase(GateEvent.Phase phase) {
        this.entityData.set(PHASE, (byte) phase.ordinal());
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            dev.spud.shadowslave.client.CreatureEffects.riftTick(this, phase());
        } else if (phase() == GateEvent.Phase.CLOSED && ++closingTicks > 40) {
            discard();
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 160 * 160;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setPhase(GateEvent.Phase.byName(tag.getString("phase")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("phase", phase().getSerializedName());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rift", 6, state -> switch (phase()) {
            case WARNING -> state.setAndContinue(FORMING);
            case OPEN, GUARDIAN -> state.setAndContinue(OPEN);
            case CLOSED -> state.setAndContinue(CLOSING);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
