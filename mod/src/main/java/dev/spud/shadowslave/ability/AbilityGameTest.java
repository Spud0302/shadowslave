package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.AspectRoller;
import dev.spud.shadowslave.awakening.PlaystyleEvidence;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AbilityGameTest {
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void kindleLightsNearbyHostilesAndThenRecovers(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 1, 2.5)));
        SoulIdentityService.replace(player, emberIdentity());
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 3));
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(1, 1, 1));

        helper.assertTrue(AbilityService.useAbility(player) == AbilityService.Outcome.USED, "Kindle should fire");
        helper.assertTrue(player.hasEffect(MobEffects.NIGHT_VISION), "night vision");
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED), "speed");
        helper.assertTrue(zombie.isOnFire(), "zombie should be alight");
        helper.assertFalse(pig.isOnFire(), "pig is not hostile");
        helper.assertTrue(AbilityService.useAbility(player) == AbilityService.Outcome.RECOVERING, "second press recovers");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void abilityKeyDoesNothingWithoutAnAspect(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(AbilityService.useAbility(player) == AbilityService.Outcome.NO_ASPECT, "no Aspect yet");
        helper.succeed();
    }

    private static SoulIdentityData emberIdentity() {
        PlaystyleEvidence.Totals none = new PlaystyleEvidence.Totals(0, 0, 0, 0);
        for (long seed = 0; ; seed++) {
            AspectRoller.Roll roll = AspectRoller.roll(seed, PlaystyleEvidence.fromTotals(none));
            if (roll.kit() == AspectKitCatalog.EMBER) {
                return roll.identity();
            }
        }
    }
}
