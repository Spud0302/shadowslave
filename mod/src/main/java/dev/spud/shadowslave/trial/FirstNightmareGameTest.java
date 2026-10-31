package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.AwakeningService;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.awakening.RankUpRequirements;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.trial.content.TrialContent;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.Set;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FirstNightmareGameTest {
    private static final ResourceLocation CHAIN_ROAD = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "chain_road");

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 400)
    public static void withTheRuleOffAwakeningStaysFullCoreOnly(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        TrialData.get(player.server).forget(player.getUUID());
        CoreService.set(player, Integer.MAX_VALUE);
        helper.assertFalse(TrialRules.firstNightmare(player.server), "the First Nightmare is off by default");
        helper.assertFalse(TrialRules.firstNightmareBreach(player.server), "the breach is off by default");
        helper.assertFalse(FirstNightmare.due(player), "no Seed blooms while the rule is off");
        helper.assertTrue(AwakeningService.ready(player), "a full core is enough to Awaken");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 400)
    public static void theFirstNightmareIsFacedAloneAndOpensAwakening(GameTestHelper helper) {
        helper.assertTrue(TrialContent.get().nightmare(CHAIN_ROAD).isPresent(), "The Chain Road loads from its data pack");
        helper.assertTrue(TrialContent.get().firstNightmare().nightmares().contains(CHAIN_ROAD), "the settings name it as a First Nightmare");
        MinecraftServer server = helper.getLevel().getServer();
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        TrialData.get(server).forget(player.getUUID());
        boolean rule = TrialRules.firstNightmare(server);
        server.getGameRules().getRule(TrialRules.FIRST_NIGHTMARE).set(true, server);
        try {
            helper.assertFalse(FirstNightmare.due(player), "an empty core keeps the Seed asleep");
            CoreService.set(player, Integer.MAX_VALUE);
            helper.assertTrue(FirstNightmare.due(player), "a saturated Dormant core makes the Seed ready");
            helper.assertFalse(AwakeningService.ready(player), "a full core is no longer enough to Awaken");
            helper.assertTrue(RankUpRequirements.forRank(SoulRank.AWAKENED).orElseThrow().describe(player).contains("First Nightmare"),
                    "the Soul Sea says what is missing");

            Vec3 home = player.position();
            TrialInstance instance = FirstNightmare.bloom(player).orElseThrow(() -> new AssertionError("the Seed blooms"));
            player.hasChangedDimension();
            helper.assertTrue(instance.first() && instance.category() == 1, "a First Nightmare, of Category 1");
            helper.assertTrue(instance.nightmareId.equals(CHAIN_ROAD), "the only First Nightmare in the settings");
            helper.assertTrue(instance.members.size() == 1 && TrialWorld.is(player.level()), "the carrier is inside, alone");
            helper.assertTrue(instance.seed.isEmpty() && instance.site.isEmpty(), "no echo Seed: it grew inside them");
            String role = instance.member(player.getUUID()).orElseThrow().role().id();
            helper.assertTrue(role.equals("road_slave") || role.equals("ash_pilgrim"), "the Spell gives them a role");
            helper.assertFalse(FirstNightmare.due(player), "it cannot bloom twice at once");

            ServerLevel nightmare = (ServerLevel) player.level();
            TrialService.skip(nightmare, instance, 20);
            helper.assertTrue(!instance.spawned.isEmpty(), "the first creatures came on time");
            // Walk to the gate; the ending can be met from 120 s on.
            Vec3 gate = Vec3.atBottomCenterOf(instance.at("watch_gate"));
            player.teleportTo(nightmare, gate.x, gate.y, gate.z, Set.of(), 0, 0);
            TrialService.skip(nightmare, instance, 90);
            helper.assertFalse(instance.over(), "the gate does not end it before its time");
            TrialService.skip(nightmare, instance, 20);
            helper.assertTrue(instance.over() && "gate".equals(instance.endedWith), "reaching the gate ends it");
            player.hasChangedDimension();

            helper.assertTrue(TrialService.instanceOf(player).isEmpty() && player.level() == helper.getLevel()
                    && player.position().distanceTo(home) < 1.0, "home again, where they slept");
            helper.assertTrue(FirstNightmare.conquered(player), "the conquest is on their record");
            helper.assertFalse(FirstNightmare.due(player), "a conquered Seed does not bloom again");
            helper.assertTrue(AwakeningService.ready(player), "now they can Awaken");
            helper.assertTrue(AwakeningService.awaken(player, KitLadder.EMBER_WARD.abilityId()) == AwakeningService.Result.AWAKENED,
                    "and they do");
            helper.assertTrue(SoulService.get(player).soulRank().equals(Optional.of(SoulRank.AWAKENED)), "Awakened");
        } finally {
            server.getGameRules().getRule(TrialRules.FIRST_NIGHTMARE).set(rule, server);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 400)
    public static void aDeathRevertsAndTheSeedBloomsAgain(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.STONE);
        TrialData.get(server).forget(player.getUUID());
        boolean rule = TrialRules.firstNightmare(server);
        boolean breach = TrialRules.firstNightmareBreach(server);
        server.getGameRules().getRule(TrialRules.FIRST_NIGHTMARE).set(true, server);
        server.getGameRules().getRule(TrialRules.FIRST_NIGHTMARE_BREACH).set(true, server);
        try {
            CoreService.set(player, Integer.MAX_VALUE);
            player.getInventory().add(new ItemStack(Items.BREAD, 4));
            Vec3 home = player.position();
            TrialInstance instance = FirstNightmare.bloom(player).orElseThrow(() -> new AssertionError("the Seed blooms"));
            player.hasChangedDimension();
            player.getInventory().add(new ItemStack(Items.DIAMOND, 2));

            TrialService.fall(player);
            player.hasChangedDimension();
            helper.assertTrue(instance.over() && TrialService.instanceOf(player).isEmpty(), "thrown out, and the Nightmare closes");
            helper.assertTrue(player.level() == helper.getLevel() && player.position().distanceTo(home) < 1.0, "home again");
            helper.assertTrue(countOf(player, Items.DIAMOND) == 0 && countOf(player, Items.BREAD) == 4, "reverted to how they went in");
            helper.assertFalse(FirstNightmare.conquered(player), "a death conquers nothing");
            helper.assertFalse(AwakeningService.ready(player), "so they cannot Awaken yet");
            helper.assertTrue(FirstNightmare.due(player), "but the Seed grows back and can bloom again");
            AABB around = new AABB(player.blockPosition()).inflate(8);
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(NightmareCreature.class, around).isEmpty(),
                    "with the breach on, something followed them out");
            helper.getLevel().getEntitiesOfClass(NightmareCreature.class, around).forEach(NightmareCreature::discard);
        } finally {
            server.getGameRules().getRule(TrialRules.FIRST_NIGHTMARE).set(rule, server);
            server.getGameRules().getRule(TrialRules.FIRST_NIGHTMARE_BREACH).set(breach, server);
        }
        helper.succeed();
    }

    private static int countOf(ServerPlayer player, net.minecraft.world.item.Item item) {
        var inventory = player.getInventory();
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(item)) {
                count += inventory.getItem(i).getCount();
            }
        }
        return count;
    }
}
