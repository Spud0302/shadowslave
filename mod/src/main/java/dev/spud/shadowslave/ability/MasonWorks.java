package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Arrays;

/**
 * What a Mason has built, for Far From Home: the last {@link #REMEMBERED}
 * blocks they placed in each dimension, kept in the player's saved data.
 * Abilities cost half again as much with none of them within
 * {@link #HOME_RANGE} blocks.
 */
public final class MasonWorks {
    static final String KEY = "shadowslave_mason_works";
    static final int REMEMBERED = 512;
    static final double HOME_RANGE = 32.0;
    static final float FAR_FROM_HOME_FACTOR = 1.5F;

    private MasonWorks() {
    }

    /** Remembers a block a Mason placed by hand. */
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isMason(player)) {
            record(player, event.getPos());
        }
    }

    static boolean isMason(ServerPlayer player) {
        return KitRules.hasFlaw(player, AspectKitCatalog.MASON.flawEffectId());
    }

    /** Remembers a placed block, oldest forgotten first once the list is full. */
    static void record(ServerPlayer player, BlockPos pos) {
        CompoundTag all = player.getPersistentData().getCompound(KEY);
        String dimension = player.level().dimension().location().toString();
        long[] works = all.contains(dimension, Tag.TAG_LONG_ARRAY) ? all.getLongArray(dimension) : new long[0];
        all.put(dimension, new LongArrayTag(remember(works, pos.asLong(), REMEMBERED)));
        player.getPersistentData().put(KEY, all);
    }

    static long[] remember(long[] works, long pos, int limit) {
        for (long known : works) {
            if (known == pos) {
                return works;
            }
        }
        long[] next = Arrays.copyOf(works, works.length + 1);
        next[works.length] = pos;
        return next.length > limit ? Arrays.copyOfRange(next, next.length - limit, next.length) : next;
    }

    /** True if something the player built in this dimension stands within {@link #HOME_RANGE}. */
    static boolean nearWorks(ServerPlayer player) {
        CompoundTag all = player.getPersistentData().getCompound(KEY);
        long[] works = all.getLongArray(player.level().dimension().location().toString());
        return nearAny(works, player.blockPosition(), HOME_RANGE);
    }

    static boolean nearAny(long[] works, BlockPos at, double range) {
        double limit = range * range;
        for (long work : works) {
            if (BlockPos.of(work).distSqr(at) <= limit) {
                return true;
            }
        }
        return false;
    }

    /** Far From Home: the cost after the Flaw, for a Mason away from their works. */
    static int costFor(ServerPlayer player, int cost) {
        return isMason(player) && !nearWorks(player) ? farCost(cost) : cost;
    }

    static int farCost(int cost) {
        return (int) Math.ceil(cost * FAR_FROM_HOME_FACTOR);
    }
}
