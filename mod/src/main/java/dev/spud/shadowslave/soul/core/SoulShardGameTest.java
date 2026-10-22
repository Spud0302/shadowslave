package dev.spud.shadowslave.soul.core;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.item.ModDataComponents;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.item.ShardContents;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SoulShardGameTest {
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void mobsCarryTheirCreatureRank(GameTestHelper helper) {
        assertRank(helper, EntityType.ZOMBIE, Optional.of(ShardRank.DORMANT));
        assertRank(helper, EntityType.PILLAGER, Optional.of(ShardRank.AWAKENED));
        assertRank(helper, EntityType.BLAZE, Optional.of(ShardRank.AWAKENED));
        assertRank(helper, EntityType.WITHER_SKELETON, Optional.of(ShardRank.FALLEN));
        assertRank(helper, EntityType.EVOKER, Optional.of(ShardRank.FALLEN));
        assertRank(helper, EntityType.WARDEN, Optional.of(ShardRank.CORRUPTED));
        assertRank(helper, EntityType.PIG, Optional.empty());

        Mob armoured = helper.spawnWithNoFreeWill(EntityType.SKELETON, new BlockPos(2, 1, 2));
        armoured.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        helper.assertTrue(ShardDrops.rankOf(armoured).equals(Optional.of(ShardRank.AWAKENED)), "armour makes it Awakened");
        helper.assertTrue(ShardDrops.chance(helper.spawnWithNoFreeWill(EntityType.ELDER_GUARDIAN, new BlockPos(2, 1, 2)),
                ShardRank.FALLEN) == 1.0F, "elder guardians always drop");
        helper.succeed();
    }

    private static void assertRank(GameTestHelper helper, EntityType<? extends Mob> type, Optional<ShardRank> expected) {
        LivingEntity entity = helper.spawnWithNoFreeWill(type, new BlockPos(2, 1, 2));
        helper.assertTrue(ShardDrops.rankOf(entity).equals(expected), type + " should be " + expected);
        entity.discard();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void absorbingFillsTheCoreUntilSaturated(GameTestHelper helper) {
        ServerPlayer player = sleeper(helper);
        float baseHealth = player.getMaxHealth();
        ItemStack stack = new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.use(player.level(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(CoreService.saturation(player) == 4 && stack.getCount() == 2, "one shard absorbed");
        helper.assertTrue(player.getMaxHealth() > baseHealth, "the core adds a little health");

        CoreService.set(player, CoreRules.DORMANT_LIMIT - 2);
        player.setShiftKeyDown(true);
        stack.use(player.level(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(CoreService.saturated(player) && stack.getCount() == 1, "the core takes what fits, then stops");
        stack.use(player.level(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(stack.getCount() == 1, "a saturated core refuses more");
        helper.assertTrue(player.getMaxHealth() == baseHealth + CoreRules.HEALTH_PER_RANK, "full Dormant bonus");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void aPvpKillTearsAShardFromTheVictim(GameTestHelper helper) {
        ServerPlayer killer = sleeper(helper);
        ServerPlayer victim = sleeper(helper);
        CoreService.set(victim, 100);

        victim.die(killer.damageSources().playerAttack(killer));

        helper.assertTrue(CoreService.saturation(victim) == 75, "victim lost a quarter: " + CoreService.saturation(victim));
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(victim.blockPosition()).inflate(3));
        ItemEntity shard = drops.stream().filter(item -> item.getItem().is(ModItems.DORMANT_SOUL_SHARD.get()))
                .findFirst().orElse(null);
        helper.assertTrue(shard != null, "a Dormant shard dropped");
        ShardContents contents = shard.getItem().get(ModDataComponents.SHARD_CONTENTS.get());
        helper.assertTrue(contents != null && contents.units() == 25, "it carries the lost saturation: " + contents);

        CoreService.absorbFromStack(killer, shard.getItem(), ShardRank.DORMANT, false);
        helper.assertTrue(CoreService.saturation(killer) == 25, "the killer absorbs it");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void pvpShardsCanBeTurnedOff(GameTestHelper helper) {
        ServerPlayer killer = sleeper(helper);
        ServerPlayer victim = sleeper(helper);
        CoreService.set(victim, 100);
        helper.getLevel().getGameRules().getRule(ShardDrops.PVP_SHARDS).set(false, helper.getLevel().getServer());
        try {
            ItemStack dropped = ShardDrops.dropPlayerShard(helper.getLevel(), victim, killer);
            helper.assertTrue(dropped.isEmpty() && CoreService.saturation(victim) == 100, "nothing is torn away");
        } finally {
            helper.getLevel().getGameRules().getRule(ShardDrops.PVP_SHARDS).set(true, helper.getLevel().getServer());
        }
        helper.succeed();
    }

    private static ServerPlayer sleeper(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 1, 2.5)));
        SoulService.awakenThroughRitual(player, ResourceLocation.fromNamespaceAndPath("shadowslave", "test/aspect"),
                SoulRank.DORMANT, ResourceLocation.fromNamespaceAndPath("shadowslave", "test/flaw"));
        CoreService.refresh(player);
        return player;
    }
}
