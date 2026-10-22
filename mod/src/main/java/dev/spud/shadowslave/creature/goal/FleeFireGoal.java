package dev.spud.shadowslave.creature.goal;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;

/** Beast package: bolts from fire, from burning, and from a player waving a torch or flint and steel close by. */
public final class FleeFireGoal extends Goal {
    static final int SEARCH_RADIUS = 4;
    static final double TORCH_RANGE = 4.0;

    private final NightmareCreature creature;
    private Vec3 fleeTo;
    private int checkCooldown;

    public FleeFireGoal(NightmareCreature creature) {
        this.creature = creature;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (creature.creatureClass() != CreatureClass.BEAST) {
            return false;
        }
        if (--checkCooldown > 0 && !creature.isOnFire()) {
            return false;
        }
        checkCooldown = 10;
        Optional<Vec3> fire = creature.isOnFire() ? Optional.of(creature.position()) : nearestFire();
        if (fire.isEmpty()) {
            return false;
        }
        fleeTo = DefaultRandomPos.getPosAway(creature, 10, 5, fire.get());
        return fleeTo != null;
    }

    private Optional<Vec3> nearestFire() {
        BlockPos origin = creature.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-SEARCH_RADIUS, -1, -SEARCH_RADIUS),
                origin.offset(SEARCH_RADIUS, 2, SEARCH_RADIUS))) {
            var state = creature.level().getBlockState(pos);
            if (state.is(BlockTags.FIRE) || state.is(BlockTags.CAMPFIRES) && state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)
                    || state.is(Blocks.LAVA)) {
                return Optional.of(Vec3.atCenterOf(pos));
            }
        }
        Player player = creature.level().getNearestPlayer(creature, TORCH_RANGE);
        if (player != null && (player.isHolding(Items.TORCH) || player.isHolding(Items.SOUL_TORCH)
                || player.isHolding(Items.FLINT_AND_STEEL) || player.isHolding(Items.FIRE_CHARGE))) {
            return Optional.of(player.position());
        }
        return Optional.empty();
    }

    @Override
    public void start() {
        creature.getNavigation().moveTo(fleeTo.x, fleeTo.y, fleeTo.z, 1.4);
    }

    @Override
    public boolean canContinueToUse() {
        return !creature.getNavigation().isDone();
    }
}
