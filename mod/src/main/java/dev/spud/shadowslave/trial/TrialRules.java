package dev.spud.shadowslave.trial;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

/**
 * Server options for Nightmares, set like any game rule (decided with Jack on
 * 2026-10-29). A death inside a Nightmare always reverts you to the moment you
 * entered and bars you from that Seed; these rules add harsher costs on top.
 */
public final class TrialRules {
    /** A death leaves a Nightmare Scar: weakness for a while after you are thrown out. */
    public static final GameRules.Key<GameRules.BooleanValue> SCAR = GameRules.register(
            "shadowslaveNightmareScar", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
    /** True Death: a death also costs a rank. */
    public static final GameRules.Key<GameRules.BooleanValue> TRUE_DEATH = GameRules.register(
            "shadowslaveNightmareTrueDeath", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
    /** Hardcore: a death throws you all the way back to Dormant. */
    public static final GameRules.Key<GameRules.BooleanValue> HARDCORE = GameRules.register(
            "shadowslaveNightmareHardcore", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
    /**
     * The First Nightmare: with this on, Awakening also needs the First
     * Nightmare conquered, as in the novel (ch. 1-17). Once a Dormant core is
     * saturated, sleeping beside a lit Dream Candle makes the Seed inside the
     * carrier bloom into a solo Nightmare ({@link FirstNightmare}). Off until
     * Jack decides to switch it on, so Awakening stays full-core-only
     * (Jack, 2026-10-29 and 2026-10-31).
     */
    public static final GameRules.Key<GameRules.BooleanValue> FIRST_NIGHTMARE = GameRules.register(
            "shadowslaveFirstNightmare", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
    /**
     * Breach: a death in the First Nightmare lets one of its creatures out into
     * the waking world beside the fallen carrier (CANON, ch. 1 and 23). Off by default.
     */
    public static final GameRules.Key<GameRules.BooleanValue> FIRST_NIGHTMARE_BREACH = GameRules.register(
            "shadowslaveFirstNightmareBreach", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

    private TrialRules() {
    }

    /** Forces the game rule class to load during mod construction, before worlds read their rules. */
    public static void bootstrap() {
    }

    public static boolean scar(MinecraftServer server) {
        return server.getGameRules().getBoolean(SCAR);
    }

    public static boolean trueDeath(MinecraftServer server) {
        return server.getGameRules().getBoolean(TRUE_DEATH);
    }

    public static boolean hardcore(MinecraftServer server) {
        return server.getGameRules().getBoolean(HARDCORE);
    }

    public static boolean firstNightmare(MinecraftServer server) {
        return server.getGameRules().getBoolean(FIRST_NIGHTMARE);
    }

    public static boolean firstNightmareBreach(MinecraftServer server) {
        return server.getGameRules().getBoolean(FIRST_NIGHTMARE_BREACH);
    }
}
