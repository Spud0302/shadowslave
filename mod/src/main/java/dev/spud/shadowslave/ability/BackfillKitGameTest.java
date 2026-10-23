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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Warden, Seer, Reaper and Wellspring: each innate, ability and Flaw doing its one thing. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BackfillKitGameTest {
    private static final String ROOM = "ritual_test_room";

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void markPreyGlowsAndTakesMoreWhileQuarryLockHalvesTheRest(GameTestHelper helper) {
        ServerPlayer warden = AbilityGameTest.playerWith(helper, AspectKitCatalog.WARDEN);
        Pig prey = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(4, 1, 2));
        Pig other = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(2, 1, 4));
        warden.lookAt(EntityAnchorArgument.Anchor.EYES, prey.getEyePosition());
        helper.assertTrue(AbilityService.useAbility(warden, 0) == AbilityService.Outcome.USED, "Mark Prey fires");
        helper.assertTrue(MarkPreyAbility.markOf(warden).orElse(null) == prey, "the pig is marked");
        helper.assertTrue(prey.hasEffect(MobEffects.GLOWING), "the mark glows");
        prey.hurt(warden.damageSources().playerAttack(warden), 4.0F);
        other.hurt(warden.damageSources().playerAttack(warden), 4.0F);
        helper.assertTrue(Math.abs(prey.getHealth() - (prey.getMaxHealth() - 6.0F)) < 0.01F, "marked takes 6: " + prey.getHealth());
        helper.assertTrue(Math.abs(other.getHealth() - (other.getMaxHealth() - 2.0F)) < 0.01F, "Quarry Lock halves: " + other.getHealth());
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void snareSigilRootsTheFirstHostileToCrossIt(GameTestHelper helper) {
        ServerPlayer warden = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.WARDEN, KitLadder.SNARE_SIGIL);
        warden.setXRot(90.0F);
        helper.assertTrue(AbilityService.useAbility(warden, 1) == AbilityService.Outcome.USED, "Snare Sigil fires");
        helper.assertTrue(SnareSigilAbility.hasSigil(warden), "a sigil is drawn");
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
        AspectAbility sigil = AbilityRegistry.get(SnareSigilAbility.ID).orElseThrow();
        helper.succeedWhen(() -> {
            sigil.tick(warden);
            helper.assertTrue(zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "the zombie is rooted");
            helper.assertFalse(SnareSigilAbility.hasSigil(warden), "the sigil is spent");
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void relentlessHuntDashesToTheMark(GameTestHelper helper) {
        ServerPlayer warden = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.WARDEN, KitLadder.RELENTLESS_HUNT);
        helper.assertTrue(AbilityService.useAbility(warden, 1) == AbilityService.Outcome.FAILED, "no mark, no hunt");
        warden.moveTo(helper.absoluteVec(new Vec3(0.5, 1, 0.5)));
        Pig prey = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(4, 1, 4));
        MarkPreyAbility.mark(warden, prey);
        helper.assertTrue(AbilityService.useAbility(warden, 1) == AbilityService.Outcome.USED, "the hunt fires");
        helper.assertTrue(warden.distanceTo(prey) < 2.6F, "beside the prey: " + warden.distanceTo(prey));
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void foresightOpensLightsTheHudAndRevealsHostiles(GameTestHelper helper) {
        ServerPlayer seer = AbilityGameTest.playerWith(helper, AspectKitCatalog.SEER);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
        helper.assertTrue(AbilityService.useAbility(seer, 0) == AbilityService.Outcome.USED, "Foresight fires");
        helper.assertTrue(ForesightAbility.active(seer), "Foresight is open");
        helper.assertTrue(AbilityService.status(seer).slots().get(0).primed(), "lit on the HUD, which lifts Clouded Eyes");
        helper.assertTrue(zombie.hasEffect(MobEffects.GLOWING), "hostiles glow");
        float before = EssenceService.current(seer);
        helper.assertTrue(AbilityService.useAbility(seer, 0) == AbilityService.Outcome.FAILED, "already open");
        helper.assertTrue(EssenceService.current(seer) == before, "a second press costs nothing");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM, timeoutTicks = 120)
    public static void fateThreadSnapsBackThreeSeconds(GameTestHelper helper) {
        ServerPlayer seer = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.SEER, KitLadder.FATE_THREAD);
        AspectAbility thread = AbilityRegistry.get(FateThreadAbility.ID).orElseThrow();
        Vec3 start = seer.position();
        helper.onEachTick(() -> thread.tick(seer));
        helper.runAtTickTime(70, () -> seer.teleportTo(start.x + 3, start.y, start.z + 3));
        helper.runAtTickTime(75, () -> {
            helper.assertTrue(AbilityService.useAbility(seer, 1) == AbilityService.Outcome.USED, "Fate Thread fires");
            helper.assertTrue(seer.position().distanceTo(start) < 0.5, "back at the start: " + seer.position());
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void sharedSightMakesHostilesGlow(GameTestHelper helper) {
        ServerPlayer seer = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.SEER, KitLadder.SHARED_SIGHT);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
        helper.assertTrue(AbilityService.useAbility(seer, 1) == AbilityService.Outcome.USED, "Shared Sight fires");
        helper.assertTrue(SharedSightAbility.active(seer), "sight is shared");
        helper.assertTrue(zombie.hasEffect(MobEffects.GLOWING), "hostiles glow");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM, timeoutTicks = 100)
    public static void soulSiphonDrainsTheCreatureAndFeedsTheReaper(GameTestHelper helper) {
        ServerPlayer reaper = AbilityGameTest.playerWith(helper, AspectKitCatalog.REAPER);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 2));
        reaper.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.getEyePosition());
        EssenceService.spend(reaper, 50);
        helper.assertTrue(AbilityService.useAbility(reaper, 0) == AbilityService.Outcome.USED, "Soul Siphon fires");
        float afterCast = EssenceService.current(reaper);
        AspectAbility siphon = AbilityRegistry.get(SoulSiphonAbility.ID).orElseThrow();
        helper.succeedWhen(() -> {
            siphon.tick(reaper);
            helper.assertFalse(SoulSiphonAbility.draining(reaper), "the drain runs its course");
            helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth(), "the zombie is drained");
            helper.assertTrue(EssenceService.current(reaper) > afterCast + SoulSiphonAbility.ESSENCE_COST,
                    "a full drain pays back more than it cost: " + EssenceService.current(reaper));
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void harvestDrainsEverythingAround(GameTestHelper helper) {
        ServerPlayer reaper = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.REAPER, KitLadder.HARVEST);
        Zombie first = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 2));
        Zombie second = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 4));
        EssenceService.spend(reaper, 50);
        helper.assertTrue(AbilityService.useAbility(reaper, 1) == AbilityService.Outcome.USED, "Harvest fires");
        helper.assertTrue(first.getHealth() < first.getMaxHealth() && second.getHealth() < second.getMaxHealth(), "both are drained");
        helper.assertTrue(EssenceService.current(reaper)
                        == EssenceService.DORMANT_CAPACITY - 50 - HarvestAbility.ESSENCE_COST + 2 * HarvestAbility.ESSENCE_PER_TARGET,
                "fed by both: " + EssenceService.current(reaper));
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void reapingArcFinishesTheBadlyWounded(GameTestHelper helper) {
        ServerPlayer reaper = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.REAPER, KitLadder.REAPING_ARC);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 2));
        zombie.setHealth(8.0F);
        reaper.setYRot(-90.0F);
        reaper.setXRot(0.0F);
        helper.assertTrue(AbilityService.useAbility(reaper, 1) == AbilityService.Outcome.USED, "Reaping Arc fires");
        helper.assertFalse(zombie.isAlive(), "the wounded zombie is reaped");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void soulFeastAndHungerShapeTheReapersPool(GameTestHelper helper) {
        ServerPlayer reaper = AbilityGameTest.playerWith(helper, AspectKitCatalog.REAPER);
        EssenceService.spend(reaper, 60);
        float drained = EssenceService.current(reaper);
        helper.assertTrue(EssenceService.regenPerTick(reaper) == 0.0F, "Hunger: no refill");
        EssenceService.tick(reaper);
        helper.assertTrue(EssenceService.current(reaper) == drained, "the pool stays where it was");
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 2));
        zombie.hurt(reaper.damageSources().playerAttack(reaper), 100.0F);
        helper.assertTrue(EssenceService.current(reaper) == drained + KitPassives.FEAST_BASE,
                "Soul Feast: " + EssenceService.current(reaper));
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void essenceGiftMendsYouWithNoAllyInSight(GameTestHelper helper) {
        ServerPlayer well = AbilityGameTest.playerWith(helper, AspectKitCatalog.WELLSPRING);
        well.setXRot(90.0F);
        well.setHealth(8.0F);
        helper.assertTrue(AbilityService.useAbility(well, 0) == AbilityService.Outcome.USED, "Essence Gift fires");
        helper.assertTrue(well.getHealth() >= 14.0F, "mended: " + well.getHealth());
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void soulWardShieldsYou(GameTestHelper helper) {
        ServerPlayer well = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.WELLSPRING, KitLadder.SOUL_WARD);
        well.setXRot(90.0F);
        helper.assertTrue(AbilityService.useAbility(well, 1) == AbilityService.Outcome.USED, "Soul Ward fires");
        helper.assertTrue(well.hasEffect(MobEffects.ABSORPTION), "warded");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM, timeoutTicks = 80)
    public static void overflowRegeneratesThoseInside(GameTestHelper helper) {
        ServerPlayer well = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.WELLSPRING, KitLadder.OVERFLOW);
        helper.assertTrue(AbilityService.useAbility(well, 1) == AbilityService.Outcome.USED, "Overflow fires");
        AspectAbility overflow = AbilityRegistry.get(OverflowAbility.ID).orElseThrow();
        helper.succeedWhen(() -> {
            overflow.tick(well);
            helper.assertTrue(well.hasEffect(MobEffects.REGENERATION), "regenerating inside the aura");
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void brimmingVesselSpillsOnAHeavyHit(GameTestHelper helper) {
        ServerPlayer well = AbilityGameTest.playerWith(helper, AspectKitCatalog.WELLSPRING);
        well.hurt(well.damageSources().generic(), 2.0F);
        helper.assertTrue(EssenceService.current(well) == EssenceService.DORMANT_CAPACITY, "a light hit spills nothing");
        well.invulnerableTime = 0;
        well.hurt(well.damageSources().generic(), 8.0F);
        helper.assertTrue(EssenceService.current(well) == EssenceService.DORMANT_CAPACITY * 0.75F,
                "a heavy hit spills a quarter: " + EssenceService.current(well));
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void everyNewNatureHasOfferingsTheSpellWants(GameTestHelper helper) {
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.BOW)).orElse("").equals("hunt"), "bow leans Warden");
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.SPYGLASS)).orElse("").equals("foresight"), "spyglass leans Seer");
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.ECHO_SHARD)).orElse("").equals("hunger"), "echo shard leans Reaper");
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.GOLDEN_APPLE)).orElse("").equals("giving"), "golden apple leans Wellspring");
        helper.succeed();
    }
}
