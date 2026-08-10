package dev.spud.shadowslave.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

/**
 * Physical execution adapter for the Java-owned {@code chainback} creature profile.
 *
 * <p>The stable content profile remains authoritative for creature identity. This entity
 * deliberately reuses vanilla Spider movement, climbing and hostile-mob AI as a visible
 * placeholder execution layer; those Minecraft behaviors are DESIGN, not canon rules.</p>
 */
public final class ChainbackEntity extends Spider {
    public ChainbackEntity(EntityType<? extends ChainbackEntity> type, Level level) {
        super(type, level);
    }
}
