package dev.spud.shadowslave.fieldguide;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/** Loot function {@code shadowslave:lost_field_note}: writes a lost note (see {@link FieldNotes#lost}) where the loot is rolled. */
public final class LostFieldNoteFunction implements LootItemFunction {
    public static final LostFieldNoteFunction INSTANCE = new LostFieldNoteFunction();
    public static final MapCodec<LostFieldNoteFunction> CODEC = MapCodec.unit(INSTANCE);

    private LostFieldNoteFunction() {
    }

    @Override
    public LootItemFunctionType<LostFieldNoteFunction> getType() {
        return FieldGuideRegistry.LOST_FIELD_NOTE.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        if (origin == null) {
            return stack;
        }
        stack.set(FieldGuideRegistry.NOTE_PAGE.get(), FieldNotes.lost(context.getLevel(), origin, context.getRandom()));
        return stack;
    }
}
