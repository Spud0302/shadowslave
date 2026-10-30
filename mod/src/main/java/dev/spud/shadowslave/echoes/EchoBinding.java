package dev.spud.shadowslave.echoes;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.item.SoulShardItem;
import dev.spud.shadowslave.network.payload.EchoStatusPayload;
import dev.spud.shadowslave.soul.core.ShardRank;
import dev.spud.shadowslave.weaving.WeavingContent;
import dev.spud.shadowslave.weaving.WeavingMenu;
import dev.spud.shadowslave.weaving.WeavingRules;
import dev.spud.shadowslave.weaving.WeavingService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Binding an echo remnant into an Echo at the Weaving Table (DESIGN: the
 * Binding branch is the hardest, matching how rare Echoes are). The remnant
 * goes in the piece slot and a soul shard of the creature's rank or better in
 * the anchor; binding takes essence, the Binding level the creature's rank
 * asks for, and the anchor shard, and earns Binding XP.
 */
public final class EchoBinding {
    public static final ResourceLocation BINDING = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "binding");

    private EchoBinding() {
    }

    static int bindingLevel(ServerPlayer player) {
        return WeavingRules.heldLevel(WeavingContent.current(), WeavingService.get(player), BINDING);
    }

    static Optional<ShardRank> anchor(WeavingMenu menu) {
        return menu.anchor().getItem() instanceof SoulShardItem shard ? Optional.of(shard.rank()) : Optional.empty();
    }

    /** What the remnant on the player's open Weaving Table needs, for the table's screen. */
    static Optional<EchoStatusPayload.Binding> preview(ServerPlayer player) {
        if (!(player.containerMenu instanceof WeavingMenu menu)) {
            return Optional.empty();
        }
        EchoBody body = menu.centre().get(EchoesRegistry.REMNANT_OF.get());
        if (body == null) {
            return Optional.empty();
        }
        EchoSettings settings = EchoContent.current().settings();
        CreatureRank rank = body.rank();
        return Optional.of(new EchoStatusPayload.Binding(body.name() + " Echo", body.rankText(), settings.bindingLevel(rank),
                bindingLevel(player), settings.bindEssence(rank), CreatureRank.of(EchoRules.anchorFor(rank)).displayName(),
                EchoRules.bindLock(settings, rank, bindingLevel(player), anchor(menu), EssenceService.current(player)).orElse("")));
    }

    /** The table's Bind button. Returns the bound Echo item, or empty if nothing was bound. */
    public static ItemStack bind(ServerPlayer player, WeavingMenu menu) {
        EchoBody body = menu.centre().get(EchoesRegistry.REMNANT_OF.get());
        if (body == null) {
            return ItemStack.EMPTY;
        }
        EchoSettings settings = EchoContent.current().settings();
        CreatureRank rank = body.rank();
        Optional<String> lock = EchoRules.bindLock(settings, rank, bindingLevel(player), anchor(menu), EssenceService.current(player));
        if (lock.isPresent()) {
            WeavingService.report(player, lock.get() + ".");
            return ItemStack.EMPTY;
        }
        if (!menu.output().isEmpty()) {
            WeavingService.report(player, "Take what is in the output slot first.");
            return ItemStack.EMPTY;
        }
        EssenceService.spend(player, settings.bindEssence(rank));
        menu.table().removeItem(WeavingMenu.ANCHOR_SLOT, 1);
        menu.table().setItem(WeavingMenu.CENTRE_SLOT, ItemStack.EMPTY);
        ItemStack echo = EchoService.item(EchoRecord.bind(body, player.getGameProfile().getName()));
        menu.table().setItem(WeavingMenu.OUTPUT_SLOT, echo);
        int xp = settings.bindXp(rank);
        WeavingService.creditXp(player, BINDING, xp);
        player.level().playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 1.0F);
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.5F);
        player.sendSystemMessage(Component.literal("You bind the echo of the " + body.name() + ". It is yours now: an Echo.")
                .withStyle(ChatFormatting.AQUA));
        WeavingService.report(player, "Bound the " + body.name() + " Echo (+" + xp + " Binding XP).");
        EchoService.sync(player);
        return echo;
    }
}
