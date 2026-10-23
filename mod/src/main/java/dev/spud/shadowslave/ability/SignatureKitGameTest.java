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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Gloam, Rime and Fury: each signature rule, ability and Flaw doing its one thing. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SignatureKitGameTest {
    private static final String ROOM = "ritual_test_room";

    // ---- Gloam ---------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void gloamEdgeCutsByHowDarkItIs(GameTestHelper helper) {
        ServerPlayer gloam = AbilityGameTest.playerWith(helper, AspectKitCatalog.GLOAM);
        Zombie plain = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        Zombie cut = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 3));
        plain.hurt(gloam.damageSources().playerAttack(gloam), 2.0F);
        helper.assertTrue(AbilityService.useAbility(gloam, 0) == AbilityService.Outcome.USED, "Gloam Edge fires");
        cut.hurt(gloam.damageSources().playerAttack(gloam), 2.0F);
        float expected = switch (SignatureKitPassives.lightAt(cut)) {
            case DARK -> GloamEdgeAbility.DARK_CUT;
            case DIM -> GloamEdgeAbility.DIM_CUT;
            case BRIGHT -> GloamEdgeAbility.BRIGHT_CUT;
        };
        float extra = (plain.getHealth() - cut.getHealth());
        // A zombie's armour trims a little off both blows.
        helper.assertTrue(Math.abs(extra - expected) < 0.25F, "the edge adds " + expected + ", saw " + extra);
        AbilityRegistry.get(GloamEdgeAbility.ID).orElseThrow().clear(gloam);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void shadeStepRefusesABrightSpot(GameTestHelper helper) {
        ServerPlayer gloam = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.GLOAM, KitLadder.SHADE_STEP);
        helper.setBlock(new BlockPos(2, 1, 0), Blocks.STONE);
        helper.setBlock(new BlockPos(2, 2, 0), Blocks.SEA_LANTERN);
        gloam.lookAt(EntityAnchorArgument.Anchor.EYES, helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 2.0, 0.0)));
        helper.assertTrue(AbilityService.useAbility(gloam, 1) == AbilityService.Outcome.FAILED, "too bright to step into");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void snuffPutsOutLightsAndBringsThemBack(GameTestHelper helper) {
        ServerPlayer gloam = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.GLOAM, KitLadder.SNUFF);
        helper.setBlock(new BlockPos(0, 1, 0), Blocks.TORCH);
        helper.setBlock(new BlockPos(4, 1, 4), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        helper.assertTrue(AbilityService.useAbility(gloam, 1) == AbilityService.Outcome.USED, "Snuff fires");
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(0, 1, 0));
        helper.assertFalse(helper.getBlockState(new BlockPos(4, 1, 4)).getValue(CampfireBlock.LIT), "the campfire is out");
        helper.assertTrue(AbilityService.useAbility(gloam, 1) == AbilityService.Outcome.FAILED, "one darkness at a time");
        AbilityRegistry.get(SnuffAbility.ID).orElseThrow().clear(gloam);
        helper.assertBlockPresent(Blocks.TORCH, new BlockPos(0, 1, 0));
        helper.assertTrue(helper.getBlockState(new BlockPos(4, 1, 4)).getValue(CampfireBlock.LIT), "the campfire burns again");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void sunstruckStopsTheRefillOnlyInOpenDaylight(GameTestHelper helper) {
        ServerPlayer gloam = AbilityGameTest.playerWith(helper, AspectKitCatalog.GLOAM);
        float regen = EssenceService.regenPerTick(gloam);
        helper.assertTrue(SignatureKitPassives.sunstruck(gloam) == (regen == 0.0F),
                "no refill exactly when sunstruck: " + SignatureKitPassives.sunstruck(gloam) + ", " + regen);
        helper.succeed();
    }

    // ---- Rime ----------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void frostMarksStackFromBlowsUpToFive(GameTestHelper helper) {
        ServerPlayer rime = AbilityGameTest.playerWith(helper, AspectKitCatalog.RIME);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 0));
        zombie.setHealth(zombie.getMaxHealth());
        for (int i = 0; i < 7; i++) {
            zombie.invulnerableTime = 0;
            zombie.hurt(rime.damageSources().playerAttack(rime), 0.5F);
        }
        helper.assertTrue(SignatureKitPassives.marks(zombie) == SignatureKitPassives.MAX_MARKS,
                "five marks at most: " + SignatureKitPassives.marks(zombie));
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void iceLanceHurtsAndLeavesTwoMarks(GameTestHelper helper) {
        ServerPlayer rime = AbilityGameTest.playerWith(helper, AspectKitCatalog.RIME);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 0));
        rime.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.getEyePosition());
        helper.assertTrue(AbilityService.useAbility(rime, 0) == AbilityService.Outcome.USED, "Ice Lance fires");
        helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth(), "it hurts");
        helper.assertTrue(SignatureKitPassives.marks(zombie) == IceLanceAbility.MARKS, "two marks");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void encaseFreezesACreatureInRealIce(GameTestHelper helper) {
        ServerPlayer rime = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.RIME, KitLadder.ENCASE);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 0));
        rime.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.getEyePosition());
        helper.assertTrue(AbilityService.useAbility(rime, 1) == AbilityService.Outcome.USED, "Encase fires");
        helper.assertTrue(EncaseAbility.encased(zombie), "it is frozen");
        helper.assertFalse(EncaseAbility.shellOf(zombie).isEmpty(), "inside a shell");
        helper.assertTrue(EncaseAbility.shellOf(zombie).stream().allMatch(pos -> helper.getLevel().getBlockState(pos).is(Blocks.ICE)),
                "of real ice");
        float health = rime.getHealth();
        rime.hurt(rime.damageSources().mobAttack(zombie), 4.0F);
        helper.assertTrue(rime.getHealth() == health, "a frozen creature lands no blows");
        var shell = EncaseAbility.shellOf(zombie);
        EncaseAbility.thawAll(helper.getLevel().getServer());
        helper.assertTrue(shell.stream().noneMatch(pos -> helper.getLevel().getBlockState(pos).is(Blocks.ICE)), "it melts away");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void rimepathFreezesStillWaterUnderfoot(GameTestHelper helper) {
        ServerPlayer rime = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.RIME, KitLadder.RIMEPATH);
        BlockPos under = rime.blockPosition().below();
        helper.getLevel().setBlockAndUpdate(under.east(), Blocks.WATER.defaultBlockState());
        helper.assertTrue(AbilityService.useAbility(rime, 1) == AbilityService.Outcome.USED, "Rimepath fires");
        helper.assertTrue(helper.getLevel().getBlockState(under.east()).is(Blocks.FROSTED_ICE), "the water froze");
        AbilityRegistry.get(RimepathAbility.ID).orElseThrow().clear(rime);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void thinBloodFeelsTheHeat(GameTestHelper helper) {
        ServerPlayer rime = AbilityGameTest.playerWith(helper, AspectKitCatalog.RIME);
        helper.assertTrue(SignatureKitPassives.measureHeat(rime) == SignatureKitPassives.Heat.NONE, "nothing hot yet");
        helper.assertTrue(EssenceService.regenPerTick(rime) > 0.0F, "the refill runs");
        BlockPos fire = rime.blockPosition().offset(4, 0, 0);
        helper.getLevel().setBlockAndUpdate(fire, Blocks.MAGMA_BLOCK.defaultBlockState());
        helper.assertTrue(SignatureKitPassives.measureHeat(rime) == SignatureKitPassives.Heat.NEAR, "magma four blocks off");
        helper.getLevel().setBlockAndUpdate(rime.blockPosition().offset(1, 0, 0),
                Blocks.FURNACE.defaultBlockState().setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.LIT, true));
        helper.assertTrue(SignatureKitPassives.measureHeat(rime) == SignatureKitPassives.Heat.CLOSE, "a lit furnace beside you");
        SignatureKitPassives.heat(rime, SignatureKitPassives.Heat.NEAR);
        helper.assertTrue(EssenceService.regenPerTick(rime) == 0.0F, "no refill near heat");
        SignatureKitPassives.forget(rime.getUUID());
        helper.succeed();
    }

    // ---- Fury ----------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void hitsFillTheRageThatGrowsAFury(GameTestHelper helper) {
        ServerPlayer fury = AbilityGameTest.playerWith(helper, AspectKitCatalog.FURY);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 0));
        double damage = fury.getAttributeValue(Attributes.ATTACK_DAMAGE);
        fury.hurt(fury.damageSources().mobAttack(zombie), 4.0F);
        helper.assertTrue(FuryRage.rage(fury) >= 4.0F * FuryRage.PER_DAMAGE_TAKEN - 0.01F, "rage: " + FuryRage.rage(fury));
        helper.assertTrue(fury.getAttributeValue(Attributes.ATTACK_DAMAGE) > damage, "and the blows get heavier");
        helper.assertTrue(fury.getAttributeValue(Attributes.SCALE) > 1.0, "and the Fury bigger");
        FuryRage.forget(fury.getUUID());
        FuryRage.grow(fury, 0.0F);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void redHazeTakesHoldAtFullRageAndLiftsAsItCools(GameTestHelper helper) {
        ServerPlayer fury = AbilityGameTest.playerWith(helper, AspectKitCatalog.FURY);
        FuryRage.set(fury, FuryRage.MAX, true);
        helper.assertTrue(FuryRage.hazed(fury), "Red Haze at full rage");
        net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start eat =
                new net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start(fury, new ItemStack(Items.BREAD), 32);
        FuryRage.onUseItem(eat);
        helper.assertTrue(eat.isCanceled(), "no eating");
        FuryRage.set(fury, 80.0F, false);
        helper.assertTrue(FuryRage.hazed(fury), "still hazed above the line");
        FuryRage.set(fury, FuryRage.HAZE_CLEARS, false);
        helper.assertFalse(FuryRage.hazed(fury), "it lifts once cooled");
        FuryRage.forget(fury.getUUID());
        FuryRage.grow(fury, 0.0F);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void provokeTurnsHostilesAndRaisesRage(GameTestHelper helper) {
        ServerPlayer fury = AbilityGameTest.playerWith(helper, AspectKitCatalog.FURY);
        helper.assertTrue(AbilityService.useAbility(fury, 0) == AbilityService.Outcome.FAILED, "nothing to provoke");
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
        helper.assertTrue(AbilityService.useAbility(fury, 0) == AbilityService.Outcome.USED, "Provoke fires");
        helper.assertTrue(zombie.getTarget() == fury, "the zombie turns on the Fury");
        helper.assertTrue(FuryRage.rage(fury) >= ProvokeAbility.RAGE, "the rage rises");
        FuryRage.forget(fury.getUUID());
        FuryRage.grow(fury, 0.0F);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void groundSlamHitsHarderWithRageAndSpendsHalf(GameTestHelper helper) {
        ServerPlayer fury = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.FURY, KitLadder.GROUND_SLAM);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        FuryRage.set(fury, 80.0F, true);
        helper.assertTrue(AbilityService.useAbility(fury, 1) == AbilityService.Outcome.USED, "Ground Slam fires");
        float lost = zombie.getMaxHealth() - zombie.getHealth();
        helper.assertTrue(Math.abs(lost - GroundSlamAbility.damageAt(0.8F)) < 0.01F, "rage adds to the slam: " + lost);
        helper.assertTrue(Math.abs(FuryRage.rage(fury) - 40.0F) < 0.01F, "half the rage is spent: " + FuryRage.rage(fury));
        FuryRage.forget(fury.getUUID());
        FuryRage.grow(fury, 0.0F);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void secondWindBurnsRageIntoHealth(GameTestHelper helper) {
        ServerPlayer fury = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.FURY, KitLadder.SECOND_WIND);
        helper.assertTrue(AbilityService.useAbility(fury, 1) == AbilityService.Outcome.FAILED, "no rage to burn");
        fury.setHealth(4.0F);
        FuryRage.set(fury, 60.0F, true);
        helper.assertTrue(AbilityService.useAbility(fury, 1) == AbilityService.Outcome.USED, "Second Wind fires");
        helper.assertTrue(Math.abs(fury.getHealth() - 10.0F) < 0.01F, "six health back: " + fury.getHealth());
        helper.assertTrue(FuryRage.rage(fury) == 0.0F, "all rage spent");
        FuryRage.forget(fury.getUUID());
        FuryRage.grow(fury, 0.0F);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void gloamRimeAndFuryHaveOfferingsTheSpellWants(GameTestHelper helper) {
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.BLACK_DYE)).orElse("").equals("gloam"), "black dye leans Gloam");
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.SNOWBALL)).orElse("").equals("rime"), "a snowball leans Rime");
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.GOAT_HORN)).orElse("").equals("fury"), "a goat horn leans Fury");
        helper.succeed();
    }
}
