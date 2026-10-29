package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * The dimension Nightmares are played in: an empty void where each run's
 * structure is placed in its own slot, far from every other run, so a
 * cohort's Nightmare is theirs alone.
 */
public final class TrialWorld {
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "nightmare_trial"));
    /** Blocks between slot corners: wide enough that no run ever sees another. */
    static final int SLOT_SPACING = 4096;
    static final int SLOT_Y = 64;

    private TrialWorld() {
    }

    /**
     * The Nightmare dimension's key on this server. Game test servers bake only
     * the flat preset's dimensions, so there the End stands in (the Dream Realm
     * already borrows the Nether).
     */
    public static ResourceKey<Level> key(MinecraftServer server) {
        return server instanceof net.minecraft.gametest.framework.GameTestServer ? Level.END : LEVEL;
    }

    public static ServerLevel level(MinecraftServer server) {
        return server.getLevel(key(server));
    }

    public static boolean is(Level level) {
        return level.getServer() != null ? level.dimension().equals(key(level.getServer())) : level.dimension().equals(LEVEL);
    }

    /** The corner of a slot's structure. Slots run along the X axis, starting one slot out. */
    public static BlockPos slotOrigin(int slot) {
        return new BlockPos((slot + 1) * SLOT_SPACING, SLOT_Y, 0);
    }
}
