package dev.spud.shadowslave.echoes;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.weaving.WeavingMenu;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Echoes for every player: remnants, binding, soul slots, summoning, upkeep, orders and permanent death. */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EchoesGameTest {
    private static final String ROOM = "ritual_test_room";

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aSlainCreatureCanLeaveAnEchoRemnant(GameTestHelper helper) {
        ServerPlayer player = player(helper, true);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 3, 1, 3);
        EchoContent before = EchoContent.current();
        try {
            EchoSettings always = new EchoSettings(before.settings().slots(), List.of(1.0F), before.settings().bindingLevel(),
                    before.settings().bindEssence(), before.settings().bindXp(), before.settings().summonEssence(),
                    before.settings().upkeep(), true, 0.02F, 4.0, 24.0, 16.0, 32.0);
            EchoContent.set(new EchoContent(always, Map.of()));
            ItemStack remnant = EchoRemnants.drop(helper.getLevel(), zombie, player, RandomSource.create(1));
            helper.assertFalse(remnant.isEmpty(), "a sure chance leaves a remnant");
            EchoBody body = remnant.get(EchoesRegistry.REMNANT_OF.get());
            helper.assertTrue(body != null && body.entity().equals(ResourceLocation.withDefaultNamespace("zombie"))
                    && body.rank() == CreatureRank.DORMANT, "it remembers a Dormant zombie: " + body);
            helper.assertFalse(body.tag().contains("UUID") || body.tag().contains("Pos"), "its saved body forgets where it was");
            List<ItemEntity> dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(zombie.blockPosition()).inflate(2),
                    item -> item.getItem().is(EchoesRegistry.ECHO_REMNANT.get()));
            helper.assertTrue(dropped.size() == 1, "it lies where the zombie fell");
            Mob creeper = helper.spawnWithNoFreeWill(EntityType.CREEPER, 4, 1, 4);
            helper.assertTrue(EchoRemnants.bodyOf(EchoContent.current(), creeper).isEmpty(), "a creeper leaves none");
        } finally {
            EchoContent.set(before);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aRemnantIsBoundAtTheWeavingTable(GameTestHelper helper) {
        ServerPlayer player = player(helper, true);
        WeavingMenu menu = new WeavingMenu(0, player.getInventory(), ContainerLevelAccess.NULL);
        player.containerMenu = menu;
        EchoBody zombie = body(helper, player, "zombie");
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, EchoRemnants.remnant(zombie));
        helper.assertTrue(EchoBinding.bind(player, menu).isEmpty(), "no anchor, no binding");
        helper.assertTrue(EchoBinding.preview(player).map(binding -> binding.lock().contains("soul shard")).orElse(false),
                "the table says it wants an anchor");
        menu.table().setItem(WeavingMenu.ANCHOR_SLOT, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get(), 2));
        float essence = EssenceService.current(player);
        ItemStack bound = EchoBinding.bind(player, menu);
        helper.assertFalse(bound.isEmpty(), "bound");
        helper.assertTrue(menu.output().has(EchoesRegistry.ECHO.get()), "the Echo comes out of the output slot");
        helper.assertTrue(menu.centre().isEmpty() && menu.anchor().getCount() == 1, "the remnant and one anchor shard are used");
        helper.assertTrue(EssenceService.current(player) < essence, "binding takes essence");

        EchoBody awakened = new EchoBody(zombie.entity(), zombie.name(), CreatureRank.AWAKENED, "", zombie.tag(), zombie.maxHealth());
        menu.table().setItem(WeavingMenu.OUTPUT_SLOT, ItemStack.EMPTY);
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, EchoRemnants.remnant(awakened));
        menu.table().setItem(WeavingMenu.ANCHOR_SLOT, new ItemStack(ModItems.AWAKENED_SOUL_SHARD.get()));
        EssenceService.fill(player);
        helper.assertTrue(EchoBinding.bind(player, menu).isEmpty(), "an Awakened creature needs a higher Binding level");
        player.containerMenu = player.inventoryMenu;
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void soulSlotsFollowRank(GameTestHelper helper) {
        ServerPlayer dormant = player(helper, false);
        dormant.setItemInHand(InteractionHand.MAIN_HAND, bound(helper, dormant, "zombie"));
        helper.assertTrue(EchoService.absorb(dormant, InteractionHand.MAIN_HAND) == EchoService.Outcome.REFUSED, "a Dormant soul holds none");
        helper.assertFalse(dormant.getMainHandItem().isEmpty(), "it stays in hand");

        ServerPlayer awakened = player(helper, true);
        awakened.setItemInHand(InteractionHand.MAIN_HAND, bound(helper, awakened, "zombie"));
        helper.assertTrue(EchoService.absorb(awakened, InteractionHand.MAIN_HAND) == EchoService.Outcome.DONE, "an Awakened soul holds one");
        helper.assertTrue(awakened.getMainHandItem().isEmpty() && EchoService.held(awakened).size() == 1, "it is in the soul");
        awakened.setItemInHand(InteractionHand.MAIN_HAND, bound(helper, awakened, "skeleton"));
        helper.assertTrue(EchoService.absorb(awakened, InteractionHand.MAIN_HAND) == EchoService.Outcome.REFUSED, "but only one");
        helper.assertTrue(EchoService.drawOut(awakened, 1) == EchoService.Outcome.NONE, "nothing at a slot it does not have");
        awakened.getInventory().clearContent();
        helper.assertTrue(EchoService.drawOut(awakened, 0) == EchoService.Outcome.DONE, "drawn out to hand over");
        helper.assertTrue(awakened.getMainHandItem().has(EchoesRegistry.ECHO.get()) && EchoService.held(awakened).isEmpty(), "back in hand");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM, timeoutTicks = 200)
    public static void aSummonedEchoServesItsOwnerAndCostsUpkeep(GameTestHelper helper) {
        ServerPlayer player = player(helper, true);
        player.setItemInHand(InteractionHand.MAIN_HAND, bound(helper, player, "zombie"));
        EchoService.absorb(player, InteractionHand.MAIN_HAND);
        float before = EssenceService.current(player);
        helper.assertTrue(EchoService.summon(player, 0) == EchoService.Outcome.DONE, "summoned");
        helper.assertTrue(EssenceService.current(player) < before, "summoning takes essence");
        List<Mob> bodies = EchoService.bodies(player);
        helper.assertTrue(bodies.size() == 1 && bodies.getFirst() instanceof Zombie, "a zombie body stands beside its owner");
        Mob echo = bodies.getFirst();
        helper.assertTrue(EchoService.bond(echo).map(bond -> bond.owner().equals(player.getUUID())).orElse(false), "bonded to its owner");
        echo.setTarget(player);
        helper.assertTrue(echo.getTarget() == null, "its own AI can't set it on its owner");
        helper.assertFalse(player.hurt(player.damageSources().mobAttack(echo), 4.0F), "it can't hurt its owner");
        helper.assertFalse(echo.hurt(player.damageSources().playerAttack(player), 4.0F), "nor its owner it");
        Zombie wild = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 5, 1, 5);
        helper.assertTrue(EchoService.order(player, EchoCommand.STAY) == EchoService.Outcome.DONE, "Stay");
        helper.assertTrue(EchoService.held(player).order() == EchoCommand.STAY, "the order is kept");
        EchoService.aim(echo, wild);
        helper.assertTrue(echo.getTarget() == wild, "an order can set it on a foe");
        float essence = EssenceService.current(player);
        helper.runAfterDelay(41, () -> {
            helper.assertTrue(EssenceService.current(player) < essence + EssenceService.regenPerTick(player) * 41,
                    "upkeep is drawn while it is out");
            echo.hurt(helper.getLevel().damageSources().magic(), 5.0F);
            float health = echo.getHealth();
            helper.assertTrue(EchoService.dismiss(player, EchoService.held(player).held().getFirst().id(), true), "dismissed");
            helper.assertTrue(echo.isRemoved(), "its body leaves the world");
            helper.assertTrue(Math.abs(EchoService.held(player).held().getFirst().health() - health) < 0.01F, "it keeps its wounds");
            wild.discard();
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void aDestroyedEchoIsGoneForGood(GameTestHelper helper) {
        ServerPlayer player = player(helper, true);
        player.setItemInHand(InteractionHand.MAIN_HAND, bound(helper, player, "zombie"));
        EchoService.absorb(player, InteractionHand.MAIN_HAND);
        EchoService.summon(player, 0);
        Mob echo = EchoService.bodies(player).getFirst();
        echo.kill();
        helper.assertTrue(EchoService.held(player).isEmpty(), "its death takes it off the soul");
        helper.assertTrue(EchoService.summonedCount(player) == 0, "and nothing is out");
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(echo.blockPosition()).inflate(3));
        helper.assertTrue(drops.isEmpty(), "it leaves nothing behind");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void runningDrySendsEchoesHome(GameTestHelper helper) {
        ServerPlayer player = player(helper, true);
        player.setItemInHand(InteractionHand.MAIN_HAND, bound(helper, player, "zombie"));
        EchoService.absorb(player, InteractionHand.MAIN_HAND);
        EchoService.summon(player, 0);
        EssenceService.spend(player, EssenceService.capacity(player));
        for (int i = 0; i < 4; i++) {
            EchoService.tick(player);
        }
        helper.assertTrue(EchoService.summonedCount(player) == 0, "an empty pool can't keep an Echo out");
        helper.assertTrue(EchoService.held(player).size() == 1, "it is back in the soul, not lost");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = ROOM)
    public static void anOwnersDeathSendsEchoesHome(GameTestHelper helper) {
        ServerPlayer player = player(helper, true);
        player.setItemInHand(InteractionHand.MAIN_HAND, bound(helper, player, "zombie"));
        EchoService.absorb(player, InteractionHand.MAIN_HAND);
        EchoService.summon(player, 0);
        Mob echo = EchoService.bodies(player).getFirst();
        player.kill();
        helper.assertTrue(echo.isRemoved() && EchoService.held(player).size() == 1, "the Echo returns to the soul, alive");
        helper.succeed();
    }

    private static EchoBody body(GameTestHelper helper, ServerPlayer player, String entity) {
        return EchoCommands.bodyFor(player, ResourceLocation.withDefaultNamespace(entity))
                .orElseThrow(() -> new IllegalStateException(entity + " has no Echo body"));
    }

    private static ItemStack bound(GameTestHelper helper, ServerPlayer player, String entity) {
        return EchoService.item(EchoRecord.bind(body(helper, player, entity), player.getGameProfile().getName()));
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
