package dev.spud.shadowslave.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.Level;

/**
 * Physical execution adapter for the Java-owned {@code drowned_listener} creature profile.
 *
 * <p>The stable content profile remains authoritative for creature identity. This entity
 * deliberately reuses vanilla Drowned water/ground locomotion, navigation and hostile AI as a
 * visible placeholder execution layer. It does not implement the authored sound/vibration sense
 * as a canonical detection formula, and its vanilla model/combat behavior is DESIGN.</p>
 */
public final class DrownedListenerEntity extends Drowned {
    public DrownedListenerEntity(EntityType<? extends DrownedListenerEntity> type, Level level) {
        super(type, level);
    }
}
