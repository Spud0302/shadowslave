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
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Threshold, Anchor and Hourglass: each signature rule, ability and Flaw doing its one thing. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PocketWeightTimeGameTest {
    private static final String ROOM = "ritual_test_room";

    // ---- Threshold -----------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void thePocketKeepsWhatYouPutInIt(GameTestHelper helper) {
        ServerPlayer threshold = AbilityGameTest.playerWith(helper, AspectKitCatalog.THRESHOLD);
        SimpleContainer pocket = PocketSpace.pocket(threshold);
        pocket.setItem(3, new ItemStack(Items.DIAMOND, 5));
        SimpleContainer again = PocketSpace.pocket(threshold);
        helper.assertTrue(again.getItem(3).is(Items.DIAMOND) && again.getItem(3).getCount() == 5, "the diamonds are still there");
        helper.assertTrue(AbilityService.useAbility(threshold, 0) == AbilityService.Outcome.USED, "Pocket opens");
        helper.assertTrue(threshold.containerMenu != threshold.inventoryMenu, "a pocket menu is open");
        threshold.closeContainer();
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void unmooredCostsMaxHealthUntilYouSleep(GameTestHelper helper) {
        ServerPlayer threshold = AbilityGameTest.playerWith(helper, AspectKitCatalog.THRESHOLD);
        double full = threshold.getMaxHealth();
        for (int i = 0; i < 8; i++) {
            PocketSpace.doorOpened(threshold);
        }
        helper.assertTrue(PocketSpace.toll(threshold) == PocketSpace.MAX_TOLL, "the toll caps: " + PocketSpace.toll(threshold));
        helper.assertTrue(Math.abs(threshold.getMaxHealth() - (full - PocketSpace.MAX_TOLL)) < 0.01, "max health " + threshold.getMaxHealth());
        PocketSpace.moor(threshold);
        helper.assertTrue(Math.abs(threshold.getMaxHealth() - full) < 0.01, "rested, it comes back");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void doorwayStepsYouFromOneDoorToTheOther(GameTestHelper helper) {
        ServerPlayer threshold = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.THRESHOLD, KitLadder.DOORWAY);
        Vec3 first = helper.absoluteVec(new Vec3(0.5, 1, 0.5));
        Vec3 second = helper.absoluteVec(new Vec3(4.5, 1, 4.5));
        threshold.teleportTo(first.x, first.y, first.z);
        helper.assertTrue(AbilityService.useAbility(threshold, 1) == AbilityService.Outcome.PRIMED, "the first door opens");
        threshold.teleportTo(second.x, second.y, second.z);
        helper.assertTrue(AbilityService.useAbility(threshold, 1) == AbilityService.Outcome.USED, "its twin opens");
        helper.assertTrue(DoorwayAbility.open(threshold), "the doors stand open");
        DoorwayAbility.stepNow(threshold);
        helper.assertTrue(threshold.position().distanceTo(first) < 0.5, "stepped through to the first door: " + threshold.position());
        AbilityRegistry.get(DoorwayAbility.ID).orElseThrow().clear(threshold);
        PocketSpace.moor(threshold);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM, timeoutTicks = 200)
    public static void banishTakesACreatureAwayAndBringsItBack(GameTestHelper helper) {
        ServerPlayer threshold = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.THRESHOLD, KitLadder.BANISH);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 0));
        java.util.UUID id = zombie.getUUID();
        threshold.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.getEyePosition());
        helper.assertTrue(AbilityService.useAbility(threshold, 1) == AbilityService.Outcome.USED, "Banish fires");
        helper.assertTrue(zombie.isRemoved() && PocketSpace.banished(id), "the zombie is gone");
        helper.runAfterDelay(BanishAbility.AWAY_TICKS + 5, () -> {
            helper.assertTrue(helper.getLevel().getEntity(id) != null, "and back again");
            PocketSpace.moor(threshold);
            helper.succeed();
        });
    }

    // ---- Anchor --------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void shiftWeightCyclesHeavyLightNormal(GameTestHelper helper) {
        ServerPlayer anchor = AbilityGameTest.playerWith(helper, AspectKitCatalog.ANCHOR);
        double gravity = anchor.getAttributeValue(Attributes.GRAVITY);
        helper.assertTrue(AbilityService.useAbility(anchor, 0) == AbilityService.Outcome.USED, "a shift");
        helper.assertTrue(AnchorWeight.weight(anchor) == AnchorWeight.Weight.HEAVY, "heavy first");
        helper.assertTrue(anchor.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) >= 1.0, "no knockback");
        helper.assertTrue(anchor.getAttributeValue(Attributes.GRAVITY) > gravity, "and falls harder");
        EssenceService.fill(anchor);
        AbilityService.useAbility(anchor, 0);
        helper.assertTrue(AnchorWeight.weight(anchor) == AnchorWeight.Weight.LIGHT, "then light");
        helper.assertTrue(anchor.getAttributeValue(Attributes.GRAVITY) < gravity, "falling softer");
        helper.assertTrue(anchor.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) < 1.0, "knockback is back");
        EssenceService.fill(anchor);
        AbilityService.useAbility(anchor, 0);
        helper.assertTrue(AnchorWeight.weight(anchor) == AnchorWeight.Weight.NORMAL, "then normal");
        helper.assertTrue(Math.abs(anchor.getAttributeValue(Attributes.GRAVITY) - gravity) < 1.0e-6, "as before");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void leadenBoatsWillNotBearYou(GameTestHelper helper) {
        ServerPlayer anchor = AbilityGameTest.playerWith(helper, AspectKitCatalog.ANCHOR);
        Boat boat = EntityType.BOAT.create(helper.getLevel());
        EntityMountEvent mount = new EntityMountEvent(anchor, boat, helper.getLevel(), true);
        AnchorWeight.onMount(mount);
        helper.assertTrue(mount.isCanceled(), "the boat refuses an Anchor");
        ServerPlayer other = AbilityGameTest.playerWith(helper, AspectKitCatalog.MIST);
        EntityMountEvent fine = new EntityMountEvent(other, boat, helper.getLevel(), true);
        AnchorWeight.onMount(fine);
        helper.assertFalse(fine.isCanceled(), "anyone else rides");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void crushTurnsAFallIntoAShockwave(GameTestHelper helper) {
        ServerPlayer anchor = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.ANCHOR, KitLadder.CRUSH);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 2));
        helper.assertTrue(AbilityService.useAbility(anchor, 1) == AbilityService.Outcome.USED, "Crush is ready");
        LivingFallEvent fall = new LivingFallEvent(anchor, 8.0F, 1.0F);
        CrushAbility.onFall(fall);
        helper.assertTrue(fall.getDamageMultiplier() == 0.0F, "the fall cannot hurt");
        helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth(), "the shockwave does");
        AbilityRegistry.get(CrushAbility.ID).orElseThrow().clear(anchor);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void gravityWellPinsHostilesAroundWhereYouLook(GameTestHelper helper) {
        ServerPlayer anchor = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.ANCHOR, KitLadder.GRAVITY_WELL);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
        anchor.lookAt(EntityAnchorArgument.Anchor.EYES, helper.absoluteVec(new Vec3(0.5, 0.0, 0.5)));
        helper.assertTrue(AbilityService.useAbility(anchor, 1) == AbilityService.Outcome.USED, "the well opens");
        helper.assertTrue(zombie.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN), "the zombie is pinned");
        AbilityRegistry.get(GravityWellAbility.ID).orElseThrow().clear(anchor);
        helper.succeed();
    }

    // ---- Hourglass -----------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void stasisBanksBlowsAndLandsThemAQuarterHarder(GameTestHelper helper) {
        ServerPlayer hourglass = AbilityGameTest.playerWith(helper, AspectKitCatalog.HOURGLASS);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 0));
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        hourglass.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.getEyePosition());
        helper.assertTrue(AbilityService.useAbility(hourglass, 0) == AbilityService.Outcome.USED, "Stasis fires");
        helper.assertTrue(Stasis.held(zombie), "held out of time");
        zombie.hurt(hourglass.damageSources().playerAttack(hourglass), 4.0F);
        zombie.invulnerableTime = 0;
        zombie.hurt(hourglass.damageSources().playerAttack(hourglass), 4.0F);
        helper.assertTrue(zombie.getHealth() == zombie.getMaxHealth(), "nothing lands yet");
        helper.assertTrue(Stasis.banked(zombie) >= 7.99F, "but it is banked: " + Stasis.banked(zombie));
        float health = hourglass.getHealth();
        hourglass.hurt(hourglass.damageSources().mobAttack(zombie), 4.0F);
        helper.assertTrue(hourglass.getHealth() == health, "a held creature cannot strike");
        Stasis.releaseAll(helper.getLevel().getServer());
        float lost = zombie.getMaxHealth() - zombie.getHealth();
        helper.assertTrue(Math.abs(lost - 8.0F * Stasis.BANK_BONUS) < 0.1F, "all at once, a quarter harder: " + lost);
        helper.assertTrue(EssenceService.regenPerTick(hourglass) == 0.0F, "Borrowed Time stops the refill");
        Stasis.forget(hourglass.getUUID());
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void rewindReturnsYouAndYourHealth(GameTestHelper helper) {
        ServerPlayer hourglass = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.HOURGLASS, KitLadder.REWIND);
        helper.assertTrue(AbilityService.useAbility(hourglass, 1) == AbilityService.Outcome.FAILED, "no past yet");
        Vec3 then = hourglass.position();
        RewindAbility.remember(hourglass);
        hourglass.teleportTo(then.x + 3.0, then.y, then.z + 1.0);
        hourglass.setHealth(5.0F);
        helper.assertTrue(AbilityService.useAbility(hourglass, 1) == AbilityService.Outcome.USED, "Rewind fires");
        helper.assertTrue(hourglass.position().distanceTo(then) < 0.1, "back where you were");
        helper.assertTrue(hourglass.getHealth() == hourglass.getMaxHealth(), "with the health you had");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void stillMomentHoldsEveryHostileAround(GameTestHelper helper) {
        ServerPlayer hourglass = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.HOURGLASS, KitLadder.STILL_MOMENT);
        helper.assertTrue(AbilityService.useAbility(hourglass, 1) == AbilityService.Outcome.FAILED, "nothing to hold");
        Zombie a = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
        Zombie b = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
        helper.assertTrue(AbilityService.useAbility(hourglass, 1) == AbilityService.Outcome.USED, "Still Moment fires");
        helper.assertTrue(Stasis.held(a) && Stasis.held(b), "both held");
        Stasis.releaseAll(helper.getLevel().getServer());
        Stasis.forget(hourglass.getUUID());
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void thresholdAnchorAndHourglassHaveOfferings(GameTestHelper helper) {
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.BUNDLE)).orElse("").equals("threshold"), "a bundle leans Threshold");
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.LODESTONE)).orElse("").equals("anchor"), "a lodestone leans Anchor");
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.SAND)).orElse("").equals("hourglass"), "sand leans Hourglass");
        helper.succeed();
    }
}
