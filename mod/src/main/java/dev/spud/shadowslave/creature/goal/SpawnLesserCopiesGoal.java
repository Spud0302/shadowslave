package dev.spud.shadowslave.creature.goal;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Tyrant package (CANON: only Tyrants and above create lesser versions of
 * themselves, ch. 4): while fighting it spawns small copies one Rank lower,
 * as Beasts, a few at a time.
 */
public final class SpawnLesserCopiesGoal extends Goal {
    public static final String SPAWNED_BY = "shadowslave_spawned_by";
    static final int INTERVAL_TICKS = 240;
    static final int PER_WAVE = 2;
    static final int MAX_ALIVE = 4;
    static final double LESSER_SCALE = 0.45;

    private final NightmareCreature creature;
    private long readyAt;
    private int windup;

    public SpawnLesserCopiesGoal(NightmareCreature creature) {
        this.creature = creature;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = creature.getTarget();
        if (creature.creatureClass().ordinal() < CreatureClass.TYRANT.ordinal() || target == null
                || creature.level().getGameTime() < readyAt) {
            return false;
        }
        readyAt = creature.level().getGameTime() + INTERVAL_TICKS;
        return aliveCopies() < MAX_ALIVE;
    }

    private int aliveCopies() {
        UUID self = creature.getUUID();
        return creature.level().getEntitiesOfClass(NightmareCreature.class, creature.getBoundingBox().inflate(48.0),
                other -> other.isAlive() && self.equals(spawnerOf(other))).size();
    }

    public static UUID spawnerOf(NightmareCreature creature) {
        var data = creature.getPersistentData();
        return data.hasUUID(SPAWNED_BY) ? data.getUUID(SPAWNED_BY) : null;
    }

    @Override
    public void start() {
        windup = 30;
        creature.getNavigation().stop();
        creature.onSpawnLesserWindup();
    }

    @Override
    public void tick() {
        creature.getNavigation().stop();
        if (--windup == 0 && creature.level() instanceof ServerLevel level) {
            for (int i = 0; i < PER_WAVE; i++) {
                spawnCopy(level);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void spawnCopy(ServerLevel level) {
        NightmareCreature copy = (NightmareCreature) ((EntityType<? extends NightmareCreature>) creature.getType()).create(level);
        if (copy == null) {
            return;
        }
        double angle = creature.getRandom().nextDouble() * Math.PI * 2;
        BlockPos at = BlockPos.containing(creature.getX() + Math.cos(angle) * 2.5, creature.getY(), creature.getZ() + Math.sin(angle) * 2.5);
        copy.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, creature.getYRot(), 0);
        copy.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.MOB_SUMMONED, null);
        copy.setRankAndClass(creature.rank().lower(), CreatureClass.BEAST);
        copy.setBodyScale(LESSER_SCALE);
        copy.getPersistentData().putUUID(SPAWNED_BY, creature.getUUID());
        copy.setTarget(creature.getTarget());
        level.addFreshEntity(copy);
    }

    @Override
    public boolean canContinueToUse() {
        return windup > 0;
    }
}
