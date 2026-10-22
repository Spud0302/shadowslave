package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AbilityGrants;
import dev.spud.shadowslave.awakening.AbilityVariant;
import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** The Awakened ability variants: granted on the second key, each doing its one thing. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AwakenedAbilityGameTest {
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void grantedVariantTakesTheSecondKey(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        helper.assertFalse(AbilityGrants.grant(player, KitLadder.VEIL), "Veil is a Mist ability");
        helper.assertTrue(AbilityGrants.grant(player, KitLadder.EMBER_WARD), "Ember Ward is an Ember ability");
        helper.assertFalse(AbilityGrants.grant(player, KitLadder.EMBER_WARD), "granted only once");
        AbilityStatusPayload status = AbilityService.status(player);
        helper.assertTrue(status.slots().size() == 2, "two slots: " + status);
        helper.assertTrue(status.slots().get(0).abilityName().equals("Kindle"), "Kindle stays on the first key");
        helper.assertTrue(status.slots().get(1).abilityName().equals("Ember Ward"), "the new ability is on the second key");
        helper.assertTrue(AbilityService.useAbility(player, 1) == AbilityService.Outcome.USED, "the second key fires it");
        helper.assertTrue(EssenceService.current(player) == EssenceService.DORMANT_CAPACITY - EmberWardAbility.ESSENCE_COST,
                "it draws on the shared pool");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void everyNatureHasTwoRegisteredVariants(GameTestHelper helper) {
        for (AspectKit kit : List.of(AspectKitCatalog.EMBER, AspectKitCatalog.MIST, AspectKitCatalog.STONE, AspectKitCatalog.TIDE)) {
            List<AbilityVariant> variants = KitLadder.variants(kit.natureId(), SoulRank.AWAKENED);
            helper.assertTrue(variants.size() == 2, kit.natureName() + " has " + variants.size());
            for (AbilityVariant variant : variants) {
                helper.assertTrue(AbilityRegistry.isActivatable(variant.abilityId()), variant.name() + " is not registered");
            }
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void carryTheFlameWarmsYourselfWithNoAllyInSight(GameTestHelper helper) {
        ServerPlayer player = awakened(helper, AspectKitCatalog.EMBER, KitLadder.CARRY_THE_FLAME);
        player.setXRot(90.0F);
        helper.assertTrue(AbilityService.useAbility(player, 1) == AbilityService.Outcome.USED, "should fire");
        helper.assertTrue(player.hasEffect(MobEffects.REGENERATION), "regeneration");
        helper.assertTrue(player.hasEffect(MobEffects.FIRE_RESISTANCE), "fire resistance");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void emberWardBurnsHostilesInTheRing(GameTestHelper helper) {
        ServerPlayer player = awakened(helper, AspectKitCatalog.EMBER, KitLadder.EMBER_WARD);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 2));
        helper.assertTrue(AbilityService.useAbility(player, 1) == AbilityService.Outcome.USED, "should fire");
        AspectAbility ward = AbilityRegistry.get(EmberWardAbility.ID).orElseThrow();
        helper.succeedWhen(() -> {
            ward.tick(player);
            helper.assertTrue(zombie.isOnFire(), "zombie in the ring should burn");
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void throughTheHazeMakesCreaturesGlow(GameTestHelper helper) {
        ServerPlayer player = awakened(helper, AspectKitCatalog.MIST, KitLadder.THROUGH_THE_HAZE);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
        helper.assertTrue(AbilityService.useAbility(player, 1) == AbilityService.Outcome.USED, "should fire");
        helper.assertTrue(zombie.hasEffect(MobEffects.GLOWING), "zombie glows");
        helper.assertFalse(player.hasEffect(MobEffects.GLOWING), "the caster does not");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void veilBlindsHostilesInTheDome(GameTestHelper helper) {
        ServerPlayer player = awakened(helper, AspectKitCatalog.MIST, KitLadder.VEIL);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
        zombie.setTarget(player);
        helper.assertTrue(AbilityService.useAbility(player, 1) == AbilityService.Outcome.USED, "should fire");
        helper.assertTrue(zombie.hasEffect(MobEffects.BLINDNESS), "zombie is blind");
        helper.assertTrue(zombie.getTarget() == null, "zombie loses its prey");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void rootInStoneRaisesAWallThatCrumbles(GameTestHelper helper) {
        ServerPlayer player = awakened(helper, AspectKitCatalog.STONE, KitLadder.ROOT_IN_STONE);
        player.setYRot(-90.0F);
        player.setXRot(0.0F);
        helper.assertTrue(AbilityService.useAbility(player, 1) == AbilityService.Outcome.USED, "should fire");
        List<BlockPos> wall = RootInStoneAbility.wallOf(player);
        helper.assertFalse(wall.isEmpty(), "a wall should rise");
        for (BlockPos pos : wall) {
            helper.assertTrue(player.serverLevel().getBlockState(pos).is(Blocks.STONE), "stone at " + pos);
            helper.assertTrue(pos.getX() > player.getBlockX(), "the wall is ahead (east) of the player");
        }
        AbilityRegistry.get(RootInStoneAbility.ID).orElseThrow().clear(player);
        for (BlockPos pos : wall) {
            helper.assertTrue(player.serverLevel().getBlockState(pos).isAir(), "crumbled at " + pos);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void quakeHurtsAndSlowsNearbyHostiles(GameTestHelper helper) {
        ServerPlayer player = awakened(helper, AspectKitCatalog.STONE, KitLadder.QUAKE);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 2));
        helper.assertTrue(AbilityService.useAbility(player, 1) == AbilityService.Outcome.USED, "should fire");
        helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth(), "zombie is hurt");
        helper.assertTrue(zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "zombie is slowed");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void lowTideSurgesForward(GameTestHelper helper) {
        ServerPlayer player = awakened(helper, AspectKitCatalog.TIDE, KitLadder.LOW_TIDE);
        player.setYRot(-90.0F);
        player.setXRot(0.0F);
        helper.assertTrue(AbilityService.useAbility(player, 1) == AbilityService.Outcome.USED, "should fire");
        helper.assertTrue(player.getDeltaMovement().x > 1.0, "surging east: " + player.getDeltaMovement());
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void undertowSlowsMobsNearThePoint(GameTestHelper helper) {
        ServerPlayer player = awakened(helper, AspectKitCatalog.TIDE, KitLadder.UNDERTOW);
        player.setXRot(90.0F);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
        helper.assertTrue(AbilityService.useAbility(player, 1) == AbilityService.Outcome.USED, "should fire");
        helper.assertTrue(zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "zombie is caught in the undertow");
        helper.succeed();
    }

    private static ServerPlayer awakened(GameTestHelper helper, AspectKit kit, AbilityVariant variant) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, kit);
        if (!AbilityGrants.grant(player, variant)) {
            throw new IllegalStateException("Could not grant " + variant.name());
        }
        return player;
    }
}
