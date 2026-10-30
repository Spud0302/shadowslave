package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.fieldguide.FieldGuideService;
import dev.spud.shadowslave.spell.SpellContentLoader;
import dev.spud.shadowslave.spell.SpellRegistry;
import dev.spud.shadowslave.spell.calling.Calling;
import dev.spud.shadowslave.spell.calling.CallingData;
import dev.spud.shadowslave.spell.calling.CallingFrequency;
import dev.spud.shadowslave.spell.calling.SpellCallings;
import dev.spud.shadowslave.world.entity.ChainbackEntity;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Fortune and Cantor: each innate, ability and Flaw doing its one thing. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FortuneCantorGameTest {
    private static final String ROOM = "ritual_test_room";

    // ---- Fortune -------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void favouredCarriesLuckAndSometimesABoonMore(GameTestHelper helper) {
        ServerPlayer fortune = AbilityGameTest.playerWith(helper, AspectKitCatalog.FORTUNE);
        ServerPlayer other = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        FortuneLuck.applyLuck(fortune);
        FortuneLuck.applyLuck(other);
        helper.assertTrue(fortune.getLuck() >= FortuneLuck.LUCK, "a point of luck: " + fortune.getLuck());
        helper.assertTrue(other.getLuck() < FortuneLuck.LUCK, "nobody else has it");
        FortuneLuck.forcedLuck = true;
        try {
            List<ItemStack> boons = FortuneLuck.boons(fortune, List.of(new ItemStack(Items.STONE)), () -> List.of(new ItemStack(Items.DIAMOND)));
            helper.assertTrue(boons.size() == 2 && boons.get(1).is(Items.DIAMOND), "one boon more: " + boons);
            helper.assertTrue(FortuneLuck.boons(other, List.of(new ItemStack(Items.STONE)), () -> List.of(new ItemStack(Items.DIAMOND))).size() == 1,
                    "only for the Favoured");
        } finally {
            FortuneLuck.forcedLuck = null;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void coinOfFateLandsDoubleOrHalf(GameTestHelper helper) {
        ServerPlayer fortune = AbilityGameTest.playerWith(helper, AspectKitCatalog.FORTUNE);
        Pig heads = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 1, 2));
        Pig tails = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(1, 1, 2));
        try {
            FortuneLuck.forcedLuck = true;
            helper.assertTrue(AbilityService.useAbility(fortune, 0) == AbilityService.Outcome.USED, "the coin is flipped");
            helper.assertTrue(AbilityService.useAbility(fortune, 0) == AbilityService.Outcome.FAILED, "one coin at a time");
            heads.hurt(helper.getLevel().damageSources().playerAttack(fortune), 4.0F);
            helper.assertTrue(Math.abs(heads.getMaxHealth() - heads.getHealth() - 8.0F) < 0.01, "heads: double, " + heads.getHealth());
            helper.assertFalse(FortuneLuck.coinArmed(fortune), "the coin is spent on one hit");

            FortuneLuck.forcedLuck = false;
            EssenceService.fill(fortune);
            helper.assertTrue(AbilityService.useAbility(fortune, 0) == AbilityService.Outcome.USED, "flipped again");
            tails.hurt(helper.getLevel().damageSources().playerAttack(fortune), 4.0F);
            helper.assertTrue(Math.abs(tails.getMaxHealth() - tails.getHealth() - 2.0F) < 0.01, "tails: half, " + tails.getHealth());
        } finally {
            FortuneLuck.forcedLuck = null;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void twistFateMakesTheNextBlowMiss(GameTestHelper helper) {
        ServerPlayer fortune = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.FORTUNE, KitLadder.TWIST_FATE);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 4));
        try {
            FortuneLuck.forcedLuck = true;
            helper.assertTrue(AbilityService.useAbility(fortune, 1) == AbilityService.Outcome.USED, "fate is twisted");
            fortune.hurt(helper.getLevel().damageSources().indirectMagic(zombie, zombie), 4.0F);
            helper.assertTrue(fortune.getHealth() == fortune.getMaxHealth(), "the blow goes wide: " + fortune.getHealth());
            helper.assertFalse(FortuneLuck.twisted(fortune), "one blow spends the ward");
            fortune.hurt(helper.getLevel().damageSources().indirectMagic(zombie, zombie), 4.0F);
            helper.assertTrue(fortune.getHealth() < fortune.getMaxHealth(), "the next one lands");
        } finally {
            FortuneLuck.forcedLuck = null;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void borrowedLuckDoublesWhatAMarkedCreatureDrops(GameTestHelper helper) {
        ServerPlayer fortune = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.FORTUNE, KitLadder.BORROWED_LUCK);
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(2, 1, 5));
        fortune.lookAt(EntityAnchorArgument.Anchor.EYES, pig.getBoundingBox().getCenter());
        helper.assertTrue(AbilityService.useAbility(fortune, 1) == AbilityService.Outcome.USED, "the pig is marked");
        helper.assertTrue(FortuneLuck.marked(fortune).filter(pig.getUUID()::equals).isPresent(), "the mark holds");

        List<ItemEntity> drops = new ArrayList<>();
        drops.add(new ItemEntity(helper.getLevel(), pig.getX(), pig.getY(), pig.getZ(), new ItemStack(Items.PORKCHOP, 3)));
        NeoForge.EVENT_BUS.post(new LivingDropsEvent(pig, helper.getLevel().damageSources().generic(), drops, false));
        helper.assertTrue(drops.get(0).getItem().getCount() == 6, "twice the drops: " + drops.get(0).getItem());
        helper.assertTrue(FortuneLuck.marked(fortune).isEmpty(), "the mark is paid out once");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void illOmenFollowsALuckyStreak(GameTestHelper helper) {
        ServerPlayer fortune = AbilityGameTest.playerWith(helper, AspectKitCatalog.FORTUNE);
        float before = EssenceService.current(fortune);
        FortuneLuck.lucky(fortune);
        FortuneLuck.lucky(fortune);
        helper.assertTrue(FortuneLuck.streak(fortune) == 2, "two lucky breaks counted");
        helper.assertFalse(fortune.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || fortune.hasEffect(MobEffects.DIG_SLOWDOWN),
                "no mishap yet");
        FortuneLuck.lucky(fortune);
        boolean mishap = fortune.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || fortune.hasEffect(MobEffects.DIG_SLOWDOWN)
                || EssenceService.current(fortune) < before;
        helper.assertTrue(mishap, "the third brings a mishap");
        helper.assertTrue(FortuneLuck.streak(fortune) == 0, "and the streak starts over");
        helper.succeed();
    }

    // ---- Cantor --------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void attunedCallingsComeTwiceAsOftenAndSayMore(GameTestHelper helper) {
        ServerPlayer cantor = AbilityGameTest.playerWith(helper, AspectKitCatalog.CANTOR);
        ServerPlayer other = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        long gap = SpellContentLoader.settings().gapTicks(CallingFrequency.NORMAL);
        helper.assertTrue(SpellCallings.gapTicks(cantor, CallingFrequency.NORMAL) == gap / 2, "half the wait for a Cantor");
        helper.assertTrue(SpellCallings.gapTicks(other, CallingFrequency.NORMAL) == gap, "the usual wait for others");

        cantor.setData(SpellRegistry.CALLINGS, CallingData.empty());
        helper.assertTrue(SpellCallings.call(cantor, Calling.IDLE, "idle", Map.of(), true), "the Spell calls");
        String said = SpellCallings.data(cantor).history().get(0).text();
        helper.assertTrue(said.contains("Even a Cantor") || said.contains("keeps your name"), "and says a little more: " + said);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void commandWordNamesOnlyWhatTheGuideKnows(GameTestHelper helper) {
        ServerPlayer cantor = AbilityGameTest.playerWith(helper, AspectKitCatalog.CANTOR);
        ChainbackEntity chainback = helper.spawnWithNoFreeWill(NightmareCreatureEntities.CHAINBACK.get(), new BlockPos(2, 1, 4));
        cantor.lookAt(EntityAnchorArgument.Anchor.EYES, chainback.getBoundingBox().getCenter());
        helper.assertTrue(AbilityService.useAbility(cantor, 0) == AbilityService.Outcome.FAILED, "an unknown creature cannot be named");

        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(chainback.getType());
        String dimension = FieldGuideService.dimension(helper.getLevel());
        FieldGuideService.get(cantor).sight(type, 1, dimension, 0, 0, 0L, 0, 1);
        for (int i = 0; i < 3; i++) {
            FieldGuideService.get(cantor).kill(type, 1, dimension, 0, 0);
        }
        helper.assertTrue(FieldGuideService.pageComplete(cantor, type), "a sighting and three kills complete the Chainback's page");
        helper.assertTrue(AbilityService.useAbility(cantor, 0) == AbilityService.Outcome.USED, "now its name is known");
        helper.assertTrue(chainback.stunned(), "and it is held still");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void silencingWordQuietsACreaturesPowers(GameTestHelper helper) {
        ServerPlayer cantor = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.CANTOR, KitLadder.SILENCING_WORD);
        ChainbackEntity chainback = helper.spawnWithNoFreeWill(NightmareCreatureEntities.CHAINBACK.get(), new BlockPos(2, 1, 4));
        cantor.lookAt(EntityAnchorArgument.Anchor.EYES, chainback.getBoundingBox().getCenter());
        helper.assertFalse(chainback.silenced(), "not silent yet");
        helper.assertTrue(AbilityService.useAbility(cantor, 1) == AbilityService.Outcome.USED, "the word is spoken");
        helper.assertTrue(chainback.silenced() && !chainback.stunned(), "silenced, but still free to move");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void rallyingNameClearsAnAllysAilments(GameTestHelper helper) {
        ServerPlayer cantor = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.CANTOR, KitLadder.RALLYING_NAME);
        ServerPlayer ally = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        Vec3 front = helper.absoluteVec(new Vec3(2.5, 1, 0.5));
        ally.teleportTo(front.x, front.y, front.z);
        ally.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200));
        cantor.lookAt(EntityAnchorArgument.Anchor.EYES, ally.getEyePosition());
        helper.assertTrue(AbilityService.useAbility(cantor, 1) == AbilityService.Outcome.USED, "the name is called");
        helper.assertTrue(ally.getActiveEffects().stream().noneMatch(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL),
                "every ailment is gone");
        helper.assertTrue(ally.hasEffect(MobEffects.MOVEMENT_SPEED), "and the ally runs faster");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void spokenSelfShowsTheCantorOnOtherMaps(GameTestHelper helper) {
        ServerPlayer cantor = AbilityGameTest.playerWith(helper, AspectKitCatalog.CANTOR);
        ServerPlayer other = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        CantorVoice.onAbilityUsed(cantor);
        helper.assertTrue(FieldGuideService.status(other, false).pins().stream()
                .anyMatch(pin -> pin.kind().equals("voice") && pin.x() == cantor.getBlockX() && pin.z() == cantor.getBlockZ()),
                "the other player's map shows the Cantor");
        // Only this Cantor's voice is checked: other Cantor tests may be running in the same batch.
        helper.assertTrue(FieldGuideService.status(cantor, false).pins().stream().noneMatch(pin -> pin.kind().equals("voice")
                        && pin.x() == cantor.getBlockX() && pin.z() == cantor.getBlockZ()),
                "not on the Cantor's own");
        CantorVoice.forget(cantor.getUUID());
        helper.assertTrue(FieldGuideService.status(other, false).pins().stream().noneMatch(pin -> pin.kind().equals("voice")
                        && pin.x() == cantor.getBlockX() && pin.z() == cantor.getBlockZ()),
                "and the voice fades");
        helper.succeed();
    }
}
