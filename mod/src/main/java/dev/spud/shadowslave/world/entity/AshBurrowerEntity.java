package dev.spud.shadowslave.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Placeholder Minecraft executor for the Java-owned Ash Burrower Nightmare Creature.
 * Vanilla-sized movement/hostile AI is removable execution, not canonical creature state.
 */
public final class AshBurrowerEntity extends Silverfish {
    public AshBurrowerEntity(EntityType<? extends Silverfish> type, Level level) {
        super(type, level);
        // Empty loot is not enough: vanilla Silverfish still award XP independently of loot tables.
        // This bounded encounter must not create any gameplay reward authority.
        this.xpReward = 0;
    }

    @Override
    protected void registerGoals() {
        // Deliberately do not call Silverfish.registerGoals(): it installs merge-with-stone and
        // wake-friends goals that can replace blocks, discard this executor, and later spawn a
        // vanilla Silverfish. Keep only generic removable movement/combat execution here.
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
}
