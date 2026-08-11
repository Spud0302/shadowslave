package dev.spud.shadowslave.memory;

import com.mojang.brigadier.Command;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.function.Function;

/** Player-facing manifestation controls for implemented Memories. */
public final class MemoryCommands {
    private MemoryCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_memory")
                .then(Commands.literal("summon")
                        .then(Commands.literal("ash_compass").executes(context -> summon(context.getSource().getPlayerOrException(), "Ash Compass", MemoryManifestationService::summonAshCompass)))
                        .then(Commands.literal("bellglass_token").executes(context -> summon(context.getSource().getPlayerOrException(), "Bellglass Token", MemoryManifestationService::summonBellglassToken)))
                        .then(Commands.literal("red_thread_bracelet").executes(context -> summon(context.getSource().getPlayerOrException(), "Red Thread Bracelet", MemoryManifestationService::summonRedThreadBracelet)))
                        .then(Commands.literal("borrowed_dawn").executes(context -> summon(context.getSource().getPlayerOrException(), "Borrowed Dawn", MemoryManifestationService::summonBorrowedDawn))))
                .then(Commands.literal("dismiss")
                        .then(Commands.literal("ash_compass").executes(context -> dismiss(context.getSource().getPlayerOrException(), "Ash Compass", MemoryManifestationService::dismissAshCompass)))
                        .then(Commands.literal("bellglass_token").executes(context -> dismiss(context.getSource().getPlayerOrException(), "Bellglass Token", MemoryManifestationService::dismissBellglassToken)))
                        .then(Commands.literal("red_thread_bracelet").executes(context -> dismiss(context.getSource().getPlayerOrException(), "Red Thread Bracelet", MemoryManifestationService::dismissRedThreadBracelet)))
                        .then(Commands.literal("borrowed_dawn").executes(context -> dismiss(context.getSource().getPlayerOrException(), "Borrowed Dawn", MemoryManifestationService::dismissBorrowedDawn)))));
    }

    private static int summon(ServerPlayer player, String name, Function<ServerPlayer, MemoryManifestationService.ManifestResult> operation) {
        MemoryManifestationService.ManifestResult result = operation.apply(player);
        switch (result) {
            case SUMMONED -> player.sendSystemMessage(Component.literal("[" + name + "] manifests.").withStyle(ChatFormatting.AQUA));
            case ALREADY_SUMMONED -> player.sendSystemMessage(Component.literal("[" + name + "] is already manifested.").withStyle(ChatFormatting.GRAY));
            case INVENTORY_FULL -> player.sendSystemMessage(Component.literal("[" + name + "] cannot manifest while your inventory is full.").withStyle(ChatFormatting.RED));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [" + name + "].").withStyle(ChatFormatting.RED));
            default -> throw new IllegalStateException("Unexpected summon result: " + result);
        }
        return result == MemoryManifestationService.ManifestResult.SUMMONED || result == MemoryManifestationService.ManifestResult.ALREADY_SUMMONED ? Command.SINGLE_SUCCESS : 0;
    }

    private static int dismiss(ServerPlayer player, String name, Function<ServerPlayer, MemoryManifestationService.ManifestResult> operation) {
        MemoryManifestationService.ManifestResult result = operation.apply(player);
        switch (result) {
            case DISMISSED -> player.sendSystemMessage(Component.literal("[" + name + "] dissolves back into your soul.").withStyle(ChatFormatting.DARK_AQUA));
            case NOT_SUMMONED -> player.sendSystemMessage(Component.literal("[" + name + "] is not currently manifested.").withStyle(ChatFormatting.GRAY));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [" + name + "].").withStyle(ChatFormatting.RED));
            default -> throw new IllegalStateException("Unexpected dismiss result: " + result);
        }
        return result == MemoryManifestationService.ManifestResult.DISMISSED || result == MemoryManifestationService.ManifestResult.NOT_SUMMONED ? Command.SINGLE_SUCCESS : 0;
    }
}
