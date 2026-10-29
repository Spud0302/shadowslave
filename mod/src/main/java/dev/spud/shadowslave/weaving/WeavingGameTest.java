package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.smithing.ForgedData;
import dev.spud.shadowslave.smithing.Quality;
import dev.spud.shadowslave.smithing.SmithingService;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.world.entity.ChainbackEntity;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantments;
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

import java.util.List;
import java.util.Optional;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WeavingGameTest {
    private static final ResourceLocation FORGING = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "forging");
    private static final ResourceLocation CHAINBACK_MAIL = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "chainback_mail");

    private static final ResourceLocation DRAGGING_LINKS = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "dragging_links");
    private static final ResourceLocation CINDER_SKIN = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "cinder_skin");
    private static final ResourceLocation RATTLING_GUARD = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "rattling_guard");
    private static final ResourceLocation WHETTED_EDGE = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "whetted_edge");
    private static final ResourceLocation SPLINTER_EDGE = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "splinter_edge");

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void studyingAWeaveTeachesItsStrand(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        WeavingMenu menu = menu(player);
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, new ItemStack(WeavingRegistry.CHAINBACK_LINK.get()));
        helper.assertTrue(StrandService.teaches(WeavingContent.current(), menu.centre()).contains(DRAGGING_LINKS), "a link holds Dragging Links");
        helper.assertFalse(StrandService.study(player, menu, DRAGGING_LINKS, 10), "a glance is not enough");
        helper.assertTrue(StrandService.study(player, menu, DRAGGING_LINKS, StrandRules.STUDY_TICKS), "a steady study is");
        helper.assertTrue(WeavingService.get(player).studied().contains(DRAGGING_LINKS), "it is learned");
        helper.assertTrue(menu.centre().getCount() == 1, "studying spends nothing of the piece");
        ItemStack blade = new ItemStack(Items.IRON_SWORD);
        blade.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SHARPNESS), 2);
        helper.assertTrue(StrandService.teaches(WeavingContent.current(), blade).contains(WHETTED_EDGE), "an enchanted blade teaches Whetted Edge");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void aCleanTraceLaysAStrandAndAWildOneFizzles(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        learn(player, DRAGGING_LINKS);
        WeavingMenu menu = menu(player);
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, new ItemStack(Items.IRON_SWORD));
        menu.table().setItem(WeavingMenu.ANCHOR_SLOT, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 2));
        menu.table().setItem(WeavingMenu.FIRST_MATERIAL, new ItemStack(WeavingRegistry.CHAINBACK_LINK.get(), 2));
        float[] wild = {0.95F, 0.95F, 0.9F, 0.95F, 0.95F, 0.9F, 0.9F, 0.9F};
        helper.assertTrue(StrandService.weave(player, menu, DRAGGING_LINKS, 1, Optional.empty(), wild) == 0, "a wild trace fizzles");
        helper.assertTrue(menu.anchor().getCount() == 2 && menu.materialAmounts().get(WeavingRegistry.CHAINBACK_LINK.getId()) == 2,
                "a fizzle keeps the anchor and the links");
        EssenceService.fill(player);
        int potency = StrandService.weave(player, menu, DRAGGING_LINKS, 1, Optional.empty(), clean(DRAGGING_LINKS));
        helper.assertTrue(potency == 100, "a clean trace lays it whole: " + potency + " " + WeavingService.lastResult(player));
        WovenStrands strands = StrandService.strands(menu.centre());
        helper.assertTrue(strands.size() == 1 && strands.strands().getFirst().id().equals(DRAGGING_LINKS), "the sword holds it");
        helper.assertTrue(menu.anchor().getCount() == 1, "one shard became the anchor");
        helper.assertTrue(WeavingService.get(player).xp(StrandService.PATTERNS) > 0, "Patterns XP is paid");
        EssenceService.fill(player);
        learn(player, WHETTED_EDGE);
        menu.table().setItem(WeavingMenu.FIRST_MATERIAL + 1, new ItemStack(Items.LAPIS_LAZULI, 4));
        helper.assertTrue(StrandService.weave(player, menu, WHETTED_EDGE, 1, Optional.empty(), clean(WHETTED_EDGE)) == 0,
                "a plain sword has only one thread");
        helper.assertTrue(WeavingService.lastResult(player).contains("thread"), WeavingService.lastResult(player));
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void aFineBladeHoldsTwoStrandsAndTuningNeedsPatternsTwo(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        learn(player, DRAGGING_LINKS);
        learn(player, WHETTED_EDGE);
        WeavingMenu menu = menu(player);
        ItemStack blade = new ItemStack(Items.IRON_SWORD);
        SmithingService.stamp(blade, new ForgedData(Quality.FINE, 70, "Jack"));
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, blade);
        menu.table().setItem(WeavingMenu.ANCHOR_SLOT, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 4));
        menu.table().setItem(WeavingMenu.FIRST_MATERIAL, new ItemStack(WeavingRegistry.CHAINBACK_LINK.get(), 2));
        menu.table().setItem(WeavingMenu.FIRST_MATERIAL + 1, new ItemStack(Items.LAPIS_LAZULI, 4));
        helper.assertTrue(StrandService.lock(player, menu, DRAGGING_LINKS, 1, Optional.of(StrandRestriction.NIGHT)).isPresent(),
                "tuning waits for Patterns level 2");
        helper.assertTrue(StrandService.lock(player, menu, DRAGGING_LINKS, 2, Optional.empty()).isPresent(), "a Dormant anchor holds Tier I only");
        helper.assertTrue(StrandService.weave(player, menu, DRAGGING_LINKS, 1, Optional.empty(), clean(DRAGGING_LINKS)) == 100, "first strand");
        EssenceService.fill(player);
        helper.assertTrue(StrandService.weave(player, menu, WHETTED_EDGE, 1, Optional.empty(), clean(WHETTED_EDGE)) == 100, "second strand");
        helper.assertTrue(StrandService.strands(menu.centre()).size() == 2, "a Fine blade holds two");
        ItemStack plain = new ItemStack(Items.IRON_SWORD);
        SmithingService.stamp(plain, new ForgedData(Quality.FINE, 70, "Jack"));
        double withStrand = attackDamage(menu.centre());
        double without = attackDamage(plain);
        helper.assertTrue(Math.abs(withStrand - without - 1.0) < 0.01, "Whetted Edge sharpens the blade: " + withStrand + " vs " + without);
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void unravellingMovesAStrandOntoAnotherPiece(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        WeavingMenu menu = menu(player);
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(WeavingRegistry.WOVEN_STRANDS.get(), new WovenStrands(List.of(
                new WovenStrands.Laid(DRAGGING_LINKS, 1, 100, Optional.empty(), "Jack"))));
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, sword);
        helper.assertTrue(StrandService.unravel(player, menu, 0), "it comes loose: " + WeavingService.lastResult(player));
        helper.assertTrue(StrandService.strands(menu.centre()).isEmpty(), "the sword is bare again");
        ItemStack thread = menu.output();
        helper.assertTrue(thread.is(WeavingRegistry.WOVEN_THREAD.get())
                && StrandService.strands(thread).strands().getFirst().potency() == StrandRules.UNRAVEL_KEEP, "a frayed thread");
        menu.table().setItem(WeavingMenu.OUTPUT_SLOT, ItemStack.EMPTY);
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, new ItemStack(Items.IRON_AXE));
        menu.table().setItem(WeavingMenu.FIRST_MATERIAL, thread);
        helper.assertTrue(StrandService.layThread(player, menu), "laid into an axe: " + WeavingService.lastResult(player));
        helper.assertTrue(StrandService.strands(menu.centre()).strands().getFirst().id().equals(DRAGGING_LINKS), "the axe pulls now");
        helper.assertTrue(menu.table().getItem(WeavingMenu.FIRST_MATERIAL).isEmpty(), "the thread is used up");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void cinderSkinAndRattlingGuardSoftenHits(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.setGameMode(GameType.SURVIVAL);
        StrandEffects.clear();
        ItemStack vest = new ItemStack(Items.IRON_CHESTPLATE);
        vest.set(WeavingRegistry.WOVEN_STRANDS.get(), new WovenStrands(List.of(
                new WovenStrands.Laid(CINDER_SKIN, 1, 100, Optional.empty(), "Jack"),
                new WovenStrands.Laid(RATTLING_GUARD, 1, 100, Optional.empty(), "Jack"))));
        player.setItemSlot(EquipmentSlot.CHEST, vest);
        net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent fire = incoming(player, player.damageSources().onFire(), 4.0F);
        helper.assertTrue(Math.abs(fire.getAmount() - 4.0F * 0.8F) < 0.01F, "fire is softened by a fifth: " + fire.getAmount());
        net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent heavy = incoming(player, player.damageSources().cactus(), 10.0F);
        helper.assertTrue(Math.abs(heavy.getAmount() - 7.5F) < 0.01F, "the guard takes a quarter off a heavy hit: " + heavy.getAmount());
        net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent again = incoming(player, player.damageSources().cactus(), 10.0F);
        helper.assertTrue(again.getAmount() == 10.0F, "then it rests: " + again.getAmount());
        StrandEffects.clear();
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void draggingLinksPullsAStruckFoeCloser(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        StrandEffects.clear();
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(WeavingRegistry.WOVEN_STRANDS.get(), new WovenStrands(List.of(
                new WovenStrands.Laid(DRAGGING_LINKS, 3, 100, Optional.empty(), "Jack"))));
        player.setItemSlot(EquipmentSlot.MAINHAND, sword);
        net.minecraft.world.entity.monster.Zombie zombie = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.ZOMBIE, new BlockPos(5, 1, 2));
        zombie.setNoGravity(true);
        zombie.hurt(player.damageSources().playerAttack(player), 1.0F);
        helper.runAfterDelay(2, () -> {
            double toward = player.getX() - zombie.getX();
            helper.assertTrue(Math.signum(zombie.getDeltaMovement().x) == Math.signum(toward), "the links drag it in: " + zombie.getDeltaMovement());
            StrandEffects.clear();
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void anOldWovenMemoryKeepsItsEnchantmentAsAStrand(GameTestHelper helper) {
        ItemStack old = new ItemStack(WeavingRegistry.SHARD_KNIFE.get());
        old.set(WeavingRegistry.WOVEN_MEMORY.get(), new dev.spud.shadowslave.memory.MemoryInstanceData(
                ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "memory/shard_knife"), "Shard Knife", "weaving", "Woven by Jack"));
        WovenMemoryItems.grade(old, helper.getLevel());
        WovenStrands strands = StrandService.strands(old);
        helper.assertTrue(strands.size() == 1 && strands.strands().getFirst().id().equals(SPLINTER_EDGE)
                && strands.strands().getFirst().weaver().equals("Jack"), "Splinter Edge is live: " + strands);
        helper.assertTrue(StrandService.threads(old) == 1, "Fair: one thread, and it is full");
        helper.succeed();
    }

    private static net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent incoming(ServerPlayer player,
                                                                                               net.minecraft.world.damagesource.DamageSource source, float amount) {
        net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event =
                new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(player,
                        new net.neoforged.neoforge.common.damagesource.DamageContainer(source, amount));
        StrandEffects.onIncomingDamage(event);
        return event;
    }

    private static double attackDamage(ItemStack stack) {
        double[] total = {0.0};
        stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                total[0] += modifier.amount();
            }
        });
        return total[0];
    }

    private static WeavingMenu menu(ServerPlayer player) {
        EssenceService.fill(player);
        return new WeavingMenu(0, player.getInventory(), ContainerLevelAccess.NULL);
    }

    private static void learn(ServerPlayer player, ResourceLocation strand) {
        WeavingService.set(player, WeavingService.get(player).withStudied(strand));
    }

    /** A trace through the strand's points in order, ten samples to a stroke. */
    static float[] clean(ResourceLocation id) {
        List<StrandPattern.Point> points = StrandRules.points(id, WeavingContent.current().strand(id).orElseThrow());
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
        helper.assertTrue(content.strands().size() == 14, "fourteen strands: " + content.strands().size());
        helper.assertTrue(content.branch(StrandService.PATTERNS).isPresent() && content.branch(StrandService.FORGERY).isPresent(),
                "the Patterns and Forgery branches load");
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
