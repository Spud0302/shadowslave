package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.echoes.EchoCommands;
import dev.spud.shadowslave.echoes.EchoRecord;
import dev.spud.shadowslave.echoes.EchoService;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Shepherd and Vessel: each innate, ability and Flaw doing its one thing. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ShepherdVesselGameTest {
    private static final String ROOM = "ritual_test_room";

    // ---- Shepherd ------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void herdbondHalvesUpkeepAndOpensADormantSlot(GameTestHelper helper) {
        ServerPlayer shepherd = AbilityGameTest.playerWith(helper, AspectKitCatalog.SHEPHERD);
        ServerPlayer vessel = AbilityGameTest.playerWith(helper, AspectKitCatalog.VESSEL);
        ServerPlayer other = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        helper.assertTrue(EchoService.slots(shepherd) == 1, "a Dormant Shepherd holds one Echo: " + EchoService.slots(shepherd));
        helper.assertTrue(EchoService.slots(vessel) == 1, "so does a Dormant Vessel");
        helper.assertTrue(EchoService.slots(other) == 0, "nobody else does while Dormant");
        helper.assertTrue(ShepherdBond.upkeepShare(shepherd) == ShepherdBond.HERDBOND_UPKEEP, "half the upkeep");
        helper.assertTrue(ShepherdBond.upkeepShare(vessel) == 1.0F, "only for the Shepherd");
        summon(helper, shepherd, "zombie");
        helper.assertTrue(EchoService.summonedCount(shepherd) == 1, "and the Echo comes when called");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void secondBondHoldsOneEchoMore(GameTestHelper helper) {
        ServerPlayer shepherd = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.SHEPHERD, KitLadder.SECOND_BOND);
        helper.assertTrue(EchoService.slots(shepherd) == 2, "one slot more: " + EchoService.slots(shepherd));
        helper.assertTrue(AbilityService.useAbility(shepherd, 1) == AbilityService.Outcome.NO_ABILITY, "it takes no ability slot");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void rallySpeedsAndMendsEchoesAndPets(GameTestHelper helper) {
        ServerPlayer shepherd = AbilityGameTest.playerWith(helper, AspectKitCatalog.SHEPHERD);
        helper.assertTrue(AbilityService.useAbility(shepherd, 0) == AbilityService.Outcome.FAILED, "nobody to call");
        Mob echo = summon(helper, shepherd, "zombie");
        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(1, 1, 3));
        wolf.tame(shepherd);
        echo.setHealth(echo.getMaxHealth() - 10.0F);
        wolf.setHealth(wolf.getMaxHealth() - 6.0F);
        float echoHealth = echo.getHealth();
        float wolfHealth = wolf.getHealth();
        helper.assertTrue(AbilityService.useAbility(shepherd, 0) == AbilityService.Outcome.USED, "Rally is called");
        helper.assertTrue(echo.hasEffect(MobEffects.MOVEMENT_SPEED) && wolf.hasEffect(MobEffects.MOVEMENT_SPEED), "both run faster");
        helper.assertTrue(echo.getHealth() > echoHealth && wolf.getHealth() > wolfHealth, "and both mend");
        wolf.discard();
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void lonelySoulHitsWeakerWithNoEchoOut(GameTestHelper helper) {
        ServerPlayer shepherd = AbilityGameTest.playerWith(helper, AspectKitCatalog.SHEPHERD);
        Pig alone = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 1, 2));
        Pig together = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(1, 1, 2));
        alone.hurt(helper.getLevel().damageSources().playerAttack(shepherd), 5.0F);
        helper.assertTrue(Math.abs(alone.getMaxHealth() - alone.getHealth() - 4.0F) < 0.01F, "a fifth weaker alone: " + alone.getHealth());
        summon(helper, shepherd, "zombie");
        together.hurt(helper.getLevel().damageSources().playerAttack(shepherd), 5.0F);
        helper.assertTrue(Math.abs(together.getMaxHealth() - together.getHealth() - 5.0F) < 0.01F, "full strength with an Echo out");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void packTacticsSendsTheEchoToFlank(GameTestHelper helper) {
        ServerPlayer shepherd = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.SHEPHERD, KitLadder.PACK_TACTICS);
        Pig prey = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(2, 1, 2));
        Vec3 stand = helper.absoluteVec(new Vec3(2.5, 1, 0.5));
        shepherd.teleportTo(stand.x, stand.y, stand.z);
        shepherd.lookAt(EntityAnchorArgument.Anchor.EYES, prey.position().add(0, 0.4, 0));
        helper.assertTrue(AbilityService.useAbility(shepherd, 1) == AbilityService.Outcome.FAILED, "no Echo to flank with");
        Mob echo = summon(helper, shepherd, "zombie");
        shepherd.lookAt(EntityAnchorArgument.Anchor.EYES, prey.position().add(0, 0.4, 0));
        helper.assertTrue(AbilityService.useAbility(shepherd, 1) == AbilityService.Outcome.USED, "the pack is loosed");
        helper.assertTrue(echo.getTarget() == prey, "the Echo is set on the prey");
        helper.assertTrue(echo.distanceTo(prey) < 2.5F, "beside it: " + echo.distanceTo(prey));
        helper.assertTrue(echo.distanceToSqr(shepherd) > prey.distanceToSqr(shepherd), "on the far side");
        helper.assertTrue(echo.hasEffect(MobEffects.DAMAGE_BOOST), "fighting harder");
        helper.succeed();
    }

    // ---- Vessel --------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void borrowedTraitFollowsTheEchosKind(GameTestHelper helper) {
        helper.assertTrue(VesselBond.trait(EntityType.BLAZE.create(helper.getLevel())) == MobEffects.FIRE_RESISTANCE, "a blaze: fire");
        helper.assertTrue(VesselBond.trait(EntityType.COD.create(helper.getLevel())) == MobEffects.WATER_BREATHING, "a fish: water");
        helper.assertTrue(VesselBond.trait(EntityType.PARROT.create(helper.getLevel())) == MobEffects.SLOW_FALLING, "a bird: a soft fall");
        helper.assertTrue(VesselBond.trait(EntityType.SKELETON.create(helper.getLevel())) == MobEffects.NIGHT_VISION, "the undead: dark sight");
        helper.assertTrue(VesselBond.trait(EntityType.SPIDER.create(helper.getLevel())) == MobEffects.MOVEMENT_SPEED, "a quick creature: speed");
        helper.assertTrue(VesselBond.trait(EntityType.COW.create(helper.getLevel())) == MobEffects.DIG_SPEED, "anything else: haste");
        ServerPlayer vessel = AbilityGameTest.playerWith(helper, AspectKitCatalog.VESSEL);
        VesselBond.applyTrait(vessel);
        helper.assertFalse(vessel.hasEffect(MobEffects.NIGHT_VISION), "nothing without an Echo out");
        summon(helper, vessel, "zombie");
        VesselBond.applyTrait(vessel);
        helper.assertTrue(vessel.hasEffect(MobEffects.NIGHT_VISION), "a zombie Echo lends dark sight");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void echoMergeLendsTheEchosBodyAndHollowedLocksAbilities(GameTestHelper helper) {
        ServerPlayer vessel = AbilityGameTest.playerWith(helper, AspectKitCatalog.VESSEL);
        helper.assertTrue(AbilityService.useAbility(vessel, 0) == AbilityService.Outcome.FAILED, "nothing to merge with");
        Mob echo = summon(helper, vessel, "zombie");
        double attack = vessel.getAttributeValue(Attributes.ATTACK_DAMAGE);
        helper.assertTrue(AbilityService.useAbility(vessel, 0) == AbilityService.Outcome.USED, "merged");
        helper.assertTrue(VesselBond.merging(vessel), "the merge holds");
        helper.assertTrue(echo.isInvisible() && echo.isInvulnerable() && echo.isNoAi(), "the Echo is folded away");
        helper.assertTrue(vessel.getAttributeValue(Attributes.ATTACK_DAMAGE) > attack, "its strength is the Vessel's");
        helper.assertTrue(vessel.getArmorValue() >= echo.getArmorValue() && echo.getArmorValue() > 0, "and its hide: " + vessel.getArmorValue());
        helper.assertTrue(vessel.getEffect(MobEffects.NIGHT_VISION) != null && vessel.getEffect(MobEffects.NIGHT_VISION).getAmplifier() == 1,
                "its trait grows stronger");
        helper.assertTrue(AbilityService.useAbility(vessel, 0) == AbilityService.Outcome.FAILED, "Hollowed: abilities are locked");
        VesselBond.unmerge(vessel);
        helper.assertFalse(VesselBond.merging(vessel), "the merge ends");
        helper.assertFalse(echo.isInvisible() || echo.isInvulnerable() || echo.isNoAi(), "the Echo steps back out");
        helper.assertTrue(Math.abs(vessel.getAttributeValue(Attributes.ATTACK_DAMAGE) - attack) < 0.01, "the strength goes with it");

        VesselBond.merge(vessel, echo, 200);
        EchoService.dismissAll(vessel, true);
        VesselBond.holdMerge(vessel, new VesselBond.Merge(echo.getUUID(), Long.MAX_VALUE, false));
        helper.assertFalse(VesselBond.merging(vessel), "a dismissed Echo ends the merge");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void swapPlacesTradesPositionsWithTheEcho(GameTestHelper helper) {
        ServerPlayer vessel = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.VESSEL, KitLadder.SWAP_PLACES);
        helper.assertTrue(AbilityService.useAbility(vessel, 1) == AbilityService.Outcome.FAILED, "no Echo to trade with");
        Mob echo = summon(helper, vessel, "zombie");
        Vec3 there = helper.absoluteVec(new Vec3(4.5, 1, 4.5));
        echo.teleportTo(there.x, there.y, there.z);
        Vec3 here = vessel.position();
        helper.assertTrue(AbilityService.useAbility(vessel, 1) == AbilityService.Outcome.USED, "swapped");
        helper.assertTrue(vessel.position().distanceTo(there) < 0.1 && echo.position().distanceTo(here) < 0.1, "places traded");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void sharedWoundsSplitsEachBlowWithTheEcho(GameTestHelper helper) {
        ServerPlayer vessel = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.VESSEL, KitLadder.SHARED_WOUNDS);
        helper.assertTrue(AbilityService.useAbility(vessel, 1) == AbilityService.Outcome.FAILED, "no Echo to share with");
        Mob echo = summon(helper, vessel, "zombie");
        helper.assertTrue(AbilityService.useAbility(vessel, 1) == AbilityService.Outcome.USED, "wounds are shared");
        float health = vessel.getHealth();
        float echoHealth = echo.getHealth();
        vessel.hurt(helper.getLevel().damageSources().magic(), 10.0F);
        helper.assertTrue(Math.abs(health - vessel.getHealth() - 6.0F) < 0.01F, "the Vessel takes three fifths: " + vessel.getHealth());
        helper.assertTrue(Math.abs(echoHealth - echo.getHealth() - 4.0F) < 0.01F, "the Echo takes the rest: " + echo.getHealth());
        helper.succeed();
    }

    /** Binds an Echo of {@code entity}, takes it into the soul and summons it. */
    private static Mob summon(GameTestHelper helper, ServerPlayer player, String entity) {
        EchoRecord echo = EchoRecord.bind(EchoCommands.bodyFor(player, ResourceLocation.withDefaultNamespace(entity)).orElseThrow(),
                player.getGameProfile().getName());
        player.setItemInHand(InteractionHand.MAIN_HAND, EchoService.item(echo));
        int index = EchoService.held(player).size();
        if (EchoService.absorb(player, InteractionHand.MAIN_HAND) != EchoService.Outcome.DONE
                || EchoService.summon(player, index) != EchoService.Outcome.DONE) {
            throw new IllegalStateException("Could not summon a " + entity + " Echo");
        }
        return EchoService.summonedBodies(player).getLast();
    }
}
