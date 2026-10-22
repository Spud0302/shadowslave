package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.creature.CreatureStats;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.creature.NightmareRules;
import dev.spud.shadowslave.creature.goal.SpawnLesserCopiesGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.EnumSet;

/**
 * Ashen Tyrant, an Awakened Tyrant and the first Gate Guardian (DESIGN,
 * inspired by the Mountain King, ch. 4): a hunched giant of cooled ash with
 * five embers in its chest, one for each core. It spawns small copies of
 * itself (the Tyrant package), drives the creatures around it, and slams the
 * ground to throw back everyone near it; the slam breaks soft blocks when
 * {@link NightmareRules#BLOCK_DAMAGE} is on.
 *
 * <p>Counterplay: the slam is telegraphed by a raised fist and a held breath
 * of {@link SlamGoal#WINDUP_TICKS} ticks; step out of its ring or jump.
 * Killing the lesser copies early keeps the swarm down.</p>
 */
public final class AshenTyrantEntity extends NightmareCreature {
    public static final CreatureStats STATS = new CreatureStats(220.0, 12.0, 10.0, 0.25);
    /** Entity event for the slam's shockwave (Lodestone ring and screen shake on the client). */
    public static final byte SLAM_EVENT = 70;
    /** Entity event for the lesser-spawn wind-up glow. */
    public static final byte BROOD_EVENT = 71;

    static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    static final RawAnimation SLAM = RawAnimation.begin().thenPlay("slam");
    static final RawAnimation SWIPE = RawAnimation.begin().thenPlay("swipe");
    static final RawAnimation BROOD = RawAnimation.begin().thenPlay("brood");

    private final ServerBossEvent bossBar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_10);

    public AshenTyrantEntity(EntityType<? extends AshenTyrantEntity> type, Level level) {
        super(type, level);
        this.bossBar.setDarkenScreen(true);
    }

    @Override
    public CreatureRank nativeRank() {
        return CreatureRank.AWAKENED;
    }

    @Override
    public CreatureClass nativeClass() {
        return CreatureClass.TYRANT;
    }

    @Override
    public CreatureStats nativeStats() {
        return STATS;
    }

    @Override
    protected void registerSignatureGoals() {
        this.goalSelector.addGoal(1, new SlamGoal(this));
    }

    /** Lesser copies are not Guardians: no boss bar, no slam. */
    boolean isLesser() {
        return SpawnLesserCopiesGoal.spawnerOf(this) != null || creatureClass().ordinal() < CreatureClass.TYRANT.ordinal();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossBar.setProgress(getHealth() / getMaxHealth());
        if (tickCount % 20 == 0) {
            bossBar.setName(getDisplayName().copy().append(" · ").append(rankAndClassName()));
            for (ServerPlayer player : ((ServerLevel) level()).players()) {
                boolean near = !isLesser() && player.distanceToSqr(this) < 48 * 48;
                if (near) {
                    bossBar.addPlayer(player);
                } else {
                    bossBar.removePlayer(player);
                }
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        bossBar.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    public void onSpawnLesserWindup() {
        triggerAnim("action", "brood");
        playSound(SoundEvents.SCULK_SHRIEKER_SHRIEK, 1.5F, 0.5F);
        level().broadcastEntityEvent(this, BROOD_EVENT);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == SLAM_EVENT || id == BROOD_EVENT) {
            dev.spud.shadowslave.client.CreatureEffects.play(this, id);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            triggerAnim("action", "swipe");
        }
        return hit;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return isLesser() ? 1.4F : 0.6F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.RAVAGER_STEP, isLesser() ? 0.3F : 1.0F, isLesser() ? 1.5F : 0.7F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "body", 5,
                state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)));
        controllers.add(new AnimationController<>(this, "action", 2, state -> PlayState.STOP)
                .triggerableAnim("slam", SLAM)
                .triggerableAnim("swipe", SWIPE)
                .triggerableAnim("brood", BROOD));
    }

    /** Raises a fist, then hammers the ground: damage and knock-up in a ring, soft blocks crack. */
    static final class SlamGoal extends Goal {
        static final int WINDUP_TICKS = 22;
        static final int COOLDOWN_TICKS = 140;
        static final double RADIUS = 5.0;

        private final AshenTyrantEntity tyrant;
        private int windup;
        private long readyAt;

        SlamGoal(AshenTyrantEntity tyrant) {
            this.tyrant = tyrant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = tyrant.getTarget();
            return !tyrant.isLesser() && target != null && tyrant.onGround()
                    && tyrant.level().getGameTime() >= readyAt && tyrant.distanceTo(target) < RADIUS;
        }

        @Override
        public void start() {
            windup = WINDUP_TICKS;
            tyrant.getNavigation().stop();
            tyrant.triggerAnim("action", "slam");
            tyrant.playSound(SoundEvents.WARDEN_ROAR, 1.6F, 0.8F);
        }

        @Override
        public void tick() {
            tyrant.getNavigation().stop();
            if (--windup > 0) {
                return;
            }
            readyAt = tyrant.level().getGameTime() + COOLDOWN_TICKS;
            ServerLevel level = (ServerLevel) tyrant.level();
            level.broadcastEntityEvent(tyrant, SLAM_EVENT);
            tyrant.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.5F, 0.6F);
            float damage = (float) tyrant.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8F;
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, tyrant.getBoundingBox().inflate(RADIUS, 2.0, RADIUS),
                    other -> other != tyrant && !(other instanceof NightmareCreature) && other.onGround())) {
                double distance = victim.distanceTo(tyrant);
                if (distance > RADIUS) {
                    continue;
                }
                victim.hurt(tyrant.damageSources().mobAttack(tyrant), damage * (float) (1.0 - distance / (RADIUS * 1.5)));
                Vec3 away = victim.position().subtract(tyrant.position()).normalize();
                victim.push(away.x * 0.9, 0.7, away.z * 0.9);
                victim.hurtMarked = true;
            }
            BlockPos center = tyrant.blockPosition().below();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, 0, -3), center.offset(3, 0, 3))) {
                if (pos.distSqr(center) > 9.5) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 1.1,
                        pos.getZ() + 0.5, 4, 0.3, 0.1, 0.3, 0.2);
                if (NightmareRules.blockDamage(level) && !pos.equals(center) && tyrant.getRandom().nextFloat() < 0.35F
                        && (state.is(BlockTags.MINEABLE_WITH_SHOVEL) || state.is(BlockTags.LEAVES))
                        && state.getDestroySpeed(level, pos) >= 0 && state.getDestroySpeed(level, pos) < 1.0F) {
                    level.destroyBlock(pos.immutable(), true, tyrant);
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return windup > 0;
        }
    }
}
