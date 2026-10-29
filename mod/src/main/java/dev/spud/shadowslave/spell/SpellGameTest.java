package dev.spud.shadowslave.spell;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.spell.calling.Calling;
import dev.spud.shadowslave.spell.calling.CallingData;
import dev.spud.shadowslave.spell.calling.CallingFrequency;
import dev.spud.shadowslave.spell.calling.SpellCallings;
import dev.spud.shadowslave.world.entity.AshBurrowerEntity;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellGameTest {

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 100)
    public static void theRecorderKeepsKillsAndFirsts(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        AshBurrowerEntity burrower = NightmareCreatureEntities.ASH_BURROWER.get().create(helper.getLevel());
        helper.assertTrue(burrower != null, "an Ash Burrower can be made");
        burrower.setRankAndClass(CreatureRank.FALLEN, CreatureClass.BEAST);
        int before = SpellRecord.get(player.server).size();

        SpellRecorder.kill(player, burrower);
        SpellRecord.Tally tally = SpellRecord.get(player.server).find(player.getUUID()).orElseThrow();
        helper.assertTrue(tally.kills().getOrDefault("fallen beast", 0) == 1, "the kill is tallied by rank and class");
        helper.assertTrue(tally.uniqueKills() == 0, "a plain Beast is not unique");
        helper.assertTrue(SpellRecord.get(player.server).size() > before, "the kill is in the world's log");
        helper.assertTrue(SpellRecord.get(player.server).first("kill_fallen_beast").isPresent(), "someone was first to slay a Fallen Beast");

        burrower.setCustomName(Component.literal("Old Ashmaw"));
        SpellRecorder.kill(player, burrower);
        helper.assertTrue(SpellRecord.get(player.server).find(player.getUUID()).orElseThrow().uniqueKills() == 1,
                "a named creature counts as unique");

        String key = "test_" + player.getUUID();
        helper.assertTrue(SpellRecorder.first(player, key, "test", "kill", "test"), "a new first");
        helper.assertFalse(SpellRecorder.first(player, key, "test", "kill", "test"), "a first happens once");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 100)
    public static void theCoreCallsOnceUntilItEmptiesAgain(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        SoulService.awaken(player);
        player.setData(SpellRegistry.CALLINGS, CallingData.empty());
        SpellCallings.setFrequency(player, CallingFrequency.OFTEN);
        SpellSettings settings = SpellSettings.DEFAULT;

        CoreService.set(player, CoreService.limit(player) * 95 / 100);
        SpellCallings.check(player, settings);
        List<CallingData.Entry> heard = SpellCallings.data(player).history();
        helper.assertTrue(heard.size() == 1 && heard.get(0).calling().equals(Calling.CORE_NEARLY_FULL.id()),
                "a nearly full core calls");
        helper.assertTrue(!heard.get(0).text().isEmpty(), "the voice phrased it");

        SpellCallings.check(player, settings);
        helper.assertTrue(SpellCallings.data(player).history().size() == 1, "it does not call twice for one fill");

        SpellCallings.setFrequency(player, CallingFrequency.OFF);
        helper.assertFalse(SpellCallings.call(player, Calling.IDLE, "idle", Map.of(), false), "off means off");
        helper.assertTrue(SpellCallings.call(player, Calling.IDLE, "idle", Map.of(), true), "an operator can still force one");
        helper.succeed();
    }
}
