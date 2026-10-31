package dev.spud.shadowslave.fieldguide;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/** Loot condition {@code shadowslave:lost_notes_enabled}: whether the {@code shadowslaveLostNotes} game rule is on. */
public final class LostNotesCondition implements LootItemCondition {
    public static final LostNotesCondition INSTANCE = new LostNotesCondition();
    public static final MapCodec<LostNotesCondition> CODEC = MapCodec.unit(INSTANCE);

    private LostNotesCondition() {
    }

    @Override
    public LootItemConditionType getType() {
        return FieldGuideRegistry.LOST_NOTES_ENABLED.get();
    }

    @Override
    public boolean test(LootContext context) {
        return FieldGuideRules.lostNotes(context.getLevel().getServer());
    }
}
