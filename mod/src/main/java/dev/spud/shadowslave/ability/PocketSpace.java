package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Threshold's signature rule: a pocket of space the player carries. It holds
 * the Hollow Pocket's nine slots (kept in the part of the player's data that
 * survives death), the creatures Banish has dropped out of the world, and the
 * Unmoored Flaw's toll: each door opened into elsewhere takes a point of
 * max health until the player next sleeps.
 */
public final class PocketSpace {
    public static final int SLOTS = 9;
    static final String POCKET_TAG = "shadowslave_pocket";
    static final String UNMOORED_TAG = "shadowslave_unmoored";
    /** Unmoored: one point of max health per door, six at most. */
    static final int MAX_TOLL = 6;
    static final ResourceLocation UNMOORED_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "threshold_unmoored");

    record Banished(ResourceKey<Level> dimension, Vec3 pos, CompoundTag entity, long returnsAt) {
    }

    private static final Map<UUID, Banished> BANISHED = new ConcurrentHashMap<>();

    private PocketSpace() {
    }

    private static CompoundTag persisted(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG)) {
            data.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return data.getCompound(Player.PERSISTED_NBT_TAG);
    }

    // ---- Hollow Pocket -------------------------------------------------------------------------------------------

    /** The pocket's contents, as a container that writes itself back whenever it changes or closes. */
    static SimpleContainer pocket(ServerPlayer player) {
        NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(persisted(player).getCompound(POCKET_TAG), items, player.registryAccess());
        SimpleContainer container = new SimpleContainer(SLOTS) {
            @Override
            public void setChanged() {
                super.setChanged();
                save(player, this);
            }

            @Override
            public void stopOpen(Player who) {
                super.stopOpen(who);
                save(player, this);
            }
        };
        for (int i = 0; i < SLOTS; i++) {
            container.setItem(i, items.get(i));
        }
        return container;
    }

    private static void save(ServerPlayer player, SimpleContainer container) {
        NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < SLOTS; i++) {
            items.set(i, container.getItem(i));
        }
        persisted(player).put(POCKET_TAG, ContainerHelper.saveAllItems(new CompoundTag(), items, player.registryAccess()));
    }

    // ---- Unmoored ------------------------------------------------------------------------------------------------

    /** A door was opened: an Unmoored player pays a point of max health, up to the cap. */
    static void doorOpened(ServerPlayer player) {
        if (!KitRules.hasFlaw(player, AspectKitCatalog.THRESHOLD.flawEffectId())) {
            return;
        }
        int toll = Math.min(MAX_TOLL, toll(player) + 1);
        persisted(player).putInt(UNMOORED_TAG, toll);
        applyToll(player, toll);
        player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(),
                12, 0.3, 0.5, 0.3, 0.02);
    }

    static int toll(ServerPlayer player) {
        return persisted(player).getInt(UNMOORED_TAG);
    }

    private static void applyToll(ServerPlayer player, int toll) {
        KitModifiers.keep(player, Attributes.MAX_HEALTH, UNMOORED_ID, -toll);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    /** Once a second: keep the toll applied, or lift it if the Flaw is gone. */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) {
            boolean unmoored = KitRules.hasFlaw(player, AspectKitCatalog.THRESHOLD.flawEffectId());
            applyToll(player, unmoored ? toll(player) : 0);
        }
    }

    /** A full night's sleep moors you again. */
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.isSleepingLongEnough() && toll(player) > 0) {
            persisted(player).putInt(UNMOORED_TAG, 0);
            applyToll(player, 0);
            player.displayClientMessage(Component.literal("Rested, you feel moored again.").withStyle(ChatFormatting.GRAY), true);
        }
    }

    /** Clears the toll, for tests. */
    static void moor(ServerPlayer player) {
        persisted(player).putInt(UNMOORED_TAG, 0);
        applyToll(player, 0);
    }

    // ---- Banish --------------------------------------------------------------------------------------------------

    /** Takes a creature out of the world for a while; it returns to the same spot. */
    static boolean banish(Mob mob, int ticks) {
        CompoundTag saved = new CompoundTag();
        if (!mob.saveAsPassenger(saved)) {
            return false;
        }
        ServerLevel level = (ServerLevel) mob.level();
        BANISHED.put(mob.getUUID(), new Banished(level.dimension(), mob.position(), saved, level.getGameTime() + ticks));
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                40, 0.3, mob.getBbHeight() * 0.4, 0.3, 0.05);
        mob.ejectPassengers();
        mob.stopRiding();
        mob.discard();
        return true;
    }

    static boolean banished(UUID id) {
        return BANISHED.containsKey(id);
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (BANISHED.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        for (Iterator<Map.Entry<UUID, Banished>> it = BANISHED.entrySet().iterator(); it.hasNext(); ) {
            Banished banished = it.next().getValue();
            ServerLevel level = server.getLevel(banished.dimension());
            if (level == null) {
                it.remove();
            } else if (banished.returnsAt() <= level.getGameTime()) {
                it.remove();
                bringBack(level, banished);
            }
        }
    }

    private static void bringBack(ServerLevel level, Banished banished) {
        level.getChunk(BlockPos.containing(banished.pos()));
        Entity entity = EntityType.loadEntityRecursive(banished.entity(), level, loaded -> {
            loaded.moveTo(banished.pos().x, banished.pos().y, banished.pos().z, loaded.getYRot(), loaded.getXRot());
            return loaded;
        });
        if (entity != null && level.addFreshEntity(entity)) {
            level.sendParticles(ParticleTypes.PORTAL, banished.pos().x, banished.pos().y + 1.0, banished.pos().z,
                    40, 0.3, 0.6, 0.3, 0.3);
        }
    }

    /** Brings every banished creature back at once, when the server stops. */
    public static void onServerStopping(ServerStoppingEvent event) {
        returnAll(event.getServer());
    }

    static void returnAll(MinecraftServer server) {
        for (Banished banished : BANISHED.values()) {
            ServerLevel level = server.getLevel(banished.dimension());
            if (level != null) {
                bringBack(level, banished);
            }
        }
        BANISHED.clear();
    }
}
