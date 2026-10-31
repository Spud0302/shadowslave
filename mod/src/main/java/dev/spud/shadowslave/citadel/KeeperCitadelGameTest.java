package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.ability.AbilityRegistry;
import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.ability.KeeperHold;
import dev.spud.shadowslave.ability.SanctuaryAbility;
import dev.spud.shadowslave.awakening.AbilityGrants;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.block.ModBlocks;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.CandleBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.Set;

/** The Keeper on and off the land of a Citadel its clan holds. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class KeeperCitadelGameTest {
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void onClanLandAbilitiesCostHalfAndThePoolIsWhole(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        BlockPos centre = new BlockPos(6100, 100, 4000);
        CitadelData.Citadel citadel = CitadelGameTest.raise(realm, centre);
        CitadelGameTest.ready(helper, realm, centre, () -> {
            ServerPlayer keeper = AbilityGameTest.playerWith(helper, AspectKitCatalog.KEEPER);
            var soul = dev.spud.shadowslave.soul.SoulService.get(keeper);
            CitadelGameTest.saint(keeper);
            CitadelService.conquer(realm, citadel, Optional.of(keeper));
            helper.assertTrue(CitadelService.claim(keeper, citadel), "claimed");
            // Only a Saint binds the Gateway; the Keeper goes back to the rank this test is about.
            dev.spud.shadowslave.soul.SoulService.replace(keeper, soul);
            BlockPos stand = citadel.gateway().offset(3, 0, 3);
            keeper.teleportTo(realm, stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, Set.of(), 0, 0);
            KeeperHold.recheck(keeper);
            helper.assertTrue(KeeperHold.onLand(keeper), "the Citadel's grounds are clan land");
            helper.assertTrue(EssenceService.capacity(keeper) == EssenceService.DORMANT_CAPACITY, "the pool is whole at home");
            int cost = AbilityService.costFor(keeper, AbilityRegistry.get(SanctuaryAbility.ID).orElseThrow());
            helper.assertTrue(cost == 18, "half price at home: " + cost);

            BlockPos away = citadel.gateway().offset(200, 0, 0);
            keeper.teleportTo(realm, away.getX() + 0.5, away.getY(), away.getZ() + 0.5, Set.of(), 0, 0);
            KeeperHold.recheck(keeper);
            helper.assertFalse(KeeperHold.onLand(keeper), "far from the Gateway is no clan's land");
            helper.assertTrue(EssenceService.capacity(keeper) < EssenceService.DORMANT_CAPACITY, "Homebound again");

            ClanService.leave(keeper);
            CitadelGameTest.home(helper, keeper);
            CitadelGameTest.release(realm, centre);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void gatewayStepCarriesTheKeeperFromACandleToTheGateway(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        BlockPos centre = new BlockPos(7000, 100, 4000);
        CitadelData.Citadel citadel = CitadelGameTest.raise(realm, centre);
        CitadelGameTest.ready(helper, realm, centre, () -> {
            ServerPlayer keeper = AbilityGameTest.playerWith(helper, AspectKitCatalog.KEEPER);
            SoulService.awaken(keeper);
            helper.assertTrue(AbilityGrants.grant(keeper, KitLadder.GATEWAY_STEP), "Gateway Step granted");
            EssenceService.fill(keeper);
            CitadelService.conquer(realm, citadel, Optional.of(keeper));
            var soul = dev.spud.shadowslave.soul.SoulService.get(keeper);
            CitadelGameTest.saint(keeper);
            helper.assertTrue(CitadelService.claim(keeper, citadel), "claimed");
            // Only a Saint binds the Gateway; the Keeper goes back to the rank this test is about.
            dev.spud.shadowslave.soul.SoulService.replace(keeper, soul);
            helper.setBlock(new BlockPos(3, 1, 2), ModBlocks.DREAM_CANDLE.get().defaultBlockState().setValue(CandleBlock.LIT, true));
            helper.assertTrue(AbilityService.useAbility(keeper, 1) == AbilityService.Outcome.USED, "the Keeper steps through");
            helper.assertTrue(keeper.level() == realm && keeper.blockPosition().closerThan(citadel.gateway(), 4),
                    "arrives beside the Gateway: " + keeper.blockPosition().toShortString());
            helper.assertTrue(DreamCrossing.wake(keeper, DreamCrossing.Reason.COMMAND), "and wakes where they stood");
            ClanService.leave(keeper);
            CitadelGameTest.home(helper, keeper);
            CitadelGameTest.release(realm, centre);
            helper.succeed();
        });
    }
}
