package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.IntentionService;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.block.ModBlocks;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Wayfarer and Lull: each innate, ability and Flaw doing its one thing. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DreamKitGameTest {
    private static final String ROOM = "ritual_test_room";

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void waymarkKeepsThreeAndSnapsBackToTheNewest(GameTestHelper helper) {
        ServerPlayer wayfarer = AbilityGameTest.playerWith(helper, AspectKitCatalog.WAYFARER);
        helper.assertTrue(AbilityService.useAbility(wayfarer, 0) == AbilityService.Outcome.USED, "a mark is set");
        for (int i = 0; i < 3; i++) {
            EssenceService.fill(wayfarer);
            AbilityService.useAbility(wayfarer, 0);
        }
        helper.assertTrue(WaymarkAbility.marksHere(wayfarer).size() == WaymarkAbility.MAX_MARKS, "three at most");
        BlockPos newest = wayfarer.blockPosition();
        wayfarer.teleportTo(wayfarer.getX() + 2.0, wayfarer.getY(), wayfarer.getZ() - 2.0);
        wayfarer.setShiftKeyDown(true);
        EssenceService.fill(wayfarer);
        helper.assertTrue(AbilityService.useAbility(wayfarer, 0) == AbilityService.Outcome.USED, "the snap fires");
        helper.assertTrue(wayfarer.blockPosition().equals(newest), "back at the mark: " + wayfarer.blockPosition());
        helper.assertTrue(WaymarkAbility.marksHere(wayfarer).size() == 2, "the mark is spent");
        AbilityRegistry.get(WaymarkAbility.ID).orElseThrow().clear(wayfarer);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void waymarkSnapFailsWithNoMark(GameTestHelper helper) {
        ServerPlayer wayfarer = AbilityGameTest.playerWith(helper, AspectKitCatalog.WAYFARER);
        wayfarer.setShiftKeyDown(true);
        helper.assertTrue(AbilityService.useAbility(wayfarer, 0) == AbilityService.Outcome.FAILED, "nowhere to snap to");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM, timeoutTicks = 120)
    public static void anchorRecallChannelsHomeToYourBed(GameTestHelper helper) {
        ServerPlayer wayfarer = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.WAYFARER, KitLadder.ANCHOR_RECALL);
        helper.assertTrue(AbilityService.useAbility(wayfarer, 1) == AbilityService.Outcome.FAILED, "no bed yet");
        BlockPos bed = helper.absolutePos(new BlockPos(0, 1, 0));
        wayfarer.setRespawnPosition(wayfarer.level().dimension(), bed, 0.0F, true, false);
        wayfarer.moveTo(helper.absoluteVec(new Vec3(3.5, 1, 3.5)));
        helper.assertTrue(AbilityService.useAbility(wayfarer, 1) == AbilityService.Outcome.PRIMED, "the channel starts");
        AspectAbility recall = AbilityRegistry.get(AnchorRecallAbility.ID).orElseThrow();
        helper.runAfterDelay(AnchorRecallAbility.CHANNEL_TICKS + 2, () -> {
            recall.tick(wayfarer);
            helper.assertTrue(wayfarer.position().distanceTo(Vec3.atBottomCenterOf(bed)) < 3.0, "home: " + wayfarer.position());
            helper.assertFalse(recall.primed(wayfarer), "the channel is over");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aHitBreaksTheRecallForFree(GameTestHelper helper) {
        ServerPlayer wayfarer = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.WAYFARER, KitLadder.ANCHOR_RECALL);
        wayfarer.setRespawnPosition(wayfarer.level().dimension(), helper.absolutePos(new BlockPos(0, 1, 0)), 0.0F, true, false);
        float essence = EssenceService.current(wayfarer);
        helper.assertTrue(AbilityService.useAbility(wayfarer, 1) == AbilityService.Outcome.PRIMED, "the channel starts");
        wayfarer.hurtTime = 5;
        AspectAbility recall = AbilityRegistry.get(AnchorRecallAbility.ID).orElseThrow();
        recall.tick(wayfarer);
        helper.assertFalse(recall.primed(wayfarer), "broken");
        helper.assertTrue(EssenceService.current(wayfarer) >= essence - 0.5F, "and nothing spent");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void trailblazeHastensAlliesOnTheTrail(GameTestHelper helper) {
        ServerPlayer wayfarer = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.WAYFARER, KitLadder.TRAILBLAZE);
        ServerPlayer friend = AbilityGameTest.playerWith(helper, AspectKitCatalog.MIST);
        friend.moveTo(helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
        helper.assertTrue(AbilityService.useAbility(wayfarer, 1) == AbilityService.Outcome.USED, "Trailblaze fires");
        TrailblazeAbility.layAndHasten(wayfarer, friend.position());
        helper.assertTrue(friend.hasEffect(MobEffects.MOVEMENT_SPEED), "the friend on the trail runs faster");
        AbilityRegistry.get(TrailblazeAbility.ID).orElseThrow().clear(wayfarer);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void wanderlustSlowsTheRefillOfAWayfarerWhoLingers(GameTestHelper helper) {
        ServerPlayer wayfarer = AbilityGameTest.playerWith(helper, AspectKitCatalog.WAYFARER);
        float fresh = EssenceService.regenPerTick(wayfarer);
        DreamKitPassives.lingered(wayfarer, DreamKitPassives.WANDER_GRACE_TICKS + DreamKitPassives.WANDER_FADE_TICKS);
        float lingering = EssenceService.regenPerTick(wayfarer);
        helper.assertTrue(Math.abs(lingering - fresh * DreamKitPassives.WANDER_FLOOR) < 1.0E-4F,
                "a quarter after lingering: " + fresh + " -> " + lingering);
        DreamKitPassives.forget(wayfarer.getUUID());
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void lullabyPutsALowerRankedCreatureToSleepUntilItIsHurt(GameTestHelper helper) {
        ServerPlayer lull = AbilityGameTest.playerWith(helper, AspectKitCatalog.LULL);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 0));
        lull.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.getEyePosition());
        helper.assertTrue(AbilityService.useAbility(lull, 0) == AbilityService.Outcome.USED, "Lullaby fires");
        helper.assertTrue(Slumber.asleep(zombie), "the zombie sleeps");
        float health = lull.getHealth();
        lull.hurt(lull.damageSources().mobAttack(zombie), 4.0F);
        helper.assertTrue(lull.getHealth() == health, "a sleeper lands no blows");
        zombie.hurt(lull.damageSources().playerAttack(lull), 1.0F);
        helper.assertFalse(Slumber.asleep(zombie), "damage wakes it");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void lullabyCannotTouchAStrongerSoul(GameTestHelper helper) {
        ServerPlayer lull = AbilityGameTest.playerWith(helper, AspectKitCatalog.LULL);
        WitherSkeleton skeleton = helper.spawnWithNoFreeWill(EntityType.WITHER_SKELETON, new BlockPos(2, 1, 0));
        lull.lookAt(EntityAnchorArgument.Anchor.EYES, skeleton.getEyePosition());
        helper.assertTrue(AbilityService.useAbility(lull, 0) == AbilityService.Outcome.FAILED, "a Fallen soul shrugs it off");
        helper.assertFalse(Slumber.asleep(skeleton), "awake");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void drowsyAuraMakesEveryFourthBlowFizzle(GameTestHelper helper) {
        ServerPlayer lull = AbilityGameTest.playerWith(helper, AspectKitCatalog.LULL);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 1));
        int landed = 0;
        for (int i = 0; i < 8; i++) {
            lull.invulnerableTime = 0;
            lull.setHealth(lull.getMaxHealth());
            lull.hurt(lull.damageSources().mobAttack(zombie), 1.0F);
            if (lull.getHealth() < lull.getMaxHealth()) {
                landed++;
            }
        }
        helper.assertTrue(landed == 6, "six of eight land: " + landed);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void dreamfallLullsEverythingAround(GameTestHelper helper) {
        ServerPlayer lull = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.LULL, KitLadder.DREAMFALL);
        helper.assertTrue(AbilityService.useAbility(lull, 1) == AbilityService.Outcome.FAILED, "nothing to lull");
        Zombie a = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
        Zombie b = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
        helper.assertTrue(AbilityService.useAbility(lull, 1) == AbilityService.Outcome.USED, "Dreamfall fires");
        helper.assertTrue(Slumber.asleep(a) && Slumber.asleep(b), "both sleep");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void sleepwalkerSteersOnlyASleeper(GameTestHelper helper) {
        ServerPlayer lull = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.LULL, KitLadder.SLEEPWALKER);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 0));
        lull.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.getEyePosition());
        helper.assertTrue(AbilityService.useAbility(lull, 1) == AbilityService.Outcome.FAILED, "it is awake");
        AbilityService.useAbility(lull, 0);
        EssenceService.fill(lull);
        helper.assertTrue(AbilityService.useAbility(lull, 1) == AbilityService.Outcome.USED, "Sleepwalker takes hold");
        helper.assertTrue(Slumber.asleep(zombie) && Slumber.steered(zombie), "it walks in its sleep");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void insomniaRefusesABedWithoutACandle(GameTestHelper helper) {
        ServerPlayer lull = AbilityGameTest.playerWith(helper, AspectKitCatalog.LULL);
        BlockPos bed = helper.absolutePos(new BlockPos(1, 1, 1));
        CanPlayerSleepEvent plain = new CanPlayerSleepEvent(lull, bed, null);
        DreamKitPassives.onCanSleep(plain);
        helper.assertTrue(plain.getProblem() == Player.BedSleepingProblem.OTHER_PROBLEM, "no sleep in a plain bed");
        helper.setBlock(new BlockPos(2, 1, 1), ModBlocks.DREAM_CANDLE.get().defaultBlockState().setValue(CandleBlock.LIT, true));
        CanPlayerSleepEvent candlelit = new CanPlayerSleepEvent(lull, bed, null);
        DreamKitPassives.onCanSleep(candlelit);
        helper.assertTrue(candlelit.getProblem() == null, "a Dream Candle lets you rest");
        ServerPlayer other = AbilityGameTest.playerWith(helper, AspectKitCatalog.MIST);
        CanPlayerSleepEvent anyone = new CanPlayerSleepEvent(other, helper.absolutePos(new BlockPos(4, 1, 4)), null);
        DreamKitPassives.onCanSleep(anyone);
        helper.assertTrue(anyone.getProblem() == null, "everyone else sleeps as usual");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void wayfarerAndLullHaveOfferingsTheSpellWants(GameTestHelper helper) {
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.SADDLE)).orElse("").equals("wayfarer"), "a saddle leans Wayfarer");
        helper.assertTrue(IntentionService.tagFor(new ItemStack(Items.WHITE_BED)).orElse("").equals("lull"), "a bed leans Lull");
        helper.succeed();
    }
}
