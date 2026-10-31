package dev.spud.shadowslave.dreamrealm;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.server.MinecraftServer;

/**
 * Server options for the Dream Realm, set like any game rule
 * ({@code /gamerule shadowslaveDreamSeeds false}). Agreed with Jack on
 * 2026-10-23: natural Seeds on, off or opt-in; Seed growth speed; block
 * damage inside the Dream Realm; whether a sleeping body stays behind; and
 * the coordinate ratio. Added 2026-10-31: whether a slain body leaves its
 * dreamer Lost, and the wait before a dreamer can wake.
 */
public final class DreamRealmRules {
    /** Whether Nightmare Seeds grow on their own near Awakened players in the Dream Realm. */
    public static final GameRules.Key<GameRules.BooleanValue> SEEDS = GameRules.register(
            "shadowslaveDreamSeeds", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
    /** With this on, Seeds only grow near players who chose them with {@code /shadowslave dream seeds on}. */
    public static final GameRules.Key<GameRules.BooleanValue> SEEDS_OPT_IN = GameRules.register(
            "shadowslaveDreamSeedsOptIn", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(false));
    /** Seed growth speed in percent: 200 grows Seeds twice as fast, 50 half as fast. */
    public static final GameRules.Key<GameRules.IntegerValue> SEED_GROWTH = GameRules.register(
            "shadowslaveDreamSeedGrowth", GameRules.Category.SPAWNING, GameRules.IntegerValue.create(100));
    /** Whether Gates and creatures may break blocks inside the Dream Realm. */
    public static final GameRules.Key<GameRules.BooleanValue> BLOCK_DAMAGE = GameRules.register(
            "shadowslaveDreamBlockDamage", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));
    /** Whether an Awakened dreamer's body stays asleep in the waking world, where it can be hurt. */
    public static final GameRules.Key<GameRules.BooleanValue> SLEEPING_BODY = GameRules.register(
            "shadowslaveSleepingBody", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
    /**
     * Whether a dreamer whose sleeping body is killed becomes Lost, stranded in
     * the Dream Realm until they rise to Ascended and their body is remade
     * (CANON, ch. 360, 435 and 1230; Jack, 2026-10-31). Off, the old rule
     * holds: the dreamer dies where they stand in the Dream Realm.
     */
    public static final GameRules.Key<GameRules.BooleanValue> LOST = GameRules.register(
            "shadowslaveLostDreamers", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
    /**
     * Seconds a dreamer must spend in the Dream Realm after arriving before a
     * lit Dream Candle or their clan's Gateway lets them wake (CANON: a Gateway
     * makes the Awakened wait about ten hours, ch. 398; Jack, 2026-10-31).
     * A hurt body still pulls them out at once.
     */
    public static final GameRules.Key<GameRules.IntegerValue> WAKE_WAIT = GameRules.register(
            "shadowslaveDreamWakeWait", GameRules.Category.PLAYER, GameRules.IntegerValue.create(300));
    /** Whether Nightmare Creatures roam the Dream Realm's regions that list them, around dreamers. */
    public static final GameRules.Key<GameRules.BooleanValue> CREATURES = GameRules.register(
            "shadowslaveDreamCreatures", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
    /** How many Overworld blocks one Dream Realm block stands for, like the Nether's 8. */
    public static final GameRules.Key<GameRules.IntegerValue> RATIO = GameRules.register(
            "shadowslaveDreamRatio", GameRules.Category.MISC, GameRules.IntegerValue.create(8));

    static final int MAX_RATIO = 64;

    private DreamRealmRules() {
    }

    /** Forces the game rule class to load during mod construction, before worlds read their rules. */
    public static void bootstrap() {
    }

    /** Rules are read from the Overworld, so every dimension agrees. */
    private static GameRules rules(MinecraftServer server) {
        return server.getGameRules();
    }

    public static boolean seeds(MinecraftServer server) {
        return rules(server).getBoolean(SEEDS);
    }

    public static boolean creatures(MinecraftServer server) {
        return rules(server).getBoolean(CREATURES);
    }

    public static boolean seedsOptIn(MinecraftServer server) {
        return rules(server).getBoolean(SEEDS_OPT_IN);
    }

    public static int seedGrowthPercent(MinecraftServer server) {
        return Math.max(0, rules(server).getInt(SEED_GROWTH));
    }

    public static boolean blockDamage(Level level) {
        return level.getGameRules().getBoolean(BLOCK_DAMAGE);
    }

    public static boolean sleepingBody(MinecraftServer server) {
        return rules(server).getBoolean(SLEEPING_BODY);
    }

    public static boolean lost(MinecraftServer server) {
        return rules(server).getBoolean(LOST);
    }

    /** The wait after arriving before a Candle or Gateway wakes a dreamer, in ticks. */
    public static long wakeWaitTicks(MinecraftServer server) {
        return Math.max(0, rules(server).getInt(WAKE_WAIT)) * 20L;
    }

    public static int ratio(MinecraftServer server) {
        return Math.max(1, Math.min(MAX_RATIO, rules(server).getInt(RATIO)));
    }
}
