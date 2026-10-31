package dev.spud.shadowslave.devtools;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SpellState;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DevToolsGameTest {
    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void theKeysSwapToAnyNaturesAbilities(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        helper.assertTrue(DevTools.putDormant(player, AspectKitCatalog.TIDE.abilityId()), "a Tide ability on the first key");
        helper.assertTrue(DevTools.putAwakened(player, Optional.of(KitLadder.VEIL.abilityId())), "Mist's Veil on the second key");
        helper.assertTrue(names(player).equals(List.of(AspectKitCatalog.TIDE.abilityName(), "Veil")), "the keys hold the swapped abilities");
        helper.assertTrue(DevTools.putAwakened(player, Optional.of(KitLadder.QUAKE.abilityId())), "a second swap replaces, not adds");
        helper.assertTrue(names(player).equals(List.of(AspectKitCatalog.TIDE.abilityName(), "Quake")), "only Quake on the second key");
        helper.assertTrue(DevTools.putAwakened(player, Optional.empty()), "the second key clears");
        helper.assertTrue(names(player).size() == 1, "one key left");
        helper.assertTrue(SoulIdentityService.get(player).aspect().orElseThrow().abilitySet().contains(AspectKitCatalog.EMBER.innateId()),
                "the Nature's innate stays");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void rankGoesUpAndDownAndANatureSwapKeepsIt(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.STONE);
        helper.assertTrue(DevTools.setRank(player, SoulRank.AWAKENED), "rise without a full core");
        helper.assertTrue(SoulService.get(player).spellState() == SpellState.AWAKENED, "Awakened");
        helper.assertTrue(SoulIdentityService.get(player).aspect().orElseThrow().aspectRank() == SoulRank.AWAKENED, "the Aspect rose too");
        helper.assertTrue(names(player).equals(List.of(AspectKitCatalog.STONE.abilityName(), KitLadder.ROOT_IN_STONE.name())),
                "Stone's first Awakened ability on the second key");

        DevTools.swapNature(player, AspectKitCatalog.TIDE);
        helper.assertTrue(SoulService.get(player).spellState() == SpellState.AWAKENED, "still Awakened after the swap");
        helper.assertTrue(names(player).equals(List.of(AspectKitCatalog.TIDE.abilityName(), KitLadder.LOW_TIDE.name())),
                "Tide's kit, Awakened");

        helper.assertTrue(DevTools.setRank(player, SoulRank.DORMANT), "back down");
        helper.assertTrue(SoulService.get(player).spellState() == SpellState.DREAMER, "a Dreamer again");
        helper.assertTrue(names(player).equals(List.of(AspectKitCatalog.TIDE.abilityName())), "the Awakened ability is gone");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room")
    public static void freeCastingSpendsNothing(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        DevTools.toggleFreeCasting(player);
        EssenceService.spend(player, 50);
        helper.assertTrue(EssenceService.current(player) == EssenceService.DORMANT_CAPACITY, "nothing spent");
        helper.assertTrue(!DevTools.toggleFreeCasting(player), "toggles off");
        EssenceService.spend(player, 50);
        helper.assertTrue(EssenceService.current(player) == EssenceService.DORMANT_CAPACITY - 50, "spending is back");
        helper.succeed();
    }

    private static List<String> names(ServerPlayer player) {
        return AbilityService.status(player).slots().stream().map(slot -> slot.abilityName()).toList();
    }
}
