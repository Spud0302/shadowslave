package dev.spud.shadowslave.weaving;

import com.mojang.serialization.Codec;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

import java.util.Optional;

/**
 * The kind of piece a strand suits (lore: an enchantment must suit its item).
 * An axe counts as a weapon; a pickaxe, shovel or hoe as a tool.
 */
public enum StrandFit implements StringRepresentable {
    WEAPON("weapon", "weapons", EquipmentSlot.MAINHAND),
    TOOL("tool", "tools", EquipmentSlot.MAINHAND),
    HELMET("helmet", "helmets", EquipmentSlot.HEAD),
    CHEST("chest", "chest armour", EquipmentSlot.CHEST),
    LEGS("legs", "leggings", EquipmentSlot.LEGS),
    BOOTS("boots", "boots", EquipmentSlot.FEET);

    public static final Codec<StrandFit> CODEC = StringRepresentable.fromEnum(StrandFit::values);

    private final String serializedName;
    private final String plural;
    private final EquipmentSlot slot;

    StrandFit(String serializedName, String plural, EquipmentSlot slot) {
        this.serializedName = serializedName;
        this.plural = plural;
        this.slot = slot;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public String plural() {
        return plural;
    }

    /** Where a piece of this kind does its work. */
    public EquipmentSlot slot() {
        return slot;
    }

    /** What kind of piece a stack is, if strands can be laid into it at all. */
    public static Optional<StrandFit> of(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return Optional.empty();
        }
        if (stack.getItem() instanceof ArmorItem armour) {
            return switch (armour.getType()) {
                case HELMET -> Optional.of(HELMET);
                case CHESTPLATE, BODY -> Optional.of(CHEST);
                case LEGGINGS -> Optional.of(LEGS);
                case BOOTS -> Optional.of(BOOTS);
            };
        }
        if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof TridentItem || stack.getItem() instanceof MaceItem
                || stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)) {
            return Optional.of(WEAPON);
        }
        if (stack.getItem() instanceof DiggerItem) {
            return Optional.of(TOOL);
        }
        return Optional.empty();
    }
}
