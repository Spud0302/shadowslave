package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.EssenceService;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

/** The higher branch levels, strands of a Weaver's own making, and the Soul Sea's keeping of Memories. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WeavingDepthGameTest {
    private static final String ROOM = "ritual_test_room";
    private static final ResourceLocation LINKS = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "dragging_links");
    private static final ResourceLocation ECHO = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "echoing_cut");

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void fusingTwoStrandsMakesOneThatDoesBoth(GameTestHelper helper) {
        ServerPlayer player = player(helper, true);
        WeavingService.set(player, WeavingService.get(player).withStudied(LINKS).withStudied(ECHO));
        WeavingMenu menu = menu(player);
        helper.assertTrue(StrandService.create(player, menu, LINKS, ECHO, "").isEmpty(), "fusing waits for Patterns level 5");
        reach(player, StrandService.PATTERNS, 5);
        EssenceService.fill(player);
        Optional<ResourceLocation> made = StrandService.create(player, menu, LINKS, ECHO, "Hollow Pull");
        helper.assertTrue(made.isPresent(), "fused: " + WeavingService.lastResult(player));
        ResourceLocation id = made.get();
        helper.assertTrue(WeavingService.get(player).created().get(id).name().equals("Hollow Pull"), "it keeps its name");
        helper.assertTrue(WeavingService.status(player).strands().stream().anyMatch(strand -> strand.id().equals(id.toString()) && strand.own()),
                "it is listed among the strands you know");

        ItemStack blade = new ItemStack(Items.IRON_SWORD);
        SmithingService.stamp(blade, new ForgedData(Quality.FINE, 70, "Jack"));
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, blade);
        menu.table().setItem(WeavingMenu.ANCHOR_SLOT, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 2));
        menu.table().setItem(WeavingMenu.FIRST_MATERIAL, new ItemStack(WeavingRegistry.CHAINBACK_LINK.get(), 1));
        helper.assertTrue(StrandService.lock(player, menu, id, 1, Optional.empty()).isPresent(), "it needs both parts' materials");
        menu.table().setItem(WeavingMenu.FIRST_MATERIAL + 1, new ItemStack(WeavingRegistry.MIMIC_HUSK.get(), 1));
        EssenceService.fill(player);
        int potency = StrandService.weave(player, menu, id, 1, Optional.empty(), clean(player, id));
        helper.assertTrue(potency == 100, "laid whole: " + potency + " " + WeavingService.lastResult(player));
        WovenStrands strands = StrandService.strands(menu.centre());
        helper.assertTrue(strands.size() == 1 && strands.strands().getFirst().fused().isPresent(), "one thread, carrying both parts");
        helper.assertTrue(menu.materialAmounts().isEmpty(), "both materials were spun");

        learnAndLay(player, menu, LINKS);
        helper.assertTrue(WeavingService.lastResult(player).contains("same pattern twice"), WeavingService.lastResult(player));

        ItemStack woven = menu.centre().copy();
        player.setItemSlot(EquipmentSlot.MAINHAND, woven);
        List<StrandEffects.Active> pulls = StrandEffects.active(player, StrandPattern.Kind.PULL);
        List<StrandEffects.Active> echoes = StrandEffects.active(player, StrandPattern.Kind.ECHO);
        StrandPattern links = WeavingContent.current().strand(LINKS).orElseThrow();
        helper.assertTrue(pulls.size() == 1 && echoes.size() == 1, "it pulls and echoes");
        helper.assertTrue(Math.abs(pulls.getFirst().strength() - links.effect().values().getFirst() * 0.7F) < 1.0E-4F,
                "each part at 70%: " + pulls.getFirst().strength());

        ServerPlayer student = player(helper, true);
        WeavingMenu table = menu(student);
        table.table().setItem(WeavingMenu.CENTRE_SLOT, woven.copy());
        helper.assertTrue(StrandService.teaches(WeavingContent.current(), table.centre()).contains(id), "a fused weave can be studied");
        helper.assertTrue(StrandService.study(student, table, id, StrandRules.STUDY_TICKS), "and copied: " + WeavingService.lastResult(student));
        helper.assertTrue(WeavingService.get(student).created().get(id).name().equals("Hollow Pull"), "the copy keeps its maker's name");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void bindingLevelsWidenTheSoulAndCheapenSummons(GameTestHelper helper) {
        ServerPlayer player = player(helper, true);
        int capacity = MemoryWeave.capacity(player);
        int cost = MemoryWeave.summonCost(player);
        reach(player, StrandService.BINDING, 4);
        helper.assertTrue(MemoryWeave.capacity(player) == capacity + 1, "Binding 2 holds one more: " + MemoryWeave.capacity(player));
        helper.assertTrue(MemoryWeave.summonCost(player) < cost, "Binding 4 halves the summon: " + MemoryWeave.summonCost(player) + " vs " + cost);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void theSoulSeaSummonsAnyMemoryAndATossedOneReturns(GameTestHelper helper) {
        ServerPlayer player = player(helper, true);
        player.setItemInHand(InteractionHand.MAIN_HAND, MemoryWeaveGameTest.memory(new ItemStack(Items.IRON_SWORD), false));
        MemoryWeave.toggle(player, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, MemoryWeaveGameTest.memory(new ItemStack(Items.IRON_AXE), false));
        MemoryWeave.toggle(player, false);
        helper.assertTrue(MemoryWeave.held(player).size() == 2, "two Memories rest in the soul");
        helper.assertTrue(WeavingService.status(player).soul().held().size() == 2, "the Memories page lists both");
        EssenceService.fill(player);
        helper.assertTrue(MemoryWeave.summon(player, 0) == MemoryWeave.Outcome.SUMMONED, "the older one can be called first");
        helper.assertTrue(player.getMainHandItem().is(Items.IRON_SWORD), "the sword answers, not the axe");
        helper.assertTrue(MemoryWeave.held(player).size() == 1 && MemoryWeave.held(player).next().is(Items.IRON_AXE), "the axe still rests");

        ItemStack sword = player.getMainHandItem().copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.assertTrue(player.drop(sword, true) == null, "a tossed Memory never lands");
        helper.assertTrue(MemoryWeave.held(player).size() == 2, "it sinks back into the soul");
        helper.assertTrue(player.drop(new ItemStack(Items.IRON_SWORD), true) != null, "a plain sword still falls");

        player.setItemInHand(InteractionHand.MAIN_HAND, MemoryWeaveGameTest.memory(new ItemStack(Items.IRON_PICKAXE), false));
        ServerPlayer friend = player(helper, false);
        player.setShiftKeyDown(true);
        MemoryWeave.onInteractPlayer(new PlayerInteractEvent.EntityInteract(player, InteractionHand.MAIN_HAND, friend));
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the giver lets it go");
        helper.assertTrue(friend.getInventory().contains(stack -> stack.is(Items.IRON_PICKAXE) && MemoryWeave.isMemory(stack)),
                "and it passes hand to hand");
        helper.succeed();
    }

    private static void learnAndLay(ServerPlayer player, WeavingMenu menu, ResourceLocation strand) {
        WeavingService.set(player, WeavingService.get(player).withStudied(strand));
        menu.table().setItem(WeavingMenu.FIRST_MATERIAL, new ItemStack(WeavingRegistry.CHAINBACK_LINK.get(), 1));
        EssenceService.fill(player);
        StrandService.weave(player, menu, strand, 1, Optional.empty(), clean(player, strand));
    }

    /** Holds every level of a branch up to {@code level}, points and deeds alike. */
    private static void reach(ServerPlayer player, ResourceLocation branch, int level) {
        WeavingBranch found = WeavingContent.current().branch(branch).orElseThrow();
        WeavingData data = WeavingService.get(player);
        for (int number = 2; number <= level; number++) {
            data = found.level(number).deed().isPresent() ? data.withDeedProgress(branch, number, 0, true) : data.withBought(branch, number);
        }
        WeavingService.set(player, data);
    }

    /** A trace through the strand's points in order, as this Weaver sees the strand. */
    private static float[] clean(ServerPlayer player, ResourceLocation id) {
        StrandPattern strand = StrandFusion.resolve(WeavingContent.current(), WeavingService.get(player), id).orElseThrow();
        List<StrandPattern.Point> points = StrandRules.points(id, strand);
        List<Float> samples = new java.util.ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            StrandPattern.Point to = points.get(i);
            StrandPattern.Point from = i == 0 ? to : points.get(i - 1);
            for (int step = i == 0 ? 10 : 1; step <= 10; step++) {
                samples.add(from.x() + (to.x() - from.x()) * step / 10.0F);
                samples.add(from.y() + (to.y() - from.y()) * step / 10.0F);
            }
        }
        float[] trace = new float[samples.size()];
        for (int i = 0; i < trace.length; i++) {
            trace[i] = samples.get(i);
        }
        return trace;
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
