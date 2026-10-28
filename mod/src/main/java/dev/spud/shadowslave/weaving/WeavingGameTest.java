package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.world.entity.ChainbackEntity;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WeavingGameTest {
    private static final ResourceLocation FORGING = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "forging");
    private static final ResourceLocation CHAINBACK_MAIL = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "chainback_mail");

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void theTableNoLongerMakesItems(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        WeavingMenu menu = new WeavingMenu(0, player.getInventory(), ContainerLevelAccess.NULL);
        menu.materials().setItem(0, new ItemStack(Items.IRON_INGOT, 2));
        menu.materials().setItem(1, new ItemStack(Items.STICK));
        menu.materials().setItem(2, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get()));
        WeavingService.weave(player, menu);
        helper.assertTrue(menu.output().isEmpty(), "nothing is woven: Smithing makes items now");
        helper.assertTrue(!menu.materials().isEmpty(), "and the materials stay");
        helper.assertTrue(WeavingService.lastResult(player).contains("Forge Hearth"), "it points to the forge: " + WeavingService.lastResult(player));
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void slayingAChainbackShowsItsPatternAndDropsLinks(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        helper.assertFalse(WeavingService.get(player).learned().contains(CHAINBACK_MAIL), "not known before the kill");
        ChainbackEntity chainback = helper.spawnWithNoFreeWill(NightmareCreatureEntities.CHAINBACK.get(), new BlockPos(2, 1, 2));
        chainback.hurt(player.damageSources().playerAttack(player), 10_000.0F);
        helper.assertTrue(WeavingService.get(player).learned().contains(CHAINBACK_MAIL), "the Spell shows Chainback Mail");
        helper.assertTrue(WeavingRules.grantsForKill(WeavingContent.current(), WeavingService.get(player),
                ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "chainback")).isEmpty(), "only the first kill grants it");
        int links = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(chainback.blockPosition()).inflate(3)).stream()
                .filter(item -> item.getItem().is(WeavingRegistry.CHAINBACK_LINK.get()))
                .mapToInt(item -> item.getItem().getCount()).sum();
        helper.assertTrue(links >= 1, "Chainback Links drop: " + links);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void forgingDataLoadsToLevelThree(GameTestHelper helper) {
        WeavingContent content = WeavingContent.current();
        helper.assertTrue(content.branch(FORGING).map(WeavingBranch::maxLevel).orElse(0) == 3, "Forging has three levels");
        helper.assertTrue(content.recipes().size() == 16, "sixteen Forging patterns: " + content.recipes().size());
        content.recipes().values().stream().filter(recipe -> recipe.memory().isPresent()).forEach(recipe -> helper.assertTrue(
                net.minecraft.core.registries.BuiltInRegistries.ITEM.get(recipe.result()) instanceof net.minecraft.world.item.Item item
                        && WeavingRegistry.memoryItems().containsValue(item), "result is a woven Memory: " + recipe.result()));
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        @SuppressWarnings("removal")
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 1, 2.5)));
        SoulService.awakenThroughRitual(player, ResourceLocation.fromNamespaceAndPath("shadowslave", "test/aspect"),
                SoulRank.AWAKENED, ResourceLocation.fromNamespaceAndPath("shadowslave", "test/flaw"));
        return player;
    }
}
