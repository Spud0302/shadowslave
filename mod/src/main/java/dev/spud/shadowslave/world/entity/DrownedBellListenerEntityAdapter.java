package dev.spud.shadowslave.world.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * NeoForge execution adapter that upgrades the Drowned Bell's tagged vanilla placeholder
 * into the registered Java-bound Drowned Listener entity without changing scenario authority.
 */
public final class DrownedBellListenerEntityAdapter {
    private DrownedBellListenerEntityAdapter() {
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Entity entity = event.getEntity();
        String entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        if (!DrownedBellListenerExecutionBinding.shouldReplace(entityTypeId, entity.getTags())) {
            return;
        }

        level.getServer().execute(() -> replace(level, entity));
    }

    private static void replace(ServerLevel level, Entity placeholder) {
        if (placeholder.isRemoved()) {
            return;
        }

        DrownedListenerEntity listener = NightmareCreatureEntities.DROWNED_LISTENER.get().create(level);
        if (listener == null) {
            return;
        }

        listener.setUUID(placeholder.getUUID());
        listener.moveTo(placeholder.getX(), placeholder.getY(), placeholder.getZ(), placeholder.getYRot(), placeholder.getXRot());
        listener.setDeltaMovement(placeholder.getDeltaMovement());
        listener.setPersistenceRequired();
        // DrownedBellScenario seeds the vanilla placeholder with its player target so the legacy
        // executor attacks immediately. Carrying that target into the Listener would bypass the
        // VIBRATION / dry-ground / crouch acquisition contract because it is not vibration-owned.
        // Start the replacement untargeted; vibration sensing or later retaliation may claim a
        // target through their own explicit authority.

        placeholder.discard();
        level.addFreshEntity(listener);
    }
}
