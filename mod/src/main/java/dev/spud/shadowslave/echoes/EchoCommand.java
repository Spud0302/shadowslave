package dev.spud.shadowslave.echoes;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Optional;

/**
 * The command wheel's orders (DESIGN). Follow, Stay and Guard are standing
 * orders the soul remembers; Attack sets every summoned Echo on what the
 * player is looking at, then they fall back to guarding.
 *
 * <ul>
 *   <li>Follow: keep close and only fight what attacks the Echo.</li>
 *   <li>Stay: hold this spot and only fight what comes at the Echo.</li>
 *   <li>Guard: keep close and fight whatever hurts you or you hurt.</li>
 * </ul>
 */
public enum EchoCommand implements StringRepresentable {
    FOLLOW("follow", "Follow"),
    STAY("stay", "Stay"),
    GUARD("guard", "Guard"),
    ATTACK("attack", "Attack");

    public static final Codec<EchoCommand> CODEC = StringRepresentable.fromEnum(EchoCommand::values);

    private final String serializedName;
    private final String label;

    EchoCommand(String serializedName, String label) {
        this.serializedName = serializedName;
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** Whether the order is kept as the Echoes' standing order. */
    public boolean standing() {
        return this != ATTACK;
    }

    public static Optional<EchoCommand> byName(String name) {
        return Arrays.stream(values()).filter(command -> command.serializedName.equals(name)).findFirst();
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
