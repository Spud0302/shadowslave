package dev.spud.shadowslave.dreamrealm.seed;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.block.ModBlocks;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.dreamrealm.DreamRealmContent;
import dev.spud.shadowslave.dreamrealm.DreamRealmRegistry;
import dev.spud.shadowslave.dreamrealm.DreamRealmRules;
import dev.spud.shadowslave.gate.GateData;
import dev.spud.shadowslave.gate.GateEvent;
import dev.spud.shadowslave.gate.GateService;
import dev.spud.shadowslave.gate.GateTrigger;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SeedGameTest {
    /** Each test plants in its own far-off corner of the Dream Realm so their Gates never meet. */
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void aSeedGrowsThenBloomsIntoAnOverworldGate(GameTestHelper helper) {
        ServerLevel realm = realm(helper);
        SeedData.Seed seed = plantNear(helper, realm, new BlockPos(300, 0, 300));
        helper.assertTrue(realm.getBlockState(seed.pos()).is(DreamRealmRegistry.NIGHTMARE_SEED.get()), "the Seed block grows");
        helper.assertTrue(seed.region().getPath().equals("ashen_expanse"), "the Seed takes its region from the data pack");

        long growth = DreamRealmContent.get().seeds().growthTicks();
        SeedService.addGrowth(realm, seed, growth / 2);
        helper.assertTrue(realm.getBlockState(seed.pos()).getValue(NightmareSeedBlock.STAGE) == 2, "half grown shows stage 2");

        // Growth speed is a server option: 0% holds a Seed still.
        var speed = realm.getServer().getGameRules().getRule(DreamRealmRules.SEED_GROWTH);
        speed.set(0, realm.getServer());
        long before = seed.grownTicks();
        SeedService.grow(realm, 20L * DreamRealmRules.seedGrowthPercent(realm.getServer()));
        helper.assertTrue(seed.grownTicks() == before, "0% growth does not grow");
        speed.set(100, realm.getServer());

        SeedService.addGrowth(realm, seed, growth);
        SeedService.grow(realm, 20L * 100);
        helper.assertTrue(SeedData.get(realm).byId(seed.id()).isEmpty(), "a ripe Seed blooms and is spent");
        helper.assertTrue(realm.getBlockState(seed.pos()).isAir(), "the Seed block is gone");

        ServerLevel overworld = realm.getServer().overworld();
        int ratio = DreamRealmRules.ratio(realm.getServer());
        GateEvent gate = GateData.get(overworld).gates().stream().filter(g -> g.source().filter(seed.id()::equals).isPresent())
                .findFirst().orElseThrow(() -> new AssertionError("no Gate opened in the Overworld"));
        helper.assertTrue(gate.trigger() == GateTrigger.SEED_BLOOM, "a bloom Gate");
        helper.assertTrue(gate.center().getX() == seed.pos().getX() * ratio && gate.center().getZ() == seed.pos().getZ() * ratio,
                "at the matching Overworld spot: " + gate.center().toShortString());
        helper.assertTrue(gate.forecast().odds().keySet().stream().allMatch(c -> c == 1 || c == 2), "the region's forecast");
        GateService.close(overworld, gate.id());
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void aLitDreamCandleWardsTheBloom(GameTestHelper helper) {
        ServerLevel realm = realm(helper);
        int ratio = DreamRealmRules.ratio(realm.getServer());
        // A Seed over this very test room, and a lit Dream Candle in the room.
        BlockPos room = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.setBlock(new BlockPos(2, 1, 2), ModBlocks.DREAM_CANDLE.get().defaultBlockState().setValue(CandleBlock.LIT, true));
        BlockPos column = DreamRealm.toRealm(room, ratio);
        SeedData.Seed seed = plantNear(helper, realm, column);
        helper.assertTrue(SeedService.wardingCandle(helper.getLevel(), DreamRealm.toOverworld(seed.pos(), ratio), 48)
                        .filter(helper.absolutePos(new BlockPos(2, 1, 2))::equals).isPresent(),
                "the lit candle is found as a point of interest");
        helper.assertTrue(SeedService.bloomNow(realm, seed), "the Seed is spent");
        helper.assertTrue(GateData.get(helper.getLevel()).gates().stream().noneMatch(g -> g.source().filter(seed.id()::equals).isPresent()),
                "no Gate opens where a Dream Candle burns");
        helper.assertTrue(realm.getBlockState(seed.pos()).isAir(), "the Seed withers");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void cleansingAYoungSeedPaysOutAndEndsIt(GameTestHelper helper) {
        ServerLevel realm = realm(helper);
        SeedData.Seed seed = plantNear(helper, realm, new BlockPos(-300, 0, 300));
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        helper.assertFalse(SeedService.challenge(player, seed.pos()), "a Dormant player cannot challenge a Seed");
        SoulService.awaken(player);
        player.teleportTo(realm, seed.pos().getX() + 3.5, seed.pos().getY(), seed.pos().getZ() + 0.5, java.util.Set.of(), 0, 0);
        helper.assertTrue(SeedService.challenge(player, seed.pos()), "an Awakened player challenges it");
        GateEvent gate = GateData.get(realm).gates().stream().filter(g -> g.source().filter(seed.id()::equals).isPresent())
                .findFirst().orElseThrow(() -> new AssertionError("the cleansing Gate opens at the Seed"));
        helper.assertTrue(gate.trigger() == GateTrigger.SEED_CLEANSE && gate.center().equals(seed.pos().below()), "at the Seed");
        helper.assertTrue(seed.cleansing(), "the Seed waits on its Gate");

        // An operator closing the Gate gives the Seed back.
        GateService.close(realm, gate.id());
        helper.assertFalse(seed.cleansing(), "the Seed is free again");
        helper.assertTrue(realm.getBlockState(seed.pos()).is(DreamRealmRegistry.NIGHTMARE_SEED.get()), "the Seed returns");

        // Challenge again and win: the Seed is cleansed for good.
        helper.assertTrue(SeedService.challenge(player, seed.pos()), "challenged again");
        GateEvent second = GateData.get(realm).gates().stream().filter(g -> g.source().filter(seed.id()::equals).isPresent())
                .findFirst().orElseThrow();
        // A Seed challenged at stage 0 pays three rolls of its region's loot, 2-4 Dormant shards each.
        long shards = SeedService.rollReward(realm, seed).stream()
                .filter(stack -> stack.getItem() == dev.spud.shadowslave.item.ModItems.DORMANT_SOUL_SHARD.get())
                .mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(seed.cleansingStage == 0 && shards >= 6, "three rolls of 2-4 Dormant shards: " + shards);
        SeedService.onGateClosed(realm, second, true);
        helper.assertTrue(SeedData.get(realm).byId(seed.id()).isEmpty(), "a cleansed Seed is gone for good");
        GateService.close(realm, second.id());
        DreamRealmTestPlayers.home(player, helper);
        helper.succeed();
    }

    static ServerLevel realm(GameTestHelper helper) {
        ServerLevel realm = DreamRealm.level(helper.getLevel().getServer());
        if (realm == null) {
            throw new AssertionError("the Dream Realm dimension is missing");
        }
        return realm;
    }

    /** Plants a Seed on the surface somewhere around {@code column}, generating the ground first. */
    static SeedData.Seed plantNear(GameTestHelper helper, ServerLevel realm, BlockPos column) {
        // A saved test world may still hold a Seed from an earlier run here.
        SeedData.get(realm).seeds.removeIf(old -> Math.abs(old.pos.getX() - column.getX()) < 128
                && Math.abs(old.pos.getZ() - column.getZ()) < 128);
        for (int dx = 0; dx < 16; dx += 3) {
            for (int dz = 0; dz < 16; dz += 3) {
                int x = column.getX() + dx;
                int z = column.getZ() + dz;
                realm.getChunk(x >> 4, z >> 4);
                BlockPos pos = new BlockPos(x, realm.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
                Optional<SeedData.Seed> seed = SeedService.plant(realm, pos, null);
                if (seed.isPresent()) {
                    return seed.get();
                }
            }
        }
        throw new AssertionError("no ground for a Seed near " + column.toShortString());
    }
}
