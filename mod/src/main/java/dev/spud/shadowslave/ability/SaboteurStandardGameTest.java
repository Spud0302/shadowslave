package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.citadel.WarNatures;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Set;

/** Saboteur and Standard away from any war: each innate, ability and Flaw doing its one thing. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SaboteurStandardGameTest {
    private static final String ROOM = "ritual_test_room";

    // ---- Saboteur ------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void undermineCracksStoneButNotObsidian(GameTestHelper helper) {
        ServerPlayer saboteur = AbilityGameTest.playerWith(helper, AspectKitCatalog.SABOTEUR);
        BlockPos centre = new BlockPos(2, 1, 4);
        helper.setBlock(centre, Blocks.STONE);
        helper.setBlock(centre.above(), Blocks.STONE);
        helper.setBlock(centre.east(), Blocks.OBSIDIAN);
        saboteur.lookAt(EntityAnchorArgument.Anchor.EYES, helper.absoluteVec(Vec3.atCenterOf(centre)));
        helper.assertTrue(AbilityService.useAbility(saboteur, 0) == AbilityService.Outcome.USED, "the charge is set");
        helper.assertTrue(SaboteurCraft.charge(saboteur).filter(helper.absolutePos(centre)::equals).isPresent(), "on the stone looked at");
        helper.assertTrue(AbilityService.useAbility(saboteur, 0) == AbilityService.Outcome.FAILED, "one charge at a time");
        helper.assertTrue(SaboteurCraft.fuse(saboteur) == SaboteurCraft.FUSE_TICKS, "a slow fuse");
        helper.assertTrue(SaboteurCraft.goNow(saboteur).filter(WarNatures.Undermined.WILD::equals).isPresent(), "out in the wild");
        helper.assertBlockPresent(Blocks.AIR, centre);
        helper.assertBlockPresent(Blocks.AIR, centre.above());
        helper.assertBlockPresent(Blocks.OBSIDIAN, centre.east());
        helper.assertFalse(saboteur.hasEffect(MobEffects.GLOWING), "cracking wild stone breaks no one's structure");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void markedGlowsAfterBreakingAndCutTheLinesHurriesTheCharge(GameTestHelper helper) {
        ServerPlayer saboteur = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.SABOTEUR, KitLadder.CUT_THE_LINES);
        ServerPlayer other = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        SaboteurCraft.marked(saboteur);
        SaboteurCraft.marked(other);
        helper.assertTrue(saboteur.hasEffect(MobEffects.GLOWING), "a Marked Saboteur glows");
        helper.assertFalse(other.hasEffect(MobEffects.GLOWING), "no one else does");
        helper.assertTrue(SaboteurCraft.cutsTheLines(saboteur), "Cut the Lines is always on");
        helper.assertTrue(SaboteurCraft.fuse(saboteur) == SaboteurCraft.FUSE_TICKS / 2, "and its charges burn in half the time");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void falseTrailHidesYourWayAndLuresYourHunters(GameTestHelper helper) {
        ServerPlayer saboteur = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.SABOTEUR, KitLadder.FALSE_TRAIL);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 4));
        zombie.setTarget(saboteur);
        saboteur.lookAt(EntityAnchorArgument.Anchor.EYES, helper.absoluteVec(new Vec3(4.5, 1.5, 2.5)));
        helper.assertFalse(SaboteurCraft.hidden(saboteur), "seen at first");
        helper.assertTrue(AbilityService.useAbility(saboteur, 1) == AbilityService.Outcome.USED, "the trail is laid");
        helper.assertTrue(SaboteurCraft.hidden(saboteur), "your way is hidden");
        helper.assertTrue(MasqueFace.lured(zombie) && zombie.getTarget() == null, "the zombie follows the false trail");
        helper.assertTrue(AbilityService.useAbility(saboteur, 1) == AbilityService.Outcome.FAILED, "one trail at a time");
        helper.succeed();
    }

    // ---- Standard ------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void underTheColoursWardsYourAlliesNearYou(GameTestHelper helper) {
        ServerPlayer standard = AbilityGameTest.playerWith(helper, AspectKitCatalog.STANDARD);
        ServerPlayer other = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        Wolf ours = tamed(helper, standard, new BlockPos(1, 1, 4));
        Wolf theirs = tamed(helper, other, new BlockPos(3, 1, 4));
        helper.assertTrue(taken(ours, 10.0F) == 9.0F, "your wolf takes a tenth less");
        helper.assertTrue(taken(theirs, 10.0F) == 10.0F, "someone else's does not");
        float health = standard.getHealth();
        standard.hurt(helper.getLevel().damageSources().magic(), 4.0F);
        helper.assertTrue(health - standard.getHealth() == 4.0F, "you are not your own ally");
        standard.teleportTo(helper.getLevel(), standard.getX() + 30, standard.getY(), standard.getZ(), Set.of(), 0, 0);
        helper.assertTrue(taken(ours, 10.0F) == 10.0F, "only near you");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aPlantedStandardWardsItsGroundAndLastRallyHealsWhenItFalls(GameTestHelper helper) {
        ServerPlayer standard = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.STANDARD, KitLadder.LAST_RALLY);
        Wolf wolf = tamed(helper, standard, new BlockPos(3, 1, 3));
        // The test room has no floor of its own; give the banner ground to stand on.
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
        helper.assertTrue(AbilityService.useAbility(standard, 0) == AbilityService.Outcome.USED, "planted");
        BlockPos banner = StandardColours.planted(standard).orElseThrow(() -> new AssertionError("a banner stands"));
        helper.assertTrue(helper.getLevel().getBlockState(banner).is(Blocks.PURPLE_BANNER), "the purple Standard");
        helper.assertTrue(AbilityService.useAbility(standard, 0) == AbilityService.Outcome.FAILED, "one Standard at a time");
        standard.teleportTo(helper.getLevel(), standard.getX() + 30, standard.getY(), standard.getZ(), Set.of(), 0, 0);
        helper.assertTrue(taken(wolf, 10.0F) == 9.0F, "its ground is warded with you away");
        wolf.setHealth(4.0F);
        helper.getLevel().removeBlock(banner, false);
        StandardColours.watch(helper.getLevel().getServer());
        helper.assertTrue(StandardColours.planted(standard).isEmpty(), "struck down");
        helper.assertTrue(wolf.getHealth() >= 4.0F + StandardColours.RALLY_HEAL - 0.01F, "Last Rally heals: " + wolf.getHealth());
        helper.assertTrue(wolf.hasEffect(MobEffects.ABSORPTION), "and steadies");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void holdTheLineStopsKnockbackNearTheBanner(GameTestHelper helper) {
        ServerPlayer standard = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.STANDARD, KitLadder.HOLD_THE_LINE);
        Wolf wolf = tamed(helper, standard, new BlockPos(3, 1, 3));
        Wolf stray = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(1, 1, 3));
        // The test room has no floor of its own; give the banner ground to stand on.
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
        helper.assertTrue(AbilityService.useAbility(standard, 0) == AbilityService.Outcome.USED, "planted");
        wolf.setDeltaMovement(Vec3.ZERO);
        wolf.knockback(1.0, 1.0, 0.0);
        helper.assertTrue(wolf.getDeltaMovement().horizontalDistanceSqr() == 0.0, "yours hold their ground");
        stray.setDeltaMovement(Vec3.ZERO);
        stray.knockback(1.0, 1.0, 0.0);
        helper.assertTrue(stray.getDeltaMovement().horizontalDistanceSqr() > 0.0, "a stray is knocked back");
        StandardColours.lower(helper.getLevel().getServer(), standard.getUUID());
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void oathboundWeakensYouForWalkingAwayFromTheFight(GameTestHelper helper) {
        ServerPlayer standard = AbilityGameTest.playerWith(helper, AspectKitCatalog.STANDARD);
        Wolf wolf = tamed(helper, standard, new BlockPos(3, 1, 3));
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 1, 4));
        // The test room has no floor of its own; give the banner ground to stand on.
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
        helper.assertTrue(AbilityService.useAbility(standard, 0) == AbilityService.Outcome.USED, "planted");
        standard.teleportTo(helper.getLevel(), standard.getX() + 30, standard.getY(), standard.getZ(), Set.of(), 0, 0);
        StandardColours.watch(helper.getLevel().getServer());
        helper.assertFalse(standard.hasEffect(MobEffects.WEAKNESS), "no fight, no oath to break");
        wolf.hurt(helper.getLevel().damageSources().mobAttack(zombie), 2.0F);
        StandardColours.watch(helper.getLevel().getServer());
        helper.assertTrue(standard.hasEffect(MobEffects.WEAKNESS), "your wolf fights under your Standard and you walk away");
        StandardColours.lower(helper.getLevel().getServer(), standard.getUUID());
        helper.succeed();
    }

    static Wolf tamed(GameTestHelper helper, ServerPlayer owner, BlockPos at) {
        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, at);
        wolf.tame(owner);
        return wolf;
    }

    /** How much of a magic blow lands on the creature. */
    static float taken(Wolf wolf, float damage) {
        wolf.setHealth(wolf.getMaxHealth());
        wolf.invulnerableTime = 0;
        float before = wolf.getHealth();
        wolf.hurt(wolf.level().damageSources().magic(), damage);
        return Math.round((before - wolf.getHealth()) * 100.0F) / 100.0F;
    }
}
