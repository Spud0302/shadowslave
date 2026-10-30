package dev.spud.shadowslave.spell.truename;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.fieldguide.FieldGuideRegistry;
import dev.spud.shadowslave.fieldguide.WrittenName;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.core.CoreRules;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.soul.core.ShardDrops;
import dev.spud.shadowslave.spell.SpellJudge;
import dev.spud.shadowslave.spell.SpellRecord;
import dev.spud.shadowslave.spell.calling.Calling;
import dev.spud.shadowslave.spell.calling.SpellCallings;
import dev.spud.shadowslave.trial.content.TrialContent;
import dev.spud.shadowslave.trial.content.TrialSettings;
import dev.spud.shadowslave.weaving.MemoryWeave;
import dev.spud.shadowslave.weaving.MemoryWeaveGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

/** True Names (Run B): earning one, what it does for its bearer, and what knowing it does against them. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TrueNameGameTest {
    private static final String ROOM = "ritual_test_room";

    private static ServerPlayer named(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        TrueNameDeed deed = TrueNameContent.get().deed("steadfast").orElseThrow();
        helper.assertTrue(TrueNames.grant(player, "steadfast", deed, false).isPresent(), "the Spell found a name");
        return player;
    }

    private static SpellJudge.Judgement judged(int score, SpellJudge.Behaviour behaviour) {
        TrialSettings settings = TrialContent.get().settings();
        return new SpellJudge.Judgement(List.of(), score, settings.gradeFor(score), behaviour);
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void onlyAGloriousOrBetterAppraisalOpensTheChance(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        helper.assertTrue(TrueNames.considerAppraisal(player, judged(130, SpellJudge.Behaviour.ALONE)).isEmpty(),
                "Excellent never grants a name");
        Optional<TrueNames.Grant> grant = TrueNames.considerAppraisal(player, judged(10_000, SpellJudge.Behaviour.ALONE));
        helper.assertTrue(grant.isPresent(), "the impossible always grants one");
        helper.assertTrue(TrueNames.nameOf(player).equals(Optional.of(grant.get().name())), "the player bears it");
        helper.assertTrue(TrueNames.data(player).deed().equals("alone"), "built from the deed the Judge read: " + TrueNames.data(player).deed());
        helper.assertTrue(SpellRecord.get(player.server).bearerOf(grant.get().name()).filter(player.getUUID()::equals).isPresent(),
                "the Spell's record knows whose name it is");
        helper.assertFalse(TrueNames.data(player).shown(), "a new name is hidden");
        helper.assertTrue(TrueNames.considerAppraisal(player, judged(10_000, SpellJudge.Behaviour.ALONE)).isEmpty(), "one name per soul");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void theFirstToSlayAGreatCreatureIsNamed(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        TrueNames.considerFirst(player, "kill_fallen_demon");
        helper.assertFalse(TrueNames.data(player).named(), "a lesser first is no extraordinary deed");
        TrueNames.considerFirst(player, "kill_great_test_" + player.getUUID());
        helper.assertTrue(TrueNames.data(player).named(), "the first to slay a Great creature is named");
        helper.assertTrue(TrueNames.data(player).deed().equals("first_great"), "for that deed");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void anAnchoredSoulResistsDrainsAndAKnownNameIsAWeakness(GameTestHelper helper) {
        ServerPlayer bearer = named(helper);
        ServerPlayer other = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        float resist = TrueNames.settings().drainResist();
        float bonus = TrueNames.settings().knownDamageBonus();

        helper.assertTrue(Math.abs(TrueNames.damageFactor(bearer, true, Optional.empty()) - (1.0F - resist)) < 1.0e-4F,
                "soul-draining damage bites less deep");
        helper.assertTrue(TrueNames.damageFactor(bearer, false, Optional.of(other)) == 1.0F, "a stranger has no edge");
        helper.assertTrue(TrueNames.learn(other, bearer, "test"), "they learn it");
        helper.assertTrue(Math.abs(TrueNames.damageFactor(bearer, false, Optional.of(other)) - (1.0F + bonus)) < 1.0e-4F,
                "whoever knows the name strikes a little harder");
        helper.assertFalse(TrueNames.learn(other, bearer, "test"), "a name is learned once");

        EssenceService.fill(bearer);
        float before = EssenceService.current(bearer);
        float taken = EssenceService.drain(bearer, 20.0F);
        helper.assertTrue(Math.abs(taken - 20.0F * (1.0F - resist)) < 1.0e-3F, "an essence drain is shrugged off in part: " + taken);
        helper.assertTrue(Math.abs(EssenceService.current(bearer) - (before - taken)) < 1.0e-3F, "and only that much is gone");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aShardTornFromAnAnchoredSoulIsSmaller(GameTestHelper helper) {
        ServerPlayer bearer = named(helper);
        ServerPlayer killer = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        SoulService.awaken(bearer);
        helper.getLevel().getGameRules().getRule(ShardDrops.PVP_SHARDS).set(true, helper.getLevel().getServer());
        int saturation = CoreService.limit(bearer) / 2;
        CoreService.set(bearer, saturation);
        ShardDrops.dropPlayerShard(helper.getLevel(), bearer, killer);
        int expected = Math.round(CoreRules.pvpLoss(saturation) * TrueNames.drainFactor(bearer));
        helper.assertTrue(CoreService.saturation(bearer) == saturation - expected,
                "lost " + (saturation - CoreService.saturation(bearer)) + ", expected " + expected);
        helper.assertTrue(expected < CoreRules.pvpLoss(saturation), "less than an unnamed soul would lose");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void showingTheNameTeachesItAndHidingItDoesNotUnteach(GameTestHelper helper) {
        ServerPlayer bearer = named(helper);
        ServerPlayer other = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        helper.assertTrue(TrueNames.data(other).knows(bearer.getUUID()).isEmpty(), "hidden by default");
        helper.assertTrue(TrueNames.setShown(bearer, true), "shown");
        helper.assertTrue(bearer.getDisplayName().getString().contains(TrueNames.data(bearer).name()), "it reads as a title");
        helper.assertTrue(TrueNames.data(other).knows(bearer.getUUID()).isPresent(), "everyone who reads it knows it");
        helper.assertTrue(TrueNames.setShown(bearer, false), "hidden again");
        helper.assertFalse(bearer.getDisplayName().getString().contains(TrueNames.data(bearer).name()), "no title now");
        helper.assertTrue(TrueNames.data(other).knows(bearer.getUUID()).isPresent(), "a name once learned is not forgotten");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aKnownNameSensesItsBearerOnceADay(GameTestHelper helper) {
        ServerPlayer bearer = named(helper);
        ServerPlayer knower = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        String name = TrueNames.data(bearer).name();
        helper.assertTrue(TrueNames.sense(knower, name) == TrueNames.Sensed.UNKNOWN, "you can only speak a name you know");
        TrueNames.learn(knower, bearer, "test");
        int callings = SpellCallings.data(bearer).history().size();
        helper.assertTrue(TrueNames.sense(knower, name.toUpperCase()) == TrueNames.Sensed.SENSED, "spoken, in any case");
        helper.assertTrue(TrueNames.sense(knower, name) == TrueNames.Sensed.QUIET, "only once a day");
        helper.assertTrue(SpellCallings.data(bearer).history().size() == callings + 1
                        && SpellCallings.data(bearer).history().get(callings).calling().equals(Calling.NAME.id()),
                "the bearer feels their name spoken");
        helper.assertTrue(TrueNames.wind(0, -10).equals("north") && TrueNames.wind(10, 0).equals("east")
                && TrueNames.wind(0, 10).equals("south") && TrueNames.wind(-10, -10).equals("north-west"), "the winds point true");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aFieldNoteCarriesANameToWhoeverReadsIt(GameTestHelper helper) {
        ServerPlayer bearer = named(helper);
        ServerPlayer reader = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        ItemStack note = new ItemStack(FieldGuideRegistry.FIELD_NOTE_ITEM.get());
        note.set(FieldGuideRegistry.WRITTEN_NAME.get(),
                new WrittenName(bearer.getUUID(), bearer.getGameProfile().getName(), TrueNames.data(bearer).name()));
        reader.setItemInHand(InteractionHand.MAIN_HAND, note);
        note.use(helper.getLevel(), reader, InteractionHand.MAIN_HAND);
        helper.assertTrue(TrueNames.data(reader).knows(bearer.getUUID()).isPresent(), "reading the note teaches the name");
        helper.assertTrue(reader.getMainHandItem().is(FieldGuideRegistry.FIELD_NOTE_ITEM.get()), "and the note is still there to pass on");
        reader.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aTrueNameBindsAMemoryWithoutTheBoundPattern(GameTestHelper helper) {
        ServerPlayer stranger = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        ItemStack sword = MemoryWeaveGameTest.memory(new ItemStack(Items.IRON_SWORD), false);
        helper.assertTrue(TrueNameBinding.bind(stranger, sword) == TrueNameBinding.Outcome.NO_NAME, "it takes a True Name");

        ServerPlayer bearer = named(helper);
        EssenceService.fill(bearer);
        helper.assertTrue(TrueNameBinding.bind(bearer, new ItemStack(Items.IRON_SWORD)) == TrueNameBinding.Outcome.NOT_A_MEMORY,
                "only a Memory holds a name");
        float before = EssenceService.current(bearer);
        helper.assertTrue(TrueNameBinding.bind(bearer, sword) == TrueNameBinding.Outcome.BOUND, "bound");
        helper.assertTrue(MemoryWeave.isBound(sword), "it counts as Bound");
        helper.assertTrue(MemoryWeave.answers(sword, bearer) && !MemoryWeave.answers(sword, stranger), "it answers only to its owner");
        helper.assertTrue(EssenceService.current(bearer) <= before - TrueNames.settings().bindEssence() + 1.0e-3F, "it cost essence");
        helper.assertTrue(TrueNameBinding.bind(bearer, sword) == TrueNameBinding.Outcome.ALREADY_BOUND, "once is enough");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aBearersCallingsComeSoonerAndReachFurther(GameTestHelper helper) {
        ServerPlayer plain = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        ServerPlayer bearer = named(helper);
        helper.assertTrue(TrueNames.callingGapFactor(plain) == 1.0F && TrueNames.callingReach(plain) == 1.0F, "no name, no change");
        helper.assertTrue(TrueNames.callingGapFactor(bearer) < 1.0F && TrueNames.callingReach(bearer) > 1.0F, "a bearer is closer to the Spell");
        helper.assertTrue("yes".equals(SpellCallings.playerFacts(bearer).get("named")), "and the Spell's lines know it");
        helper.succeed();
    }
}
