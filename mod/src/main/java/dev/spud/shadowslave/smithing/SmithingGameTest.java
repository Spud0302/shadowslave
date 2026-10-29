package dev.spud.shadowslave.smithing;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.weaving.WeavingRecipe;
import dev.spud.shadowslave.weaving.WeavingRegistry;
import dev.spud.shadowslave.weaving.WeavingRules;
import dev.spud.shadowslave.weaving.WeavingService;
import dev.spud.shadowslave.weaving.WovenMemoryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SmithingGameTest {
    private static final ResourceLocation FORGING = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "forging");
    private static final BlockPos HEARTH = new BlockPos(1, 1, 1);
    private static final BlockPos ANVIL = new BlockPos(3, 1, 1);

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void anIronSwordIsHeatedHammeredAndQuenched(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ForgeHearthBlockEntity hearth = hearth(helper);
        ForgeHearthMenu menu = menu(player, hearth);
        hearth.setItem(0, new ItemStack(Items.IRON_INGOT, 2));
        hearth.setItem(1, new ItemStack(Items.STICK));
        WeavingRules.Attempt fused = SmithingService.fuse(player, menu);
        helper.assertTrue(fused.wove(), "the crucible fuses: " + fused.outcome());
        ItemStack billetStack = hearth.getItem(ForgeHearthBlockEntity.BILLET_SLOT);
        BilletData billet = billetStack.get(SmithingRegistry.BILLET.get());
        helper.assertTrue(billet != null && billet.heatMin() == 900 && billet.blows() == 6, "an iron sword billet: " + billet);
        helper.assertTrue(hearth.getItem(0).isEmpty() && hearth.getItem(1).isEmpty(), "the crucible is used up");

        // Fresh from the crucible it is cold: the hammer does nothing.
        SmithingAnvilBlockEntity anvil = anvil(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, hearth.removeItem(ForgeHearthBlockEntity.BILLET_SLOT, 1));
        SmithingService.layOnAnvil(player, anvil, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the billet lies on the anvil");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SmithingRegistry.SMITHS_HAMMER.get()));
        helper.assertTrue(SmithingService.strike(player, anvil, 0.5F).isEmpty(), "too cold to strike");

        // Heated to yellow, six blows on the mark shape it.
        long now = helper.getLevel().getGameTime();
        ItemStack piece = anvil.piece();
        piece.set(SmithingRegistry.BILLET.get(), piece.get(SmithingRegistry.BILLET.get()).measured(1200, now, 100));
        for (int i = 0; i < 6; i++) {
            player.getCooldowns().removeCooldown(SmithingRegistry.SMITHS_HAMMER.get());
            BilletData before = anvil.piece().get(SmithingRegistry.BILLET.get());
            Optional<SmithingRules.Blow> blow = SmithingService.strike(player, anvil, before.target());
            helper.assertTrue(blow.equals(Optional.of(SmithingRules.Blow.CLEAN)), "blow " + i + " lands clean: " + blow);
        }
        BilletData shaped = anvil.piece().get(SmithingRegistry.BILLET.get());
        helper.assertTrue(shaped.shaped() && shaped.inBand() == 6, "shaped, every blow in the band: " + shaped);

        // Held to the hiss, the quench is perfect.
        player.setItemInHand(InteractionHand.MAIN_HAND, anvil.take());
        SmithingService.quench(player, InteractionHand.MAIN_HAND, shaped.quenchTicks());
        ItemStack sword = player.getMainHandItem();
        helper.assertTrue(sword.is(Items.IRON_SWORD), "an iron sword: " + sword);
        ForgedData forged = sword.get(SmithingRegistry.FORGED.get());
        helper.assertTrue(forged != null && forged.quality() == Quality.MASTERWORK && forged.score() == 100, "a perfect piece: " + forged);
        helper.assertTrue(sword.getMaxDamage() == Math.round(250 * 1.3F), "durability scaled: " + sword.getMaxDamage());
        helper.assertTrue(WeavingService.get(player).xp(FORGING) == 40, "first forge pays five times: " + WeavingService.get(player).xp(FORGING));
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void coldWorkNeedsNoFireAndFinishesOnTheAnvil(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ForgeHearthBlockEntity hearth = hearth(helper);
        hearth.setItem(0, new ItemStack(Items.LEATHER, 3));
        hearth.setItem(1, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get()));
        helper.assertTrue(SmithingService.fuse(player, menu(player, hearth)).wove(), "a Sleeper's Hood is laid out");
        SmithingAnvilBlockEntity anvil = anvil(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, hearth.removeItem(ForgeHearthBlockEntity.BILLET_SLOT, 1));
        SmithingService.layOnAnvil(player, anvil, InteractionHand.MAIN_HAND);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SmithingRegistry.SMITHS_HAMMER.get()));
        int misses = 0;
        for (int i = 0; i < 40 && anvil.piece().has(SmithingRegistry.BILLET.get()); i++) {
            player.getCooldowns().removeCooldown(SmithingRegistry.SMITHS_HAMMER.get());
            BilletData before = anvil.piece().get(SmithingRegistry.BILLET.get());
            // Every other blow lands far from the mark.
            float marker = i % 2 == 0 ? before.target() : (before.target() > 0.5F ? 0.0F : 1.0F);
            if (SmithingService.strike(player, anvil, marker).equals(Optional.of(SmithingRules.Blow.MISS))) {
                misses++;
            }
        }
        ItemStack hood = anvil.piece();
        helper.assertTrue(hood.is(WeavingRegistry.SLEEPERS_HOOD.get()), "the hood waits on the anvil: " + hood);
        ForgedData forged = hood.get(SmithingRegistry.FORGED.get());
        helper.assertTrue(misses == 4 && forged != null && forged.quality() == Quality.FINE, "four misses cost it: " + misses + " " + forged);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void theHearthHeatsABilletAndALegacyPieceIsGradedFair(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ForgeHearthBlockEntity hearth = hearth(helper);
        hearth.setItem(0, new ItemStack(Items.IRON_INGOT, 2));
        hearth.setItem(1, new ItemStack(Items.STICK));
        SmithingService.fuse(player, menu(player, hearth));
        hearth.setItem(ForgeHearthBlockEntity.FUEL_SLOT, new ItemStack(Items.COAL, 4));
        ItemStack old = new ItemStack(WeavingRegistry.SHARD_KNIFE.get());
        old.set(WeavingRegistry.WOVEN_MEMORY.get(), new dev.spud.shadowslave.memory.MemoryInstanceData(
                ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "memory/shard_knife"), "Shard Knife", "weaving", "Woven by Jack"));
        WovenMemoryItems.grade(old, helper.getLevel());
        helper.runAfterDelay(140, () -> {
            int heat = hearth.getItem(ForgeHearthBlockEntity.BILLET_SLOT).get(SmithingRegistry.BILLET.get()).heatAt(helper.getLevel().getGameTime());
            helper.assertTrue(heat >= 500, "the fire heats the billet: " + heat);
            helper.assertTrue(hearth.getBlockState().getValue(ForgeHearthBlock.LIT), "the hearth is lit");
            ForgedData legacy = old.get(SmithingRegistry.FORGED.get());
            helper.assertTrue(legacy != null && legacy.quality() == Quality.FAIR && legacy.smith().equals("Jack"), "graded Fair: " + legacy);
            helper.assertTrue(old.get(DataComponents.MAX_DAMAGE) == null || old.getMaxDamage() == 300, "its stats unchanged");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void emberHeatsABilletInHandAndTideQuenchesInAnyWater(GameTestHelper helper) {
        WeavingRecipe.Forging blade = new WeavingRecipe.Forging(900, 1250, 6, true, 24);
        ResourceLocation sword = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "iron_sword");
        long now = helper.getLevel().getGameTime();

        ServerPlayer ember = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        ItemStack cold = new ItemStack(SmithingRegistry.BILLET_ITEM.get());
        cold.set(SmithingRegistry.BILLET.get(), BilletData.lay(sword, "Iron Sword", blade, now, 70, 1, false, "Jack"));
        ember.setItemInHand(InteractionHand.MAIN_HAND, cold);
        helper.assertTrue(SmithingService.emberHeat(ember, InteractionHand.MAIN_HAND), "Ember's hands take the billet");
        int heat = BilletItem.data(ember.getMainHandItem()).heatAt(now);
        helper.assertTrue(heat == SmithingRules.AMBIENT + SmithingService.EMBER_RISE, "it warms by one step: " + heat);

        ServerPlayer plain = player(helper);
        plain.setItemInHand(InteractionHand.MAIN_HAND, cold.copy());
        helper.assertFalse(SmithingService.emberHeat(plain, InteractionHand.MAIN_HAND), "anyone else needs a hearth");

        ServerPlayer tide = AbilityGameTest.playerWith(helper, AspectKitCatalog.TIDE);
        BilletData fresh = BilletData.lay(sword, "Iron Sword", blade, now, 70, 1, false, "Jack");
        BilletData shaped = new BilletData(fresh.pattern(), fresh.name(), 1100, now, 100, 900, 1250, 6, true, 24, 70, 1,
                6, 0, 0, 6, 0, false, false, "Jack");
        ItemStack hot = new ItemStack(SmithingRegistry.BILLET_ITEM.get());
        hot.set(SmithingRegistry.BILLET.get(), shaped);
        tide.setItemInHand(InteractionHand.MAIN_HAND, hot);
        helper.assertTrue(SmithingService.tideQuench(tide, InteractionHand.MAIN_HAND), "Tide quenches in plain water");
        ForgedData forged = tide.getMainHandItem().get(SmithingRegistry.FORGED.get());
        helper.assertTrue(forged != null && forged.quality() == Quality.MASTERWORK, "a perfect quench on a perfect blade: " + forged);
        helper.succeed();
    }

    private static ForgeHearthBlockEntity hearth(GameTestHelper helper) {
        helper.setBlock(HEARTH, SmithingRegistry.FORGE_HEARTH.get());
        return (ForgeHearthBlockEntity) helper.getBlockEntity(HEARTH);
    }

    private static SmithingAnvilBlockEntity anvil(GameTestHelper helper) {
        helper.setBlock(ANVIL, SmithingRegistry.SMITHING_ANVIL.get());
        return (SmithingAnvilBlockEntity) helper.getBlockEntity(ANVIL);
    }

    private static ForgeHearthMenu menu(ServerPlayer player, ForgeHearthBlockEntity hearth) {
        return new ForgeHearthMenu(0, player.getInventory(), hearth, hearth.data, ContainerLevelAccess.NULL);
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
