package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.ability.ForewarningService;
import dev.spud.shadowslave.block.ModBlocks;
import dev.spud.shadowslave.creature.BlackBriarBlock;
import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.creature.CreatureStats;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.creature.goal.SpawnLesserCopiesGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;

/**
 * Thorn Matron, a Fallen Devil (DESIGN, the lore doc's first roster: "grows
 * briar walls; fire and severing vines counter it"), mother of the black
 * briars of the Thornwake Basin. Her one power (the Devil package) is the
 * Briar Snare: after a held breath of {@link #SNARE_WINDUP} ticks, while red
 * motes ring the ground around her prey, a ring of black briar two blocks
 * high bursts up around them. Her lash tears and slows.
 *
 * <p>Counterplay: step out of the ring of motes before it closes; cut the
 * briars with a blade or an axe, or burn them; fire hurts her twice over.
 * As the Tyrant of a Nightmare she also sends out small copies of herself.</p>
 */
public final class ThornMatronEntity extends NightmareCreature {
    public static final CreatureStats STATS = new CreatureStats(130.0, 9.0, 8.0, 0.24);
    static final int SNARE_WINDUP = 24;
    static final int SNARE_RADIUS = 2;
    private static final DustParticleOptions MOTE = new DustParticleOptions(new Vector3f(0.85F, 0.12F, 0.2F), 1.2F);

    static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    static final RawAnimation LASH = RawAnimation.begin().thenPlay("lash");
    static final RawAnimation GROW = RawAnimation.begin().thenPlay("grow");

    private LivingEntity snared;
    private BlockPos snareAt;
    private int snareTicks;

    public ThornMatronEntity(EntityType<? extends ThornMatronEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public CreatureRank nativeRank() {
        return CreatureRank.FALLEN;
    }

    @Override
    public CreatureClass nativeClass() {
        return CreatureClass.DEVIL;
    }

    @Override
    public CreatureStats nativeStats() {
        return STATS;
    }

    @Override
    protected void registerSignatureGoals() {
    }

    boolean isLesser() {
        return SpawnLesserCopiesGoal.spawnerOf(this) != null;
    }

    /** The Briar Snare: marks the ground around the prey; the ring grows when the wind-up ends. */
    @Override
    public boolean useSignaturePower(LivingEntity target) {
        if (isLesser() || snareTicks > 0 || distanceToSqr(target) > 20 * 20 || !hasLineOfSight(target)) {
            return false;
        }
        snared = target;
        snareAt = target.blockPosition();
        snareTicks = SNARE_WINDUP;
        getNavigation().stop();
        triggerAnim("action", "grow");
        playSound(SoundEvents.SWEET_BERRY_BUSH_PLACE, 1.6F, 0.5F);
        ForewarningService.bigAttack(this, SNARE_WINDUP);
        return true;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (snareTicks <= 0 || !(level() instanceof ServerLevel level)) {
            return;
        }
        snareTicks--;
        if (snareTicks % 4 == 0) {
            for (BlockPos pos : ring(snareAt)) {
                level.sendParticles(MOTE, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 1, 0.2, 0.0, 0.2, 0.0);
            }
        }
        if (snareTicks == 0) {
            growRing(level, snareAt);
            snared = null;
        }
    }

    /** Grows the ring of briar around a spot, two high, wherever there is room; returns how many blocks grew. */
    public int growRing(ServerLevel level, BlockPos centre) {
        BlockState briar = ModBlocks.BLACK_BRIAR.get().defaultBlockState().setValue(BlackBriarBlock.WITHERING, true);
        int grown = 0;
        for (BlockPos base : ring(centre)) {
            for (int dy = 0; dy < 2; dy++) {
                BlockPos pos = base.above(dy);
                if (level.getBlockState(pos).canBeReplaced()
                        && level.getEntitiesOfClass(LivingEntity.class, new AABB(pos)).isEmpty()) {
                    level.setBlockAndUpdate(pos, briar);
                    grown++;
                }
            }
        }
        if (grown > 0) {
            level.playSound(null, centre, SoundEvents.SWEET_BERRY_BUSH_PLACE, net.minecraft.sounds.SoundSource.HOSTILE, 2.0F, 0.6F);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, centre.getX() + 0.5, centre.getY() + 1, centre.getZ() + 0.5,
                    30, SNARE_RADIUS, 1.0, SNARE_RADIUS, 0.0);
        }
        return grown;
    }

    /** The ground ring at the snare's radius around a spot. */
    static List<BlockPos> ring(BlockPos centre) {
        List<BlockPos> ring = new ArrayList<>();
        for (int dx = -SNARE_RADIUS; dx <= SNARE_RADIUS; dx++) {
            for (int dz = -SNARE_RADIUS; dz <= SNARE_RADIUS; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == SNARE_RADIUS) {
                    ring.add(centre.offset(dx, 0, dz));
                }
            }
        }
        return ring;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Fire was always the briars' end, and hers.
        return super.hurt(source, source.is(DamageTypeTags.IS_FIRE) ? amount * 2.0F : amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            triggerAnim("action", "lash");
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this);
            }
        }
        return hit;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITCH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOOD_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SWEET_BERRY_BUSH_BREAK;
    }

    @Override
    public float getVoicePitch() {
        return isLesser() ? 1.5F : 0.7F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "body", 5,
                state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)));
        controllers.add(new AnimationController<>(this, "action", 2, state -> PlayState.STOP)
                .triggerableAnim("lash", LASH)
                .triggerableAnim("grow", GROW));
    }
}
