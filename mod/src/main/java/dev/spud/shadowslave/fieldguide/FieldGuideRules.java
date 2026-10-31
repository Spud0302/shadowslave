package dev.spud.shadowslave.fieldguide;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

/**
 * Server options for the Field Guide's sharing, set like any game rule
 * ({@code /gamerule shadowslaveFieldNotesWearOut true}). The harsher choices
 * are off by default.
 */
public final class FieldGuideRules {
    /** Whether established paths count everyone's walks on the server; off, each player wears their own roads. */
    public static final GameRules.Key<GameRules.BooleanValue> SHARED_PATHS = GameRules.register(
            "shadowslaveSharedPaths", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
    /** Whether Field Guide boards can be used to post and read notes. */
    public static final GameRules.Key<GameRules.BooleanValue> PUBLIC_BOARDS = GameRules.register(
            "shadowslavePublicBoards", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
    /** Whether Field Notes left by Sleepers who did not make it turn up in chests. */
    public static final GameRules.Key<GameRules.BooleanValue> LOST_NOTES = GameRules.register(
            "shadowslaveLostNotes", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
    /** Harsh: a Field Note crumbles once it has been read, so each copy teaches one reader. */
    public static final GameRules.Key<GameRules.BooleanValue> NOTES_WEAR_OUT = GameRules.register(
            "shadowslaveFieldNotesWearOut", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

    private FieldGuideRules() {
    }

    /** Forces the game rule class to load during mod construction, before worlds read their rules. */
    public static void bootstrap() {
    }

    public static boolean sharedPaths(MinecraftServer server) {
        return server.getGameRules().getBoolean(SHARED_PATHS);
    }

    public static boolean publicBoards(MinecraftServer server) {
        return server.getGameRules().getBoolean(PUBLIC_BOARDS);
    }

    public static boolean lostNotes(MinecraftServer server) {
        return server.getGameRules().getBoolean(LOST_NOTES);
    }

    public static boolean notesWearOut(MinecraftServer server) {
        return server.getGameRules().getBoolean(NOTES_WEAR_OUT);
    }
}
