package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * The Dream Realm dimension and how its coordinates line up with the waking
 * world (DESIGN, Jack 2026-10-23): like the Nether, one Dream Realm block
 * stands for {@code ratio} Overworld blocks, so a Seed that blooms opens its
 * Gate at the matching Overworld spot. The ratio is a server option,
 * {@link DreamRealmRules#RATIO}.
 */
public final class DreamRealm {
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "dream_realm"));

    private DreamRealm() {
    }

    /**
     * The Dream Realm's key on this server. Game test servers bake only the
     * flat preset's dimensions, never a mod's, so there the Nether stands in.
     */
    public static ResourceKey<Level> key(MinecraftServer server) {
        return server instanceof net.minecraft.gametest.framework.GameTestServer ? Level.NETHER : LEVEL;
    }

    /** The Dream Realm level, or null if the dimension is missing. */
    public static ServerLevel level(MinecraftServer server) {
        return server.getLevel(key(server));
    }

    public static boolean is(Level level) {
        return level.getServer() != null ? level.dimension().equals(key(level.getServer())) : level.dimension().equals(LEVEL);
    }

    /** The Overworld column a Dream Realm block stands for. Y is left for the caller to find. */
    public static BlockPos toOverworld(BlockPos realm, int ratio) {
        return new BlockPos(realm.getX() * ratio, realm.getY(), realm.getZ() * ratio);
    }

    /** The Dream Realm column over an Overworld block, rounding towards negative infinity like the Nether. */
    public static BlockPos toRealm(BlockPos overworld, int ratio) {
        return new BlockPos(Math.floorDiv(overworld.getX(), ratio), overworld.getY(), Math.floorDiv(overworld.getZ(), ratio));
    }

    /** Horizontal distance from the Dream Realm's centre, which decides the region. */
    public static double distanceFromCentre(BlockPos realm) {
        return Math.sqrt((double) realm.getX() * realm.getX() + (double) realm.getZ() * realm.getZ());
    }
}
