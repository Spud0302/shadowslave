package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.block.ModBlocks;
import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.dreamrealm.DreamRealmContent;
import dev.spud.shadowslave.dreamrealm.DreamRegion;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.trial.content.EchoSite;
import dev.spud.shadowslave.trial.content.NightmarePack;
import dev.spud.shadowslave.trial.content.TrialContent;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import dev.spud.shadowslave.world.entity.ThornMatronEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Run E: Long Nightmares, the Thornwake Basin's Category 3 Nightmare, and the Nightmare Scar. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LongNightmareGameTest {
    private static final ResourceLocation LONG_THORN = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "long_thorn");
    private static final ResourceLocation BRIAR_CLOISTER = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "briar_cloister");
    private static final ResourceLocation THORNWAKE = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "thornwake_basin");

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void theBriarCloisterGrowsACategoryThreeLongNightmare(GameTestHelper helper) {
        TrialContent content = TrialContent.get();
        NightmarePack pack = content.nightmare(LONG_THORN).orElseThrow(() -> new AssertionError("The Long Thorn loads"));
        helper.assertTrue(pack.isLong(), "The Long Thorn is a Long Nightmare");
        EchoSite site = content.site(BRIAR_CLOISTER).orElseThrow(() -> new AssertionError("the Briar Cloister loads"));
        helper.assertTrue(content.category(site) == 3, "the Briar Cloister grows Category 3 Seeds");
        var structures = helper.getLevel().getServer().getStructureManager();
        helper.assertTrue(structures.get(pack.structure()).isPresent(), "the Nightmare's structure is bundled");
        helper.assertTrue(site.ruin().flatMap(structures::get).isPresent(), "the ruin is bundled");

        DreamRegion basin = DreamRealmContent.get().regions().get(THORNWAKE);
        helper.assertTrue(basin != null && basin.minDistance() == 1000, "the Thornwake Basin starts 1000 blocks out");
        helper.assertTrue(Math.hypot(site.x(), site.z()) > basin.minDistance() + 48, "the ruin lies inside the basin, past its wobble");
        helper.assertTrue(DreamRealmContent.get().regionAt(new BlockPos(site.x(), 64, site.z()))
                .filter(region -> region.getKey().equals(THORNWAKE)).isPresent(), "the region map agrees");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 400)
    public static void aLongNightmareKeepsItsSleepersAcrossALogout(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        SoulService.awaken(player);
        TrialData.get(player.server).forget(player.getUUID());
        TrialInstance instance = TrialService.begin(List.of(player), LONG_THORN, Optional.empty(), Optional.of(UUID.randomUUID()), 3)
                .orElseThrow(() -> new AssertionError("the Long Nightmare starts"));
        player.hasChangedDimension();
        ServerLevel nightmare = (ServerLevel) player.level();
        TrialService.skip(nightmare, instance, 5);
        TrialInstance.Member member = instance.member(player.getUUID()).orElseThrow();
        // A mock player loads no chunks around itself; load the one it stands in so the figure left there is findable.
        nightmare.getChunk(player.blockPosition());

        TrialService.onPlayerLoggedOut(new PlayerEvent.PlayerLoggedOutEvent(player));
        helper.assertTrue(member.away() && member.inside(), "logging out leaves them asleep inside");
        helper.assertFalse(instance.over(), "the Long Nightmare keeps running");
        helper.assertTrue(nightmare.getEntity(member.lingerer) instanceof LingeringChallenger, "a figure lingers where they stood");
        helper.assertTrue(LongNightmareData.get(player.server).runOf(player.getUUID()).isPresent(), "the run is saved with the world");

        int clock = instance.ticks;
        TrialService.skip(nightmare, instance, 10);
        helper.assertTrue(instance.ticks == clock, "with nobody awake inside, the Nightmare holds its breath");

        CompoundTag saved = instance.save();
        TrialInstance loaded = TrialInstance.load(saved, TrialContent.get()::nightmare).orElseThrow(() -> new AssertionError("reloads"));
        helper.assertTrue(loaded.id.equals(instance.id) && loaded.member(player.getUUID()).filter(TrialInstance.Member::away).isPresent()
                && loaded.member(player.getUUID()).map(m -> m.role().id()).equals(Optional.of(member.role().id())),
                "the saved run comes back with its sleeper and their role");

        UUID figure = member.lingerer;
        TrialService.onPlayerLoggedIn(new PlayerEvent.PlayerLoggedInEvent(player));
        player.hasChangedDimension();
        helper.assertTrue(TrialService.instanceOf(player).filter(run -> run == instance).isPresent(), "they wake inside it again");
        helper.assertFalse(member.away(), "awake");
        helper.assertTrue(nightmare.getEntity(figure) == null && member.lingerer == null, "their figure is gone");
        TrialService.skip(nightmare, instance, 1);
        helper.assertTrue(instance.ticks > clock, "the clock runs again");

        helper.assertTrue(TrialService.forceEnding(nightmare, instance, "mother"), "the Thorn Mother can be slain");
        player.hasChangedDimension();
        helper.assertTrue(TrialData.get(player.server).record(player.getUUID()).clears(3) == 1, "a Category 3 clear is on the record");
        helper.assertTrue(LongNightmareData.get(player.server).runOf(player.getUUID()).isEmpty(), "the finished run is forgotten");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 400)
    public static void aSlainFigureMeansItsChallengerFell(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        SoulService.awaken(player);
        TrialData.get(player.server).forget(player.getUUID());
        Vec3 entered = player.position();
        UUID seed = UUID.randomUUID();
        TrialInstance instance = TrialService.begin(List.of(player), LONG_THORN, Optional.empty(), Optional.of(seed), 3).orElseThrow();
        player.hasChangedDimension();
        ServerLevel nightmare = (ServerLevel) player.level();
        TrialService.onPlayerLoggedOut(new PlayerEvent.PlayerLoggedOutEvent(player));
        TrialInstance.Member member = instance.member(player.getUUID()).orElseThrow();
        LingeringChallenger figure = (LingeringChallenger) nightmare.getEntity(member.lingerer);
        helper.assertTrue(figure != null && figure.owner().equals(Optional.of(player.getUUID())), "the figure is theirs");

        figure.hurt(nightmare.damageSources().generic(), 1000.0F);
        helper.assertTrue(instance.over(), "with its only challenger fallen, the Nightmare closes");
        helper.assertTrue(TrialService.judgedBy(player, seed), "the Seed judged them while they slept");

        TrialService.onPlayerLoggedIn(new PlayerEvent.PlayerLoggedInEvent(player));
        player.hasChangedDimension();
        helper.assertTrue(player.level() == helper.getLevel() && player.position().distanceTo(entered) < 1.0,
                "they wake where they entered");
        helper.assertTrue(TrialData.get(player.server).find(player.getUUID()).flatMap(TrialData.Record::active).isEmpty(),
                "nothing is left on their record");
        helper.assertTrue(LongNightmareData.get(player.server).runOf(player.getUUID()).isEmpty(), "and the run is settled");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 400)
    public static void aLaterCohortJoinsTheChallengersStillInside(GameTestHelper helper) {
        ServerPlayer first = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        ServerPlayer second = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        SoulService.awaken(first);
        SoulService.awaken(second);
        TrialData.get(first.server).forget(first.getUUID());
        TrialData.get(second.server).forget(second.getUUID());
        TrialInstance instance = TrialService.begin(List.of(first), LONG_THORN, Optional.empty(), Optional.of(UUID.randomUUID()), 3)
                .orElseThrow();
        first.hasChangedDimension();
        TrialService.skip((ServerLevel) first.level(), instance, 20);

        TrialService.join(List.of(second), instance);
        second.hasChangedDimension();
        helper.assertTrue(TrialService.instanceOf(second).filter(run -> run == instance).isPresent(), "the newcomer is in the same run");
        helper.assertTrue(instance.inside().size() == 2, "both live in it now");
        helper.assertTrue(instance.ticks >= 20 * 20, "the run's clock did not restart");

        TrialService.abort(first.server, instance);
        first.hasChangedDimension();
        second.hasChangedDimension();
        helper.assertTrue(TrialService.instanceOf(first).isEmpty() && TrialService.instanceOf(second).isEmpty(), "both are out");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void scarsStackToTheirCapAndAConquestClosesOne(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        double health = player.getAttributeValue(Attributes.MAX_HEALTH);
        int max = TrialContent.get().scar().maxStacks();
        for (int i = 0; i <= max; i++) {
            NightmareScar.cut(player);
        }
        helper.assertTrue(NightmareScar.stacks(player) == max, "scars stop at the cap");
        double scarred = player.getAttributeValue(Attributes.MAX_HEALTH);
        helper.assertTrue(Math.abs(health - scarred - max * TrialContent.get().scar().healthPerStack()) < 0.01,
                "each scar takes its share of health");
        helper.assertTrue(NightmareScar.heal(player) == max - 1, "a conquest closes one");
        helper.assertTrue(player.getAttributeValue(Attributes.MAX_HEALTH) > scarred, "and gives its health back");
        player.removeAllEffects();
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void theThornMatronSnaresWithBlackBriars(GameTestHelper helper) {
        ThornMatronEntity matron = NightmareCreatureEntities.THORN_MATRON.get().create(helper.getLevel());
        helper.assertTrue(matron != null, "the Matron is registered");
        helper.assertTrue(matron.nativeRank() == CreatureRank.FALLEN && matron.nativeClass() == CreatureClass.DEVIL,
                "a Fallen Devil");
        BlockPos centre = helper.absolutePos(new BlockPos(3, 1, 3));
        int grown = matron.growRing(helper.getLevel(), centre);
        helper.assertTrue(grown > 0, "the snare grows briars");
        helper.assertTrue(helper.getLevel().getBlockState(centre.east(2)).is(ModBlocks.BLACK_BRIAR.get())
                        || helper.getLevel().getBlockState(centre.west(2)).is(ModBlocks.BLACK_BRIAR.get())
                        || helper.getLevel().getBlockState(centre.north(2)).is(ModBlocks.BLACK_BRIAR.get())
                        || helper.getLevel().getBlockState(centre.south(2)).is(ModBlocks.BLACK_BRIAR.get()),
                "in a ring around the target");
        matron.discard();
        helper.succeed();
    }
}
