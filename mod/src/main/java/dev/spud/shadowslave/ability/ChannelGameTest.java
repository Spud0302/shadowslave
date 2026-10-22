package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.AwakeningService;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.CoreService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ChannelGameTest {
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void channelingWaitsForAwakening(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        helper.assertTrue(ChannelService.channel(player, false) == ChannelService.Outcome.NOT_AWAKENED, "Dormant cannot channel");
        helper.assertTrue(EssenceService.current(player) == EssenceService.DORMANT_CAPACITY, "no essence spent");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void channeledLeapBurstsUpAndForgivesTheFall(GameTestHelper helper) {
        ServerPlayer player = awakened(helper, AspectKitCatalog.MIST);
        ServerPlayer grounded = awakened(helper, AspectKitCatalog.STONE);
        player.setYRot(-90.0F);
        player.setXRot(0.0F);
        helper.assertTrue(ChannelService.channel(player, false) == ChannelService.Outcome.LEAPT, "should leap");
        helper.assertTrue(player.getDeltaMovement().y == ChannelService.LEAP_UP && player.getDeltaMovement().x > 1.0,
                "up and east: " + player.getDeltaMovement());
        helper.assertTrue(EssenceService.current(player) == EssenceService.AWAKENED_CAPACITY - ChannelService.LEAP_COST,
                "a leap costs essence");

        player.causeFallDamage(8.0F, 1.0F, player.damageSources().fall());
        grounded.causeFallDamage(8.0F, 1.0F, grounded.damageSources().fall());
        helper.assertTrue(player.getHealth() == player.getMaxHealth(), "the leap's own height is forgiven");
        helper.assertTrue(grounded.getHealth() < grounded.getMaxHealth(), "an ordinary fall still hurts");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void channeledStrikeHitsHarderOnce(GameTestHelper helper) {
        ServerPlayer player = awakened(helper, AspectKitCatalog.TIDE);
        Zombie plain = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        Zombie struck = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
        Zombie after = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 1, 4));

        plain.hurt(player.damageSources().playerAttack(player), 4.0F);
        helper.assertTrue(ChannelService.channel(player, true) == ChannelService.Outcome.PRIMED, "sneak-channel primes");
        helper.assertTrue(AbilityService.status(player).strikePrimed(), "the HUD shows the loaded blow");
        helper.assertTrue(ChannelService.channel(player, true) == ChannelService.Outcome.ALREADY_PRIMED, "one at a time");
        struck.hurt(player.damageSources().playerAttack(player), 4.0F);
        after.hurt(player.damageSources().playerAttack(player), 4.0F);

        float plainLoss = plain.getMaxHealth() - plain.getHealth();
        float struckLoss = struck.getMaxHealth() - struck.getHealth();
        float afterLoss = after.getMaxHealth() - after.getHealth();
        helper.assertTrue(struckLoss > plainLoss * 1.5F, "heavier: " + struckLoss + " vs " + plainLoss);
        helper.assertTrue(afterLoss == plainLoss, "only the next blow: " + afterLoss);
        helper.assertFalse(ChannelService.strikePrimed(player), "spent");
        helper.succeed();
    }

    private static ServerPlayer awakened(GameTestHelper helper, AspectKit kit) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, kit);
        CoreService.set(player, Integer.MAX_VALUE);
        if (AwakeningService.awaken(player, KitLadder.variants(kit.natureId(), SoulRank.AWAKENED)
                .get(0).abilityId()) != AwakeningService.Result.AWAKENED) {
            throw new IllegalStateException("Could not Awaken");
        }
        player.setHealth(player.getMaxHealth());
        return player;
    }
}
