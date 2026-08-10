package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.nightmare.content.NightmareCreatureContentCatalog;
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
    public static final String CONTENT_ID = "chainback";

    public ChainbackEntity(EntityType<? extends ChainbackEntity> type, Level level) {
        super(type, level);
    }

    /** Returns the existing Java-owned content profile rather than duplicating identity here. */
    public static NightmareCreatureContentCatalog.CreatureProfile contentProfile() {
        return NightmareCreatureContentCatalog.waveOne().stream()
                .filter(profile -> profile.id().equals(CONTENT_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing Nightmare Creature profile: " + CONTENT_ID));
    }
}
