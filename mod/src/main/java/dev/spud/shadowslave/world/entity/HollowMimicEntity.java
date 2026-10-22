package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.creature.CreatureStats;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.creature.goal.AmbushLungeGoal;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.EnumSet;
import java.util.List;

/**
 * Hollow Mimic, an Awakened Demon (DESIGN): a gaunt thing with a hollow
 * face that hunts in the dark by imitating people. While it stands in
 * darkness and nobody is looking at it, it speaks to its prey in chat with
 * the name of another player it has heard, and the voice comes from where it
 * is waiting. It never breathes between words: its lines have no
 * punctuation at all, the one tell.
 *
 * <p>Counterplay: light (at block light {@link #BRIGHT_LIGHT} or more it is
 * weakened and silent), pairs (it will not close in on a player with a
 * companion within {@link #PAIR_RANGE} unless struck) and a verification
 * phrase between friends.</p>
 */
public final class HollowMimicEntity extends NightmareCreature {
    public static final CreatureStats STATS = new CreatureStats(40.0, 6.0, 4.0, 0.3);
    static final int BRIGHT_LIGHT = 10;
    static final int DARK_LIGHT = 7;
    static final double PAIR_RANGE = 8.0;
    static final List<String> LINES = List.of(
            "over here", "come look at this", "i found something", "wait for me", "help me",
            "are you there", "this way", "i think i am lost", "can you hear me", "behind the wall");

    static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    static final RawAnimation SPEAK = RawAnimation.begin().thenPlay("speak");
    static final RawAnimation CLAW = RawAnimation.begin().thenPlay("claw");

    private int struckTicks;

    public HollowMimicEntity(EntityType<? extends HollowMimicEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public CreatureRank nativeRank() {
        return CreatureRank.AWAKENED;
    }

    @Override
    public CreatureClass nativeClass() {
        return CreatureClass.DEMON;
    }

    @Override
    public CreatureStats nativeStats() {
        return STATS;
    }

    @Override
    protected void registerSignatureGoals() {
        this.goalSelector.addGoal(1, new KeepFromPairsGoal(this));
        this.goalSelector.addGoal(2, new MimicVoiceGoal(this));
    }

    int blockLight() {
        return level().getBrightness(LightLayer.BLOCK, blockPosition());
    }

    boolean inDarkness() {
        return blockLight() <= DARK_LIGHT && (level().getBrightness(LightLayer.SKY, blockPosition()) < 8 || !level().isDay());
    }

    /** Whether the prey has another player beside them. */
    static boolean isPaired(Player prey) {
        return !prey.level().getEntitiesOfClass(Player.class, prey.getBoundingBox().inflate(PAIR_RANGE),
                other -> other != prey && other.isAlive() && !other.isSpectator()).isEmpty();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            return;
        }
        if (struckTicks > 0) {
            struckTicks--;
        }
        if (tickCount % 20 == 0 && blockLight() >= BRIGHT_LIGHT) {
            addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 1, false, false));
            addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && source.getEntity() instanceof Player) {
            struckTicks = 200;
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            triggerAnim("action", "claw");
        }
        return hit;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.8F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "body", 4,
                state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)));
        controllers.add(new AnimationController<>(this, "action", 2, state -> PlayState.STOP)
                .triggerableAnim("speak", SPEAK)
                .triggerableAnim("claw", CLAW));
    }

    /** A player name to speak as: someone near the prey, else anyone online, else the prey themself. */
    static String borrowedName(ServerPlayer prey) {
        List<ServerPlayer> near = prey.serverLevel().getEntitiesOfClass(ServerPlayer.class,
                prey.getBoundingBox().inflate(48.0), other -> other != prey);
        if (!near.isEmpty()) {
            return near.get(prey.getRandom().nextInt(near.size())).getScoreboardName();
        }
        List<ServerPlayer> online = prey.server.getPlayerList().getPlayers().stream().filter(other -> other != prey).toList();
        return online.isEmpty() ? prey.getScoreboardName() : online.get(prey.getRandom().nextInt(online.size())).getScoreboardName();
    }

    /** Speaks to the prey from the dark, in a borrowed voice. */
    static final class MimicVoiceGoal extends Goal {
        static final int INTERVAL_TICKS = 160;
        static final double VOICE_RANGE = 32.0;
        private final HollowMimicEntity mimic;
        private long readyAt;

        MimicVoiceGoal(HollowMimicEntity mimic) {
            this.mimic = mimic;
            setFlags(EnumSet.noneOf(Flag.class));
        }

        private ServerPlayer prey() {
            if (mimic.getTarget() instanceof ServerPlayer player) {
                return player;
            }
            return mimic.level().getNearestPlayer(mimic, VOICE_RANGE) instanceof ServerPlayer player
                    && !player.isCreative() && !player.isSpectator() ? player : null;
        }

        @Override
        public boolean canUse() {
            if (mimic.level().getGameTime() < readyAt || !mimic.inDarkness()) {
                return false;
            }
            ServerPlayer prey = prey();
            return prey != null && mimic.distanceTo(prey) > 6.0 && !AmbushLungeGoal.isLookingAt(prey, mimic, 0.9);
        }

        @Override
        public void start() {
            readyAt = mimic.level().getGameTime() + INTERVAL_TICKS + mimic.getRandom().nextInt(80);
            ServerPlayer prey = prey();
            if (prey == null) {
                return;
            }
            String line = LINES.get(mimic.getRandom().nextInt(LINES.size()));
            prey.sendSystemMessage(Component.translatable("chat.type.text", borrowedName(prey), line));
            mimic.level().playSound(null, mimic.blockPosition(), SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.HOSTILE, 0.5F, 0.4F);
            mimic.triggerAnim("action", "speak");
            if (mimic.getTarget() == null) {
                mimic.setTarget(prey);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    /** Holds off a player who has company, circling at a distance, until struck. */
    static final class KeepFromPairsGoal extends Goal {
        static final double KEEP_AWAY = 10.0;
        private final HollowMimicEntity mimic;

        KeepFromPairsGoal(HollowMimicEntity mimic) {
            this.mimic = mimic;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = mimic.getTarget();
            return mimic.struckTicks <= 0 && target instanceof Player player && isPaired(player)
                    && mimic.distanceTo(player) < KEEP_AWAY;
        }

        @Override
        public void start() {
            LivingEntity target = mimic.getTarget();
            Vec3 away = target == null ? null : DefaultRandomPos.getPosAway(mimic, 14, 5, target.position());
            if (away != null) {
                mimic.getNavigation().moveTo(away.x, away.y, away.z, 1.0);
            }
        }

        @Override
        public void tick() {
            LivingEntity target = mimic.getTarget();
            if (target != null) {
                mimic.getLookControl().setLookAt(target, 20.0F, 20.0F);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() && !mimic.getNavigation().isDone();
        }
    }
}
