package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AbilityGrants;
import dev.spud.shadowslave.awakening.AbilityVariant;
import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TransformationGameTest {
    /** A Saint of the kit's Nature who holds only the Dormant ability and the given form. */
    static ServerPlayer saintWith(GameTestHelper helper, AspectKit kit, AbilityVariant form) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, kit);
        SoulService.awaken(player);
        SoulService.ascend(player);
        SoulService.ascend(player);
        AbilityGrants.grant(player, form);
        EssenceService.fill(player);
        return player;
    }

    static int slotOf(ServerPlayer player, AbilityVariant variant) {
        var slots = AbilityService.status(player).slots();
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).abilityId().equals(variant.abilityId().toString())) {
                return i;
            }
        }
        throw new IllegalStateException(variant.name() + " is on no key");
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void colossusGrowsDrainsAndEndsOnASecondPress(GameTestHelper helper) {
        ServerPlayer player = saintWith(helper, AspectKitCatalog.STONE, KitLadder.COLOSSUS);
        ColossusForm form = (ColossusForm) AbilityRegistry.get(ColossusForm.ID).orElseThrow();
        int slot = slotOf(player, KitLadder.COLOSSUS);
        float full = EssenceService.current(player);
        helper.assertTrue(AbilityService.useAbility(player, slot) == AbilityService.Outcome.USED, "the form is taken");
        helper.assertTrue(form.transformed(player), "transformed");
        helper.assertTrue(player.getAttributeValue(Attributes.SCALE) > 1.4, "half again in size");
        helper.assertTrue(player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) >= 1.0, "immovable");
        float afterShift = EssenceService.current(player);
        helper.assertTrue(afterShift < full, "taking the form costs essence");
        for (int i = 0; i < TransformationAbility.TICKS_PER_DRAIN; i++) {
            form.tick(player);
        }
        helper.assertTrue(EssenceService.current(player) <= afterShift - ColossusForm.DRAIN_PER_SECOND + 0.01F,
                "holding it drains the pool");
        helper.assertTrue(AbilityService.useAbility(player, slot) == AbilityService.Outcome.FAILED, "a second press leaves it");
        helper.assertFalse(form.transformed(player), "back to their own shape");
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.SCALE) - 1.0) < 1.0e-3, "back to their own size");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void aFormFallsAwayWhenTheEssenceRunsDry(GameTestHelper helper) {
        ServerPlayer player = saintWith(helper, AspectKitCatalog.TIDE, KitLadder.TIDEBODY);
        TidebodyForm form = (TidebodyForm) AbilityRegistry.get(TidebodyForm.ID).orElseThrow();
        AbilityService.useAbility(player, slotOf(player, KitLadder.TIDEBODY));
        EssenceService.spend(player, (int) EssenceService.current(player));
        for (int i = 0; i < TransformationAbility.TICKS_PER_DRAIN; i++) {
            form.tick(player);
        }
        helper.assertFalse(form.transformed(player), "an empty pool ends the form");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void mistformLetsBlowsPassThroughBothWays(GameTestHelper helper) {
        ServerPlayer player = saintWith(helper, AspectKitCatalog.MIST, KitLadder.MISTFORM);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 3));
        AbilityService.useAbility(player, slotOf(player, KitLadder.MISTFORM));
        float health = player.getHealth();
        player.hurt(player.damageSources().mobAttack(zombie), 4.0F);
        helper.assertTrue(player.getHealth() == health, "a zombie's blow passes through mist");
        player.hurt(player.damageSources().fall(), 6.0F);
        helper.assertTrue(player.getHealth() == health, "mist does not fall");
        float zombieHealth = zombie.getHealth();
        zombie.hurt(player.damageSources().playerAttack(player), 4.0F);
        helper.assertTrue(zombie.getHealth() == zombieHealth, "mist cannot strike");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void livingFlameCannotBurn(GameTestHelper helper) {
        ServerPlayer player = saintWith(helper, AspectKitCatalog.EMBER, KitLadder.LIVING_FLAME);
        LivingFlameForm form = (LivingFlameForm) AbilityRegistry.get(LivingFlameForm.ID).orElseThrow();
        AbilityService.useAbility(player, slotOf(player, KitLadder.LIVING_FLAME));
        // Mock players count as creative, so flight itself is checked by hand in play.
        helper.assertTrue(form.transformed(player), "the flame takes shape");
        float health = player.getHealth();
        player.hurt(player.damageSources().inFire(), 4.0F);
        helper.assertTrue(player.getHealth() == health, "fire cannot hurt it");
        AbilityService.useAbility(player, slotOf(player, KitLadder.LIVING_FLAME));
        helper.assertFalse(form.transformed(player), "a second press ends it");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void aSaintsPresenceMakesLesserCreaturesFalter(GameTestHelper helper) {
        ServerPlayer player = saintWith(helper, AspectKitCatalog.STONE, KitLadder.COLOSSUS);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 3));
        RankPowers.presence(player);
        helper.assertTrue(zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && zombie.hasEffect(MobEffects.WEAKNESS),
                "a Dormant creature falters before a Saint");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void aMastersOlderAbilitiesCostLess(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        AspectAbility kindle = AbilityRegistry.get(KindleAbility.ID).orElseThrow();
        int before = AbilityService.costFor(player, kindle);
        SoulService.awaken(player);
        SoulService.ascend(player);
        int after = AbilityService.costFor(player, kindle);
        helper.assertTrue(after < before, "Kindle is Mastered: " + before + " -> " + after);
        helper.assertTrue(RankPowers.power(player) > 1.0F, "and stronger");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void stoneskinHalvesHarm(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.STONE);
        SoulService.awaken(player);
        SoulService.ascend(player);
        AbilityGrants.grant(player, KitLadder.STONESKIN);
        EssenceService.fill(player);
        helper.assertTrue(AbilityService.useAbility(player, slotOf(player, KitLadder.STONESKIN)) == AbilityService.Outcome.USED,
                "Stoneskin on the Master's key");
        float health = player.getHealth();
        player.hurt(player.damageSources().generic(), 4.0F);
        helper.assertTrue(Math.abs(health - player.getHealth() - 2.0F) < 0.01F, "harm halved: " + (health - player.getHealth()));
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void phoenixRiteTurnsAsideOneKillingBlow(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        SoulService.awaken(player);
        SoulService.ascend(player);
        AbilityGrants.grant(player, KitLadder.PHOENIX_RITE);
        EssenceService.fill(player);
        AbilityService.useAbility(player, slotOf(player, KitLadder.PHOENIX_RITE));
        player.hurt(player.damageSources().generic(), 100.0F);
        helper.assertTrue(player.isAlive() && player.getHealth() >= PhoenixRiteAbility.SURVIVE_HEALTH - 0.01F,
                "still standing with " + player.getHealth());
        helper.assertFalse(PhoenixRiteAbility.active(player), "the rite is spent");
        helper.succeed();
    }
}
