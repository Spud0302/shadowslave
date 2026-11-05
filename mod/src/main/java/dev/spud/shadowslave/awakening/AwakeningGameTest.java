package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.ability.RankPowers;
import dev.spud.shadowslave.block.ModBlocks;
import dev.spud.shadowslave.network.payload.SoulSeaPayload;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SpellState;
import dev.spud.shadowslave.soul.core.CoreRules;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AwakeningGameTest {
    private static final BlockPos CANDLE = new BlockPos(3, 1, 3);

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void awakeningNeedsAFullCoreThenRaisesEverything(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        helper.assertTrue(AwakeningService.nextRank(player).equals(Optional.of(SoulRank.AWAKENED)), "a Dreamer can rise next");
        helper.assertTrue(AwakeningService.choices(player).equals(KitLadder.variants(AspectKitCatalog.EMBER.natureId(), SoulRank.AWAKENED)),
                "the choices are Ember's two");
        helper.assertTrue(AwakeningService.awaken(player, KitLadder.EMBER_WARD.abilityId()) == AwakeningService.Result.NOT_READY,
                "an empty core is not enough");

        CoreService.set(player, Integer.MAX_VALUE);
        helper.assertTrue(AwakeningService.awaken(player, KitLadder.EMBER_WARD.abilityId()) == AwakeningService.Result.AWAKENED,
                "a saturated core opens Awakening");
        helper.assertTrue(SoulService.get(player).spellState() == SpellState.AWAKENED, "spell state");
        helper.assertTrue(SoulService.get(player).soulRank().equals(Optional.of(SoulRank.AWAKENED)), "soul rank");
        helper.assertTrue(SoulIdentityService.get(player).aspect().orElseThrow().aspectRank() == SoulRank.AWAKENED, "aspect rank");
        helper.assertTrue(AbilityService.status(player).slots().size() == 2
                && AbilityService.status(player).slots().get(1).abilityName().equals("Ember Ward"), "Ember Ward on the second key");
        helper.assertTrue(CoreService.saturation(player) == 0 && CoreService.limit(player) == CoreRules.AWAKENED_LIMIT,
                "the core starts over at its Awakened size");
        helper.assertTrue(EssenceService.current(player) == EssenceService.AWAKENED_CAPACITY, "a larger, full essence pool");
        helper.assertTrue(AwakeningService.awaken(player, KitLadder.CARRY_THE_FLAME.abilityId())
                == AwakeningService.Result.NOT_A_CHOICE, "only once: Carry the Flame is no longer on offer");
        helper.assertTrue(AwakeningService.nextRank(player).equals(Optional.of(SoulRank.ASCENDED)), "Ascension is next");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void anAwakenedAscendsToMasterThenTranscendsToSaint(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        CoreService.set(player, Integer.MAX_VALUE);
        AwakeningService.awaken(player, KitLadder.EMBER_WARD.abilityId());
        RankUpRequirement fullCore = RankUpRequirements.SATURATED_CORE;
        RankUpRequirement ascension = RankUpRequirements.replace(SoulRank.ASCENDED, fullCore);
        RankUpRequirement transcendence = RankUpRequirements.replace(SoulRank.TRANSCENDENT, fullCore);
        try {
            List<AbilityVariant> master = AwakeningService.choices(player);
            helper.assertTrue(master.equals(List.of(KitLadder.FIRESTORM, KitLadder.PHOENIX_RITE)), "Ember's Master pair");
            Optional<ResourceLocation> pick = Optional.of(KitLadder.PHOENIX_RITE.abilityId());
            helper.assertTrue(AwakeningService.rise(player, Optional.empty()) == AwakeningService.Result.NOT_A_CHOICE,
                    "a built rung needs a pick");
            helper.assertTrue(AwakeningService.rise(player, pick) == AwakeningService.Result.NOT_READY,
                    "an empty Awakened core cannot Ascend");
            CoreService.set(player, Integer.MAX_VALUE);
            helper.assertTrue(AwakeningService.rise(player, pick) == AwakeningService.Result.AWAKENED, "Ascension");
            helper.assertTrue(SoulService.get(player).spellState() == SpellState.MASTER, "a Master");
            helper.assertTrue(SoulService.get(player).soulRank().equals(Optional.of(SoulRank.ASCENDED)), "an Ascended soul");
            helper.assertTrue(SoulIdentityService.get(player).aspect().orElseThrow().aspectRank() == SoulRank.ASCENDED, "aspect rank");
            helper.assertTrue(CoreService.saturation(player) == 0 && CoreService.limit(player) == CoreRules.ASCENDED_LIMIT,
                    "the core starts over at its Ascended size");
            helper.assertTrue(EssenceService.current(player) == EssenceService.capacity(player)
                    && EssenceService.capacity(player) >= EssenceService.ASCENDED_CAPACITY, "a larger, full essence pool");
            helper.assertTrue(RankPowers.mastered(player, KitLadder.EMBER_WARD.abilityId()), "Ember Ward is Mastered");
            helper.assertTrue(AbilityService.status(player).slots().get(2).abilityName().equals("Phoenix Rite"), "Phoenix Rite on C");

            helper.assertTrue(AwakeningService.nextRank(player).equals(Optional.of(SoulRank.TRANSCENDENT)), "Transcendence is next");
            helper.assertTrue(AwakeningService.choices(player).equals(List.of(KitLadder.LIVING_FLAME)), "one form, no choice");
            CoreService.set(player, Integer.MAX_VALUE);
            helper.assertTrue(AwakeningService.awaken(player, KitLadder.LIVING_FLAME.abilityId()) == AwakeningService.Result.AWAKENED,
                    "Transcendence");
            helper.assertTrue(SoulService.get(player).spellState() == SpellState.SAINT, "a Saint");
            helper.assertTrue(CoreService.limit(player) == CoreRules.TRANSCENDENT_LIMIT, "the Transcendent core");
            helper.assertTrue(AbilityService.status(player).slots().stream()
                    .anyMatch(slot -> slot.abilityName().equals("Living Flame")), "Living Flame on a key");
            helper.assertTrue(AwakeningService.nextRank(player).isEmpty(), "Sovereign is not open");
        } finally {
            RankUpRequirements.replace(SoulRank.ASCENDED, ascension);
            RankUpRequirements.replace(SoulRank.TRANSCENDENT, transcendence);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void ascensionWaitsForTheSecondNightmare(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.MIST);
        CoreService.set(player, Integer.MAX_VALUE);
        AwakeningService.awaken(player, KitLadder.VEIL.abilityId());
        CoreService.set(player, Integer.MAX_VALUE);
        helper.assertFalse(AwakeningService.ready(player), "a full core alone does not make a Master");
        helper.assertTrue(SoulSeaService.status(player, false).requirement().contains("Category 2"), "the Soul Sea says why");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void anAbilityBuiltAfterYouRoseIsOwed(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.TIDE);
        // Rising without the rank's ability stands in for a rung that shipped later.
        SoulService.awaken(player);
        helper.assertTrue(AwakeningService.owedRank(player).equals(Optional.of(SoulRank.AWAKENED)), "Tide's Awakened pair is owed");
        helper.assertTrue(AwakeningService.ready(player), "an owed ability is always ready");
        helper.assertTrue(SoulSeaService.status(player, false).nextRank().equals("awakened"), "the Soul Sea offers it");
        helper.assertTrue(AwakeningService.awaken(player, KitLadder.UNDERTOW.abilityId()) == AwakeningService.Result.AWAKENED, "taken");
        helper.assertTrue(AwakeningService.owedRank(player).isEmpty(), "nothing more is owed");
        helper.assertTrue(SoulService.get(player).spellState() == SpellState.AWAKENED, "no extra rank came with it");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void onlyYourNaturesAbilitiesCanBeChosen(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.STONE);
        CoreService.set(player, Integer.MAX_VALUE);
        helper.assertTrue(AwakeningService.awaken(player, KitLadder.VEIL.abilityId()) == AwakeningService.Result.NOT_A_CHOICE,
                "Veil belongs to Mist");
        helper.assertTrue(SoulService.get(player).spellState() == SpellState.DREAMER, "still a Dreamer");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void theRequirementCanBeSwapped(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.TIDE);
        CoreService.set(player, Integer.MAX_VALUE);
        RankUpRequirement never = new RankUpRequirement() {
            @Override
            public boolean met(ServerPlayer who) {
                return false;
            }

            @Override
            public String describe(ServerPlayer who) {
                return "Not today.";
            }
        };
        RankUpRequirement before = RankUpRequirements.replace(SoulRank.AWAKENED, never);
        try {
            helper.assertFalse(AwakeningService.ready(player), "the swapped requirement decides");
            helper.assertTrue(SoulSeaService.status(player, false).requirement().equals("Not today."), "and describes itself");
        } finally {
            RankUpRequirements.replace(SoulRank.AWAKENED, before);
        }
        helper.assertTrue(AwakeningService.ready(player), "the saturated core is back in charge");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void sneakUsingALitCandleOpensTheSoulSea(GameTestHelper helper) {
        helper.setBlock(CANDLE, ModBlocks.DREAM_CANDLE.get().defaultBlockState().setValue(CandleBlock.LIT, true));
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.MIST);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT));
        CoreService.set(player, Integer.MAX_VALUE);
        helper.assertTrue(SoulSeaService.requestAwakening(player, KitLadder.VEIL.abilityId().toString())
                == AwakeningService.Result.NOT_READY, "the Soul Sea needs meditation first");

        BlockPos candle = helper.absolutePos(CANDLE);
        player.setShiftKeyDown(true);
        player.gameMode.useItemOn(player, player.serverLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(candle.getCenter(), Direction.UP, candle, false));
        helper.assertTrue(SoulSeaService.meditating(player), "sneak-use meditates");
        helper.assertTrue(player.serverLevel().getBlockState(candle).getValue(CandleBlock.LIT), "the candle stays lit");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "nothing is placed");

        SoulSeaPayload sea = SoulSeaService.status(player, true);
        helper.assertTrue(sea.ready() && sea.choices().size() == 2 && sea.nextRank().equals("awakened"), "Soul Sea: " + sea);
        helper.assertTrue(SoulSeaService.requestAwakening(player, KitLadder.VEIL.abilityId().toString())
                == AwakeningService.Result.AWAKENED, "Awakening from the Soul Sea");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void aSnuffedCandleEndsMeditation(GameTestHelper helper) {
        helper.setBlock(CANDLE, ModBlocks.DREAM_CANDLE.get().defaultBlockState().setValue(CandleBlock.LIT, true));
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.STONE);
        SoulSeaService.meditate(player, helper.absolutePos(CANDLE));
        helper.assertTrue(SoulSeaService.meditating(player), "meditating");
        helper.setBlock(CANDLE, ModBlocks.DREAM_CANDLE.get().defaultBlockState().setValue(CandleBlock.LIT, false));
        helper.assertFalse(SoulSeaService.meditating(player), "a snuffed candle ends it");
        helper.succeed();
    }
}
