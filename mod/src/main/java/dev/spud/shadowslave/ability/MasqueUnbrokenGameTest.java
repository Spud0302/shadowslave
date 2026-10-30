package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.spell.SpellJudge;
import dev.spud.shadowslave.trial.content.NightmarePack;
import dev.spud.shadowslave.trial.content.TrialContent;
import dev.spud.shadowslave.trial.content.TrialSettings;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Masque and Unbroken: each innate, ability and Flaw doing its one thing. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MasqueUnbrokenGameTest {
    private static final String ROOM = "ritual_test_room";

    // ---- Masque --------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void roleWornFillsTheKitAndTheRolesReward(GameTestHelper helper) {
        ServerPlayer masque = AbilityGameTest.playerWith(helper, AspectKitCatalog.MASQUE);
        ServerPlayer other = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        List<ItemStack> kit = List.of(new ItemStack(Items.BREAD, 3), new ItemStack(Items.IRON_SWORD));
        List<ItemStack> worn = MasqueFace.roleKit(masque, kit);
        helper.assertTrue(worn.get(0).getCount() == 4, "one more bread: " + worn.get(0).getCount());
        helper.assertTrue(worn.get(1).getCount() == 1, "a sword doesn't stack");
        helper.assertTrue(MasqueFace.roleKit(other, kit).get(0).getCount() == 3, "only for a Masque");

        NightmarePack.Ending ending = new NightmarePack.Ending("vigil", NightmarePack.EndingKind.LIGHT, 0, List.of(), 90, "", "",
                Optional.of("lamplighter"), 40, "");
        TrialSettings settings = TrialContent.get().settings();
        SpellJudge.Run run = SpellJudge.Run.simple(Map.of(), 0, "lamplighter", 1, 0);
        SpellJudge.Judgement plain = SpellJudge.judge(ending, run, settings);
        helper.assertTrue(MasqueFace.trusted(masque, ending, run, plain, settings).score() == plain.score() + 20,
                "half the role's reward again");
        helper.assertTrue(MasqueFace.trusted(other, ending, run, plain, settings) == plain, "not for anyone else");
        SpellJudge.Run wrongRole = SpellJudge.Run.simple(Map.of(), 0, "watch_slave", 1, 0);
        SpellJudge.Judgement offRole = SpellJudge.judge(ending, wrongRole, settings);
        helper.assertTrue(MasqueFace.trusted(masque, ending, wrongRole, offRole, settings) == offRole, "only when the role was the one wanted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void borrowedFaceFoolsCreaturesUntilYouStrike(GameTestHelper helper) {
        ServerPlayer masque = AbilityGameTest.playerWith(helper, AspectKitCatalog.MASQUE);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 4));
        masque.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.getEyePosition());
        zombie.setTarget(masque);
        helper.assertTrue(AbilityService.useAbility(masque, 0) == AbilityService.Outcome.USED, "a face is borrowed");
        helper.assertTrue(MasqueFace.face(masque).map(id -> id.getPath().equals("zombie")).orElse(false), "a zombie's face");
        helper.assertTrue(zombie.getTarget() == null, "the zombie lets go of its prey");
        zombie.setTarget(masque);
        helper.assertTrue(zombie.getTarget() == null, "and won't take it again");
        zombie.hurt(helper.getLevel().damageSources().playerAttack(masque), 1.0F);
        helper.assertTrue(MasqueFace.face(masque).isEmpty(), "striking gives you away");
        zombie.setTarget(masque);
        helper.assertTrue(zombie.getTarget() == masque, "and the zombie can see you again");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void hollowReflectionShowsBesideStillWater(GameTestHelper helper) {
        ServerPlayer masque = AbilityGameTest.playerWith(helper, AspectKitCatalog.MASQUE);
        ServerPlayer other = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        helper.assertFalse(MasqueFace.revealed(masque), "nothing to see yourself in");
        float health = masque.getHealth();
        masque.hurt(helper.getLevel().damageSources().magic(), 4.0F);
        helper.assertTrue(Math.abs(health - masque.getHealth() - 4.0F) < 0.01F, "plain damage while hidden");
        helper.setBlock(new BlockPos(3, 1, 2), Blocks.WATER);
        helper.assertTrue(MasqueFace.revealed(masque), "still water shows you");
        masque.invulnerableTime = 0;
        health = masque.getHealth();
        masque.hurt(helper.getLevel().damageSources().magic(), 4.0F);
        helper.assertTrue(Math.abs(health - masque.getHealth() - 5.0F) < 0.01F, "a quarter more while seen: " + masque.getHealth());
        health = other.getHealth();
        other.hurt(helper.getLevel().damageSources().magic(), 4.0F);
        helper.assertTrue(Math.abs(health - other.getHealth() - 4.0F) < 0.01F, "only the Masque is hurt by it");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void mirrorSwapTradesPlacesAndDropsTheFace(GameTestHelper helper) {
        ServerPlayer masque = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.MASQUE, KitLadder.MIRROR_SWAP);
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(2, 1, 4));
        MasqueFace.wear(masque, EntityType.PIG);
        masque.lookAt(EntityAnchorArgument.Anchor.EYES, pig.position().add(0, 0.4, 0));
        Vec3 here = masque.position();
        Vec3 there = pig.position();
        helper.assertTrue(AbilityService.useAbility(masque, 1) == AbilityService.Outcome.USED, "swapped");
        helper.assertTrue(masque.position().distanceTo(there) < 0.1 && pig.position().distanceTo(here) < 0.1, "places traded");
        helper.assertTrue(MasqueFace.face(masque).isEmpty(), "using an ability gives you away");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void stolenVoiceLuresTheKind(GameTestHelper helper) {
        ServerPlayer masque = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.MASQUE, KitLadder.STOLEN_VOICE);
        Zombie near = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 1, 4));
        Zombie far = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 4));
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(0, 1, 0));
        masque.lookAt(EntityAnchorArgument.Anchor.EYES, helper.absoluteVec(new Vec3(2.5, 1.0, 4.0)));
        helper.assertTrue(AbilityService.useAbility(masque, 1) == AbilityService.Outcome.USED, "the call is thrown");
        helper.assertTrue(MasqueFace.lured(near) && MasqueFace.lured(far), "every zombie hears it");
        helper.assertFalse(MasqueFace.lured(pig), "but not other kinds");
        near.setTarget(masque);
        helper.assertTrue(near.getTarget() == null, "a lured creature hunts nothing");
        helper.succeed();
    }

    // ---- Unbroken ------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM, timeoutTicks = 200)
    public static void weatheredTurnsAsideMoreAsTheFightGoesOn(GameTestHelper helper) {
        ServerPlayer unbroken = AbilityGameTest.playerWith(helper, AspectKitCatalog.UNBROKEN);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 4));
        float health = unbroken.getHealth();
        unbroken.hurt(helper.getLevel().damageSources().indirectMagic(zombie, zombie), 4.0F);
        helper.assertTrue(Math.abs(health - unbroken.getHealth() - 4.0F) < 0.01F, "the first blow lands in full");
        helper.runAfterDelay(100, () -> {
            unbroken.setHealth(unbroken.getMaxHealth());
            unbroken.invulnerableTime = 0;
            float before = unbroken.getHealth();
            unbroken.hurt(helper.getLevel().damageSources().indirectMagic(zombie, zombie), 10.0F);
            float taken = before - unbroken.getHealth();
            helper.assertTrue(taken > 8.6F && taken < 9.4F, "five seconds in, a tenth turned aside: " + taken);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void lastStandRefusesOneKillingBlow(GameTestHelper helper) {
        ServerPlayer unbroken = AbilityGameTest.playerWith(helper, AspectKitCatalog.UNBROKEN);
        helper.assertTrue(AbilityService.useAbility(unbroken, 0) == AbilityService.Outcome.USED, "braced");
        helper.assertTrue(AbilityService.useAbility(unbroken, 0) == AbilityService.Outcome.FAILED, "once is enough");
        unbroken.hurt(helper.getLevel().damageSources().magic(), 100.0F);
        helper.assertTrue(unbroken.isAlive() && Math.abs(unbroken.getHealth() - UnbrokenWill.LAST_STAND_HEALTH) < 0.01F,
                "alive on one heart: " + unbroken.getHealth());
        helper.assertTrue(UnbrokenWill.resting(unbroken) > 0, "the will must mend");
        helper.assertTrue(AbilityService.useAbility(unbroken, 0) == AbilityService.Outcome.FAILED, "it can't be braced again yet");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void noRetreatDrainsEssenceRunningFromAFight(GameTestHelper helper) {
        ServerPlayer unbroken = AbilityGameTest.playerWith(helper, AspectKitCatalog.UNBROKEN);
        helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 0));
        Vec3 start = helper.absoluteVec(new Vec3(2.5, 1, 2.0));
        unbroken.teleportTo(start.x, start.y, start.z);
        UnbrokenWill.retreat(unbroken);
        float essence = EssenceService.current(unbroken);
        unbroken.setSprinting(true);
        unbroken.teleportTo(start.x, start.y, start.z + 2.0);
        UnbrokenWill.retreat(unbroken);
        helper.assertTrue(EssenceService.current(unbroken) < essence, "running away costs essence");
        essence = EssenceService.current(unbroken);
        unbroken.teleportTo(start.x, start.y, start.z);
        UnbrokenWill.retreat(unbroken);
        helper.assertTrue(EssenceService.current(unbroken) >= essence, "charging in costs nothing");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void gritHardensWhereYouStand(GameTestHelper helper) {
        ServerPlayer unbroken = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.UNBROKEN, KitLadder.GRIT);
        helper.assertTrue(AbilityService.useAbility(unbroken, 1) == AbilityService.Outcome.USED, "feet planted");
        float[] taken = new float[3];
        for (int i = 0; i < 3; i++) {
            unbroken.setHealth(unbroken.getMaxHealth());
            unbroken.invulnerableTime = 0;
            unbroken.hurt(helper.getLevel().damageSources().magic(), 10.0F);
            taken[i] = unbroken.getMaxHealth() - unbroken.getHealth();
        }
        helper.assertTrue(Math.abs(taken[0] - 10.0F) < 0.01F && Math.abs(taken[1] - 9.0F) < 0.01F && Math.abs(taken[2] - 8.0F) < 0.01F,
                "each blow hardens you: " + java.util.Arrays.toString(taken));
        unbroken.teleportTo(unbroken.getX(), unbroken.getY(), unbroken.getZ() + 2.0);
        unbroken.setHealth(unbroken.getMaxHealth());
        unbroken.invulnerableTime = 0;
        unbroken.hurt(helper.getLevel().damageSources().magic(), 10.0F);
        helper.assertTrue(Math.abs(unbroken.getMaxHealth() - unbroken.getHealth() - 10.0F) < 0.01F, "stepping away loses it");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void defianceHoldsEveryFoesAttention(GameTestHelper helper) {
        ServerPlayer unbroken = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.UNBROKEN, KitLadder.DEFIANCE);
        helper.assertTrue(AbilityService.useAbility(unbroken, 1) == AbilityService.Outcome.FAILED, "nothing to defy");
        Zombie first = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 1, 4));
        Zombie second = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 4));
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(0, 1, 0));
        helper.assertTrue(AbilityService.useAbility(unbroken, 1) == AbilityService.Outcome.USED, "the dare is made");
        helper.assertTrue(first.getTarget() == unbroken && second.getTarget() == unbroken, "every foe turns on you");
        first.setTarget(pig);
        helper.assertTrue(first.getTarget() == unbroken, "and can't turn away");
        helper.assertTrue(unbroken.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE), "braced against them");
        helper.succeed();
    }
}
