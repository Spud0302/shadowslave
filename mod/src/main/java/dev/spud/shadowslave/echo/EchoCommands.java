package dev.spud.shadowslave.echo;

import com.mojang.brigadier.Command;
import dev.spud.shadowslave.echo.content.EchoContentCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Player-facing controls for implemented Echo manifestations. */
public final class EchoCommands {
    private EchoCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_echo")
                .then(Commands.literal("summon").then(Commands.literal("ash_burrower").executes(context -> summonAshBurrower(context.getSource().getPlayerOrException()))))
                .then(Commands.literal("dismiss").then(Commands.literal("ash_burrower").executes(context -> dismissAshBurrower(context.getSource().getPlayerOrException()))))
                .then(Commands.literal("follow").then(Commands.literal("ash_burrower").executes(context -> commandAshBurrower(context.getSource().getPlayerOrException(), EchoContentCatalog.CommandMode.FOLLOW))))
                .then(Commands.literal("hold").then(Commands.literal("ash_burrower").executes(context -> commandAshBurrower(context.getSource().getPlayerOrException(), EchoContentCatalog.CommandMode.HOLD))))
                .then(Commands.literal("guard").then(Commands.literal("ash_burrower").executes(context -> commandAshBurrower(context.getSource().getPlayerOrException(), EchoContentCatalog.CommandMode.GUARD_POINT))))
                .then(Commands.literal("carry").then(Commands.literal("ash_burrower").executes(context -> carryAshBurrower(context.getSource().getPlayerOrException()))))
                .then(Commands.literal("unload").then(Commands.literal("ash_burrower").executes(context -> unloadAshBurrower(context.getSource().getPlayerOrException())))));
    }

    private static int summonAshBurrower(ServerPlayer player) {
        EchoManifestationService.ManifestResult result = EchoManifestationService.summonAshBurrower(player);
        switch (result) {
            case SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Burrower] takes shape beside you.").withStyle(ChatFormatting.AQUA));
            case ALREADY_SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Burrower] is already manifested.").withStyle(ChatFormatting.GRAY));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].").withStyle(ChatFormatting.RED));
            case SPAWN_FAILED -> player.sendSystemMessage(Component.literal("[Ash Burrower] could not take physical form here.").withStyle(ChatFormatting.RED));
            default -> throw new IllegalStateException("Unexpected Echo summon result: " + result);
        }
        return result == EchoManifestationService.ManifestResult.SUMMONED || result == EchoManifestationService.ManifestResult.ALREADY_SUMMONED ? Command.SINGLE_SUCCESS : 0;
    }

    private static int dismissAshBurrower(ServerPlayer player) {
        EchoManifestationService.ManifestResult result = EchoManifestationService.dismissAshBurrower(player);
        switch (result) {
            case DISMISSED -> player.sendSystemMessage(Component.literal("[Ash Burrower] dissolves out of the physical world.").withStyle(ChatFormatting.DARK_AQUA));
            case NOT_SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Burrower] is not currently manifested.").withStyle(ChatFormatting.GRAY));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].").withStyle(ChatFormatting.RED));
            default -> throw new IllegalStateException("Unexpected Echo dismiss result: " + result);
        }
        return result == EchoManifestationService.ManifestResult.DISMISSED || result == EchoManifestationService.ManifestResult.NOT_SUMMONED ? Command.SINGLE_SUCCESS : 0;
    }

    private static int commandAshBurrower(ServerPlayer player, EchoContentCatalog.CommandMode commandMode) {
        EchoManifestationService.CommandResult result = EchoManifestationService.commandAshBurrower(player, commandMode);
        switch (result) {
            case COMMAND_SET -> player.sendSystemMessage(Component.literal("[Ash Burrower] — " + commandText(commandMode)).withStyle(ChatFormatting.AQUA));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].").withStyle(ChatFormatting.RED));
            case UNSUPPORTED -> player.sendSystemMessage(Component.literal("[Ash Burrower] cannot execute that command yet.").withStyle(ChatFormatting.RED));
        }
        return result == EchoManifestationService.CommandResult.COMMAND_SET ? Command.SINGLE_SUCCESS : 0;
    }

    private static int carryAshBurrower(ServerPlayer player) {
        EchoManifestationService.CargoResult result = EchoManifestationService.loadAshBurrower(player);
        switch (result) {
            case LOADED -> player.sendSystemMessage(Component.literal("[Ash Burrower] takes the stack and follows with the load.").withStyle(ChatFormatting.AQUA));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].").withStyle(ChatFormatting.RED));
            case NOT_SUMMONED -> player.sendSystemMessage(Component.literal("Manifest [Ash Burrower] before giving it cargo.").withStyle(ChatFormatting.RED));
            case TOO_FAR -> player.sendSystemMessage(Component.literal("Move closer to [Ash Burrower] before handing over cargo.").withStyle(ChatFormatting.RED));
            case EMPTY_HAND -> player.sendSystemMessage(Component.literal("Hold a plain item stack in your main hand first.").withStyle(ChatFormatting.RED));
            case ALREADY_CARRYING -> player.sendSystemMessage(Component.literal("[Ash Burrower] is already carrying a load.").withStyle(ChatFormatting.RED));
            case UNSUPPORTED_ITEM -> player.sendSystemMessage(Component.literal("That stack carries custom item data and is not safe for this cargo executor yet.").withStyle(ChatFormatting.RED));
            default -> throw new IllegalStateException("Unexpected Echo load result: " + result);
        }
        return result == EchoManifestationService.CargoResult.LOADED ? Command.SINGLE_SUCCESS : 0;
    }

    private static int unloadAshBurrower(ServerPlayer player) {
        EchoManifestationService.CargoResult result = EchoManifestationService.unloadAshBurrower(player);
        switch (result) {
            case UNLOADED -> player.sendSystemMessage(Component.literal("[Ash Burrower] sets its cargo down beside itself.").withStyle(ChatFormatting.AQUA));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].").withStyle(ChatFormatting.RED));
            case NOT_SUMMONED -> player.sendSystemMessage(Component.literal("Manifest [Ash Burrower] before unloading its cargo.").withStyle(ChatFormatting.RED));
            case TOO_FAR -> player.sendSystemMessage(Component.literal("Move closer to [Ash Burrower] before unloading it.").withStyle(ChatFormatting.RED));
            case NO_CARGO -> player.sendSystemMessage(Component.literal("[Ash Burrower] is not carrying anything.").withStyle(ChatFormatting.GRAY));
            case UNSUPPORTED_ITEM -> player.sendSystemMessage(Component.literal("The stored cargo identity is no longer available.").withStyle(ChatFormatting.RED));
            case SPAWN_FAILED -> player.sendSystemMessage(Component.literal("There is no safe place to set that cargo down yet.").withStyle(ChatFormatting.RED));
            default -> throw new IllegalStateException("Unexpected Echo unload result: " + result);
        }
        return result == EchoManifestationService.CargoResult.UNLOADED ? Command.SINGLE_SUCCESS : 0;
    }

    private static String commandText(EchoContentCatalog.CommandMode commandMode) {
        return switch (commandMode) {
            case FOLLOW -> "Follow.";
            case HOLD -> "Hold here.";
            case GUARD_POINT -> "Guard this point.";
            default -> throw new IllegalArgumentException("No player-facing command text for " + commandMode);
        };
    }
}
