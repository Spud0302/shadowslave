package dev.spud.shadowslave.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;

/**
 * Placeholder Minecraft executor for the Java-owned Ash Burrower Nightmare Creature.
 * Vanilla Silverfish movement/hostile AI is removable execution, not canonical creature state.
 */
public final class AshBurrowerEntity extends Silverfish {
    public AshBurrowerEntity(EntityType<? extends Silverfish> type, Level level) {
        super(type, level);
    }
}
