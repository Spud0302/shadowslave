package dev.spud.shadowslave.citadel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Some items a Citadel asks for or takes: one item, or any item of a tag
 * ({@code {"item": "minecraft:stone_bricks", "count": 32}} or
 * {@code {"tag": "c:ingots/iron", "count": 8}}). Used for upgrade costs, and
 * with {@code points} for what feeds a Gateway.
 *
 * @param count  how many an upgrade asks for (feeding ignores it)
 * @param points how much one of them feeds the Gateway (costs ignore it)
 */
public record Cost(Optional<ResourceLocation> item, Optional<TagKey<Item>> tag, int count, double points) {
    public static final Codec<Cost> CODEC = RecordCodecBuilder.<Cost>create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("item").forGetter(Cost::item),
            TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(Cost::tag),
            Codec.intRange(1, 4096).optionalFieldOf("count", 1).forGetter(Cost::count),
            Codec.doubleRange(0, 1_000_000).optionalFieldOf("points", 0.0).forGetter(Cost::points)
    ).apply(instance, Cost::new)).validate(cost -> cost.item.isPresent() != cost.tag.isPresent()
            ? DataResult.success(cost) : DataResult.error(() -> "a Citadel cost names one item or one tag"));

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (item.isPresent()) {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(item.get());
        }
        return tag.map(stack::is).orElse(false);
    }

    /** "32 Stone Bricks" or "8 of any Iron Ingots": how the Gateway names it. */
    public String describe() {
        String what = item.map(id -> BuiltInRegistries.ITEM.get(id).getDescription().getString())
                .orElseGet(() -> tag.map(Cost::tagName).orElse("?"));
        return count + " " + what;
    }

    /** A tag's readable name: its first item's name for a single-item tag, else its path in words. */
    static String tagName(TagKey<Item> tag) {
        var items = BuiltInRegistries.ITEM.getTag(tag);
        if (items.isPresent() && items.get().size() > 0) {
            String first = items.get().get(0).value().getDescription().getString();
            return items.get().size() == 1 ? first : first + " (or like)";
        }
        String path = tag.location().getPath();
        String last = path.substring(path.lastIndexOf('/') + 1).replace('_', ' ');
        return Character.toUpperCase(last.charAt(0)) + last.substring(1);
    }
}
