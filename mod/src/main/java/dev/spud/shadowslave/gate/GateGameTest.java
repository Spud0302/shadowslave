package dev.spud.shadowslave.gate;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.creature.NightmareRules;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.world.entity.AshenTyrantEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GateGameTest {
    /**
     * One test for the whole Gate, so no other Gate test opens one within the
     * spacing: feed a Lure, light it, then run the warning, the waves and the
     * Guardian, and check that killing the Guardian closes the Gate.
     */
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void aLureCallsAGateThatTheGuardianCloses(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        level.getGameRules().getRule(NightmareRules.BLOCK_DAMAGE).set(false, level.getServer());
        @SuppressWarnings("removal")
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(0.5, 1, 0.5)));

        BlockPos lurePos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(lurePos, GateRegistry.GATE_LURE.get().defaultBlockState());
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(lurePos), Direction.UP, lurePos, false);

        // Three Dormant shards are not enough; a fourth wakes the first rune.
        feed(level, player, lurePos, hit, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 3));
        helper.assertTrue(level.getBlockState(lurePos).getValue(GateLureBlock.CHARGE) == 0, "three shards leave it dark");
        feed(level, player, lurePos, hit, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 1));
        helper.assertTrue(level.getBlockState(lurePos).getValue(GateLureBlock.CHARGE) == 1, "four shards light one rune");
        // An Awakened shard is worth four Dormant ones: 8 points forecast Category 2.
        feed(level, player, lurePos, hit, new ItemStack(ModItems.AWAKENED_SOUL_SHARD.get(), 1));
        GateLureBlockEntity lure = (GateLureBlockEntity) level.getBlockEntity(lurePos);
        helper.assertTrue(lure.pull() == 8 && lure.shards() == 5, "pull 8 from five shards: " + lure.pull());
        helper.assertTrue(GateForecast.fromPull(lure.pull()).likeliest() == 2, "forecast Category 2");

        // Sneak-use empty-handed: the Lure burns away and the Gate begins its warning.
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        level.getBlockState(lurePos).useWithoutItem(level, player, hit);
        player.setShiftKeyDown(false);
        helper.assertTrue(level.getBlockState(lurePos).isAir(), "the Lure is consumed");
        GateEvent gate = GateData.get(level).gates.stream().filter(g -> g.center.equals(lurePos.below()))
                .findFirst().orElseThrow(() -> new AssertionError("no Gate opened at the Lure"));
        helper.assertTrue(gate.phase == GateEvent.Phase.WARNING && gate.trigger == GateTrigger.LURE, "warning phase");
        helper.assertTrue(!GateService.canOpen(level, lurePos.east(10)), "no second Gate close by");
        helper.assertTrue(level.getEntity(gate.rift) instanceof GateRiftEntity, "the rift stands at the Gate");

        // A Seer's Foresight rolls the Category early and reads out the roster; the opening keeps it.
        helper.assertTrue(GateService.foresee(level, player.blockPosition(), 128.0).orElseThrow() == gate, "Foresight finds the Gate");
        int foreseen = gate.category;
        helper.assertTrue(gate.foreseen, "the Gate is foreseen");
        helper.assertTrue(GateService.roster(gate).get(0).startsWith("Wave 1: "), "roster: " + GateService.roster(gate));

        // Skip to the end of the countdown: the Gate opens at a Category from the forecast.
        gate.phaseTicks = GateEvent.WARNING_TICKS - 1;
        GateService.tick(level, gate);
        helper.assertTrue(gate.phase == GateEvent.Phase.OPEN, "the Gate opens after the countdown");
        helper.assertTrue(gate.category >= 1 && gate.category <= 3, "Category within the forecast: " + gate.category);
        helper.assertTrue(gate.category == foreseen, "the Gate opens at the foreseen Category");

        // Pour out every wave.
        int waves = GateCategory.waves(gate.category).size();
        while (gate.wave < waves) {
            gate.nextWaveIn = 1;
            GateService.tick(level, gate);
        }
        int expected = GateCategory.waves(gate.category).stream().flatMap(List::stream)
                .mapToInt(GateCategory.Spawn::count).sum();
        helper.assertTrue(gate.spawned.size() == expected, "every wave spawned: " + gate.spawned.size() + " of " + expected);

        // Clear the waves and the Guardian emerges.
        for (UUID id : List.copyOf(gate.spawned)) {
            if (level.getEntity(id) instanceof NightmareCreature creature) {
                creature.discard();
            }
        }
        GateService.tick(level, gate);
        helper.assertTrue(gate.phase == GateEvent.Phase.GUARDIAN, "the Guardian phase follows the waves");
        AshenTyrantEntity guardian = (AshenTyrantEntity) level.getEntity(gate.guardian);
        helper.assertTrue(guardian != null && guardian.rank() == GateCategory.guardianRank(gate.category)
                && guardian.creatureClass() == CreatureClass.TYRANT, "an Ashen Tyrant guards the Gate at the Category's rank");

        // Killing the Guardian closes the Gate.
        guardian.kill();
        helper.assertTrue(gate.phase == GateEvent.Phase.CLOSED, "the Gate closes when its Guardian falls");
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(GateData.get(level).gates.stream().noneMatch(g -> g.id.equals(gate.id)), "the closed Gate is forgotten");
            helper.succeed();
        });
    }

    private static void feed(ServerLevel level, ServerPlayer player, BlockPos pos, BlockHitResult hit, ItemStack shards) {
        player.setItemInHand(InteractionHand.MAIN_HAND, shards);
        while (!player.getMainHandItem().isEmpty()) {
            BlockState state = level.getBlockState(pos);
            state.useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, hit);
        }
    }
}
