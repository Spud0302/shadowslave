package dev.spud.shadowslave.fieldguide;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.dreamrealm.DreamRealmRules;
import dev.spud.shadowslave.dreamrealm.seed.SeedData;
import dev.spud.shadowslave.dreamrealm.seed.SeedService;
import dev.spud.shadowslave.network.payload.FieldGuidePinRequestPayload;
import dev.spud.shadowslave.network.payload.FieldGuideStatusPayload;
import dev.spud.shadowslave.world.entity.AshBurrowerEntity;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.Set;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FieldGuideGameTest {
    private static final ResourceLocation ASH_BURROWER = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "ash_burrower");

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void everyPlayerStartsWithOneGuide(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FieldGuideService.giveFirstGuide(player);
        FieldGuideService.giveFirstGuide(player);
        int guides = player.getInventory().countItem(FieldGuideRegistry.FIELD_GUIDE_ITEM.get());
        helper.assertTrue(guides == 1, "one guide, not one per login: " + guides);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void sightingsAndKillsFillACreaturePage(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        AshBurrowerEntity burrower = helper.spawnWithNoFreeWill(NightmareCreatureEntities.ASH_BURROWER.get(), new BlockPos(4, 1, 4));
        player.lookAt(EntityAnchorArgument.Anchor.EYES, burrower.getBoundingBox().getCenter());
        helper.assertTrue(FieldGuideService.inScope(player, 64).filter(burrower::equals).isPresent(), "the spyglass finds it");

        helper.assertTrue(FieldGuideService.recordSighting(player, burrower), "the first sighting counts");
        helper.assertFalse(FieldGuideService.recordSighting(player, burrower), "staring longer does not, until the cooldown passes");
        FieldGuideStatusPayload.Creature page = page(player);
        helper.assertTrue(page.sightings() == 1 && page.sections().size() == 1, "one section after one sighting: " + page);
        helper.assertTrue(page.sections().get(0).title().equals("Rank and Class"), "rank and class first");
        helper.assertTrue(page.ranks().contains("Dormant"), "met as Dormant");

        burrower.hurt(player.damageSources().playerAttack(player), 10_000.0F);
        page = page(player);
        helper.assertTrue(page.kills() == 1, "the kill counts: " + page.kills());
        helper.assertTrue(page.sections().stream().anyMatch(section -> section.title().equals("Habits")), "a kill reveals Habits");
        helper.assertTrue(page.locked() == 2 && !page.hint().isEmpty(), "two entries still hidden, with a hint: " + page.hint());
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void walkingFillsTheMapAndPinsCanBePlaced(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FieldGuideService.explore(player, true);
        String dimension = FieldGuideService.dimension(helper.getLevel());
        int chunks = FieldGuideService.get(player).exploredChunks(dimension).size();
        helper.assertTrue(chunks >= 1, "the chunks around the player are mapped: " + chunks);
        helper.assertTrue(FieldGuideService.get(player).exploredChunks(dimension).values().stream()
                .allMatch(colours -> colours.length == FieldGuideData.CHUNK_BYTES), "each chunk keeps its sampled colours");

        FieldGuideService.requestPin(player, new FieldGuidePinRequestPayload(FieldGuidePinRequestPayload.WARNING, dimension, 10, 20, "Burrowers"));
        helper.assertTrue(FieldGuideService.status(player, false).pins().stream()
                .anyMatch(pin -> pin.kind().equals("warning") && pin.label().equals("Burrowers")), "the warning is pinned");
        FieldGuideService.requestPin(player, new FieldGuidePinRequestPayload(FieldGuidePinRequestPayload.REMOVE, dimension, 11, 19, ""));
        helper.assertTrue(FieldGuideService.status(player, false).pins().stream().noneMatch(pin -> pin.kind().equals("warning")),
                "and removed again");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void aCreatureDeathRaisesTheAreasDanger(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        AshBurrowerEntity burrower = helper.spawnWithNoFreeWill(NightmareCreatureEntities.ASH_BURROWER.get(), new BlockPos(3, 1, 3));
        FieldGuideService.recordDeath(player, burrower);
        String dimension = FieldGuideService.dimension(helper.getLevel());
        int cell = FieldGuideContent.get().settings().dangerCell();
        int cellX = Math.floorDiv(player.getBlockX(), cell);
        int cellZ = Math.floorDiv(player.getBlockZ(), cell);
        Optional<FieldGuideStatusPayload.Danger> danger = FieldGuideService.status(player, false).danger().stream()
                .filter(d -> d.dimension().equals(dimension) && d.cellX() == cellX && d.cellZ() == cellZ).findFirst();
        helper.assertTrue(danger.map(FieldGuideStatusPayload.Danger::rating).orElse(-1) == CreatureRank.AWAKENED.ordinal(),
                "a Dormant killer plus one death rates the area Awakened: " + danger);
        burrower.discard();
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void aSeedPinShowsWhereItsGateWouldOpen(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ServerLevel realm = DreamRealm.level(helper.getLevel().getServer());
        helper.assertTrue(realm != null, "the Dream Realm exists");
        SeedData.Seed seed = plant(realm, new BlockPos(900, 0, -900));
        player.teleportTo(realm, seed.pos().getX() + 4.5, seed.pos().getY() + 1, seed.pos().getZ() + 0.5, Set.of(), 0, 0);
        player.hasChangedDimension();
        FieldGuideService.logPlaces(player);
        int ratio = DreamRealmRules.ratio(helper.getLevel().getServer());
        Optional<FieldGuideStatusPayload.Pin> pin = FieldGuideService.status(player, false).pins().stream()
                .filter(each -> each.kind().equals("seed")).findFirst();
        helper.assertTrue(pin.isPresent(), "the Seed is logged");
        helper.assertTrue(pin.get().gateX() == seed.pos().getX() * ratio && pin.get().gateZ() == seed.pos().getZ() * ratio,
                "its Gate spot is the 1:" + ratio + " match: " + pin.get());
        helper.assertTrue(pin.get().detail().contains(Integer.toString(seed.pos().getX() * ratio)), "and says so: " + pin.get().detail());

        SeedService.witherNow(realm, seed);
        helper.assertTrue(FieldGuideService.status(player, false).pins().stream().noneMatch(each -> each.kind().equals("seed")),
                "a spent Seed leaves the guide");
        Vec3 room = helper.absoluteVec(new Vec3(2.5, 1, 2.5));
        player.teleportTo(helper.getLevel(), room.x, room.y, room.z, Set.of(), 0, 0);
        helper.succeed();
    }

    private static SeedData.Seed plant(ServerLevel realm, BlockPos column) {
        for (int dx = 0; dx < 32; dx += 3) {
            for (int dz = 0; dz < 32; dz += 3) {
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

    private static FieldGuideStatusPayload.Creature page(ServerPlayer player) {
        return FieldGuideService.status(player, false).creatures().stream()
                .filter(creature -> creature.id().equals(ASH_BURROWER.toString())).findFirst()
                .orElseThrow(() -> new AssertionError("no Ash Burrower page"));
    }

    private static ServerPlayer player(GameTestHelper helper) {
        @SuppressWarnings("removal")
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
        return player;
    }
}
