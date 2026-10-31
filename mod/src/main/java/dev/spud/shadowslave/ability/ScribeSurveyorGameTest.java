package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.fieldguide.FieldGuideData;
import dev.spud.shadowslave.fieldguide.FieldGuideRegistry;
import dev.spud.shadowslave.fieldguide.FieldGuideService;
import dev.spud.shadowslave.fieldguide.FieldNotePage;
import dev.spud.shadowslave.fieldguide.FieldNotes;
import dev.spud.shadowslave.gate.GateEvent;
import dev.spud.shadowslave.gate.GateForecast;
import dev.spud.shadowslave.gate.GateService;
import dev.spud.shadowslave.gate.GateTrigger;
import dev.spud.shadowslave.network.payload.FieldGuideCopyRequestPayload;
import dev.spud.shadowslave.world.entity.AshBurrowerEntity;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/** Scribe and Surveyor, the Field Guide Natures: each innate, ability and Flaw doing its one thing. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ScribeSurveyorGameTest {
    private static final String ROOM = "ritual_test_room";
    private static final ResourceLocation ASH_BURROWER = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "ash_burrower");

    // ---- Scribe --------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void quickStudyFillsPagesTwiceAsFastAndWritesWithoutInk(GameTestHelper helper) {
        ServerPlayer scribe = AbilityGameTest.playerWith(helper, AspectKitCatalog.SCRIBE);
        scribe.getInventory().clearContent();
        AshBurrowerEntity burrower = helper.spawnWithNoFreeWill(NightmareCreatureEntities.ASH_BURROWER.get(), new BlockPos(4, 1, 4));
        helper.assertTrue(FieldGuideService.recordSighting(scribe, burrower), "the sighting counts");
        int sightings = FieldGuideService.get(scribe).creature(ASH_BURROWER).map(FieldGuideData.Creature::sightings).orElse(0);
        helper.assertTrue(sightings == 2, "one look fills two sightings: " + sightings);

        scribe.getInventory().add(new ItemStack(Items.PAPER, 1));
        Optional<ItemStack> note = FieldNotes.copy(scribe, new FieldGuideCopyRequestPayload("creature", ASH_BURROWER.toString(), "", 0, 0));
        helper.assertTrue(note.isPresent(), "paper alone is enough for a Scribe");
        helper.assertTrue(scribe.getInventory().countItem(Items.PAPER) == 0, "the paper is used");
        burrower.discard();
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void wardScriptPushesLesserCreaturesOutAndInkboundNeedsPaper(GameTestHelper helper) {
        ServerPlayer scribe = AbilityGameTest.playerWith(helper, AspectKitCatalog.SCRIBE);
        scribe.getInventory().clearContent();
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 2));
        helper.assertTrue(AbilityService.useAbility(scribe, 0) == AbilityService.Outcome.FAILED, "Inkbound: nothing to write with");
        helper.assertTrue(ScribeLore.ward(scribe).isEmpty(), "no ward written");

        scribe.getInventory().add(new ItemStack(Items.PAPER, 2));
        helper.assertTrue(AbilityService.useAbility(scribe, 0) == AbilityService.Outcome.USED, "Ward Script is written");
        helper.assertTrue(scribe.getInventory().countItem(Items.PAPER) == 1, "and Inkbound spends a sheet of paper");
        Vec3 centre = ScribeLore.ward(scribe).orElseThrow(() -> new AssertionError("a ward stands"));
        double out = zombie.position().subtract(centre).horizontalDistance();
        helper.assertTrue(out >= ScribeLore.WARD_RADIUS, "the zombie is pushed to the ward's edge: " + out);
        zombie.teleportTo(centre.x, zombie.getY(), centre.z + 1.0);
        ScribeLore.guard(helper.getLevel().getServer());
        helper.assertTrue(zombie.position().subtract(centre).horizontalDistance() >= ScribeLore.WARD_RADIUS, "and cannot cross back in");
        AbilityService.clearLingering(scribe);
        helper.assertTrue(ScribeLore.ward(scribe).isEmpty(), "the ward is gone when the Scribe goes");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void lorebindMakesAKindTakeMoreFromTheScholar(GameTestHelper helper) {
        ServerPlayer scribe = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.SCRIBE, KitLadder.LOREBIND);
        scribe.getInventory().add(new ItemStack(Items.PAPER, 4));
        Zombie first = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 2));
        Zombie second = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
        float plain = struck(scribe, first);
        scribe.lookAt(EntityAnchorArgument.Anchor.EYES, second.getBoundingBox().getCenter());
        helper.assertTrue(AbilityService.useAbility(scribe, 1) == AbilityService.Outcome.USED, "Lorebind studies the zombie");
        helper.assertTrue(ScribeLore.studied(scribe).filter(EntityType.ZOMBIE::equals).isPresent(), "its kind is studied");
        float bound = struck(scribe, second);
        helper.assertTrue(Math.abs(bound - plain * (1.0F + (float) ScribeLore.LOREBIND_SHARE)) < 0.05F,
                "its kind takes a quarter more: " + plain + " then " + bound);
        ServerPlayer stranger = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        helper.assertTrue(Math.abs(struck(stranger, first) - plain) < 0.05F, "no one outside the clan gains from it");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aLivingPageKeepsUpWithItsWriter(GameTestHelper helper) {
        ServerPlayer scribe = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.SCRIBE, KitLadder.LIVING_PAGE);
        scribe.setGameMode(GameType.CREATIVE);
        AshBurrowerEntity burrower = helper.spawnWithNoFreeWill(NightmareCreatureEntities.ASH_BURROWER.get(), new BlockPos(4, 1, 4));
        FieldGuideService.recordSighting(scribe, burrower);
        ItemStack note = FieldNotes.copy(scribe, new FieldGuideCopyRequestPayload("creature", ASH_BURROWER.toString(), "", 0, 0))
                .orElseThrow(() -> new AssertionError("a note"));
        FieldNotePage written = note.get(FieldGuideRegistry.NOTE_PAGE.get());
        helper.assertTrue(written.lines().contains("A Living Page: it keeps up with its writer."), "its face says it lives: " + written.lines());

        // The Scribe learns more after writing it.
        FieldGuideService.get(scribe).kill(ASH_BURROWER, burrower.rank().ordinal(), FieldGuideService.dimension(helper.getLevel()), 0, 0);
        ServerPlayer reader = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        FieldNotes.read(reader, note, written);
        int kills = FieldGuideService.get(reader).creature(ASH_BURROWER).map(FieldGuideData.Creature::kills).orElse(0);
        helper.assertTrue(kills == 1, "the reader learns the kill written after the note: " + kills);
        FieldNotePage now = note.get(FieldGuideRegistry.NOTE_PAGE.get());
        helper.assertTrue(now.id().equals(written.id()) && now.knowledge().creature(ASH_BURROWER).map(FieldGuideData.Creature::kills).orElse(0) == 1,
                "the note itself is rewritten");
        burrower.discard();
        helper.succeed();
    }

    // ---- Surveyor ------------------------------------------------------------------------------------------------

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void wideEyeMapsFurtherAndUnchartedDreadDoublesCostsOffTheMap(GameTestHelper helper) {
        ServerPlayer surveyor = AbilityGameTest.playerWith(helper, AspectKitCatalog.SURVEYOR);
        AspectAbility pulse = AbilityRegistry.get(SurveyPulseAbility.ID).orElseThrow();
        int base = AbilityService.costFor(surveyor, pulse);
        helper.assertFalse(SurveyorCraft.charted(surveyor), "a fresh guide has not mapped here");
        helper.assertTrue(base == SurveyPulseAbility.ESSENCE_COST * 2, "off the map it costs double: " + base);

        FieldGuideService.explore(surveyor, true);
        helper.assertTrue(SurveyorCraft.charted(surveyor), "now it has");
        helper.assertTrue(AbilityService.costFor(surveyor, pulse) == SurveyPulseAbility.ESSENCE_COST, "and costs what it should");
        String dimension = FieldGuideService.dimension(helper.getLevel());
        int reach = FieldGuideService.exploreRadius(surveyor);
        ChunkPos centre = surveyor.chunkPosition();
        if (helper.getLevel().hasChunk(centre.x + reach, centre.z)) {
            helper.assertTrue(FieldGuideService.get(surveyor).explored(dimension, ChunkPos.asLong(centre.x + reach, centre.z)),
                    "Wide Eye maps " + SurveyorCraft.WIDE_EYE + " chunks further");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void surveyPulseChartsTheLandAndOutlinesIt(GameTestHelper helper) {
        ServerPlayer surveyor = AbilityGameTest.playerWith(helper, AspectKitCatalog.SURVEYOR);
        helper.assertTrue(AbilityService.useAbility(surveyor, 0) == AbilityService.Outcome.USED, "the pulse goes out");
        helper.assertTrue(SurveyorCraft.charted(surveyor), "the ground underfoot is charted");
        helper.assertTrue(SurveyorCraft.outline(surveyor).isPresent(), "and outlined for a while");
        AbilityService.clearLingering(surveyor);
        helper.assertTrue(SurveyorCraft.outline(surveyor).isEmpty(), "gone with the Surveyor");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void triangulatePinsTheNearestGate(GameTestHelper helper) {
        ServerPlayer surveyor = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.SURVEYOR, KitLadder.TRIANGULATE);
        BlockPos at = helper.absolutePos(new BlockPos(2, 40, 2));
        GateEvent gate = GateService.open(helper.getLevel(), at, GateTrigger.COMMAND, GateForecast.certain(1))
                .orElseThrow(() -> new AssertionError("a Gate opens overhead"));
        SurveyorCraft.Fix fix = SurveyorCraft.triangulate(surveyor).orElseThrow(() -> new AssertionError("a bearing"));
        helper.assertTrue(fix.kind() == FieldGuideData.PinKind.GATE && fix.pos().distSqr(surveyor.blockPosition()) <= at.distSqr(surveyor.blockPosition()),
                "the nearest Gate: " + fix);
        helper.assertTrue(AbilityService.useAbility(surveyor, 1) == AbilityService.Outcome.USED, "Triangulate fires");
        helper.assertTrue(FieldGuideService.get(surveyor).hasPin(fix.ref()), "and pins it in the guide");
        GateService.close(helper.getLevel(), gate.id());
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void faultLineHurtsAndSlowsWhatIsOnTheLine(GameTestHelper helper) {
        ServerPlayer surveyor = AwakenedAbilityGameTest.awakened(helper, AspectKitCatalog.SURVEYOR, KitLadder.FAULT_LINE);
        surveyor.setGameMode(GameType.CREATIVE);
        surveyor.moveTo(helper.absoluteVec(new Vec3(0.5, 1, 2.5)));
        Zombie onLine = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 2));
        Zombie aside = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 5));
        surveyor.lookAt(EntityAnchorArgument.Anchor.EYES, helper.absoluteVec(new Vec3(5.5, 1.6, 2.5)));
        helper.assertTrue(AbilityService.useAbility(surveyor, 1) == AbilityService.Outcome.USED, "the ground cracks");
        helper.assertTrue(onLine.getHealth() < onLine.getMaxHealth() && onLine.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "the zombie on the line is hurt and slowed");
        helper.assertTrue(aside.getHealth() == aside.getMaxHealth() && !aside.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "the one aside is not");
        helper.succeed();
    }

    /** How much a plain 10-point blow from {@code attacker} takes off {@code target}. */
    static float struck(ServerPlayer attacker, Zombie target) {
        target.setHealth(target.getMaxHealth());
        target.invulnerableTime = 0;
        float before = target.getHealth();
        target.hurt(target.level().damageSources().playerAttack(attacker), 10.0F);
        return before - target.getHealth();
    }
}
