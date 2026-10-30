package dev.spud.shadowslave.trial;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

/**
 * A challenger who logged out inside a Long Nightmare and is still in it
 * (DESIGN, Jack 2026-10-29: later cohorts meet earlier challengers who are
 * still alive in there, as the novel's current arc shows a Saint long alive
 * inside a Nightmare). It wears their face and their role's name, holds what
 * they held, and fights off what comes for it. If it is slain, its challenger
 * has fallen in the Nightmare and learns so when they return; when they
 * return it is gone and they stand where it stood.
 *
 * <p>It has no will beyond that: {@link TrialService} decides what its death
 * means.</p>
 */
public final class LingeringChallenger extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(LingeringChallenger.class, EntityDataSerializers.OPTIONAL_UUID);
    private UUID run;

    public LingeringChallenger(EntityType<? extends LingeringChallenger> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        setDropChance(EquipmentSlot.OFFHAND, 0.0F);
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    /** Leaves a figure where {@code player} stood, as hurt as they were, wearing their role's name. */
    static LingeringChallenger leave(ServerLevel level, ServerPlayer player, TrialInstance instance, TrialInstance.Member member,
                                     float healthShare) {
        LingeringChallenger figure = TrialRegistry.LINGERING_CHALLENGER.get().create(level);
        if (figure == null) {
            throw new IllegalStateException("Could not create a lingering challenger");
        }
        figure.entityData.set(OWNER, Optional.of(player.getUUID()));
        figure.run = instance.id;
        figure.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(1.0, player.getMaxHealth() * healthShare));
        figure.setHealth(Math.max(1.0F, Math.min(figure.getMaxHealth(), player.getHealth() * healthShare)));
        figure.setItemSlot(EquipmentSlot.MAINHAND, player.getMainHandItem().copy());
        figure.setItemSlot(EquipmentSlot.OFFHAND, player.getOffhandItem().copy());
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            figure.setItemSlot(slot, player.getItemBySlot(slot).copy());
            figure.setDropChance(slot, 0.0F);
        }
        figure.setCustomName(Component.literal("[" + member.role().name() + "]"));
        figure.setCustomNameVisible(true);
        figure.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        figure.setYHeadRot(player.getYHeadRot());
        level.addFreshEntity(figure);
        return figure;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, Optional.empty());
    }

    public Optional<UUID> owner() {
        return entityData.get(OWNER);
    }

    public Optional<UUID> run() {
        return Optional.ofNullable(run);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, dev.spud.shadowslave.creature.NightmareCreature.class,
                true));
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount % 20 == 0 && level() instanceof ServerLevel level && !TrialService.lingers(this, level.getServer())) {
            // Its challenger came back, or the Nightmare is gone: no one is left for it to hold a place for.
            discard();
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level) {
            TrialService.lingererDied(this, level.getServer());
        }
    }

    @Override
    public boolean canBeLeashed() {
        return false;
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
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof Player) && super.canAttack(target);
    }

    @Override
    public ItemStack getPickedResult(net.minecraft.world.phys.HitResult target) {
        return ItemStack.EMPTY;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        owner().ifPresent(owner -> tag.putUUID("owner", owner));
        if (run != null) {
            tag.putUUID("run", run);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(OWNER, tag.hasUUID("owner") ? Optional.of(tag.getUUID("owner")) : Optional.empty());
        run = tag.hasUUID("run") ? tag.getUUID("run") : null;
    }
}
