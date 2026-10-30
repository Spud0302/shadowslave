package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.clan.ClanData;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.dreamrealm.DreamRealmGameTest;
import dev.spud.shadowslave.fieldguide.FieldGuideData;
import dev.spud.shadowslave.fieldguide.FieldGuideService;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.Set;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CitadelGameTest {
    private static final ResourceLocation BURROWER = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "ash_burrower");

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void aCitadelWakesHeldByAGuardianOfItsRegionsRank(GameTestHelper helper) {
        ServerLevel realm = realm(helper);
        // Inside the Ashen Expanse: past 1000 blocks out the Thornwake Basin begins, a Fallen region.
        BlockPos centre = new BlockPos(-700, 100, -300);
        CitadelData.Citadel citadel = raise(realm, centre);
        ready(helper, realm, centre, () -> {
            helper.assertTrue(realm.getBlockState(citadel.gateway()).is(CitadelRegistry.GATEWAY.get()), "the Gateway stands on the dais");
            helper.assertTrue(realm.getBlockState(citadel.gateway()).getValue(GatewayBlock.STATE) == GatewayBlock.State.SEALED, "sealed");
            helper.assertTrue(citadel.hold() == CitadelData.Hold.GUARDIAN, "held by its Guardian");
            NightmareCreature guardian = guardian(realm, citadel);
            helper.assertTrue(guardian.rank() == CreatureRank.DORMANT, "the Ashen Expanse is a Dormant region: " + guardian.rank());
            helper.assertTrue(guardian.creatureClass() == CreatureClass.TYRANT, "a Tyrant holds it");
            helper.assertTrue(guardian.hasRestriction() && guardian.getRestrictCenter().equals(citadel.gateway()), "it keeps to the Citadel");
            helper.assertTrue(guardian.isPersistenceRequired(), "it never despawns");

            // A Guardian that goes missing without dying is replaced once its post is loaded again.
            guardian.discard();
            for (int i = 0; i < 10; i++) {
                CitadelService.tickGateway(realm, citadel.gateway());
            }
            helper.assertTrue(guardian(realm, citadel) != guardian, "another Guardian takes the post");
            guardian(realm, citadel).discard();
            release(realm, centre);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void slayingTheGuardianOpensTheGatewayAndTheSlayerClaimsIt(GameTestHelper helper) {
        ServerLevel realm = realm(helper);
        BlockPos centre = new BlockPos(4300, 100, 4000);
        CitadelData.Citadel citadel = raise(realm, centre);
        ready(helper, realm, centre, () -> {
            ServerPlayer slayer = dreamer(helper, realm, citadel);
            ServerPlayer rival = dreamer(helper, realm, citadel);

            CitadelService.useGateway(slayer, citadel.gateway());
            helper.assertTrue(citadel.hold() == CitadelData.Hold.GUARDIAN, "a sealed Gateway cannot be claimed");

            NightmareCreature guardian = guardian(realm, citadel);
            guardian.hurt(realm.damageSources().playerAttack(slayer), 100000.0F);
            helper.assertTrue(citadel.hold() == CitadelData.Hold.CONQUERED, "the Guardian's death conquers the Citadel");
            helper.assertTrue(realm.getBlockState(citadel.gateway()).getValue(GatewayBlock.STATE) == GatewayBlock.State.OPEN, "the Gateway opens");

            helper.assertFalse(CitadelService.claim(rival, citadel), "the slayer has first claim");
            helper.assertTrue(ClanService.clanOf(realm.getServer(), rival.getUUID()).isEmpty(), "a refused claim founds no clan");

            CitadelService.useGateway(slayer, citadel.gateway());
            helper.assertTrue(citadel.hold() == CitadelData.Hold.CLAN, "claimed");
            ClanData.Clan clan = ClanService.clanOf(realm.getServer(), slayer.getUUID())
                    .orElseThrow(() -> new AssertionError("the slayer stands as a clan of one"));
            helper.assertTrue(clan.members().size() == 1 && clan.leader().equals(slayer.getUUID()), "a clan of one");
            helper.assertTrue(citadel.clan().filter(clan.id()::equals).isPresent(), "the clan holds the Citadel");
            helper.assertTrue(realm.getBlockState(citadel.gateway()).getValue(GatewayBlock.STATE) == GatewayBlock.State.CLAIMED, "claimed Gateway");
            helper.assertTrue(DreamCrossing.data(slayer).anchor().filter(citadel.gateway()::equals).isPresent(), "the claimant anchors there");
            helper.assertTrue(CitadelService.heldBy(realm.getServer(), clan.id()).size() == 1, "the clan's Citadels list it");

            CitadelService.useGateway(rival, citadel.gateway());
            helper.assertFalse(DreamCrossing.data(rival).anchor().filter(citadel.gateway()::equals).isPresent(), "outsiders cannot anchor");

            // The last member leaving lets the Citadel fall open, free for anyone to claim.
            ClanService.leave(slayer);
            helper.assertTrue(citadel.hold() == CitadelData.Hold.CONQUERED && citadel.clan().isEmpty(), "falls open with its clan");
            helper.assertTrue(CitadelService.claim(rival, citadel), "anyone may claim a Citadel with no first claim on it");
            ClanService.leave(rival);
            home(helper, slayer, rival);
            release(realm, centre);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void membersShareTheirFieldGuidesAtTheBoard(GameTestHelper helper) {
        ServerLevel realm = realm(helper);
        BlockPos centre = new BlockPos(4600, 100, 4000);
        CitadelData.Citadel citadel = raise(realm, centre);
        ready(helper, realm, centre, () -> {
            ServerPlayer founder = dreamer(helper, realm, citadel);
            ServerPlayer recruit = dreamer(helper, realm, citadel);
            CitadelService.conquer(realm, citadel, Optional.of(founder));
            helper.assertTrue(CitadelService.claim(founder, citadel), "the founder claims");
            ClanData.Clan clan = ClanService.clanOf(realm.getServer(), founder.getUUID()).orElseThrow();

            String dimension = FieldGuideService.dimension(realm);
            FieldGuideService.get(founder).sight(BURROWER, 0, dimension, 0, 0, 0, 600, 4);
            CitadelService.useGateway(founder, citadel.gateway());
            helper.assertTrue(clan.board().creature(BURROWER).isPresent(), "the founder's page goes up on the board");

            helper.assertFalse(ClanService.join(recruit, clan.name()).ok(), "no joining without an invitation");
            helper.assertTrue(ClanService.invite(founder, recruit).ok(), "invited");
            helper.assertTrue(ClanService.join(recruit, clan.name()).ok(), "joined");
            helper.assertTrue(CitadelService.welcomes(recruit, realm, citadel.gateway()), "a member may anchor here");

            CitadelService.useGateway(recruit, citadel.gateway());
            helper.assertTrue(DreamCrossing.data(recruit).anchor().filter(citadel.gateway()::equals).isPresent(), "the recruit anchors");
            helper.assertTrue(FieldGuideService.get(recruit).creature(BURROWER).map(FieldGuideData.Creature::sightings).orElse(0) == 4,
                    "the recruit learns the founder's page");

            // The recruit kills one; the founder learns of it on their next visit.
            FieldGuideService.get(recruit).kill(BURROWER, 0, dimension, 0, 0);
            CitadelService.useGateway(recruit, citadel.gateway());
            CitadelService.useGateway(founder, citadel.gateway());
            helper.assertTrue(FieldGuideService.get(founder).creature(BURROWER).orElseThrow().kills() == 1, "knowledge flows back");

            // The leader leaving passes the clan on; the Citadel stays with it.
            ClanService.leave(founder);
            helper.assertTrue(clan.leader().equals(recruit.getUUID()), "the longest-standing member leads now");
            helper.assertTrue(citadel.clan().filter(clan.id()::equals).isPresent(), "the clan keeps its Citadel");
            helper.assertFalse(CitadelService.welcomes(founder, realm, citadel.gateway()), "the founder is no longer welcome");
            ClanService.leave(recruit);
            home(helper, founder, recruit);
            release(realm, centre);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void aLoneClaimantBringsTheirCitadelIntoTheClanTheyJoin(GameTestHelper helper) {
        ServerLevel realm = realm(helper);
        BlockPos centre = new BlockPos(4900, 100, 4000);
        CitadelData.Citadel citadel = raise(realm, centre);
        ready(helper, realm, centre, () -> {
            ServerPlayer loner = dreamer(helper, realm, citadel);
            ServerPlayer leader = dreamer(helper, realm, citadel);
            CitadelService.conquer(realm, citadel, Optional.of(loner));
            helper.assertTrue(CitadelService.claim(loner, citadel), "claimed alone");

            helper.assertFalse(ClanService.create(leader, "x").ok(), "names are 3 characters or more");
            helper.assertFalse(ClanService.create(leader, "Bad<Name>").ok(), "no odd characters");
            helper.assertTrue(ClanService.create(leader, "Citadel Test Clan " + realm.getGameTime() % 100000).ok(), "founded");
            ClanData.Clan clan = ClanService.clanOf(realm.getServer(), leader.getUUID()).orElseThrow();
            helper.assertTrue(ClanService.invite(leader, loner).ok(), "invited");
            helper.assertTrue(ClanService.join(loner, clan.name()).ok(), "the loner joins");
            helper.assertTrue(citadel.clan().filter(clan.id()::equals).isPresent(), "their Citadel comes with them");
            helper.assertTrue(CitadelService.welcomes(leader, realm, citadel.gateway()), "and is home to the whole clan");
            ClanService.leave(loner);
            ClanService.leave(leader);
            home(helper, loner, leader);
            release(realm, centre);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void anchoredMembersArriveAtTheGateway(GameTestHelper helper) {
        ServerPlayer player = DreamRealmGameTest.dreamerInBed(helper, true);
        ServerLevel realm = realm(helper);
        BlockPos centre = new BlockPos(5200, 100, 4000);
        CitadelData.Citadel citadel = raise(realm, centre);
        ready(helper, realm, centre, () -> {
            CitadelService.conquer(realm, citadel, Optional.of(player));
            helper.assertTrue(CitadelService.claim(player, citadel), "claimed");
            player.startSleeping(helper.absolutePos(new BlockPos(2, 1, 2)));
            helper.assertTrue(DreamCrossing.tryCross(player), "crossed");
            helper.assertTrue(player.level() == realm && player.blockPosition().closerThan(citadel.gateway(), 4),
                    "arrives beside the Gateway: " + player.blockPosition().toShortString());
            DreamCrossing.wake(player, DreamCrossing.Reason.COMMAND);

            // Once the clan no longer holds it, the Gateway lets the anchor go and a new one is lit over the bed.
            ClanService.leave(player);
            player.startSleeping(helper.absolutePos(new BlockPos(2, 1, 2)));
            helper.assertTrue(DreamCrossing.tryCross(player), "crossed again");
            BlockPos anchor = DreamCrossing.data(player).anchor().orElseThrow();
            helper.assertFalse(anchor.equals(citadel.gateway()), "the anchor moved off the Gateway");
            helper.assertTrue(DreamCrossing.wake(player, DreamCrossing.Reason.COMMAND), "woke");
            release(realm, centre);
            helper.succeed();
        });
    }

    // Helpers

    static ServerLevel realm(GameTestHelper helper) {
        ServerLevel realm = DreamRealm.level(helper.getLevel().getServer());
        if (realm == null) {
            throw new AssertionError("the Dream Realm dimension is missing");
        }
        return realm;
    }

    /** Raises a fresh Citadel, forgetting any an earlier run left at the same spot. */
    static CitadelData.Citadel raise(ServerLevel realm, BlockPos centre) {
        CitadelData data = CitadelData.get(realm);
        data.citadels.removeIf(old -> old.gateway.closerThan(centre, 64));
        CitadelService.build(realm, centre, 12, CitadelPalette.DEFAULT);
        return CitadelService.wake(realm, CitadelLayout.gateway(centre));
    }

    /** Holds the Citadel's chunks loaded and runs the test once its Guardian has taken its post. */
    static void ready(GameTestHelper helper, ServerLevel realm, BlockPos centre, Runnable test) {
        forEachChunk(centre, (x, z) -> realm.setChunkForced(x, z, true));
        helper.runAfterDelay(20, test);
    }

    static void release(ServerLevel realm, BlockPos centre) {
        forEachChunk(centre, (x, z) -> realm.setChunkForced(x, z, false));
    }

    private static void forEachChunk(BlockPos centre, java.util.function.BiConsumer<Integer, Integer> action) {
        for (int x = (centre.getX() >> 4) - 1; x <= (centre.getX() >> 4) + 1; x++) {
            for (int z = (centre.getZ() >> 4) - 1; z <= (centre.getZ() >> 4) + 1; z++) {
                action.accept(x, z);
            }
        }
    }

    static NightmareCreature guardian(ServerLevel realm, CitadelData.Citadel citadel) {
        Entity entity = citadel.guardian().map(realm::getEntity).orElse(null);
        if (!(entity instanceof NightmareCreature creature)) {
            throw new AssertionError("no Guardian holds " + citadel.name());
        }
        return creature;
    }

    /** An Awakened player standing in the Citadel's courtyard. */
    static ServerPlayer dreamer(GameTestHelper helper, ServerLevel realm, CitadelData.Citadel citadel) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        SoulService.awaken(player);
        BlockPos stand = citadel.gateway().offset(3, 0, 3);
        player.teleportTo(realm, stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, Set.of(), 0, 0);
        player.hasChangedDimension();
        return player;
    }

    static void home(GameTestHelper helper, ServerPlayer... players) {
        var room = helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 1, 2.5));
        for (ServerPlayer player : players) {
            player.teleportTo(helper.getLevel(), room.x, room.y, room.z, Set.of(), 0, 0);
        }
    }
}
