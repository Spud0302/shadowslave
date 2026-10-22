package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IntentionGameTest {
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void offeringTakesOneTaggedItemAndNamesTheLonging(GameTestHelper helper) {
        ServerPlayer player = dreamer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BLAZE_POWDER, 3));
        helper.assertTrue(IntentionService.offer(player, Longing.BURN) == IntentionService.Result.OFFERED, "offered");
        helper.assertTrue(player.getMainHandItem().getCount() == 2, "one blaze powder taken");
        IntentionData intention = IntentionService.get(player);
        helper.assertTrue(intention.offeringTag().equals("ember") && intention.longing().equals("burn"), "intention " + intention);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT, 5));
        helper.assertTrue(IntentionService.offer(player, Longing.HIDE) == IntentionService.Result.NAMED_ONLY, "dirt is not wanted");
        helper.assertTrue(player.getMainHandItem().getCount() == 5, "dirt stays in hand");
        intention = IntentionService.get(player);
        helper.assertTrue(intention.offeringTag().equals("ember") && intention.longing().equals("hide"),
                "the earlier offering stands, the longing changes: " + intention);
        helper.assertTrue(AspectRollService.evidence(player).weightFor("ember") == 2
                && AspectRollService.evidence(player).weightFor("mist") == 2, "both lean the roll");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void theRollSpendsTheIntention(GameTestHelper helper) {
        ServerPlayer player = dreamer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.KELP));
        IntentionService.offer(player, Longing.RETURN);
        AspectRollService.roll(player);
        helper.assertTrue(IntentionService.get(player).isEmpty(), "the intention is used up");
        helper.assertTrue(IntentionService.offer(player, Longing.BURN) == IntentionService.Result.ALREADY_NAMED,
                "no offering once named");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void offeringNeedsTheCandle(GameTestHelper helper) {
        ServerPlayer player = dreamer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FEATHER));
        SoulSeaService.requestIntention(player, "hide");
        helper.assertTrue(IntentionService.get(player).isEmpty(), "nothing recorded away from the candle");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "nothing taken");
        helper.succeed();
    }

    @SuppressWarnings("removal")
    private static ServerPlayer dreamer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 1, 2.5)));
        return player;
    }
}
