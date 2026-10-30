package dev.spud.shadowslave.fieldguide;

import dev.spud.shadowslave.spell.truename.TrueNames;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A Field Note: a loose page in the Field Guide's hand (DESIGN). For now it
 * carries one thing, a True Name someone wrote down (Run B); reading it
 * teaches the name to the reader, so a note is a secret anyone can pass on.
 * Copying guide pages onto notes comes with the Field Guide's next slice.
 */
public final class FieldNoteItem extends Item {
    public FieldNoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer reader) {
            WrittenName written = stack.get(FieldGuideRegistry.WRITTEN_NAME.get());
            if (written == null) {
                reader.displayClientMessage(Component.translatable("item.shadowslave.field_note.blank").withStyle(ChatFormatting.GRAY), true);
            } else if (written.bearer().equals(reader.getUUID())) {
                reader.displayClientMessage(Component.translatable("item.shadowslave.field_note.own").withStyle(ChatFormatting.GOLD), true);
            } else if (!TrueNames.learn(reader, written.bearer(), written.bearerName(), written.name(),
                    "It was written on a Field Note.")) {
                reader.displayClientMessage(Component.translatable("item.shadowslave.field_note.known").withStyle(ChatFormatting.GRAY), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** The note never shows the name on its face: it has to be read. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        WrittenName written = stack.get(FieldGuideRegistry.WRITTEN_NAME.get());
        if (written == null) {
            tooltip.add(Component.translatable("item.shadowslave.field_note.tooltip_blank").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable("item.shadowslave.field_note.tooltip_name").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.shadowslave.field_note.tooltip_read").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(FieldGuideRegistry.WRITTEN_NAME.get());
    }
}
