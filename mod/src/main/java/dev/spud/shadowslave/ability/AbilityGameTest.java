package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.AspectRoller;
import dev.spud.shadowslave.awakening.PlaystyleEvidence;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AbilityGameTest {
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void kindleLightsNearbyHostilesAndSpendsEssence(GameTestHelper helper) {
        ServerPlayer player = playerWith(helper, AspectKitCatalog.EMBER);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 3));
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(1, 1, 1));

        helper.assertTrue(AbilityService.useAbility(player, 0) == AbilityService.Outcome.USED, "Kindle should fire");
        helper.assertTrue(player.hasEffect(MobEffects.NIGHT_VISION), "night vision");
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED), "speed");
        helper.assertTrue(zombie.isOnFire(), "zombie should be alight");
        helper.assertFalse(pig.isOnFire(), "pig is not hostile");
        float left = EssenceService.current(player);
        helper.assertTrue(left == EssenceService.DORMANT_CAPACITY - KindleAbility.ESSENCE_COST, "essence left " + left);
        helper.assertTrue(AbilityService.useAbility(player, 0) == AbilityService.Outcome.USED, "a full pool covers a second Kindle");
        helper.assertTrue(AbilityService.useAbility(player, 0) == AbilityService.Outcome.DRAINED, "the third is short of essence");
        helper.assertTrue(AbilityService.useAbility(player, 1) == AbilityService.Outcome.NO_ABILITY, "no second ability yet");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void abilityKeyDoesNothingWithoutAnAspect(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(AbilityService.useAbility(player, 0) == AbilityService.Outcome.NO_ASPECT, "no Aspect yet");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void mistPassageBlinksAheadAndCostsHunger(GameTestHelper helper) {
        ServerPlayer player = playerWith(helper, AspectKitCatalog.MIST);
        player.setYRot(-90.0F);
        player.setXRot(0.0F);
        double startX = player.getX();
        int food = player.getFoodData().getFoodLevel();

        helper.assertTrue(AbilityService.useAbility(player, 0) == AbilityService.Outcome.USED, "Mist Passage should fire");
        helper.assertTrue(player.getX() - startX >= MistPassageAbility.MIN_DISTANCE, "should blink east, moved " + (player.getX() - startX));
        helper.assertTrue(player.hasEffect(MobEffects.INVISIBILITY), "invisible");
        helper.assertTrue(player.getFoodData().getFoodLevel() == food - MistPassageAbility.HUNGER_COST, "Borrowed Breath hunger");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void holdfastShrugsOffKnockback(GameTestHelper helper) {
        ServerPlayer player = playerWith(helper, AspectKitCatalog.STONE);
        helper.assertTrue(AbilityService.useAbility(player, 0) == AbilityService.Outcome.USED, "Holdfast should fire");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_RESISTANCE), "resistance");
        player.setDeltaMovement(Vec3.ZERO);
        player.knockback(1.0, 1.0, 0.0);
        helper.assertTrue(player.getDeltaMovement().lengthSqr() == 0.0, "no knockback while braced");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void returningTideMarksThenPullsBack(GameTestHelper helper) {
        ServerPlayer player = playerWith(helper, AspectKitCatalog.TIDE);
        Vec3 mark = player.position();
        helper.assertTrue(AbilityService.useAbility(player, 0) == AbilityService.Outcome.PRIMED, "first press marks");
        helper.assertTrue(AbilityService.status(player).slots().get(0).primed(), "HUD shows the mark");
        player.teleportTo(mark.x + 2.0, mark.y, mark.z + 2.0);
        helper.assertTrue(AbilityService.useAbility(player, 0) == AbilityService.Outcome.USED, "second press returns");
        helper.assertTrue(player.position().distanceTo(mark) < 0.01, "back at the mark");
        AbilityStatusPayload status = AbilityService.status(player);
        helper.assertTrue(!status.slots().get(0).primed() && status.slots().get(0).abilityName().equals("Returning Tide")
                && status.essence() == EssenceService.DORMANT_CAPACITY - ReturningTideAbility.ESSENCE_COST,
                "HUD shows the spent essence: " + status);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void emberHalvesFireDamage(GameTestHelper helper) {
        ServerPlayer ember = playerWith(helper, AspectKitCatalog.EMBER);
        ServerPlayer stone = playerWith(helper, AspectKitCatalog.STONE);
        ember.hurt(ember.damageSources().lava(), 4.0F);
        stone.hurt(stone.damageSources().lava(), 4.0F);
        helper.assertTrue(ember.getMaxHealth() - ember.getHealth() == 2.0F, "Ember took " + (ember.getMaxHealth() - ember.getHealth()));
        helper.assertTrue(stone.getMaxHealth() - stone.getHealth() == 4.0F, "Stone took " + (stone.getMaxHealth() - stone.getHealth()));
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void tideBreathesUnderwaterAndMistRunsOutFaster(GameTestHelper helper) {
        for (int y = 1; y <= 3; y++) {
            helper.setBlock(new BlockPos(2, y, 2), Blocks.WATER);
        }
        ServerPlayer tide = playerWith(helper, AspectKitCatalog.TIDE);
        ServerPlayer mist = playerWith(helper, AspectKitCatalog.MIST);
        ServerPlayer stone = playerWith(helper, AspectKitCatalog.STONE);
        for (int tick = 0; tick < 20; tick++) {
            for (ServerPlayer player : new ServerPlayer[] {tide, mist, stone}) {
                player.tickCount = tick;
                player.baseTick();
            }
        }
        helper.assertTrue(tide.getAirSupply() == tide.getMaxAirSupply(), "Tide keeps its air");
        int stoneLost = stone.getMaxAirSupply() - stone.getAirSupply();
        int mistLost = mist.getMaxAirSupply() - mist.getAirSupply();
        helper.assertTrue(stoneLost == 20 && mistLost == 30, "air lost: stone " + stoneLost + ", mist " + mistLost);
        helper.succeed();
    }

    public static ServerPlayer playerWith(GameTestHelper helper, AspectKit kit) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 1, 2.5)));
        SoulIdentityService.replace(player, identityFor(kit));
        SoulService.awakenThroughRitual(player, ResourceLocation.fromNamespaceAndPath("shadowslave", "test/aspect"),
                SoulRank.DORMANT, ResourceLocation.fromNamespaceAndPath("shadowslave", "test/flaw"));
        EssenceService.fill(player);
        try {
            // Fresh players are briefly immune to damage; mock players never tick it away.
            Field spawnInvulnerable = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            spawnInvulnerable.setAccessible(true);
            spawnInvulnerable.setInt(player, 0);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
        return player;
    }

    private static SoulIdentityData identityFor(AspectKit kit) {
        PlaystyleEvidence.Totals none = new PlaystyleEvidence.Totals(0, 0, 0, 0);
        for (long seed = 0; ; seed++) {
            AspectRoller.Roll roll = AspectRoller.roll(seed, PlaystyleEvidence.fromTotals(none));
            if (roll.kit() == kit) {
                return roll.identity();
            }
        }
    }
}
