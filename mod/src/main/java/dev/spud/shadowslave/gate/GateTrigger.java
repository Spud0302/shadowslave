package dev.spud.shadowslave.gate;

import net.minecraft.util.StringRepresentable;

/**
 * What opened a Gate. The Gate itself does not care: any trigger calls
 * {@link GateService#open}. Natural world-event Gates (Jack, 2026-10-22: leave
 * room for them) only need a scheduler that calls it with {@link #WORLD_EVENT}.
 */
public enum GateTrigger implements StringRepresentable {
    /** A player fed and lit a Gate Lure. */
    LURE("lure"),
    /** An operator command. */
    COMMAND("command"),
    /** Reserved for natural Gates opening as world events. Nothing uses it yet. */
    WORLD_EVENT("world_event"),
    /** A Nightmare Seed in the Dream Realm bloomed; the Gate opens at the matching Overworld spot. */
    SEED_BLOOM("seed_bloom"),
    /** A player challenged a Nightmare Seed before it bloomed; the Gate opens at the Seed. */
    SEED_CLEANSE("seed_cleanse");

    private final String serializedName;

    GateTrigger(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static GateTrigger byName(String name) {
        for (GateTrigger trigger : values()) {
            if (trigger.serializedName.equals(name)) {
                return trigger;
            }
        }
        return COMMAND;
    }
}
