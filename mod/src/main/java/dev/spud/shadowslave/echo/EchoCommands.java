package dev.spud.shadowslave.echo;

import com.mojang.brigadier.Command;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Player-facing controls for implemented Echo manifestations. */
public final class EchoCommands {
    private EchoCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_echo")
                .then(Commands.literal("summon")
                        .then(Commands.literal("ash_burrower")
                                .executes(context -> summonAshBurrower(context.getSource().getPlayerOrException()))))
                .then(Commands.literal("dismiss")
                        .then(Commands.literal("ash_burrower")
                                .executes(context -> dismissAshBurrower(context.getSource().getPlayerOrException())))));
    }

    private static int summonAshBurrower(ServerPlayer player) {
        EchoManifestationService.ManifestResult result = EchoManifestationService.summonAshBurrower(player);
        switch (result) {
            case SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Burrower] takes shape beside you.")
                    .withStyle(ChatFormatting.AQUA));
            case ALREADY_SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Burrower] is already manifested.")
                    .withStyle(ChatFormatting.GRAY));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].")
                    .withStyle(ChatFormatting.RED));
            case SPAWN_FAILED -> player.sendSystemMessage(Component.literal("[Ash Burrower] could not take physical form here.")
                    .withStyle(ChatFormatting.RED));
            default -> throw new IllegalStateException("Unexpected Echo summon result: " + result);
        }
        return result == EchoManifestationService.ManifestResult.SUMMONED
                || result == EchoManifestationService.ManifestResult.ALREADY_SUMMONED
                ? Command.SINGLE_SUCCESS : 0;
    }

    private static int dismissAshBurrower(ServerPlayer player) {
        EchoManifestationService.ManifestResult result = EchoManifestationService.dismissAshBurrower(player);
        switch (result) {
            case DISMISSED -> player.sendSystemMessage(Component.literal("[Ash Burrower] dissolves out of the physical world.")
                    .withStyle(ChatFormatting.DARK_AQUA));
            case NOT_SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Burrower] is not currently manifested.")
                    .withStyle(ChatFormatting.GRAY));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].")
                    .withStyle(ChatFormatting.RED));
            default -> throw new IllegalStateException("Unexpected Echo dismiss result: " + result);
        }
        return result == EchoManifestationService.ManifestResult.DISMISSED
                || result == EchoManifestationService.ManifestResult.NOT_SUMMONED
                ? Command.SINGLE_SUCCESS : 0;
    }
}
