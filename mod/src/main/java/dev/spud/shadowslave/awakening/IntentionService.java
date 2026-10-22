package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.attachment.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * The intention offering at the Dream Candle (DESIGN): before the Spell names
 * an Aspect, a Dreamer offers an item and names a longing. Both lean the roll
 * toward a Nature; neither decides it.
 */
public final class IntentionService {
    public enum Result {
        OFFERED,
        NAMED_ONLY,
        ALREADY_NAMED
    }

    private IntentionService() {
    }

    public static IntentionData get(ServerPlayer player) {
        return player.getData(ModAttachments.INTENTION);
    }

    public static void clear(ServerPlayer player) {
        player.setData(ModAttachments.INTENTION, IntentionData.empty());
    }

    /** The evidence tag an item leans toward, if the Spell takes an interest in it. */
    public static Optional<String> tagFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        for (Longing longing : Longing.values()) {
            if (stack.is(itemTag(longing))) {
                return Optional.of(longing.evidenceTag());
            }
        }
        return Optional.empty();
    }

    /**
     * Names a longing and offers one of whatever is in the main hand. An item
     * the Spell has no interest in stays in the hand, and an earlier offering
     * stands until something new is offered.
     */
    public static Result offer(ServerPlayer player, Longing longing) {
        if (AspectRollService.hasAspect(player)) {
            return Result.ALREADY_NAMED;
        }
        IntentionData before = get(player);
        ItemStack held = player.getMainHandItem();
        Optional<String> tag = tagFor(held);
        String offeringTag = before.offeringTag();
        String offeringName = before.offeringName();
        if (tag.isPresent()) {
            offeringTag = tag.get();
            offeringName = held.getHoverName().getString();
            held.shrink(1);
        } else if (!held.isEmpty()) {
            player.sendSystemMessage(Component.literal("The Spell takes no interest in " + held.getHoverName().getString() + ".")
                    .withStyle(ChatFormatting.GRAY));
        }
        player.setData(ModAttachments.INTENTION, new IntentionData(offeringTag, offeringName, longing.serializedName()));

        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.3, 0.4, 0.3, 0.02);
        level.playSound(null, player.blockPosition(), tag.isPresent() ? SoundEvents.AMETHYST_BLOCK_RESONATE : SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.8F, 0.7F);
        return tag.isPresent() ? Result.OFFERED : Result.NAMED_ONLY;
    }

    static TagKey<Item> itemTag(Longing longing) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "intention/" + longing.evidenceTag()));
    }
}
