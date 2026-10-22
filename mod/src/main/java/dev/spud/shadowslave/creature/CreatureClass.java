package dev.spud.shadowslave.creature;

import net.minecraft.util.StringRepresentable;

/**
 * How many soul cores a Nightmare Creature has (CANON, ch. 4): one for a
 * Beast up to seven for a Titan. Each core leaves its own shard, and each
 * Class brings its own behaviour package (DESIGN, see {@link ClassBehaviours}).
 */
public enum CreatureClass implements StringRepresentable {
    BEAST("beast", "Beast", 1),
    MONSTER("monster", "Monster", 2),
    DEMON("demon", "Demon", 3),
    DEVIL("devil", "Devil", 4),
    TYRANT("tyrant", "Tyrant", 5),
    TERROR("terror", "Terror", 6),
    TITAN("titan", "Titan", 7);

    public static final com.mojang.serialization.Codec<CreatureClass> CODEC = StringRepresentable.fromEnum(CreatureClass::values);

    private final String serializedName;
    private final String displayName;
    private final int cores;

    CreatureClass(String serializedName, String displayName, int cores) {
        this.serializedName = serializedName;
        this.displayName = displayName;
        this.cores = cores;
    }

    public String displayName() {
        return displayName;
    }

    /** Soul cores, and so the shards the creature drops. */
    public int cores() {
        return cores;
    }

    public static CreatureClass byOrdinal(int ordinal) {
        CreatureClass[] values = values();
        return values[Math.max(0, Math.min(values.length - 1, ordinal))];
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
