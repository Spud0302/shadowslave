package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.awakening.Longing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilityKitRulesTest {
    @Test
    void everyUtilityAbilityIsRegistered() {
        for (var id : List.of(AspectKitCatalog.FORGE.abilityId(), AspectKitCatalog.DELVE.abilityId(),
                AspectKitCatalog.MASON.abilityId(), AspectKitCatalog.VERDANT.abilityId(),
                KitLadder.MASTERWORK.abilityId(), KitLadder.MOLTEN_EDGE.abilityId(), KitLadder.VEIN_PULL.abilityId(),
                KitLadder.STONEWALK.abilityId(), KitLadder.BLUEPRINT.abilityId(), KitLadder.SCAFFOLD.abilityId(),
                KitLadder.LIVING_WALL.abilityId(), KitLadder.WILD_CALL.abilityId())) {
            assertTrue(AbilityRegistry.isActivatable(id), id.toString());
        }
    }

    @Test
    void everyUtilityNatureHasALonging() {
        for (String nature : List.of("Forge", "Delve", "Mason", "Verdant")) {
            assertTrue(List.of(Longing.values()).stream().anyMatch(longing -> longing.natureName().equals(nature)), nature);
        }
    }

    @Test
    void tunnelStrikeDigsDownOrUpOnlyWhenLookingSteeply() {
        assertEquals(Direction.DOWN, TunnelStrikeAbility.facing(60.0F, Direction.NORTH));
        assertEquals(Direction.UP, TunnelStrikeAbility.facing(-70.0F, Direction.NORTH));
        assertEquals(Direction.EAST, TunnelStrikeAbility.facing(20.0F, Direction.EAST));
    }

    @Test
    void aMasonRemembersTheLatestWorksAndForgetsTheOldest() {
        long[] works = new long[0];
        for (int i = 0; i < 5; i++) {
            works = MasonWorks.remember(works, BlockPos.asLong(i, 64, 0), 3);
        }
        assertArrayEquals(new long[]{BlockPos.asLong(2, 64, 0), BlockPos.asLong(3, 64, 0), BlockPos.asLong(4, 64, 0)}, works);
        assertEquals(3, MasonWorks.remember(works, BlockPos.asLong(4, 64, 0), 3).length, "a block placed twice counts once");
    }

    @Test
    void farFromHomeCostsHalfAgainOnlyAwayFromYourWorks() {
        long[] works = {BlockPos.asLong(0, 64, 0)};
        assertTrue(MasonWorks.nearAny(works, new BlockPos(20, 64, 20), MasonWorks.HOME_RANGE));
        assertFalse(MasonWorks.nearAny(works, new BlockPos(40, 64, 0), MasonWorks.HOME_RANGE));
        assertFalse(MasonWorks.nearAny(new long[0], BlockPos.ZERO, MasonWorks.HOME_RANGE));
        assertEquals(30, MasonWorks.farCost(20));
        assertEquals(38, MasonWorks.farCost(25));
    }
}
