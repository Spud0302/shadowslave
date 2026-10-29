package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.block.ModBlocks;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DreamRealmGameTest {
    private static final BlockPos HEAD = new BlockPos(2, 1, 2);
    private static final BlockPos CANDLE = new BlockPos(3, 1, 2);

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void anAwakenedSleeperCrossesAndAHurtBodyWakesThem(GameTestHelper helper) {
        ServerPlayer player = dreamerInBed(helper, true);
        helper.assertTrue(DreamCrossing.tryCross(player), "a lit Dream Candle takes an Awakened sleeper across");
        helper.assertTrue(DreamRealm.is(player.level()), "the player is in the Dream Realm");
        DreamData data = DreamCrossing.data(player);
        helper.assertTrue(data.isDreaming(), "dreaming");
        BlockPos anchor = data.anchor().orElseThrow(() -> new AssertionError("a first Dream Anchor is lit on arrival"));
        helper.assertTrue(DreamCrossing.isLitCandle(player.serverLevel().getBlockState(anchor)), "the anchor is a lit Dream Candle");
        helper.assertTrue(player.blockPosition().distManhattan(anchor) <= 4, "the dreamer arrives beside the anchor");
        int ratio = DreamRealmRules.ratio(player.server);
        BlockPos bed = helper.absolutePos(HEAD);
        helper.assertTrue(Math.abs(anchor.getX() - Math.floorDiv(bed.getX(), ratio)) <= 1
                && Math.abs(anchor.getZ() - Math.floorDiv(bed.getZ(), ratio)) <= 1, "the anchor sits over the bed, scaled 1:" + ratio);

        SleepingBodyEntity body = body(helper, data);
        helper.assertTrue(body.isSleeping() && body.owner().orElseThrow().equals(player.getUUID()), "the body sleeps in the bed");

        body.hurt(helper.getLevel().damageSources().generic(), 2.0F);
        helper.runAfterDelay(3, () -> {
            helper.assertFalse(DreamCrossing.data(player).isDreaming(), "the hurt body pulled the dreamer awake");
            helper.assertTrue(player.level() == helper.getLevel(), "the dreamer is back in the waking world");
            helper.assertTrue(player.position().distanceTo(bed.getCenter()) < 3.0, "beside their bed");
            helper.assertTrue(body.isRemoved(), "the body is taken up again");
            helper.assertTrue(player.getHealth() <= player.getMaxHealth() - 2.0F + 0.01F, "the wound carries over: " + player.getHealth());
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void aDeadBodyKillsTheDreamer(GameTestHelper helper) {
        ServerPlayer player = dreamerInBed(helper, true);
        helper.assertTrue(DreamCrossing.tryCross(player), "crossed");
        player.hasChangedDimension(); // the client's teleport confirmation, which a mock player never sends
        SleepingBodyEntity body = body(helper, DreamCrossing.data(player));
        body.hurt(helper.getLevel().damageSources().generic(), 1000.0F);
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(player.isDeadOrDying(), "the dreamer dies with their body");
            helper.assertFalse(DreamCrossing.data(player).isDreaming(), "the dream is over");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void theCandleWakesAndTheAnchorStays(GameTestHelper helper) {
        ServerPlayer player = dreamerInBed(helper, true);
        helper.assertTrue(DreamCrossing.tryCross(player), "crossed");
        BlockPos anchor = DreamCrossing.data(player).anchor().orElseThrow();
        helper.assertTrue(DreamCrossing.wake(player, DreamCrossing.Reason.CANDLE), "the candle wakes the dreamer");
        helper.assertTrue(player.level() == helper.getLevel(), "back in the waking world");
        helper.assertTrue(DreamCrossing.data(player).anchor().orElseThrow().equals(anchor), "the anchor is kept for next time");

        // Lighting another Dream Candle moves the anchor; the next crossing arrives there.
        ServerLevel realm = DreamRealm.level(player.server);
        BlockPos second = DreamCrossing.firstAnchor(realm, anchor.offset(6, 0, 0));
        DreamCrossing.anchorAt(player, second);
        player.startSleeping(helper.absolutePos(HEAD));
        helper.assertTrue(DreamCrossing.tryCross(player), "crossed again");
        helper.assertTrue(player.blockPosition().distManhattan(second) <= 4, "arrives at the new anchor");
        DreamCrossing.wake(player, DreamCrossing.Reason.COMMAND);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void noCandleOrNoAwakeningMeansNoCrossing(GameTestHelper helper) {
        ServerPlayer unlit = dreamerInBed(helper, false);
        helper.assertFalse(DreamCrossing.tryCross(unlit), "an unlit candle keeps sleep vanilla");
        helper.setBlock(CANDLE, ModBlocks.DREAM_CANDLE.get().defaultBlockState().setValue(CandleBlock.LIT, true));
        ServerPlayer dormant = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        dormant.startSleeping(helper.absolutePos(HEAD));
        helper.assertFalse(DreamCrossing.tryCross(dormant), "a Dormant sleeper stays in the waking world");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void withoutTheSleepingBodyOptionNothingIsLeftBehind(GameTestHelper helper) {
        ServerPlayer player = dreamerInBed(helper, true);
        var rule = player.server.getGameRules().getRule(DreamRealmRules.SLEEPING_BODY);
        rule.set(false, player.server);
        try {
            helper.assertTrue(DreamCrossing.tryCross(player), "crossed");
            helper.assertTrue(DreamCrossing.data(player).dreaming().orElseThrow().body().isEmpty(), "no body is left");
            DreamCrossing.wake(player, DreamCrossing.Reason.COMMAND);
        } finally {
            rule.set(true, player.server);
        }
        helper.succeed();
    }

    public static ServerPlayer dreamerInBed(GameTestHelper helper, boolean lit) {
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        BlockPos foot = HEAD.relative(Direction.SOUTH);
        helper.setBlock(foot, Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.FOOT));
        helper.setBlock(HEAD, Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.HEAD));
        helper.setBlock(CANDLE, ModBlocks.DREAM_CANDLE.get().defaultBlockState().setValue(CandleBlock.LIT, lit));
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        SoulService.awaken(player);
        player.startSleeping(helper.absolutePos(HEAD));
        return player;
    }

    private static SleepingBodyEntity body(GameTestHelper helper, DreamData data) {
        Entity entity = helper.getLevel().getEntity(data.dreaming().orElseThrow().body()
                .orElseThrow(() -> new AssertionError("an Awakened dreamer leaves a body")));
        if (!(entity instanceof SleepingBodyEntity body)) {
            throw new AssertionError("no sleeping body in the bed");
        }
        return body;
    }
}
