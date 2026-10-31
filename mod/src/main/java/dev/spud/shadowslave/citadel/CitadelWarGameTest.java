package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.clan.ClanData;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.spell.calling.Calling;
import dev.spud.shadowslave.spell.calling.SpellCallings;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.Set;

/** Clan wars (Run I): declaring with notice, raids and supply-line strikes, assaults on the seat, Abandoned seats. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CitadelWarGameTest {
    /**
     * Their own batch: the war clock is shared by the whole server, and the
     * default batches stay as they were for the tests already in them.
     */
    static final String BATCH = "clan_wars";

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 300, batch = BATCH)
    public static void warIsDeclaredAtYourGatewayForShardsAndNothingIsStruckDuringTheNotice(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        MinecraftServer server = realm.getServer();
        BlockPos first = new BlockPos(7000, 100, 4000);
        BlockPos second = new BlockPos(7000, 100, 4300);
        CitadelData.Citadel home = CitadelGameTest.raise(realm, first);
        CitadelData.Citadel theirs = CitadelGameTest.raise(realm, second);
        CitadelGameTest.ready(helper, realm, second, () -> {});
        CitadelGameTest.ready(helper, realm, first, () -> {
            ServerPlayer leader = CitadelLifeGameTest.claimant(helper, realm, home);
            ServerPlayer rival = CitadelLifeGameTest.claimant(helper, realm, theirs);
            ClanData.Clan ours = ClanService.clanOf(server, leader.getUUID()).orElseThrow();
            ClanData.Clan enemy = ClanService.clanOf(server, rival.getUUID()).orElseThrow();
            helper.assertTrue(CitadelWars.seat(server, ours).filter(home::equals).isPresent(), "a clan's first Citadel is its seat");
            CitadelGameTest.home(helper, rival);

            String unpaid = CitadelWars.declare(leader, enemy.name());
            helper.assertFalse(unpaid.isEmpty(), "declaring costs shards");
            helper.assertTrue(WarData.get(server).between(ours.id(), enemy.id()).isEmpty(), "no war without paying");
            leader.getInventory().add(new ItemStack(ModItems.AWAKENED_SOUL_SHARD.get(), 8));
            String declared = CitadelWars.declare(leader, enemy.name());
            helper.assertTrue(declared.isEmpty(), "declared at their own Gateway: " + declared);
            WarData.War war = WarData.get(server).between(ours.id(), enemy.id()).orElseThrow();
            helper.assertTrue(CitadelLife.carried(leader, stack -> stack.is(ModItems.AWAKENED_SOUL_SHARD.get())) == 0, "the shards are paid");
            helper.assertTrue(SpellCallings.data(leader).history().stream().anyMatch(entry -> entry.calling().equals(Calling.WAR.id())),
                    "the Spell calls the declaring clan");
            helper.assertFalse(CitadelWars.atWar(server, ours.id(), enemy.id()), "the notice holds");
            helper.assertFalse(CitadelWars.declare(leader, enemy.name()).isEmpty(), "one war between two clans");

            // During the notice a raider on their land does nothing.
            theirs.tiers.put(CitadelLife.WARDS, 1);
            stand(leader, realm, theirs.gateway().offset(20, 0, 0));
            for (int second2 = 0; second2 < CitadelWars.settings().outskirts().raidSeconds() + 2; second2++) {
                CitadelWars.tick(server, false);
            }
            helper.assertFalse(theirs.damaged(CitadelLife.WARDS), "nothing is struck before the notice ends");

            CitadelWars.skip(server, CitadelWars.settings().declare().noticeHours());
            CitadelWars.tick(server, false);
            helper.assertTrue(CitadelWars.atWar(server, ours.id(), enemy.id()), "after the notice, war");
            helper.assertTrue(war.begun, "both clans have heard it begin");
            helper.assertFalse(CitadelWars.moveSeat(leader).isEmpty(), "a seat does not move at war");

            CitadelWars.end(server, ours.id());
            ClanService.leave(leader);
            ClanService.leave(rival);
            CitadelGameTest.home(helper, leader);
            CitadelGameTest.release(realm, first);
            CitadelGameTest.release(realm, second);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 300, batch = BATCH)
    public static void raidsOnTheOutskirtsDamageUpgradesAndACutSupplyLineStarvesTheGateway(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        MinecraftServer server = realm.getServer();
        BlockPos first = new BlockPos(7400, 100, 4000);
        BlockPos second = new BlockPos(7400, 100, 4300);
        CitadelData.Citadel home = CitadelGameTest.raise(realm, first);
        CitadelData.Citadel theirs = CitadelGameTest.raise(realm, second);
        CitadelGameTest.ready(helper, realm, second, () -> {});
        CitadelGameTest.ready(helper, realm, first, () -> {
            ServerPlayer raider = CitadelLifeGameTest.claimant(helper, realm, home);
            ServerPlayer keeper = CitadelLifeGameTest.claimant(helper, realm, theirs);
            ClanData.Clan ours = ClanService.clanOf(server, raider.getUUID()).orElseThrow();
            ClanData.Clan enemy = ClanService.clanOf(server, keeper.getUUID()).orElseThrow();
            atWar(server, raider, enemy);

            // A raid on the outskirts: unanswered, it lands; answered, it is driven off.
            theirs.tiers.put(CitadelLife.WARDS, 2);
            CitadelGameTest.home(helper, keeper);
            stand(raider, realm, theirs.gateway().offset(20, 0, 0));
            int seconds = CitadelWars.settings().outskirts().raidSeconds();
            for (int i = 0; i < seconds / 2; i++) {
                CitadelWars.tick(server, false);
            }
            stand(keeper, realm, theirs.gateway().offset(-10, 0, 0));
            CitadelWars.tick(server, false);
            CitadelGameTest.home(helper, keeper);
            for (int i = 0; i < seconds / 2 + 2; i++) {
                CitadelWars.tick(server, false);
            }
            helper.assertFalse(theirs.damaged(CitadelLife.WARDS), "a defender on the land drove the raid off");
            for (int i = 0; i < seconds; i++) {
                CitadelWars.tick(server, false);
            }
            helper.assertTrue(theirs.damaged(CitadelLife.WARDS), "an unanswered raid damages an upgrade");
            helper.assertTrue(theirs.hold() == CitadelData.Hold.CLAN && theirs.clan().filter(enemy.id()::equals).isPresent(),
                    "a raid never takes the Citadel");

            // The supply line: Gateway Links raise a Link Anchor outside the walls.
            theirs.tiers.put(CitadelLife.LINKS, 1);
            CitadelLife.built(realm, theirs, CitadelLife.LINKS);
            BlockPos anchor = theirs.linkAnchor().orElseThrow(() -> new AssertionError("Gateway Links raise a Link Anchor"));
            helper.assertTrue(realm.getBlockState(anchor).is(CitadelRegistry.LINK_ANCHOR.get()), "it stands at " + anchor);
            helper.assertTrue(theirs.gateway().getZ() - anchor.getZ() >= 21, "outside the walls");
            stand(raider, realm, anchor.offset(1, 0, 0));
            for (int i = 0; i <= CitadelWars.settings().supply().strikeSeconds(); i++) {
                CitadelWars.tick(server, false);
            }
            helper.assertTrue(theirs.linkCut(), "an unanswered strike cuts the anchor");
            helper.assertTrue(realm.getBlockState(anchor).getValue(LinkAnchorBlock.CUT), "and it shows");

            double provisions = theirs.provisions();
            stand(keeper, realm, theirs.gateway().offset(2, 0, 2));
            keeper.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 4));
            CitadelService.useGateway(keeper, theirs.gateway());
            helper.assertTrue(theirs.provisions() == provisions && keeper.getMainHandItem().getCount() == 4, "a cut Gateway cannot be fed");
            CitadelLife.eat(server, theirs, CitadelUpkeep.DAY_TICKS / 10);
            double cutMeal = provisions - theirs.provisions();
            double fedMeal = CitadelLife.perDay(server, theirs) / 10;
            helper.assertTrue(cutMeal > fedMeal * 1.5, "and it eats faster: " + cutMeal + " against " + fedMeal);

            keeper.getInventory().add(new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 64));
            keeper.getInventory().add(new ItemStack(Items.ENDER_PEARL, 8));
            stand(keeper, realm, anchor.offset(1, 0, 0));
            CitadelWars.useAnchor(keeper, anchor);
            helper.assertFalse(theirs.linkCut(), "a member mends it at the anchor");
            helper.assertFalse(realm.getBlockState(anchor).getValue(LinkAnchorBlock.CUT), "whole again");

            CitadelWars.end(server, ours.id());
            ClanService.leave(raider);
            ClanService.leave(keeper);
            CitadelGameTest.home(helper, raider, keeper);
            CitadelGameTest.release(realm, first);
            CitadelGameTest.release(realm, second);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 300, batch = BATCH)
    public static void anAnnouncedAssaultChannelsTheSeatAndTakingItEndsTheWar(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        MinecraftServer server = realm.getServer();
        BlockPos first = new BlockPos(7800, 100, 4000);
        BlockPos second = new BlockPos(7800, 100, 4300);
        BlockPos third = new BlockPos(7800, 100, 4600);
        CitadelData.Citadel home = CitadelGameTest.raise(realm, first);
        CitadelData.Citadel seat = CitadelGameTest.raise(realm, second);
        CitadelData.Citadel outpost = CitadelGameTest.raise(realm, third);
        CitadelGameTest.ready(helper, realm, second, () -> {});
        CitadelGameTest.ready(helper, realm, third, () -> {});
        CitadelGameTest.ready(helper, realm, first, () -> {
            ServerPlayer attacker = CitadelLifeGameTest.claimant(helper, realm, home);
            ServerPlayer defender = CitadelLifeGameTest.claimant(helper, realm, seat);
            CitadelService.conquer(realm, outpost, Optional.of(defender));
            helper.assertTrue(CitadelService.claim(defender, outpost), "the defenders hold a second Citadel");
            ClanData.Clan ours = ClanService.clanOf(server, attacker.getUUID()).orElseThrow();
            ClanData.Clan enemy = ClanService.clanOf(server, defender.getUUID()).orElseThrow();
            helper.assertTrue(CitadelWars.seat(server, enemy).filter(seat::equals).isPresent(), "their first claim is their seat");
            seat.tiers.put(CitadelLife.HALL, 2);
            atWar(server, attacker, enemy);
            WarData.War war = WarData.get(server).between(ours.id(), enemy.id()).orElseThrow();

            double notice = CitadelWars.settings().assault().noticeHours();
            helper.assertFalse(CitadelWars.announce(attacker, enemy.name(), notice - 1).isEmpty(), "too little notice is refused");
            helper.assertTrue(CitadelWars.announce(attacker, enemy.name(), notice).isEmpty(), "announced with the notice");
            helper.assertFalse(CitadelWars.assaultOpen(server, war, ours.id(), CitadelWars.now(server)), "not yet");

            stand(attacker, realm, seat.gateway().offset(2, 0, 2));
            CitadelWars.tick(server, false);
            helper.assertTrue(war.channel(ours.id()) == 0, "no channel before the hour");

            CitadelWars.skip(server, notice);
            stand(defender, realm, seat.gateway().offset(-2, 0, -2));
            CitadelWars.tick(server, false);
            helper.assertTrue(war.channel(ours.id()) == 0, "a defender at the Gateway contests it");

            CitadelGameTest.home(helper, defender);
            int need = CitadelWars.settings().assault().channelSeconds();
            for (int i = 0; i < need - 1; i++) {
                CitadelWars.tick(server, false);
            }
            helper.assertTrue(seat.clan().filter(enemy.id()::equals).isPresent(), "not yet taken: " + war.channel(ours.id()));
            CitadelWars.tick(server, false);
            helper.assertTrue(seat.clan().filter(ours.id()::equals).isPresent(), "uncontested, the channel takes the seat");
            helper.assertTrue(seat.tier(CitadelLife.HALL) == 2, "with its upgrades");
            helper.assertTrue(WarData.get(server).between(ours.id(), enemy.id()).isEmpty(), "the war is over");
            helper.assertTrue(WarData.get(server).truce(ours.id(), enemy.id(), CitadelWars.now(server)).isPresent(), "a truce holds");
            helper.assertTrue(CitadelWars.seat(server, enemy).filter(outpost::equals).isPresent(), "the losers keep their other Citadel as seat");
            helper.assertTrue(CitadelWars.seat(server, ours).filter(home::equals).isPresent(), "the winners' seat stays where it was");
            ClanData.Clan losers = ClanService.byId(server, enemy.id()).orElseThrow();
            helper.assertTrue(losers.seatMovedAt() == 0, "the losers may name a new seat at once");
            helper.assertFalse(CitadelWars.declare(attacker, enemy.name()).isEmpty(), "no war in the truce");

            WarData.get(server).truces.clear();
            ClanService.leave(attacker);
            ClanService.leave(defender);
            CitadelGameTest.home(helper, attacker, defender);
            CitadelGameTest.release(realm, first);
            CitadelGameTest.release(realm, second);
            CitadelGameTest.release(realm, third);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 300, batch = BATCH)
    public static void aForsakenOrAbandonedSeatCanBeTakenWithNoNotice(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        MinecraftServer server = realm.getServer();
        BlockPos first = new BlockPos(8200, 100, 4000);
        BlockPos second = new BlockPos(8200, 100, 4300);
        CitadelData.Citadel home = CitadelGameTest.raise(realm, first);
        CitadelData.Citadel seat = CitadelGameTest.raise(realm, second);
        CitadelGameTest.ready(helper, realm, second, () -> {});
        CitadelGameTest.ready(helper, realm, first, () -> {
            ServerPlayer attacker = CitadelLifeGameTest.claimant(helper, realm, home);
            ServerPlayer defender = CitadelLifeGameTest.claimant(helper, realm, seat);
            ClanData.Clan ours = ClanService.clanOf(server, attacker.getUUID()).orElseThrow();
            ClanData.Clan enemy = ClanService.clanOf(server, defender.getUUID()).orElseThrow();
            atWar(server, attacker, enemy);
            WarData.War war = WarData.get(server).between(ours.id(), enemy.id()).orElseThrow();
            long now = CitadelWars.now(server);
            helper.assertFalse(CitadelWars.assaultOpen(server, war, ours.id(), now), "a fed, visited seat needs an announced assault");

            CitadelLife.eat(server, seat, CitadelUpkeep.DAY_TICKS * 30);
            helper.assertTrue(CitadelLife.stage(seat) == Neglect.FORSAKEN, "starved to Forsaken");
            helper.assertTrue(CitadelWars.assaultOpen(server, war, ours.id(), now), "a Forsaken seat can be assaulted with no notice");
            CitadelLife.feed(server, seat, 100);
            helper.assertFalse(CitadelWars.assaultOpen(server, war, ours.id(), now), "fed again, it is not");

            // Nobody of theirs logs in for days: the Spell warns, then the seat is Abandoned.
            CitadelGameTest.home(helper, defender);
            server.getPlayerList().remove(defender);
            ClanService.byId(server, enemy.id()).orElseThrow().seen(now - CitadelWarSettings.hours(CitadelWars.settings().abandon().warnHours() + 1));
            CitadelWars.tick(server, false);
            helper.assertTrue(war.warned.getOrDefault(enemy.id(), 0) == 1, "the Spell warns when the countdown starts");
            helper.assertFalse(CitadelWars.assaultOpen(server, war, ours.id(), now), "not Abandoned yet");
            ClanService.byId(server, enemy.id()).orElseThrow().seen(now - CitadelWarSettings.hours(CitadelWars.settings().abandon().abandonedHours() + 1));
            CitadelWars.tick(server, false);
            helper.assertTrue(war.warned.getOrDefault(enemy.id(), 0) == 2, "and again when it is Abandoned");
            helper.assertTrue(CitadelWars.assaultOpen(server, war, ours.id(), CitadelWars.now(server)), "an Abandoned seat is open");
            helper.assertTrue(CitadelWars.lines(server, seat).stream().anyMatch(line -> line.contains("Abandoned")), "the Gateway says so");

            stand(attacker, realm, seat.gateway().offset(1, 0, 1));
            for (int i = 0; i < CitadelWars.settings().assault().channelSeconds(); i++) {
                CitadelWars.tick(server, false);
            }
            helper.assertTrue(seat.clan().filter(ours.id()::equals).isPresent(), "taken uncontested");

            WarData.get(server).truces.clear();
            ClanService.leave(attacker);
            CitadelGameTest.home(helper, attacker);
            CitadelGameTest.release(realm, first);
            CitadelGameTest.release(realm, second);
            helper.succeed();
        });
    }

    /** The player's clan declares on {@code enemy} from its Gateway, and the notice is skipped. */
    static void atWar(MinecraftServer server, ServerPlayer leader, ClanData.Clan enemy) {
        leader.getInventory().add(new ItemStack(ModItems.AWAKENED_SOUL_SHARD.get(), 64));
        String declared = CitadelWars.declare(leader, enemy.name());
        if (!declared.isEmpty()) {
            throw new AssertionError("could not declare war: " + declared);
        }
        CitadelWars.skip(server, CitadelWars.settings().declare().noticeHours());
        CitadelWars.tick(server, false);
        ClanData.Clan ours = ClanService.clanOf(server, leader.getUUID()).orElseThrow();
        if (!CitadelWars.atWar(server, ours.id(), enemy.id())) {
            throw new AssertionError("the war did not begin");
        }
    }

    static void stand(ServerPlayer player, ServerLevel realm, BlockPos at) {
        player.teleportTo(realm, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, Set.of(), 0, 0);
    }
}
