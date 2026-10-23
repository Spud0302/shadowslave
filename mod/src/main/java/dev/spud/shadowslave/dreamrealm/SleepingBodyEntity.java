package dev.spud.shadowslave.dreamrealm;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The body an Awakened dreamer leaves asleep in their bed while their soul
 * walks the Dream Realm (DESIGN, Jack 2026-10-23). Hostile creatures nearby
 * are drawn to it; if it is hurt the dreamer is pulled back awake, and if it
 * dies, so do they. It has no will of its own: {@link DreamCrossing} decides
 * what its wounds mean.
 */
public final class SleepingBodyEntity extends LivingEntity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(SleepingBodyEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> OWNER_NAME =
            SynchedEntityData.defineId(SleepingBodyEntity.class, EntityDataSerializers.STRING);
    /** How far hostile creatures notice a sleeping body. */
    static final double LURE_RANGE = 16.0;
    private static final EntityDimensions LYING = EntityDimensions.fixed(0.9F, 0.6F);

    public SleepingBodyEntity(EntityType<? extends SleepingBodyEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    /** Lays a new body for {@code owner} in the bed at {@code bed}, as hurt as they are. */
    static SleepingBodyEntity layDown(ServerLevel level, ServerPlayer owner, BlockPos bed) {
        SleepingBodyEntity body = DreamRealmRegistry.SLEEPING_BODY.get().create(level);
        if (body == null) {
            throw new IllegalStateException("Could not create a sleeping body");
        }
        body.entityData.set(OWNER, Optional.of(owner.getUUID()));
        body.entityData.set(OWNER_NAME, owner.getGameProfile().getName());
        body.getAttribute(Attributes.MAX_HEALTH).setBaseValue(owner.getMaxHealth());
        body.setHealth(owner.getHealth());
        body.setYRot(owner.getYRot());
        body.moveTo(bed.getX() + 0.5, bed.getY() + 0.6875, bed.getZ() + 0.5, owner.getYRot(), 0.0F);
        body.startSleeping(bed);
        level.addFreshEntity(body);
        return body;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, Optional.empty());
        builder.define(OWNER_NAME, "");
    }

    public Optional<UUID> owner() {
        return entityData.get(OWNER);
    }

    public String ownerName() {
        return entityData.get(OWNER_NAME);
    }

    @Override
    public Component getName() {
        return ownerName().isEmpty() ? super.getName()
                : Component.translatable("entity.shadowslave.sleeping_body.of", ownerName());
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        if (tickCount % 20 == 0 && !DreamCrossing.isBodyOf(this, level.getServer())) {
            // The dreamer woke some other way, or the server restarted: the body has no one to wait for.
            discard();
            return;
        }
        if (tickCount % 40 == 0) {
            lure(level);
        }
    }

    /** Hostile creatures with nothing better to do come for the sleeper. */
    private void lure(ServerLevel level) {
        for (Mob mob : level.getEntitiesOfClass(Mob.class, getBoundingBox().inflate(LURE_RANGE),
                mob -> mob instanceof Enemy && mob.isAlive() && mob.getTarget() == null && !mob.isNoAi())) {
            mob.setTarget(this);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isInvulnerableTo(source)) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && isAlive() && level() instanceof ServerLevel level) {
            DreamCrossing.bodyHurt(this, level.getServer());
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level) {
            DreamCrossing.bodyDied(this, source, level.getServer());
        }
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    protected net.minecraft.world.phys.AABB makeBoundingBox() {
        // Lying in bed, but big enough to be struck: the vanilla sleeping box is a 0.2 block speck.
        return hasPose(Pose.SLEEPING) ? LYING.makeBoundingBox(position()) : super.makeBoundingBox();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        owner().ifPresent(owner -> tag.putUUID("owner", owner));
        tag.putString("owner_name", ownerName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(OWNER, tag.hasUUID("owner") ? Optional.of(tag.getUUID("owner")) : Optional.empty());
        entityData.set(OWNER_NAME, tag.getString("owner_name"));
    }
}
