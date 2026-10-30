package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.clan.ClanData;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.dreamrealm.seed.SeedData;
import dev.spud.shadowslave.dreamrealm.seed.SeedService;
import dev.spud.shadowslave.gate.GateEvent;
import dev.spud.shadowslave.gate.GateForecast;
import dev.spud.shadowslave.gate.GateService;
import dev.spud.shadowslave.gate.GateTrigger;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.smithing.SmithingRegistry;
import dev.spud.shadowslave.spell.SpellRecord;
import dev.spud.shadowslave.spell.calling.Calling;
import dev.spud.shadowslave.spell.calling.SpellCallings;
import dev.spud.shadowslave.spell.truename.TrueNameContent;
import dev.spud.shadowslave.weaving.WeavingRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/** Citadel life (Run C): upkeep and neglect, upgrades, Gateway Links, and raids from blooming Seeds. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CitadelLifeGameTest {

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void theGatewayHungersMoreForABiggerClanAndNeglectNeverLosesTheCitadel(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        BlockPos centre = new BlockPos(5000, 100, 4000);
        CitadelData.Citadel citadel = CitadelGameTest.raise(realm, centre);
        CitadelGameTest.ready(helper, realm, centre, () -> {
            ServerPlayer founder = claimant(helper, realm, citadel);
            ServerPlayer recruit = CitadelGameTest.dreamer(helper, realm, citadel);
            CitadelUpkeep upkeep = CitadelContentLoader.content().upkeep();
            double alone = CitadelLife.perDay(realm.getServer(), citadel);
            helper.assertTrue(Math.abs(citadel.provisions() - alone * upkeep.claimDays()) < 1e-6, "a fresh claim holds a few days' provisions");

            ClanData.Clan clan = ClanService.clanOf(realm.getServer(), founder.getUUID()).orElseThrow();
            ClanService.invite(founder, recruit);
            ClanService.join(recruit, clan.name());
            helper.assertTrue(CitadelLife.perDay(realm.getServer(), citadel) > alone, "a bigger clan feeds a hungrier Gateway");

            // Feeding: a member holds soul shards and uses the Gateway.
            citadel.provisions = 0;
            founder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 5));
            CitadelService.useGateway(founder, citadel.gateway());
            double shard = upkeep.worth(new ItemStack(ModItems.DORMANT_SOUL_SHARD.get())).orElseThrow();
            helper.assertTrue(Math.abs(citadel.provisions() - 5 * shard) < 1e-6, "the Gateway ate the shards: " + citadel.provisions());
            helper.assertTrue(founder.getMainHandItem().isEmpty(), "they are spent");

            // Neglect: wards drop, then workshops slow, then the echo is silenced; the Citadel stays the clan's.
            citadel.tiers.put(CitadelLife.WARDS, 1);
            citadel.tiers.put(CitadelLife.WORKSHOPS, 1);
            BlockPos inside = citadel.gateway().offset(10, 0, 0);
            helper.assertTrue(CitadelLife.warded(realm, inside), "a fed Gateway's wards stand");
            CitadelLife.eat(realm.getServer(), citadel, CitadelUpkeep.DAY_TICKS);
            helper.assertTrue(CitadelLife.stage(citadel) == Neglect.HUNGRY, "out of provisions: " + CitadelLife.stage(citadel));
            helper.assertFalse(CitadelLife.warded(realm, inside), "the wards drop first");
            helper.assertTrue(CitadelLife.workingTier(citadel, CitadelLife.WORKSHOPS) == 1, "the workshops still work");
            CitadelLife.eat(realm.getServer(), citadel, Math.round(upkeep.starvingDays() * CitadelUpkeep.DAY_TICKS));
            helper.assertTrue(CitadelLife.stage(citadel) == Neglect.STARVING, "starving");
            helper.assertTrue(CitadelLife.workingTier(citadel, CitadelLife.WORKSHOPS) == 0, "the workshops slow");
            CitadelLife.eat(realm.getServer(), citadel, CitadelUpkeep.DAY_TICKS * 30);
            helper.assertTrue(CitadelLife.stage(citadel) == Neglect.FORSAKEN, "forsaken");
            helper.assertTrue(citadel.hold() == CitadelData.Hold.CLAN && citadel.clan().filter(clan.id()::equals).isPresent(),
                    "neglect never loses the Citadel");

            CitadelLife.feed(realm.getServer(), citadel, 10);
            helper.assertTrue(CitadelLife.stage(citadel) == Neglect.FED && citadel.hungryTicks() == 0, "one meal sets it right");
            helper.assertTrue(CitadelLife.warded(realm, inside), "and the wards rise again");

            ClanService.leave(recruit);
            ClanService.leave(founder);
            CitadelGameTest.home(helper, founder, recruit);
            CitadelGameTest.release(realm, centre);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void upgradesArePaidForAtTheGatewayAndChangeTheCitadel(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        BlockPos centre = new BlockPos(5300, 100, 4000);
        CitadelData.Citadel citadel = CitadelGameTest.raise(realm, centre);
        CitadelGameTest.ready(helper, realm, centre, () -> {
            ServerPlayer founder = claimant(helper, realm, citadel);
            helper.assertFalse(CitadelLife.warded(realm, citadel.gateway().offset(10, 0, 0)), "nothing is warded before the Wards");
            String refused = CitadelLife.upgrade(founder, citadel, CitadelLife.WARDS);
            helper.assertTrue(citadel.tier(CitadelLife.WARDS) == 0, "unpaid, nothing is built: " + refused);

            founder.getInventory().add(new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 8));
            founder.getInventory().add(new ItemStack(Items.STONE_BRICKS, 32));
            String built = CitadelLife.upgrade(founder, citadel, CitadelLife.WARDS);
            helper.assertTrue(citadel.tier(CitadelLife.WARDS) == 1, "Wards I stands: " + built);
            helper.assertTrue(CitadelLife.carried(founder, stack -> stack.is(ModItems.DORMANT_SOUL_SHARD.get())) == 0
                    && CitadelLife.carried(founder, stack -> stack.is(Items.STONE_BRICKS)) == 0, "its cost was taken");
            double radius = CitadelLife.value(citadel, CitadelLife.WARDS, "radius");
            helper.assertTrue(CitadelLife.warded(realm, citadel.gateway().offset((int) radius - 2, 0, 0)), "inside the wards");
            helper.assertFalse(CitadelLife.warded(realm, citadel.gateway().offset((int) radius + 8, 0, 0)), "outside them");

            // The Hall sets how many may anchor here.
            int slots = CitadelLife.anchorSlots(citadel);
            citadel.tiers.put(CitadelLife.HALL, 1);
            helper.assertTrue(CitadelLife.anchorSlots(citadel) > slots, "the Hall makes room");
            helper.assertTrue(citadel.anchored().contains(founder.getUUID()), "the claimant is counted at the Hall");

            // Workshops raise a hearth and a table, and help on the clan's land.
            citadel.tiers.put(CitadelLife.WORKSHOPS, 1);
            CitadelLife.built(realm, citadel, CitadelLife.WORKSHOPS);
            BlockPos floor = citadel.gateway().below();
            helper.assertTrue(realm.getBlockState(floor.offset(-6, 0, -2)).is(SmithingRegistry.FORGE_HEARTH.get()), "a Forge Hearth");
            helper.assertTrue(realm.getBlockState(floor.offset(6, 0, 0)).is(WeavingRegistry.WEAVING_TABLE.get()), "a Weaving Table");
            helper.assertTrue(CitadelLife.forgeBonus(founder) > 0, "forging on the clan's land scores higher");
            helper.assertTrue(CitadelLife.weaveEssencePercent(founder) < 100, "weaving there costs less essence");

            // A damaged upgrade works a tier lower until repaired.
            citadel.damaged.add(CitadelLife.WORKSHOPS);
            helper.assertTrue(CitadelLife.forgeBonus(founder) == 0, "damaged Workshops I gives nothing");
            founder.getInventory().add(new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 64));
            founder.getInventory().add(new ItemStack(Items.OAK_LOG, 64));
            founder.getInventory().add(new ItemStack(Items.IRON_INGOT, 64));
            String repaired = CitadelLife.repair(founder, citadel, CitadelLife.WORKSHOPS);
            helper.assertFalse(citadel.damaged(CitadelLife.WORKSHOPS), "repaired: " + repaired);
            helper.assertTrue(CitadelLife.carried(founder, stack -> stack.is(Items.IRON_INGOT)) == 64 - 8, "for half the tier's cost");

            ClanService.leave(founder);
            CitadelGameTest.home(helper, founder);
            CitadelGameTest.release(realm, centre);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void gatewayLinksCarryAMemberBetweenTheClansCitadelsForEssence(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        BlockPos first = new BlockPos(5600, 100, 4000);
        BlockPos second = new BlockPos(5600, 100, 4200);
        CitadelData.Citadel home = CitadelGameTest.raise(realm, first);
        CitadelData.Citadel away = CitadelGameTest.raise(realm, second);
        CitadelGameTest.ready(helper, realm, second, () -> {});
        CitadelGameTest.ready(helper, realm, first, () -> {
            ServerPlayer founder = claimant(helper, realm, home);
            CitadelService.conquer(realm, away, Optional.of(founder));
            helper.assertTrue(CitadelService.claim(founder, away), "the clan holds a second Citadel");
            CitadelGameTest.home(helper, founder);
            founder.teleportTo(realm, home.gateway().getX() + 2.5, home.gateway().getY(), home.gateway().getZ() + 2.5, java.util.Set.of(), 0, 0);

            helper.assertTrue(CitadelLife.travelCost(home) < 0, "no Links, no travel");
            helper.assertFalse(CitadelLife.travel(founder, home, away.id()).isEmpty(), "refused without Links");

            home.tiers.put(CitadelLife.LINKS, 1);
            EssenceService.fill(founder);
            float before = EssenceService.current(founder);
            int cost = CitadelLife.travelCost(home);
            String result = CitadelLife.travel(founder, home, away.id());
            helper.assertTrue(result.isEmpty(), "the Links carry them: " + result);
            helper.assertTrue(founder.blockPosition().closerThan(away.gateway(), 4), "beside the other Gateway: " + founder.blockPosition());
            helper.assertTrue(Math.abs(before - cost - EssenceService.current(founder)) < 1.0F, "for the trip's essence");

            ClanService.leave(founder);
            CitadelGameTest.home(helper, founder);
            CitadelGameTest.release(realm, first);
            CitadelGameTest.release(realm, second);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void aSeedBloomingAtTheWallsRaidsTheCitadelAndALostRaidDamagesUpgrades(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        BlockPos centre = new BlockPos(6200, 100, 4000);
        CitadelData.Citadel citadel = CitadelGameTest.raise(realm, centre);
        CitadelGameTest.ready(helper, realm, centre, () -> {
            ServerPlayer founder = claimant(helper, realm, citadel);
            citadel.tiers.put(CitadelLife.WARDS, 2);
            citadel.tiers.put(CitadelLife.HALL, 1);
            double provisions = citadel.provisions();

            SeedData.Seed seed = SeedService.plant(realm, citadel.gateway().offset(-4, -1, 4), null)
                    .orElseThrow(() -> new AssertionError("no ground for a Seed in the courtyard"));
            helper.assertTrue(SeedService.bloomNow(realm, seed), "the Seed blooms");
            CitadelData.Raid raid = citadel.raid().orElseThrow(() -> new AssertionError("a Seed blooming at the walls raids the Citadel"));
            GateEvent gate = GateService.byId(realm, raid.gate()).orElseThrow();
            helper.assertTrue(gate.trigger() == GateTrigger.CITADEL_RAID, "a raid Gate, in the Dream Realm");
            helper.assertTrue(gate.source().filter(citadel.id()::equals).isPresent(), "against this Citadel");
            helper.assertTrue(SpellCallings.data(founder).history().stream().anyMatch(entry -> entry.calling().equals(Calling.CITADEL_RAID.id())),
                    "the Spell calls the clan to it");

            CitadelRaids.tick(realm, citadel);
            helper.assertTrue(raid.defenders().contains(founder.getUUID()), "a member at the walls defends it");

            CitadelRaids.lose(realm, citadel, "test");
            helper.assertTrue(citadel.raid().isEmpty(), "the raid is over");
            helper.assertTrue(citadel.damaged(CitadelLife.WARDS) && citadel.damaged(CitadelLife.HALL), "its upgrades are damaged");
            helper.assertTrue(CitadelLife.workingTier(citadel, CitadelLife.WARDS) == 1, "damaged Wards II works as Wards I");
            helper.assertTrue(citadel.provisions() < provisions, "provisions spoiled");
            helper.assertTrue(citadel.hold() == CitadelData.Hold.CLAN, "a lost raid never loses the Citadel");
            helper.assertTrue(GateService.byId(realm, gate.id()).map(found -> found.phase() == GateEvent.Phase.CLOSED).orElse(true),
                    "its Gate closes");

            founder.getInventory().add(new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 64));
            founder.getInventory().add(new ItemStack(Items.STONE_BRICKS, 64));
            founder.getInventory().add(new ItemStack(Items.OAK_PLANKS, 64));
            CitadelLife.repair(founder, citadel, CitadelLife.HALL);
            helper.assertFalse(citadel.damaged(CitadelLife.HALL), "repaired at the Gateway");

            ClanService.leave(founder);
            CitadelGameTest.home(helper, founder);
            CitadelGameTest.release(realm, centre);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void holdingARaidPaysEachDefenderAndTheSpellRemembers(GameTestHelper helper) {
        ServerLevel realm = CitadelGameTest.realm(helper);
        // Inside the Ashen Expanse, so the shards it pays are Dormant ones.
        BlockPos centre = new BlockPos(-700, 100, 300);
        CitadelData.Citadel citadel = CitadelGameTest.raise(realm, centre);
        CitadelGameTest.ready(helper, realm, centre, () -> {
            ServerPlayer founder = claimant(helper, realm, citadel);
            helper.assertTrue(CitadelRaids.begin(realm, citadel, citadel.gateway().offset(0, -1, 30), GateForecast.certain(1)), "a raid begins");
            CitadelRaids.tick(realm, citadel);
            GateEvent gate = GateService.byId(realm, citadel.raid().orElseThrow().gate()).orElseThrow();
            int heldBefore = SpellRecord.get(realm.getServer()).tally(founder.getUUID()).raidsHeld();

            CitadelRaids.onGateClosed(realm, gate, true);
            GateService.close(realm, gate.id());
            helper.assertTrue(citadel.raid().isEmpty(), "the raid is over");
            int shards = CitadelLife.carried(founder, stack -> stack.is(ModItems.DORMANT_SOUL_SHARD.get()));
            helper.assertTrue(shards == CitadelContentLoader.content().raids().shardsPerCategory(), "the defender is paid in shards: " + shards);
            helper.assertTrue(SpellRecord.get(realm.getServer()).tally(founder.getUUID()).raidsHeld() == heldBefore + 1,
                    "the Spell's record keeps it for their next appraisal");
            helper.assertTrue(TrueNameContent.get().best(deed -> deed.matchesRaid(true)).isPresent(),
                    "holding a Citadel alone is a deed the Spell may name");
            helper.assertTrue(citadel.hold() == CitadelData.Hold.CLAN, "still the clan's");

            ClanService.leave(founder);
            CitadelGameTest.home(helper, founder);
            CitadelGameTest.release(realm, centre);
            helper.succeed();
        });
    }

    /** A dreamer who slays the Guardian (by command) and claims the Citadel for a clan of one. */
    static ServerPlayer claimant(GameTestHelper helper, ServerLevel realm, CitadelData.Citadel citadel) {
        ServerPlayer founder = CitadelGameTest.dreamer(helper, realm, citadel);
        citadel.guardian().map(realm::getEntity).ifPresent(net.minecraft.world.entity.Entity::discard);
        CitadelService.conquer(realm, citadel, Optional.of(founder));
        if (!CitadelService.claim(founder, citadel)) {
            throw new AssertionError("the founder could not claim " + citadel.name());
        }
        return founder;
    }
}
