package dev.spud.shadowslave.smithing;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

/**
 * How well a piece was forged (DESIGN). The grade scales the pattern's base
 * damage, armour and durability, and sets how many strands Weaving can lay
 * into the piece later.
 */
public enum Quality implements StringRepresentable {
    CRUDE("crude", "Crude", 0, 85, 1, ChatFormatting.GRAY),
    FAIR("fair", "Fair", 40, 100, 1, ChatFormatting.WHITE),
    FINE("fine", "Fine", 65, 110, 2, ChatFormatting.GREEN),
    SUPERIOR("superior", "Superior", 85, 120, 3, ChatFormatting.AQUA),
    MASTERWORK("masterwork", "Masterwork", 97, 130, 4, ChatFormatting.GOLD);

    public static final Codec<Quality> CODEC = StringRepresentable.fromEnum(Quality::values);

    private final String serializedName;
    private final String displayName;
    private final int minScore;
    private final int statPercent;
    private final int threads;
    private final ChatFormatting colour;

    Quality(String serializedName, String displayName, int minScore, int statPercent, int threads, ChatFormatting colour) {
        this.serializedName = serializedName;
        this.displayName = displayName;
        this.minScore = minScore;
        this.statPercent = statPercent;
        this.threads = threads;
        this.colour = colour;
    }

    public static Quality forScore(int score) {
        Quality best = CRUDE;
        for (Quality quality : values()) {
            if (score >= quality.minScore) {
                best = quality;
            }
        }
        return best;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public String displayName() {
        return displayName;
    }

    public int minScore() {
        return minScore;
    }

    /** Damage, armour and durability against the pattern's base, as a percentage. */
    public int statPercent() {
        return statPercent;
    }

    /** How many strands Weaving can lay into a piece of this grade. */
    public int threads() {
        return threads;
    }

    public ChatFormatting colour() {
        return colour;
    }
}
