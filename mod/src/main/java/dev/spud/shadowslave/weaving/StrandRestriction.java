package dev.spud.shadowslave.weaving;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;

/**
 * A condition a Weaver may tie a strand to when tuning it (lore: a
 * self-imposed restriction makes an enchantment stronger). A tuned strand
 * works only while its condition holds, and works half again as hard.
 */
public enum StrandRestriction implements StringRepresentable {
    NIGHT("night", "only at night"),
    WOUNDED("wounded", "only while wounded"),
    DEEP("deep", "only out of the sky's sight");

    public static final Codec<StrandRestriction> CODEC = StringRepresentable.fromEnum(StrandRestriction::values);

    private final String serializedName;
    private final String label;

    StrandRestriction(String serializedName, String label) {
        this.serializedName = serializedName;
        this.label = label;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public String label() {
        return label;
    }

    /** Whether the condition holds for whoever wears or wields the piece. */
    public boolean holds(LivingEntity holder) {
        return switch (this) {
            case NIGHT -> !holder.level().isDay();
            case WOUNDED -> holder.getHealth() <= holder.getMaxHealth() * 0.5F;
            case DEEP -> !holder.level().canSeeSky(holder.blockPosition());
        };
    }
}
