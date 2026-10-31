package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.awakening.AbilityGrants;
import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.clan.ClanData;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Saboteur and Standard in a clan war: sensing and cutting supply lines, and Standards leaning on a channel. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WarNaturesGameTest {

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 300, batch = CitadelWarGameTest.BATCH)
    public static void aSaboteurSensesUnderminesAndCutsTheLinesFaster(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        MinecraftServer server = realm.getServer();
        BlockPos first = new BlockPos(9000, 100, 4000);
        BlockPos second = new BlockPos(9000, 100, 4300);
        CitadelData.Citadel home = CitadelGameTest.raise(realm, first);
        CitadelData.Citadel theirs = CitadelGameTest.raise(realm, second);
        CitadelGameTest.ready(helper, realm, second, () -> {});
        CitadelGameTest.ready(helper, realm, first, () -> {
            ServerPlayer saboteur = CitadelLifeGameTest.claimant(helper, realm, home);
            ServerPlayer keeper = CitadelLifeGameTest.claimant(helper, realm, theirs);
            become(saboteur, AspectKitCatalog.SABOTEUR);
            ClanData.Clan ours = ClanService.clanOf(server, saboteur.getUUID()).orElseThrow();
            ClanData.Clan enemy = ClanService.clanOf(server, keeper.getUUID()).orElseThrow();
            theirs.tiers.put(CitadelLife.LINKS, 1);
            CitadelLife.built(realm, theirs, CitadelLife.LINKS);
            BlockPos anchor = theirs.linkAnchor().orElseThrow();

            // Line Sense: the other clan's supply line shows; not yet an enemy.
            CitadelGameTest.home(helper, keeper);
            CitadelWarGameTest.stand(saboteur, realm, anchor.offset(10, 0, 0));
            List<WarNatures.Sensed> sensed = WarNatures.sense(saboteur, 48);
            helper.assertTrue(sensed.size() == 1 && sensed.get(0).id().equals(theirs.id()), "their Citadel is sensed: " + sensed);
            helper.assertTrue(sensed.get(0).anchor().filter(anchor::equals).isPresent() && !sensed.get(0).enemy(), "its anchor, not yet at war");

            // Out of war the Spell holds their ground.
            helper.assertTrue(WarNatures.undermine(saboteur, realm, anchor) == WarNatures.Undermined.HELD, "no war, no sabotage");
            helper.assertFalse(theirs.linkCut(), "the line holds");

            CitadelWarGameTest.stand(saboteur, realm, home.gateway().offset(2, 0, 2));
            CitadelWarGameTest.atWar(server, saboteur, enemy);
            CitadelWarGameTest.stand(saboteur, realm, anchor.offset(10, 0, 0));
            helper.assertTrue(WarNatures.sense(saboteur, 48).get(0).enemy(), "now an enemy's line");

            // A member standing over the charge stamps it out.
            CitadelWarGameTest.stand(keeper, realm, anchor.offset(1, 0, 1));
            helper.assertTrue(WarNatures.undermine(saboteur, realm, anchor) == WarNatures.Undermined.DEFUSED, "stamped out");
            CitadelGameTest.home(helper, keeper);

            // Unanswered, the charge cuts the anchor.
            helper.assertTrue(WarNatures.undermine(saboteur, realm, anchor) == WarNatures.Undermined.CUT, "the charge cuts the line");
            helper.assertTrue(theirs.linkCut(), "cut");
            theirs.linkCut = false;
            CitadelWars.showAnchor(realm, theirs);

            // On their land, it damages an upgrade, once per raid cooldown.
            theirs.tiers.put(CitadelLife.WARDS, 2);
            BlockPos land = theirs.gateway().offset(12, 0, 0);
            helper.assertTrue(WarNatures.undermine(saboteur, realm, land) == WarNatures.Undermined.RAIDED, "a charge on their land");
            helper.assertTrue(theirs.damaged(CitadelLife.WARDS) || theirs.damaged(CitadelLife.LINKS), "damages an upgrade");
            helper.assertTrue(WarNatures.undermine(saboteur, realm, land) == WarNatures.Undermined.HELD, "not again until the cooldown");

            // Cut the Lines: a strike at the anchor counts twice.
            if (!AbilityGrants.grant(saboteur, KitLadder.CUT_THE_LINES)) {
                throw new AssertionError("could not grant Cut the Lines");
            }
            CitadelWarGameTest.stand(saboteur, realm, anchor.offset(1, 0, 0));
            int seconds = CitadelWars.settings().supply().strikeSeconds();
            for (int i = 0; i < seconds / 2; i++) {
                CitadelWars.tick(server, false);
            }
            helper.assertTrue(theirs.linkCut(), "cut in half the time");
            helper.assertTrue(saboteur.hasEffect(MobEffects.GLOWING), "and Marked for it");

            CitadelWars.end(server, ours.id());
            WarData.get(server).raidedUntil.remove(theirs.id());
            ClanService.leave(saboteur);
            ClanService.leave(keeper);
            CitadelGameTest.home(helper, saboteur, keeper);
            CitadelGameTest.release(realm, first);
            CitadelGameTest.release(realm, second);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 300, batch = CitadelWarGameTest.BATCH)
    public static void plantedStandardsLeanOnTheChannel(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        MinecraftServer server = realm.getServer();
        BlockPos first = new BlockPos(9400, 100, 4000);
        BlockPos second = new BlockPos(9400, 100, 4300);
        CitadelData.Citadel home = CitadelGameTest.raise(realm, first);
        CitadelData.Citadel seat = CitadelGameTest.raise(realm, second);
        CitadelGameTest.ready(helper, realm, second, () -> {});
        CitadelGameTest.ready(helper, realm, first, () -> {
            ServerPlayer attacker = CitadelLifeGameTest.claimant(helper, realm, home);
            ServerPlayer defender = CitadelLifeGameTest.claimant(helper, realm, seat);
            become(attacker, AspectKitCatalog.STANDARD);
            become(defender, AspectKitCatalog.STANDARD);
            ClanData.Clan ours = ClanService.clanOf(server, attacker.getUUID()).orElseThrow();
            ClanData.Clan enemy = ClanService.clanOf(server, defender.getUUID()).orElseThrow();
            CitadelWarGameTest.atWar(server, attacker, enemy);
            WarData.War war = WarData.get(server).between(ours.id(), enemy.id()).orElseThrow();
            CitadelGameTest.home(helper, defender);

            CitadelWarGameTest.stand(attacker, realm, seat.gateway().offset(3, 0, 0));
            CitadelWars.channel(server, realm, war, ours, enemy, seat, CitadelWars.now(server));
            helper.assertTrue(war.channel(ours.id()) == 1, "one attacker, one a second: " + war.channel(ours.id()));
            helper.assertTrue(AbilityService.useAbility(attacker, 0) == AbilityService.Outcome.USED, "the attackers' Standard is planted");
            CitadelWars.channel(server, realm, war, ours, enemy, seat, CitadelWars.now(server));
            helper.assertTrue(war.channel(ours.id()) == 3, "their Standard speeds the channel: " + war.channel(ours.id()));

            CitadelWarGameTest.stand(defender, realm, seat.gateway().offset(-3, 0, 0));
            helper.assertTrue(AbilityService.useAbility(defender, 0) == AbilityService.Outcome.USED, "the defenders' Standard is planted");
            CitadelGameTest.home(helper, defender);
            CitadelWars.channel(server, realm, war, ours, enemy, seat, CitadelWars.now(server));
            helper.assertTrue(war.channel(ours.id()) == 4, "the defenders' Standard cancels theirs: one second, not two: "
                    + war.channel(ours.id()));

            AbilityService.clearLingering(attacker);
            AbilityService.clearLingering(defender);
            CitadelWars.end(server, ours.id());
            ClanService.leave(attacker);
            ClanService.leave(defender);
            CitadelGameTest.home(helper, attacker, defender);
            CitadelGameTest.release(realm, first);
            CitadelGameTest.release(realm, second);
            helper.succeed();
        });
    }

    /** Gives a Citadel claimant a war Nature, keeping their rank. */
    static void become(ServerPlayer player, AspectKit kit) {
        SoulIdentityService.replace(player, AbilityGameTest.identityFor(kit));
    }
}
