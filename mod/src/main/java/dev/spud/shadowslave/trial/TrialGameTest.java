package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityGameTest;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.RankUpRequirements;
import dev.spud.shadowslave.dreamrealm.seed.SeedData;
import dev.spud.shadowslave.dreamrealm.seed.SeedService;
import dev.spud.shadowslave.item.ModDataComponents;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.trial.content.EchoSite;
import dev.spud.shadowslave.trial.content.NightmarePack;
import dev.spud.shadowslave.trial.content.TrialContent;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TrialGameTest {
    private static final ResourceLocation LAST_VIGIL = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "last_vigil");
    private static final ResourceLocation EMBER_WATCH = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "ember_watch");

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 400)
    public static void anEchoSeedIsChallengedAndItsNightmareConquered(GameTestHelper helper) {
        TrialContent content = TrialContent.get();
        helper.assertTrue(content.nightmare(LAST_VIGIL).isPresent(), "The Last Vigil loads from its data pack");
        EchoSite site = content.site(EMBER_WATCH).orElseThrow(() -> new AssertionError("the Ember Watch echo site loads"));
        helper.assertTrue(content.category(site) == 2, "the Ember Watch grows Category 2 Seeds");

        ServerLevel realm = SeedService.realm(helper.getLevel().getServer()).orElseThrow();
        SeedData.get(realm).seeds().stream().filter(seed -> seed.echoSite().isPresent())
                .forEach(seed -> SeedService.spend(realm, seed.id()));
        EchoSites.wake(realm, EMBER_WATCH);
        SeedData.Seed seed = EchoSites.grow(realm, EMBER_WATCH, site).orElseThrow(() -> new AssertionError("a Seed grows at the site"));
        helper.assertTrue(seed.echoSite().filter(EMBER_WATCH::equals).isPresent(), "the Seed remembers its echo site");

        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        TrialData.get(player.server).forget(player.getUUID());
        helper.assertFalse(TrialService.challenge(player, realm, seed), "a Dormant player cannot challenge it");
        SoulService.awaken(player);
        helper.assertFalse(RankUpRequirements.nightmareConquered(2).met(player), "no Nightmare conquered yet");

        Vec3 home = player.position();
        player.teleportTo(realm, seed.pos().getX() + 2.5, seed.pos().getY(), seed.pos().getZ() + 0.5, Set.of(), 0, 0);
        player.hasChangedDimension();
        Vec3 entered = player.position();
        helper.assertTrue(TrialService.challenge(player, realm, seed), "an Awakened player challenges it");
        helper.assertFalse(TrialService.challenge(player, realm, seed), "a waking Seed cannot be challenged twice");
        TrialService.wakeGatheringsNow(player.server);
        player.hasChangedDimension();

        TrialInstance instance = TrialService.instanceOf(player).orElseThrow(() -> new AssertionError("the Seed drew them in"));
        helper.assertTrue(TrialWorld.is(player.level()), "the challenger is inside the Nightmare");
        helper.assertTrue(instance.member(player.getUUID()).map(member -> member.role().id()).orElse("").equals("lamplighter"),
                "a lone challenger gets the role the pack hands out first");
        ItemStack flint = findItem(player, Items.FLINT_AND_STEEL);
        helper.assertTrue(flint.has(ModDataComponents.NIGHTMARE_BOUND.get()), "the role's kit is bound to the Nightmare");
        helper.assertTrue(TrialData.get(player.server).find(player.getUUID()).flatMap(TrialData.Record::active).isPresent(),
                "the entry snapshot is saved");

        ServerLevel nightmare = (ServerLevel) player.level();
        TrialService.skip(nightmare, instance, 30);
        helper.assertTrue(!instance.spawned.isEmpty(), "the first wave came on time");
        player.getInventory().add(new ItemStack(Items.DIAMOND));

        // Light the three lamps, as the Lamplighter's fate asks; the ending can be met from 150 s on.
        for (String lamp : List.of("lamp_nw", "lamp_ne", "lamp_sw")) {
            nightmare.setBlockAndUpdate(instance.at(lamp), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        }
        TrialService.skip(nightmare, instance, 130);
        helper.assertTrue(instance.over(), "the lit lamps end the Nightmare");
        player.hasChangedDimension();
        helper.assertTrue(TrialService.instanceOf(player).isEmpty(), "the challenger is out");
        helper.assertTrue(player.level() == realm && player.position().distanceTo(entered) < 1.0,
                "back where they stood when they went in");
        helper.assertTrue(findItem(player, Items.FLINT_AND_STEEL).isEmpty(), "the bound kit stayed in the Nightmare");
        helper.assertTrue(!findItem(player, Items.DIAMOND).isEmpty(), "what they won inside is theirs");
        TrialData.Record record = TrialData.get(player.server).record(player.getUUID());
        helper.assertTrue(record.clears(2) == 1 && record.active().isEmpty(), "the clear is on their record");
        helper.assertTrue(RankUpRequirements.nightmareConquered(2).met(player), "a Category 2 Nightmare now counts towards Ascension");
        helper.assertTrue(RankUpRequirements.forRank(SoulRank.ASCENDED).orElseThrow().describe(player).contains("conquered"),
                "the Ascension requirement reads the clear");
        helper.assertTrue(SeedData.get(realm).byId(seed.id()).isEmpty(), "the conquered Seed is gone");
        helper.assertTrue(EchoSites.grow(realm, EMBER_WATCH, site).isEmpty(), "the site rests after its Nightmare is conquered");

        player.teleportTo(helper.getLevel(), home.x, home.y, home.z, Set.of(), 0, 0);
        player.hasChangedDimension();
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 400)
    public static void aDeathRevertsTheChallengerAndTheSeedJudgesThem(GameTestHelper helper) {
        ServerPlayer player = AbilityGameTest.playerWith(helper, AspectKitCatalog.EMBER);
        SoulService.awaken(player);
        TrialData.get(player.server).forget(player.getUUID());
        player.getInventory().add(new ItemStack(Items.BREAD, 5));
        int level = player.experienceLevel;
        Vec3 entered = player.position();
        UUID seed = UUID.randomUUID();

        TrialInstance instance = TrialService.begin(List.of(player), LAST_VIGIL, Optional.empty(), Optional.of(seed), 2)
                .orElseThrow(() -> new AssertionError("the Nightmare starts"));
        player.hasChangedDimension();
        player.getInventory().add(new ItemStack(Items.DIAMOND, 3));
        player.giveExperienceLevels(10);
        ServerLevel nightmare = (ServerLevel) player.level();
        TrialService.skip(nightmare, instance, 10);

        TrialService.fall(player);
        player.hasChangedDimension();
        helper.assertTrue(TrialService.instanceOf(player).isEmpty(), "thrown out of the Nightmare");
        helper.assertTrue(player.level() == helper.getLevel() && player.position().distanceTo(entered) < 1.0, "back where they entered");
        helper.assertTrue(findItem(player, Items.DIAMOND).isEmpty(), "what they gained inside is lost");
        helper.assertTrue(findItem(player, Items.BREAD).getCount() == 5, "what they brought is theirs again");
        helper.assertTrue(player.experienceLevel == level, "experience reverts too");
        helper.assertTrue(player.getHealth() > 0, "alive");
        helper.assertTrue(instance.over(), "a Nightmare with nobody left in it closes");
        helper.assertTrue(TrialService.judgedBy(player, seed), "the Seed has judged them");
        helper.assertFalse(RankUpRequirements.nightmareConquered(2).met(player), "a death conquers nothing");
        helper.succeed();
    }

    @GameTest(templateNamespace = ShadowSlaveMod.MOD_ID, template = "ritual_test_room", timeoutTicks = 200)
    public static void rolesAreDealtMinimumsFirst(GameTestHelper helper) {
        NightmarePack pack = TrialContent.get().nightmare(LAST_VIGIL).orElseThrow();
        for (int members = 1; members <= 6; members++) {
            List<NightmarePack.Role> roles = TrialService.assignRoles(pack.roles(), members, helper.getLevel().getRandom());
            helper.assertTrue(roles.size() == members, "one role each");
            helper.assertTrue(roles.stream().anyMatch(role -> role.id().equals("lamplighter")), "there is always a Lamplighter");
        }
        helper.succeed();
    }

    private static ItemStack findItem(ServerPlayer player, net.minecraft.world.item.Item item) {
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(item)) {
                return inventory.getItem(i);
            }
        }
        return ItemStack.EMPTY;
    }
}
