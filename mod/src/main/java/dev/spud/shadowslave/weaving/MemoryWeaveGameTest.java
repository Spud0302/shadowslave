package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.smithing.ForgedData;
import dev.spud.shadowslave.smithing.Quality;
import dev.spud.shadowslave.smithing.SmithingService;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The Memory weave, Bound, and the Weaving edges of Wellspring and Seer. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MemoryWeaveGameTest {
    private static final String ROOM = "ritual_test_room";

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void everyWeaverKnowsTheMemoryWeaveAndItMakesAMemory(GameTestHelper helper) {
        ServerPlayer player = player(helper, false);
        WeavingMenu menu = menu(player);
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, new ItemStack(Items.IRON_SWORD));
        menu.table().setItem(WeavingMenu.ANCHOR_SLOT, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 2));
        menu.table().setItem(WeavingMenu.FIRST_MATERIAL, new ItemStack(Items.ENDER_PEARL));
        helper.assertTrue(WeavingService.status(player).strands().stream().anyMatch(strand -> strand.id().equals(MemoryWeave.MEMORY_WEAVE.toString())),
                "the Memory weave is known without study");
        helper.assertTrue(StrandService.weave(player, menu, MemoryWeave.MEMORY_WEAVE, 1, Optional.empty(),
                WeavingGameTest.clean(MemoryWeave.MEMORY_WEAVE)) == 100, "laid: " + WeavingService.lastResult(player));
        helper.assertTrue(MemoryWeave.isMemory(menu.centre()), "the sword is a Memory now");
        helper.assertFalse(StrandService.lock(player, menu, MemoryWeave.BOUND, 1, Optional.empty()).isEmpty(),
                "Bound is not known without study");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aMemoryIsDismissedMendsAndIsSummonedBack(GameTestHelper helper) {
        ServerPlayer player = player(helper, false);
        ItemStack sword = memory(new ItemStack(Items.IRON_SWORD), false);
        sword.setDamageValue(100);
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        helper.assertTrue(MemoryWeave.toggle(player, false) == MemoryWeave.Outcome.DISMISSED, "dismissed");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "gone from the hand");
        helper.assertTrue(MemoryWeave.held(player).size() == 1, "held in the soul");
        MemoryWeave.mend(player);
        int mended = MemoryWeave.held(player).next().getDamageValue();
        helper.assertTrue(mended == 100 - StrandRules.mend(Items.IRON_SWORD.getDefaultInstance().getMaxDamage()), "it mends: " + mended);
        helper.assertTrue(MemoryWeave.toggle(player, false) == MemoryWeave.Outcome.SUMMONED, "summoned");
        helper.assertTrue(MemoryWeave.isMemory(player.getMainHandItem()) && player.getMainHandItem().getDamageValue() == mended,
                "the same sword, mended, is back in hand");
        helper.assertTrue(MemoryWeave.toggle(player, false) == MemoryWeave.Outcome.DISMISSED, "and away again");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aMemorySinksIntoTheSoulInsteadOfFalling(GameTestHelper helper) {
        ServerPlayer player = player(helper, false);
        List<ItemEntity> drops = new ArrayList<>();
        Vec3 at = player.position();
        drops.add(new ItemEntity(helper.getLevel(), at.x, at.y, at.z, memory(new ItemStack(Items.IRON_AXE), false)));
        drops.add(new ItemEntity(helper.getLevel(), at.x, at.y, at.z, new ItemStack(Items.IRON_PICKAXE)));
        LivingDropsEvent event = new LivingDropsEvent(player, player.damageSources().generic(), drops, false);
        MemoryWeave.onDrops(event);
        helper.assertTrue(event.getDrops().size() == 1 && event.getDrops().iterator().next().getItem().is(Items.IRON_PICKAXE),
                "only the plain pickaxe falls");
        helper.assertTrue(MemoryWeave.held(player).next().is(Items.IRON_AXE), "the axe waits in the soul");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aBoundMemoryBondsToItsFirstHandAndSlipsFromOthers(GameTestHelper helper) {
        ServerPlayer owner = player(helper, true);
        ServerPlayer thief = player(helper, true);
        ItemStack blade = memory(new ItemStack(Items.IRON_SWORD), true);
        owner.setItemInHand(InteractionHand.MAIN_HAND, blade);
        MemoryWeave.checkBonds(owner);
        BoundTo bond = owner.getMainHandItem().get(WeavingRegistry.BOUND_TO.get());
        helper.assertTrue(bond != null && bond.owner().equals(owner.getUUID()), "bonded to the first hand");
        thief.setItemInHand(InteractionHand.MAIN_HAND, owner.getMainHandItem().copy());
        owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        MemoryWeave.checkBonds(thief);
        helper.assertTrue(thief.getMainHandItem().isEmpty(), "it slips from another hand");
        helper.assertTrue(thief.getInventory().contains(stack -> MemoryWeave.isBound(stack)), "into the pack, not lost");
        helper.assertTrue(MemoryWeave.dismiss(thief, blade.copy()) == MemoryWeave.Outcome.REFUSED, "nor will it sink into the wrong soul");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void boundNeedsAMemoryAndUnbindingNeedsAnAwakenedShard(GameTestHelper helper) {
        ServerPlayer player = player(helper, true);
        WeavingService.set(player, WeavingService.get(player).withStudied(MemoryWeave.BOUND));
        WeavingMenu menu = menu(player);
        ItemStack plain = new ItemStack(Items.IRON_SWORD);
        SmithingService.stamp(plain, new ForgedData(Quality.FINE, 70, "Jack"));
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, plain);
        menu.table().setItem(WeavingMenu.ANCHOR_SLOT, new ItemStack(ModItems.AWAKENED_SOUL_SHARD.get(), 1));
        menu.table().setItem(WeavingMenu.FIRST_MATERIAL, new ItemStack(ModItems.AWAKENED_SOUL_SHARD.get(), 1));
        helper.assertTrue(StrandService.lock(player, menu, MemoryWeave.BOUND, 1, Optional.empty()).orElse("").contains("Memory"),
                "only a Memory can be Bound");
        ItemStack bound = memory(plain.copy(), true);
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, bound);
        menu.table().setItem(WeavingMenu.ANCHOR_SLOT, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 1));
        helper.assertFalse(StrandService.unravel(player, menu, 0), "the Memory weave stays while Bound");
        helper.assertFalse(StrandService.unravel(player, menu, 1), "a Dormant shard can't cut the bond");
        menu.table().setItem(WeavingMenu.ANCHOR_SLOT, new ItemStack(ModItems.AWAKENED_SOUL_SHARD.get(), 1));
        helper.assertTrue(StrandService.unravel(player, menu, 1), "an Awakened shard can: " + WeavingService.lastResult(player));
        helper.assertTrue(menu.anchor().isEmpty(), "the shard is spent");
        helper.assertTrue(MemoryWeave.isMemory(menu.centre()) && !MemoryWeave.isBound(menu.centre()), "still a Memory, no longer Bound");
        helper.assertTrue(menu.output().isEmpty(), "no thread comes of it");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void wellspringPaysLessAndASeerStudiesForFree(GameTestHelper helper) {
        ServerPlayer wellspring = AbilityGameTest.playerWith(helper, AspectKitCatalog.WELLSPRING);
        helper.assertTrue(StrandService.essencePercent(wellspring) == StrandRules.WELLSPRING_PERCENT, "Wellspring pays three quarters");
        WeavingMenu menu = new WeavingMenu(0, wellspring.getInventory(), ContainerLevelAccess.NULL);
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, new ItemStack(Items.IRON_SWORD));
        menu.table().setItem(WeavingMenu.ANCHOR_SLOT, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 1));
        menu.table().setItem(WeavingMenu.FIRST_MATERIAL, new ItemStack(Items.ENDER_PEARL));
        float before = EssenceService.current(wellspring);
        StrandService.weave(wellspring, menu, MemoryWeave.MEMORY_WEAVE, 1, Optional.empty(), WeavingGameTest.clean(MemoryWeave.MEMORY_WEAVE));
        int full = WeavingContent.current().strand(MemoryWeave.MEMORY_WEAVE).orElseThrow().essence().getFirst();
        helper.assertTrue(Math.round(before - EssenceService.current(wellspring)) == StrandRules.discounted(full, StrandRules.WELLSPRING_PERCENT),
                "paid " + (before - EssenceService.current(wellspring)) + " of " + full);

        ServerPlayer seer = AbilityGameTest.playerWith(helper, AspectKitCatalog.SEER);
        EssenceService.spend(seer, (int) EssenceService.current(seer));
        WeavingMenu table = new WeavingMenu(0, seer.getInventory(), ContainerLevelAccess.NULL);
        table.table().setItem(WeavingMenu.CENTRE_SLOT, new ItemStack(WeavingRegistry.CHAINBACK_LINK.get()));
        ResourceLocation links = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "dragging_links");
        helper.assertTrue(StrandService.study(seer, table, links, StrandRules.STUDY_TICKS), "a Seer studies with an empty core");
        helper.assertTrue(WeaveSight.read(dropped(helper, memory(new ItemStack(Items.IRON_SWORD), false))).getFirst().contains("Memory Weave"),
                "and reads a weave lying on the ground");
        helper.succeed();
    }

    private static ItemEntity dropped(GameTestHelper helper, ItemStack stack) {
        return new ItemEntity(helper.getLevel(), 0, 0, 0, stack);
    }

    /** A piece laid with the Memory weave, and Bound (unbonded) if asked. */
    public static ItemStack memory(ItemStack stack, boolean bound) {
        List<WovenStrands.Laid> laid = new ArrayList<>();
        laid.add(new WovenStrands.Laid(MemoryWeave.MEMORY_WEAVE, 1, 100, Optional.empty(), "Jack"));
        if (bound) {
            laid.add(new WovenStrands.Laid(MemoryWeave.BOUND, 1, 100, Optional.empty(), "Jack"));
        }
        stack.set(WeavingRegistry.WOVEN_STRANDS.get(), new WovenStrands(laid));
        return stack;
    }

    private static WeavingMenu menu(ServerPlayer player) {
        EssenceService.fill(player);
        return new WeavingMenu(0, player.getInventory(), ContainerLevelAccess.NULL);
    }

    private static ServerPlayer player(GameTestHelper helper, boolean awakened) {
        @SuppressWarnings("removal")
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 1, 2.5)));
        SoulService.awakenThroughRitual(player, ResourceLocation.fromNamespaceAndPath("shadowslave", "test/aspect"),
                SoulRank.DORMANT, ResourceLocation.fromNamespaceAndPath("shadowslave", "test/flaw"));
        if (awakened) {
            SoulService.awaken(player);
        }
        EssenceService.fill(player);
        return player;
    }
}
