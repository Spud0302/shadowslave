package dev.spud.shadowslave.creature;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

/**
 * Server options for Nightmare Creatures and Gates, set like any game rule
 * ({@code /gamerule shadowslaveNightmareBlockDamage false}).
 */
public final class NightmareRules {
    /**
     * Whether Gates and the creatures from them may break blocks, the way
     * {@code mobGriefing} works for vanilla mobs (Jack, 2026-10-22). On by
     * default, like mob griefing.
     */
    public static final GameRules.Key<GameRules.BooleanValue> BLOCK_DAMAGE = GameRules.register(
            "shadowslaveNightmareBlockDamage", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    private NightmareRules() {
    }

    /** Forces the game rule class to load during mod construction, before worlds read their rules. */
    public static void bootstrap() {
    }

    /** Inside the Dream Realm its own option, {@code shadowslaveDreamBlockDamage}, decides instead. */
    public static boolean blockDamage(Level level) {
        if (dev.spud.shadowslave.dreamrealm.DreamRealm.is(level)) {
            return dev.spud.shadowslave.dreamrealm.DreamRealmRules.blockDamage(level);
        }
        return level.getGameRules().getBoolean(BLOCK_DAMAGE);
    }
}
