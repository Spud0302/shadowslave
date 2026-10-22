package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.block.ModBlocks;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SpellState;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DreamCandleRitualGameTest {
    private static final BlockPos HEAD = new BlockPos(2, 1, 2);
    private static final BlockPos CANDLE = new BlockPos(3, 1, 2);

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void litCandleRollsAnAspectOnWaking(GameTestHelper helper) {
        ServerPlayer player = setUp(helper, true, true);
        helper.assertTrue(SoulIdentityService.get(player).isRevealed(), "Aspect should be revealed");
        helper.assertTrue(SoulService.get(player).spellState() == SpellState.DREAMER, "should be Dreamer");
        helper.assertBlockNotPresent(ModBlocks.DREAM_CANDLE.get(), CANDLE);
        ShadowSlaveMod.LOGGER.info("RITUAL GAMETEST rolled {}", SoulIdentityService.get(player).aspect().orElseThrow().displayedName());
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void unlitCandleKeepsSleepVanilla(GameTestHelper helper) {
        ServerPlayer player = setUp(helper, true, false);
        helper.assertFalse(SoulIdentityService.get(player).isRevealed(), "no roll without a lit candle");
        helper.assertBlockPresent(ModBlocks.DREAM_CANDLE.get(), CANDLE);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void noCandleKeepsSleepVanilla(GameTestHelper helper) {
        ServerPlayer player = setUp(helper, false, false);
        helper.assertFalse(SoulIdentityService.get(player).isRevealed(), "no roll without a candle");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void secondRitualDoesNotReroll(GameTestHelper helper) {
        ServerPlayer player = setUp(helper, true, true);
        var first = SoulIdentityService.get(player);
        helper.setBlock(CANDLE, ModBlocks.DREAM_CANDLE.get().defaultBlockState().setValue(CandleBlock.LIT, true));
        sleepAndWake(helper, player);
        helper.assertTrue(first.equals(SoulIdentityService.get(player)), "Aspect must not change");
        helper.assertBlockPresent(ModBlocks.DREAM_CANDLE.get(), CANDLE);
        helper.succeed();
    }

    private static ServerPlayer setUp(GameTestHelper helper, boolean candle, boolean lit) {
        helper.getLevel().setDayTime(18000);
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        BlockPos foot = HEAD.relative(Direction.SOUTH);
        helper.setBlock(foot, Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.FOOT));
        helper.setBlock(HEAD, Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.HEAD));
        if (candle) {
            helper.setBlock(CANDLE, ModBlocks.DREAM_CANDLE.get().defaultBlockState().setValue(CandleBlock.LIT, lit));
        }
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        sleepAndWake(helper, player);
        return player;
    }

    private static void sleepAndWake(GameTestHelper helper, ServerPlayer player) {
        player.startSleeping(helper.absolutePos(HEAD));
        try {
            Field counter = Player.class.getDeclaredField("sleepCounter");
            counter.setAccessible(true);
            counter.setInt(player, 100);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
        player.stopSleepInBed(false, true);
    }
}
