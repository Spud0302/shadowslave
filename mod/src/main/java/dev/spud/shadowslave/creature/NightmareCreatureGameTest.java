package dev.spud.shadowslave.creature;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.item.SoulShardItem;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.soul.core.ShardDrops;
import dev.spud.shadowslave.soul.core.ShardRank;
import dev.spud.shadowslave.world.entity.ChainbackEntity;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NightmareCreatureGameTest {
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void aCreatureLeavesOneShardPerCore(GameTestHelper helper) {
        ServerPlayer killer = player(helper, SoulRank.DORMANT);
        ChainbackEntity chainback = helper.spawnWithNoFreeWill(NightmareCreatureEntities.CHAINBACK.get(), new BlockPos(2, 1, 2));
        helper.assertTrue(chainback.rank() == CreatureRank.AWAKENED && chainback.creatureClass() == CreatureClass.MONSTER,
                "Chainback is an Awakened Monster");
        chainback.die(killer.damageSources().playerAttack(killer));
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(chainback.blockPosition()).inflate(3));
        int shards = drops.stream().filter(item -> item.getItem().is(ModItems.AWAKENED_SOUL_SHARD.get()))
                .mapToInt(item -> item.getItem().getCount()).sum();
        helper.assertTrue(shards == 2, "two cores, two Awakened shards: " + shards);
        helper.assertTrue(drops.stream().noneMatch(item -> SoulShardItem.isFaint(item.getItem())), "creature shards are not faint");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void rankAndClassRescaleStats(GameTestHelper helper) {
        ChainbackEntity chainback = helper.spawnWithNoFreeWill(NightmareCreatureEntities.CHAINBACK.get(), new BlockPos(2, 1, 2));
        float awakenedHealth = chainback.getMaxHealth();
        helper.assertTrue(awakenedHealth == (float) ChainbackEntity.STATS.health(), "native stats at native rank");
        chainback.setRankAndClass(CreatureRank.DORMANT, CreatureClass.BEAST);
        helper.assertTrue(chainback.getMaxHealth() < awakenedHealth / 2, "a Dormant Beast copy is far weaker");
        chainback.setRankAndClass(CreatureRank.FALLEN, CreatureClass.MONSTER);
        helper.assertTrue(chainback.getMaxHealth() > awakenedHealth * 1.5, "a Fallen copy is far stronger");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void vanillaShardsAreFaint(GameTestHelper helper) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
        helper.assertTrue(ShardDrops.dropMobShard(helper.getLevel(), zombie, new LegacyRandomSource(1) {
            @Override
            public float nextFloat() {
                return 0.0F;
            }
        }), "the zombie drops a shard");
        ItemEntity drop = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(zombie.blockPosition()).inflate(2))
                .stream().findFirst().orElseThrow();
        ItemStack faint = drop.getItem();
        helper.assertTrue(SoulShardItem.isFaint(faint), "a vanilla shard is faint");
        helper.assertTrue(SoulShardItem.units(faint, ShardRank.DORMANT) == 1, "worth a quarter of a Dormant shard");

        ServerPlayer dormant = player(helper, SoulRank.DORMANT);
        helper.assertTrue(CoreService.absorbFromStack(dormant, faint.copy(), ShardRank.DORMANT, false) == 1
                && CoreService.saturation(dormant) == 1, "a Dormant core still takes it");
        // No player can be Ascended yet; CoreRulesTest covers the cut-off.
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper, SoulRank rank) {
        @SuppressWarnings("removal")
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 1, 2.5)));
        SoulService.awakenThroughRitual(player, ResourceLocation.fromNamespaceAndPath("shadowslave", "test/aspect"),
                rank, ResourceLocation.fromNamespaceAndPath("shadowslave", "test/flaw"));
        CoreService.refresh(player);
        return player;
    }
}
