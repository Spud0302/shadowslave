package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.IntentionService;
import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Forge, Delve, Mason and Verdant: each innate, ability and Flaw doing its one thing. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class UtilityKitGameTest {
    private static final String ROOM = "ritual_test_room";
    /** Facing north (toward z = 0) from the back of the room. */
    private static final float NORTH = 180.0F;

    private static void standAtBack(GameTestHelper helper, ServerPlayer player) {
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 1, 4.5)), NORTH, 0.0F);
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void quenchSmeltsWhatYouHold(GameTestHelper helper) {
        ServerPlayer forge = AbilityGameTest.playerWith(helper, AspectKitCatalog.FORGE);
        forge.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RAW_IRON, 12));
        helper.assertTrue(AbilityService.useAbility(forge, 0) == AbilityService.Outcome.USED, "Quench fires");
        helper.assertTrue(forge.getMainHandItem().getCount() == 4, "eight raw iron taken: " + forge.getMainHandItem());
        int ingots = forge.getInventory().countItem(Items.IRON_INGOT);
        helper.assertTrue(ingots >= 8 && ingots <= 16, "eight ingots or more: " + ingots);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void quenchBurstsWhenThereIsNothingToSmelt(GameTestHelper helper) {
        ServerPlayer forge = AbilityGameTest.playerWith(helper, AspectKitCatalog.FORGE);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 3));
        helper.assertTrue(AbilityService.useAbility(forge, 0) == AbilityService.Outcome.USED, "the heat bursts");
        helper.assertTrue(zombie.isOnFire(), "the zombie burns");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void thriftyHandsDoublesSomeSmeltsOnlyForAForge(GameTestHelper helper) {
        ServerPlayer forge = AbilityGameTest.playerWith(helper, AspectKitCatalog.FORGE);
        ServerPlayer delve = AbilityGameTest.playerWith(helper, AspectKitCatalog.DELVE);
        int extra = KitPassives.thriftyExtra(forge, 1000);
        helper.assertTrue(extra > 120 && extra < 280, "about one in five: " + extra);
        helper.assertTrue(KitPassives.thriftyExtra(delve, 1000) == 0, "nobody else");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void moltenEdgeBurnsWhatYouHit(GameTestHelper helper) {
        ServerPlayer forge = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.FORGE, KitLadder.MOLTEN_EDGE);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 3));
        helper.assertTrue(AbilityService.useAbility(forge, 1) == AbilityService.Outcome.USED, "Molten Edge fires");
        helper.assertTrue(AbilityService.useAbility(forge, 1) == AbilityService.Outcome.FAILED, "already burning");
        float before = zombie.getHealth();
        zombie.hurt(forge.damageSources().playerAttack(forge), 2.0F);
        helper.assertTrue(zombie.isOnFire(), "the blow sets it alight");
        helper.assertTrue(before - zombie.getHealth() >= 4.9F, "2 plus 3 seared: " + (before - zombie.getHealth()));
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void masterworkWaitsForTheNextWeave(GameTestHelper helper) {
        ServerPlayer forge = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.FORGE, KitLadder.MASTERWORK);
        helper.assertTrue(AbilityService.useAbility(forge, 1) == AbilityService.Outcome.USED, "Masterwork fires");
        helper.assertTrue(MasterworkAbility.active(forge), "the masterwork waits");
        helper.assertTrue(AbilityService.status(forge).slots().get(1).primed(), "lit on the HUD");
        helper.assertTrue(AbilityService.useAbility(forge, 1) == AbilityService.Outcome.FAILED, "only one at a time");
        MasterworkAbility.spend(forge);
        helper.assertFalse(MasterworkAbility.active(forge), "a weave spends it");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void tunnelStrikeBreaksAThreeByThreeTunnel(GameTestHelper helper) {
        ServerPlayer delve = AbilityGameTest.playerWith(helper, AspectKitCatalog.DELVE);
        standAtBack(helper, delve);
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(1, 1, 1), new BlockPos(3, 3, 3))) {
            helper.setBlock(pos, Blocks.STONE);
        }
        helper.assertTrue(AbilityService.useAbility(delve, 0) == AbilityService.Outcome.USED, "Tunnel Strike fires");
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(1, 1, 1), new BlockPos(3, 3, 3))) {
            helper.assertBlockPresent(Blocks.AIR, pos);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void veinPullTakesTheWholeVein(GameTestHelper helper) {
        ServerPlayer delve = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.DELVE, KitLadder.VEIN_PULL);
        delve.moveTo(helper.absoluteVec(new Vec3(2.5, 1, 3.5)));
        BlockPos[] vein = {new BlockPos(2, 1, 1), new BlockPos(2, 2, 1), new BlockPos(3, 2, 1)};
        for (BlockPos pos : vein) {
            helper.setBlock(pos, Blocks.IRON_ORE);
        }
        delve.lookAt(EntityAnchorArgument.Anchor.EYES, helper.absoluteVec(new Vec3(2.5, 1.5, 2.0)));
        helper.assertTrue(AbilityService.useAbility(delve, 1) == AbilityService.Outcome.FAILED, "a bare hand can't pull iron");
        delve.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
        helper.assertTrue(AbilityService.useAbility(delve, 1) == AbilityService.Outcome.USED, "Vein Pull fires");
        for (BlockPos pos : vein) {
            helper.assertBlockPresent(Blocks.AIR, pos);
        }
        helper.assertTrue(delve.getInventory().countItem(Items.RAW_IRON) >= 3, "the ore is in the pack");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void stonewalkStepsThroughTheWall(GameTestHelper helper) {
        ServerPlayer delve = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.DELVE, KitLadder.STONEWALK);
        standAtBack(helper, delve);
        helper.assertTrue(AbilityService.useAbility(delve, 1) == AbilityService.Outcome.FAILED, "no wall, no Stonewalk");
        for (int x = 0; x <= 4; x++) {
            helper.setBlock(new BlockPos(x, 1, 3), Blocks.STONE);
            helper.setBlock(new BlockPos(x, 2, 3), Blocks.STONE);
        }
        helper.assertTrue(AbilityService.useAbility(delve, 1) == AbilityService.Outcome.USED, "Stonewalk fires");
        Vec3 at = helper.relativeVec(delve.position());
        helper.assertTrue(Math.abs(at.z - 2.5) < 0.01 && Math.abs(at.x - 2.5) < 0.01, "just past the wall: " + at);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void raiseWallBuildsFromTheBlocksInHand(GameTestHelper helper) {
        ServerPlayer mason = AbilityGameTest.playerWith(helper, AspectKitCatalog.MASON);
        standAtBack(helper, mason);
        helper.assertTrue(AbilityService.useAbility(mason, 0) == AbilityService.Outcome.FAILED, "no blocks in hand");
        mason.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COBBLESTONE, 16));
        helper.assertTrue(AbilityService.useAbility(mason, 0) == AbilityService.Outcome.USED, "Raise Wall fires");
        helper.assertTrue(mason.getMainHandItem().getCount() == 7, "nine blocks used: " + mason.getMainHandItem().getCount());
        helper.assertBlockPresent(Blocks.COBBLESTONE, new BlockPos(1, 1, 2));
        helper.assertBlockPresent(Blocks.COBBLESTONE, new BlockPos(3, 3, 2));
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void farFromHomeRaisesCostsUntilYouHaveBuiltNearby(GameTestHelper helper) {
        ServerPlayer mason = AbilityGameTest.playerWith(helper, AspectKitCatalog.MASON);
        AspectAbility wall = AbilityRegistry.get(RaiseWallAbility.ID).orElseThrow();
        helper.assertTrue(AbilityService.costFor(mason, wall) == 30, "half again away from home: " + AbilityService.costFor(mason, wall));
        MasonWorks.record(mason, mason.blockPosition().north(3));
        helper.assertTrue(AbilityService.costFor(mason, wall) == 20, "the usual cost at home");
        KitPassives.keepLongReach(mason, true);
        helper.assertTrue(mason.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) == 6.5, "Long Reach: "
                + mason.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE));
        KitPassives.keepLongReach(mason, false);
        helper.assertTrue(mason.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) == 4.5, "and it goes with the kit");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void blueprintLearnsTheShapeAheadForFree(GameTestHelper helper) {
        ServerPlayer mason = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.MASON, KitLadder.BLUEPRINT);
        standAtBack(helper, mason);
        helper.setBlock(new BlockPos(2, 1, 1), Blocks.BRICKS);
        helper.setBlock(new BlockPos(2, 2, 1), Blocks.BRICKS);
        float before = EssenceService.current(mason);
        helper.assertTrue(AbilityService.useAbility(mason, 1) == AbilityService.Outcome.PRIMED, "the shape is learned");
        helper.assertTrue(BlueprintAbility.learned(mason) >= 2, "two bricks at least: " + BlueprintAbility.learned(mason));
        helper.assertTrue(EssenceService.current(mason) == before, "learning is free");
        helper.assertTrue(AbilityService.status(mason).slots().get(1).primed(), "held on the HUD");
        mason.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BRICKS, 1));
        helper.assertTrue(BlueprintAbility.takeOne(mason, Blocks.BRICKS.defaultBlockState()), "a brick from the pack");
        helper.assertFalse(BlueprintAbility.takeOne(mason, Blocks.BRICKS.defaultBlockState()), "and no more");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void scaffoldCatchesYouOverAGap(GameTestHelper helper) {
        ServerPlayer mason = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.MASON, KitLadder.SCAFFOLD);
        helper.assertTrue(AbilityService.useAbility(mason, 1) == AbilityService.Outcome.USED, "Scaffold fires");
        helper.assertTrue(ScaffoldAbility.active(mason), "footing for ten seconds");
        mason.moveTo(helper.absoluteVec(new Vec3(2.5, 3, 2.5)));
        AbilityRegistry.get(ScaffoldAbility.ID).orElseThrow().tick(mason);
        helper.assertBlockPresent(Blocks.BAMBOO_MOSAIC, new BlockPos(2, 2, 2));
        helper.assertTrue(ScaffoldAbility.planks(mason) == 1, "one plank laid");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void bloomGrowsTheCropsAround(GameTestHelper helper) {
        ServerPlayer verdant = AbilityGameTest.playerWith(helper, AspectKitCatalog.VERDANT);
        helper.setBlock(new BlockPos(3, 0, 2), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(3, 1, 2), Blocks.WHEAT);
        helper.assertTrue(AbilityService.useAbility(verdant, 0) == AbilityService.Outcome.USED, "Bloom fires");
        int age = helper.getBlockState(new BlockPos(3, 1, 2)).getValue(CropBlock.AGE);
        helper.assertTrue(age > 0, "the wheat grew: " + age);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void rootboundOnlyRefillsOnLivingGround(GameTestHelper helper) {
        ServerPlayer verdant = AbilityGameTest.playerWith(helper, AspectKitCatalog.VERDANT);
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
        helper.assertTrue(EssenceService.regenPerTick(verdant) == 0.0F, "nothing on stone");
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.GRASS_BLOCK);
        helper.assertTrue(EssenceService.regenPerTick(verdant) > 0.0F, "a refill on grass");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM, timeoutTicks = 60)
    public static void livingWallCutsWhatPressesAgainstIt(GameTestHelper helper) {
        ServerPlayer verdant = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.VERDANT, KitLadder.LIVING_WALL);
        standAtBack(helper, verdant);
        helper.assertTrue(AbilityService.useAbility(verdant, 1) == AbilityService.Outcome.USED, "Living Wall fires");
        helper.assertTrue(LivingWallAbility.hedgeOf(verdant).size() == 10, "five by two: " + LivingWallAbility.hedgeOf(verdant).size());
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 1));
        float health = zombie.getHealth();
        AspectAbility hedge = AbilityRegistry.get(LivingWallAbility.ID).orElseThrow();
        helper.succeedWhen(() -> {
            hedge.tick(verdant);
            helper.assertTrue(zombie.getHealth() < health, "the thorns cut");
            helper.assertTrue(zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "and slow");
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void wildCallTamesTheWolfYouLookAt(GameTestHelper helper) {
        ServerPlayer verdant = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.VERDANT, KitLadder.WILD_CALL);
        helper.assertTrue(AbilityService.useAbility(verdant, 1) == AbilityService.Outcome.FAILED, "nothing answers");
        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(2, 1, 0));
        verdant.lookAt(EntityAnchorArgument.Anchor.EYES, wolf.getEyePosition());
        helper.assertTrue(AbilityService.useAbility(verdant, 1) == AbilityService.Outcome.USED, "Wild Call fires");
        helper.assertTrue(wolf.isTame() && wolf.isOwnedBy(verdant), "the wolf is yours");
        helper.assertTrue(wolf.hasEffect(MobEffects.REGENERATION) && wolf.hasEffect(MobEffects.DAMAGE_BOOST), "and roused");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void everyUtilityNatureHasOfferingsTheSpellWants(GameTestHelper helper) {
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.ANVIL)).orElse("").equals("forge"), "anvil leans Forge");
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.IRON_PICKAXE)).orElse("").equals("delve"), "pickaxe leans Delve");
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.BRICKS)).orElse("").equals("mason"), "bricks lean Mason");
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.WHEAT_SEEDS)).orElse("").equals("verdant"), "seeds lean Verdant");
        helper.succeed();
    }
}
