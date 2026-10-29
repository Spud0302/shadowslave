package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Herald and Keeper: each innate, ability and Flaw doing its one thing (clan land is tested with the Citadels). */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HeraldKeeperGameTest {
    private static final String ROOM = "ritual_test_room";

    // ---- Herald --------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void commandPresenceMakesAlliesHitHarder(GameTestHelper helper) {
        ServerPlayer herald = AbilityGameTest.playerWith(helper, AspectKitCatalog.HERALD);
        ServerPlayer ally = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        helper.assertTrue(HeraldCommand.commanded(ally), "an ally beside a Herald is commanded");
        helper.assertFalse(HeraldCommand.commanded(herald), "the Herald does not command themselves");
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 1, 2));
        float before = pig.getHealth();
        pig.hurt(helper.getLevel().damageSources().playerAttack(ally), 4.0F);
        float dealt = before - pig.getHealth();
        helper.assertTrue(Math.abs(dealt - 4.0F * HeraldCommand.PRESENCE_BONUS) < 0.01, "the blow lands harder: " + dealt);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void warBannerRalliesAlliesAndIsFurledWhenDone(GameTestHelper helper) {
        ServerPlayer herald = AbilityGameTest.playerWith(helper, AspectKitCatalog.HERALD);
        ServerPlayer ally = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        // The test room has no floor of its own; give the banner ground to stand on.
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
        helper.assertTrue(AbilityService.useAbility(herald, 0) == AbilityService.Outcome.USED, "the banner is planted");
        BlockPos banner = WarBannerAbility.planted(herald).orElseThrow();
        helper.assertTrue(helper.getLevel().getBlockState(banner).is(Blocks.RED_BANNER), "a red banner stands");
        helper.assertTrue(ally.hasEffect(MobEffects.MOVEMENT_SPEED) && ally.hasEffect(MobEffects.DAMAGE_BOOST), "the ally is rallied");
        helper.assertTrue(herald.hasEffect(MobEffects.DAMAGE_BOOST), "so is the Herald");
        helper.assertTrue(AbilityService.useAbility(herald, 0) == AbilityService.Outcome.FAILED, "one banner at a time");
        AbilityRegistry.get(WarBannerAbility.ID).orElseThrow().clear(herald);
        helper.assertTrue(helper.getLevel().getBlockState(banner).isAir(), "the banner is gone");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void burdenOfCommandWeakensTheHeraldWhenAnAllyFalls(GameTestHelper helper) {
        ServerPlayer herald = AbilityGameTest.playerWith(helper, AspectKitCatalog.HERALD);
        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(3, 1, 3));
        wolf.tame(herald);
        wolf.kill();
        helper.assertTrue(herald.hasEffect(MobEffects.WEAKNESS) && herald.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                "the Herald bears the loss");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void rallyCallDrawsOnlyThoseWhoAnswer(GameTestHelper helper) {
        ServerPlayer herald = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.HERALD, KitLadder.RALLY_CALL);
        ServerPlayer willing = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        ServerPlayer unwilling = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        Vec3 far = herald.position().add(40, 0, 0);
        willing.teleportTo(far.x, far.y, far.z);
        unwilling.teleportTo(far.x, far.y, far.z + 2);
        helper.assertTrue(AbilityService.useAbility(herald, 1) == AbilityService.Outcome.USED, "the call sounds");
        helper.assertTrue(RallyCallAbility.waiting(herald).contains(willing.getUUID()), "it is heard");
        willing.setShiftKeyDown(true);
        AbilityRegistry.get(RallyCallAbility.ID).orElseThrow().tick(herald);
        helper.assertTrue(willing.distanceTo(herald) < 4, "the one who answered comes: " + willing.distanceTo(herald));
        helper.assertTrue(unwilling.distanceTo(herald) > 30, "nobody is pulled who did not answer");
        willing.setShiftKeyDown(false);
        AbilityRegistry.get(RallyCallAbility.ID).orElseThrow().clear(herald);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void oathOfArmsSharesTheBlows(GameTestHelper helper) {
        ServerPlayer herald = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.HERALD, KitLadder.OATH_OF_ARMS);
        ServerPlayer ally = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        Vec3 front = helper.absoluteVec(new Vec3(2.5, 1, 0.5));
        ally.teleportTo(front.x, front.y, front.z);
        herald.lookAt(EntityAnchorArgument.Anchor.EYES, ally.getEyePosition());
        helper.assertTrue(AbilityService.useAbility(herald, 1) == AbilityService.Outcome.USED, "the oath is sworn");
        ally.hurt(helper.getLevel().damageSources().generic(), 8.0F);
        helper.assertTrue(Math.abs(ally.getHealth() - 16.0F) < 0.01, "the ally takes half: " + ally.getHealth());
        helper.assertTrue(Math.abs(herald.getHealth() - 16.0F) < 0.01, "the Herald takes the rest: " + herald.getHealth());
        AbilityRegistry.get(OathOfArmsAbility.ID).orElseThrow().clear(herald);
        helper.succeed();
    }

    // ---- Keeper --------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void sanctuaryWardsThoseInsideButLetsNoBlowOut(GameTestHelper helper) {
        ServerPlayer keeper = AbilityGameTest.playerWith(helper, AspectKitCatalog.KEEPER);
        ServerPlayer ally = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 9));
        helper.assertTrue(AbilityService.useAbility(keeper, 0) == AbilityService.Outcome.USED, "the dome rises");
        helper.assertTrue(AbilityService.useAbility(keeper, 0) == AbilityService.Outcome.FAILED, "one dome at a time");
        ally.hurt(helper.getLevel().damageSources().generic(), 8.0F);
        helper.assertTrue(Math.abs(ally.getHealth() - 16.0F) < 0.01, "half harm inside: " + ally.getHealth());
        float before = zombie.getHealth();
        zombie.hurt(helper.getLevel().damageSources().playerAttack(keeper), 5.0F);
        helper.assertTrue(zombie.getHealth() == before, "no blow leaves the Sanctuary");
        AbilityRegistry.get(SanctuaryAbility.ID).orElseThrow().clear(keeper);
        zombie.hurt(helper.getLevel().damageSources().playerAttack(keeper), 5.0F);
        helper.assertTrue(zombie.getHealth() < before, "once it falls, blows land again");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void bulwarkTurnsArrowsBack(GameTestHelper helper) {
        ServerPlayer keeper = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.KEEPER, KitLadder.BULWARK);
        helper.assertTrue(AbilityService.useAbility(keeper, 1) == AbilityService.Outcome.USED, "the Bulwark rises");
        KeeperDomes.Dome dome = KeeperDomes.dome(keeper).orElseThrow();
        Arrow arrow = EntityType.ARROW.create(helper.getLevel());
        Vec3 start = dome.centre().add(0, 1, BulwarkAbility.RADIUS + 0.8);
        arrow.moveTo(start.x, start.y, start.z);
        arrow.setDeltaMovement(0, 0, -1.5);
        helper.getLevel().addFreshEntity(arrow);
        KeeperDomes.reflect(helper.getLevel().getServer(), keeper.getUUID(), helper.getLevel(), dome);
        helper.assertTrue(arrow.getDeltaMovement().z > 0, "the arrow flies back: " + arrow.getDeltaMovement());
        arrow.discard();
        AbilityRegistry.get(BulwarkAbility.ID).orElseThrow().clear(keeper);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void awayFromClanLandTheKeeperIsHomebound(GameTestHelper helper) {
        ServerPlayer keeper = AbilityGameTest.playerWith(helper, AspectKitCatalog.KEEPER);
        KeeperHold.recheck(keeper);
        helper.assertFalse(KeeperHold.onLand(keeper), "the waking world is no clan's land");
        helper.assertTrue(EssenceService.capacity(keeper) == KeeperHold.homebound(EssenceService.DORMANT_CAPACITY),
                "a third less: " + EssenceService.capacity(keeper));
        AspectAbility sanctuary = AbilityRegistry.get(SanctuaryAbility.ID).orElseThrow();
        helper.assertTrue(AbilityService.costFor(keeper, sanctuary) == SanctuaryAbility.ESSENCE_COST, "full price away from home");
        helper.assertTrue(KeeperHold.halved(SanctuaryAbility.ESSENCE_COST) == 18, "half, rounded up");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void gatewayStepNeedsACandleAndACitadel(GameTestHelper helper) {
        ServerPlayer keeper = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.KEEPER, KitLadder.GATEWAY_STEP);
        helper.assertTrue(AbilityService.useAbility(keeper, 1) == AbilityService.Outcome.FAILED, "no candle, no step");
        helper.succeed();
    }
}
