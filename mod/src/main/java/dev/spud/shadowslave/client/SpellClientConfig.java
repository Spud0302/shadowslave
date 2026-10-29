package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.CallingFrequencyPayload;
import dev.spud.shadowslave.spell.calling.CallingFrequency;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client options for the Spell (in the mod's config screen, and the
 * frequency also on the Field Guide's Callings page). The frequency is sent to
 * the server, which decides when a Calling may come.
 */
public final class SpellClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.EnumValue<CallingFrequency> CALLING_FREQUENCY = BUILDER
            .comment("How often the Spell calls to you: OFF, RARE, NORMAL or OFTEN.")
            .translation("shadowslave.configuration.calling_frequency")
            .defineEnum("callingFrequency", CallingFrequency.NORMAL);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private SpellClientConfig() {
    }

    public static CallingFrequency frequency() {
        return SPEC.isLoaded() ? CALLING_FREQUENCY.get() : CallingFrequency.NORMAL;
    }

    /** Changes the frequency, saves it, and tells the server. */
    public static void setFrequency(CallingFrequency frequency) {
        CALLING_FREQUENCY.set(frequency);
        CALLING_FREQUENCY.save();
        sync();
    }

    /** Tells the server the frequency, if connected to one that understands it. */
    public static void sync() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null && minecraft.getConnection().hasChannel(CallingFrequencyPayload.TYPE)) {
            PacketDistributor.sendToServer(new CallingFrequencyPayload(frequency().ordinal()));
        }
    }
}
