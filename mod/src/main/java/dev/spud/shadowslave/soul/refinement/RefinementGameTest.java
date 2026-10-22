package dev.spud.shadowslave.soul.refinement;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.ability.AbilityRegistry;
import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.ability.KindleAbility;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.AwakeningService;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.awakening.SoulSeaService;
import dev.spud.shadowslave.soul.core.CoreService;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RefinementGameTest {
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void bodyPointAddsHealthAndRespecCostsAPoint(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        float baseHealth = player.getMaxHealth();
        helper.assertTrue(RefinementService.unspent(player) == 1, "one point at naming");
        helper.assertTrue(RefinementService.allocate(player, RefinementStat.BODY), "place it in Body");
        helper.assertTrue(player.getMaxHealth() == baseHealth + RefinementRules.HEALTH_PER_BODY, "a heart more");
        helper.assertFalse(RefinementService.allocate(player, RefinementStat.SOUL), "no points left");
        helper.assertTrue(RefinementService.respec(player), "respec");
        helper.assertTrue(player.getMaxHealth() == baseHealth, "the heart goes back");
        helper.assertTrue(RefinementService.unspent(player) == 0, "and the point is lost");
        helper.assertFalse(RefinementService.respec(player), "nothing left to respec");

        CoreService.set(player, Integer.MAX_VALUE);
        AwakeningService.awaken(player, KitLadder.EMBER_WARD.abilityId());
        helper.assertTrue(RefinementService.unspent(player) == 3, "three more at Awakening, less the lost one");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void soulAndAspectPointsShapeTheEssencePool(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        CoreService.set(player, Integer.MAX_VALUE);
        AwakeningService.awaken(player, KitLadder.CARRY_THE_FLAME.abilityId());
        RefinementService.allocate(player, RefinementStat.SOUL);
        RefinementService.allocate(player, RefinementStat.ASPECT);
        helper.assertTrue(EssenceService.capacity(player) == EssenceService.AWAKENED_CAPACITY + RefinementRules.CAPACITY_PER_SOUL,
                "capacity " + EssenceService.capacity(player));
        int kindle = AbilityService.costFor(player, AbilityRegistry.get(KindleAbility.ID).orElseThrow());
        helper.assertTrue(kindle == 33, "Kindle costs " + kindle);
        helper.assertTrue(AbilityService.status(player).slots().get(0).cost() == 33, "the HUD shows the cheaper cost");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void refiningNeedsTheCandle(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.STONE);
        helper.assertFalse(SoulSeaService.requestRefinement(player, "body"), "not meditating");
        helper.assertTrue(RefinementService.unspent(player) == 1, "the point is still there");
        helper.succeed();
    }
}
