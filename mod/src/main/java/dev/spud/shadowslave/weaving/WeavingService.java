package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.KitRules;
import dev.spud.shadowslave.ability.MasterworkAbility;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import dev.spud.shadowslave.memory.MemoryInstanceData;
import dev.spud.shadowslave.network.payload.WeavingStatusPayload;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server side of Weaving: the table's buttons, Spell-granted patterns, and keeping the client's view current. */
public final class WeavingService {
    static final ResourceLocation FORGING = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "forging");
    /** Thrifty Hands (Forge): Forging XP earned, as a percentage of the usual. */
    static final int FORGE_XP_PERCENT = 150;
    /** What each player's last press of Weave did, for the table screen. Not saved. */
    private static final Map<UUID, String> LAST_RESULT = new ConcurrentHashMap<>();

    private WeavingService() {
    }

    public static WeavingData get(ServerPlayer player) {
        return player.getData(WeavingRegistry.WEAVING);
    }

    public static void set(ServerPlayer player, WeavingData data) {
        player.setData(WeavingRegistry.WEAVING, data);
    }

    private static Optional<SoulRank> soulRank(ServerPlayer player) {
        return SoulService.get(player).soulRank();
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        LAST_RESULT.remove(player.getUUID());
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, opener) -> new WeavingMenu(id, inventory, ContainerLevelAccess.create(player.level(), pos)),
                Component.translatable("container.shadowslave.weaving_table")));
        sync(player);
    }

    static void handleButton(ServerPlayer player, WeavingMenu menu, int button) {
        WeavingContent content = WeavingContent.current();
        if (button == WeavingMenu.BUTTON_WEAVE) {
            weave(player, menu);
        } else if (button == WeavingMenu.BUTTON_RESPEC) {
            respec(player);
        } else if (button >= WeavingMenu.BUTTON_RAISE && button < WeavingMenu.BUTTON_RAISE + content.branches().size()) {
            raise(player, content.branchOrder().get(button - WeavingMenu.BUTTON_RAISE));
        }
    }

    /** Weaves whatever lies on the table. Returns what happened. */
    public static WeavingRules.Attempt weave(ServerPlayer player, WeavingMenu menu) {
        WeavingContent content = WeavingContent.current();
        WeavingData before = get(player);
        if (!menu.output.isEmpty()) {
            report(player, "Take what you have woven first.");
            return new WeavingRules.Attempt(WeavingRules.Outcome.EMPTY, Optional.empty(), before, 0, Optional.empty(), List.of());
        }
        WeavingRules.Boons boons = boons(player);
        WeavingRules.Attempt attempt = WeavingRules.attempt(content, before, menu.materialAmounts(), soulRank(player), boons);
        set(player, attempt.data());
        StringBuilder message = new StringBuilder();
        Optional<WeavingRecipe> recipe = attempt.recipe().map(content.recipes()::get);
        String name = recipe.map(value -> content.profile(value).formalName()).orElse("");
        switch (attempt.outcome()) {
            case EMPTY -> message.append("Lay materials on the table first.");
            case WOVEN, DISCOVERED -> {
                menu.materials.clearContent();
                menu.output.setItem(0, woven(player, recipe.orElseThrow()));
                if (attempt.outcome() == WeavingRules.Outcome.DISCOVERED) {
                    message.append("A new pattern takes shape: ").append(name).append("! ");
                } else {
                    message.append("You weave the ").append(name).append(". ");
                }
                message.append("+").append(attempt.xp()).append(' ').append(content.branchName(recipe.get().branch())).append(" XP");
                if (boons.tierBonus() > 0) {
                    MasterworkAbility.spend(player);
                    message.append(" · Masterwork spent");
                }
                player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
            }
            case DISCOVERED_LOCKED -> message.append("A new pattern takes shape: ").append(name).append(". ")
                    .append(attempt.lock().orElse("")).append(" to weave it.");
            case LOCKED -> message.append(name).append(": ").append(attempt.lock().orElse("")).append('.');
            case UNSHOWN -> message.append("The threads won't hold. The Spell has not shown you this pattern.");
            case HINTED -> message.append("The threads slip apart, but something almost held. See Weaving on your Soul screen.");
            case NOTHING -> message.append("The threads slip apart.");
        }
        if (!attempt.wove() && attempt.outcome() != WeavingRules.Outcome.EMPTY && attempt.outcome() != WeavingRules.Outcome.LOCKED
                && attempt.outcome() != WeavingRules.Outcome.DISCOVERED_LOCKED) {
            int lost = consume(menu, content.settings().failureConsumes());
            if (lost > 0) {
                message.append(lost == 1 ? " One material frays away." : " " + lost + " materials fray away.");
            }
        }
        for (String deed : attempt.deeds()) {
            player.sendSystemMessage(Component.literal("Deed done. " + deed).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        }
        if (attempt.outcome() == WeavingRules.Outcome.DISCOVERED || attempt.outcome() == WeavingRules.Outcome.DISCOVERED_LOCKED) {
            player.sendSystemMessage(Component.literal("You have learned a Weaving pattern: " + name + ".")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        report(player, message.toString());
        return attempt;
    }

    /** Forge's gifts at the table: Masterwork's extra Tier while it lasts, and Thrifty Hands' quicker Forging. */
    static WeavingRules.Boons boons(ServerPlayer player) {
        boolean forge = KitRules.hasInnate(player, AspectKitCatalog.FORGE.innateId());
        return new WeavingRules.Boons(MasterworkAbility.active(player) ? MasterworkAbility.TIER_BONUS : 0,
                forge ? Optional.of(FORGING) : Optional.empty(), forge ? FORGE_XP_PERCENT : 100);
    }

    private static ItemStack woven(ServerPlayer player, WeavingRecipe recipe) {
        WeavingContent content = WeavingContent.current();
        Item item = BuiltInRegistries.ITEM.get(recipe.result());
        ItemStack stack = new ItemStack(item == Items.AIR ? Items.BARRIER : item);
        MemoryContentCatalog.MemoryProfile profile = content.profile(recipe);
        stack.set(WeavingRegistry.WOVEN_MEMORY.get(), new MemoryInstanceData(profile.id(), profile.formalName(),
                "weaving", "Woven by " + player.getGameProfile().getName()));
        return stack;
    }

    /** Takes up to {@code amount} items off the table, first slot first. */
    private static int consume(WeavingMenu menu, int amount) {
        int lost = 0;
        for (int i = 0; i < menu.materials.getContainerSize() && lost < amount; i++) {
            ItemStack stack = menu.materials.getItem(i);
            int take = Math.min(stack.getCount(), amount - lost);
            if (take > 0) {
                stack.shrink(take);
                lost += take;
            }
        }
        menu.materials.setChanged();
        return lost;
    }

    public static boolean raise(ServerPlayer player, ResourceLocation branch) {
        WeavingContent content = WeavingContent.current();
        Optional<WeavingData> next = WeavingRules.raise(content, get(player), branch);
        if (next.isEmpty()) {
            report(player, "You can't raise " + content.branchName(branch) + " yet.");
            return false;
        }
        set(player, next.get());
        report(player, content.branchName(branch) + " rises to level " + WeavingRules.heldLevel(content, next.get(), branch) + ".");
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.4F);
        return true;
    }

    public static boolean respec(ServerPlayer player) {
        WeavingContent content = WeavingContent.current();
        WeavingData before = get(player);
        Optional<WeavingData> next = WeavingRules.respec(before);
        if (next.isEmpty()) {
            report(player, "No points to take back.");
            return false;
        }
        int refunded = WeavingRules.pointsSpent(content, before);
        set(player, next.get());
        report(player, refunded + (refunded == 1 ? " point returns" : " points return") + " to you. Deeds you earned stay earned.");
        return true;
    }

    /** The Spell shows a pattern the first time a player slays the creature it names. */
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide || event.isCanceled()) {
            return;
        }
        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer player ? player
                : victim.getKillCredit() instanceof ServerPlayer player ? player : null;
        if (killer != null && killer != victim) {
            grantForKill(killer, BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()));
        }
    }

    /** Teaches any patterns slaying this entity grants. Returns the ones learned. */
    public static List<ResourceLocation> grantForKill(ServerPlayer player, ResourceLocation entity) {
        WeavingContent content = WeavingContent.current();
        WeavingData data = get(player);
        List<ResourceLocation> granted = WeavingRules.grantsForKill(content, data, entity);
        if (granted.isEmpty()) {
            return granted;
        }
        for (ResourceLocation id : granted) {
            data = data.withLearned(id);
            String name = content.profile(content.recipes().get(id)).formalName();
            player.sendSystemMessage(Component.literal("The Spell shows you a Weaving pattern: " + name + ".")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        set(player, data);
        sync(player);
        return granted;
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_RESULT.remove(event.getEntity().getUUID());
    }

    private static void report(ServerPlayer player, String message) {
        LAST_RESULT.put(player.getUUID(), message);
        sync(player);
    }

    public static void sync(ServerPlayer player) {
        // Fake and game-test players have no negotiated mod channel to send on.
        if (player.connection == null || !player.connection.hasChannel(WeavingStatusPayload.TYPE)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, status(player));
    }

    public static String lastResult(ServerPlayer player) {
        return LAST_RESULT.getOrDefault(player.getUUID(), "");
    }

    public static WeavingStatusPayload status(ServerPlayer player) {
        return status(WeavingContent.current(), get(player), soulRank(player), lastResult(player));
    }

    static WeavingStatusPayload status(WeavingContent content, WeavingData data, Optional<SoulRank> soulRank, String lastResult) {
        List<WeavingStatusPayload.Branch> branches = new ArrayList<>();
        content.branches().forEach((id, branch) -> branches.add(new WeavingStatusPayload.Branch(
                branch.name(), WeavingRules.heldLevel(content, data, id), branch.maxLevel(), data.xp(id),
                nextText(content, data, id), WeavingRules.canRaise(content, data, id))));
        List<WeavingStatusPayload.Pattern> patterns = new ArrayList<>();
        List<WeavingStatusPayload.Hint> hints = new ArrayList<>();
        content.recipes().forEach((id, recipe) -> {
            MemoryContentCatalog.MemoryProfile profile = content.profile(recipe);
            if (WeavingRules.knows(data, id, recipe)) {
                patterns.add(new WeavingStatusPayload.Pattern(profile.formalName(), recipe.result().toString(),
                        content.branchName(recipe.branch()), profile.tier(), WeavingRules.rankName(profile.rank()),
                        recipe.materials().stream().map(material -> new WeavingStatusPayload.Material(material.item().toString(), material.count())).toList(),
                        data.woven(id), WeavingRules.lock(content, data, recipe, soulRank).orElse("")));
            } else if (!data.hint(id).isEmpty()) {
                hints.add(new WeavingStatusPayload.Hint(content.branchName(recipe.branch()), profile.tier(), recipe.materials().size(),
                        data.hint(id).stream().map(ResourceLocation::toString).toList(), recipe.hint()));
            }
        });
        return new WeavingStatusPayload(WeavingRules.unspent(content, data), WeavingRules.xpTowardPoint(content, data),
                content.settings().xpPerPoint(), WeavingRules.canRespec(data), branches, patterns, hints, lastResult);
    }

    static String nextText(WeavingContent content, WeavingData data, ResourceLocation branch) {
        WeavingRules.NextStep next = WeavingRules.next(content, data, branch);
        if (next.maxed()) {
            return "Every level is yours.";
        }
        if (next.deed().isPresent()) {
            WeavingBranch.Deed deed = next.deed().get();
            return "Level " + next.level() + " is a deed: " + deed.description() + " (" + next.progress() + "/" + deed.count() + ")";
        }
        return "Level " + next.level() + " costs " + next.cost() + (next.cost() == 1 ? " point" : " points");
    }
}
