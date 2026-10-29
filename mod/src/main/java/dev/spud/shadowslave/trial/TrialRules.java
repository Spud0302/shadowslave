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
     * The First Nightmare: with this on, Awakening also needs a cleared
     * Category 1 Nightmare, as in the novel (ch. 1-17). Off for now, so
     * Awakening stays full-core-only while the rest is tested (Jack, 2026-10-29).
     */
    public static final GameRules.Key<GameRules.BooleanValue> FIRST_NIGHTMARE = GameRules.register(
            "shadowslaveFirstNightmare", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

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
}
