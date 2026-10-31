package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** NPC-held Citadels and the Saint's claim (DESIGN, Jack 2026-10-31). */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NpcCitadelGameTest {
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void onlyASaintBindsAnOpenGateway(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        BlockPos centre = new BlockPos(7600, 100, -6000);
        CitadelData.Citadel citadel = CitadelGameTest.raise(realm, centre);
        CitadelGameTest.ready(helper, realm, centre, () -> {
            ServerPlayer awakened = CitadelGameTest.awakened(helper, realm, citadel);
            NightmareCreature guardian = CitadelGameTest.guardian(realm, citadel);
            guardian.hurt(realm.damageSources().playerAttack(awakened), 100000.0F);
            helper.assertTrue(citadel.hold() == CitadelData.Hold.CONQUERED, "anyone may conquer a Citadel");
            helper.assertFalse(CitadelService.claim(awakened, citadel), "an Awakened cannot bind its Gateway");

            CitadelService.useGateway(awakened, citadel.gateway());
            helper.assertTrue(citadel.hold() == CitadelData.Hold.CONQUERED, "still nobody's");
            helper.assertTrue(ClanService.clanOf(realm.getServer(), awakened.getUUID()).isEmpty(), "no clan is founded");
            helper.assertTrue(DreamCrossing.data(awakened).anchor().filter(citadel.gateway()::equals).isPresent(), "but they anchor there");
            helper.assertTrue(CitadelService.welcomes(awakened, realm, citadel.gateway()), "and the open Gateway keeps the anchor");

            ServerPlayer saint = CitadelGameTest.dreamer(helper, realm, citadel);
            citadel.conqueredAt = realm.getGameTime() - 100000L;
            CitadelService.useGateway(saint, citadel.gateway());
            helper.assertTrue(citadel.hold() == CitadelData.Hold.CLAN, "a Saint binds it");
            helper.assertFalse(CitadelService.welcomes(awakened, realm, citadel.gateway()), "binding writes the Gateway to the Saint's clan alone");
            ClanService.leave(saint);
            CitadelGameTest.home(helper, awakened, saint);
            CitadelGameTest.release(realm, centre);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void aHeldCitadelsPeopleStandOnlyWhileAPlayerIsNear(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        BlockPos centre = new BlockPos(7900, 100, -6000);
        CitadelData.Citadel citadel = CitadelGameTest.raise(realm, centre, NpcCitadels.Kind.HELD);
        NpcCitadelSettings settings = NpcCitadels.settings();
        helper.assertTrue(citadel.hold() == CitadelData.Hold.NPC, "held by an NPC clan");
        helper.assertTrue(realm.getBlockState(citadel.gateway()).getValue(GatewayBlock.STATE) == GatewayBlock.State.CLAIMED, "its Gateway is bound");
        helper.assertTrue(citadel.guardian().isEmpty(), "no Guardian holds it");
        UUID clan = citadel.npcClan().orElseThrow(() -> new AssertionError("an NPC clan holds it"));
        helper.assertTrue(citadel.guards() == settings.guards() && citadel.residents() == settings.residents(), "its people are counted");
        CitadelGameTest.ready(helper, realm, centre, () -> {
            helper.assertTrue(NpcCitadels.present(realm, citadel).isEmpty(), "with nobody near, its people are only a count");
            ServerPlayer visitor = CitadelGameTest.awakened(helper, realm, citadel);
            CitadelService.tickGateway(realm, citadel.gateway());
            List<CitadelResident> present = NpcCitadels.present(realm, citadel);
            int expected = Math.min(settings.maxPresent(), 1 + settings.guards() + settings.residents());
            helper.assertTrue(present.size() == expected, "everyone takes their post: " + present.size() + " of " + expected);
            helper.assertTrue(present.stream().filter(resident -> resident.role() == CitadelResident.Role.LORD).count() == 1, "one Saint lord");
            helper.assertTrue(present.stream().filter(resident -> resident.role() == CitadelResident.Role.TRADER).count() == 1, "one trader");
            CitadelService.tickGateway(realm, citadel.gateway());
            helper.assertTrue(NpcCitadels.present(realm, citadel).size() == expected, "nobody is spawned twice");

            CitadelService.useGateway(visitor, citadel.gateway());
            helper.assertTrue(DreamCrossing.data(visitor).anchor().filter(citadel.gateway()::equals).isPresent(), "a stranger may anchor here");
            helper.assertTrue(CitadelService.welcomes(visitor, realm, citadel.gateway()), "and the Gateway keeps the anchor");
            helper.assertTrue(NpcCitadels.standing(visitor, clan) == NpcCitadels.Standing.STRANGER, "a stranger to the clan");
            NpcCitadels.traded(visitor, clan);
            helper.assertTrue(NpcCitadels.points(visitor, clan) == settings.standing().trade(), "trading earns standing");

            CitadelResident guard = present.stream().filter(resident -> resident.role() == CitadelResident.Role.GUARD).findFirst().orElseThrow();
            guard.hurt(realm.damageSources().playerAttack(visitor), 1.0F);
            helper.assertTrue(NpcCitadels.hostile(visitor, clan), "striking a guard turns the clan against you");
            helper.assertFalse(CitadelService.welcomes(visitor, realm, citadel.gateway()), "an enemy's anchor is let go");
            guard.hurt(realm.damageSources().playerAttack(visitor), 100000.0F);
            helper.assertTrue(citadel.guards() == settings.guards() - 1, "a slain guard is one fewer");

            CitadelGameTest.home(helper, visitor);
            CitadelService.tickGateway(realm, citadel.gateway());
            helper.assertTrue(NpcCitadels.present(realm, citadel).isEmpty(), "once the player leaves, its people are a count again");
            CitadelGameTest.release(realm, centre);
            helper.succeed();
        });
    }
}
