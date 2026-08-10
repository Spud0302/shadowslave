package dev.spud.shadowslave.memory;

import com.mojang.brigadier.Command;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Player-facing manifestation controls for implemented Memories. */
public final class MemoryCommands {
    private MemoryCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_memory")
                .then(Commands.literal("summon")
                        .then(Commands.literal("ash_compass")
                                .executes(context -> summonAshCompass(context.getSource().getPlayerOrException()))))
                .then(Commands.literal("dismiss")
                        .then(Commands.literal("ash_compass")
                                .executes(context -> dismissAshCompass(context.getSource().getPlayerOrException())))));
    }

    private static int summonAshCompass(ServerPlayer player) {
        MemoryManifestationService.ManifestResult result = MemoryManifestationService.summonAshCompass(player);
        switch (result) {
            case SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Compass] manifests in your hand-held world.")
                    .withStyle(ChatFormatting.AQUA));
            case ALREADY_SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Compass] is already manifested.")
                    .withStyle(ChatFormatting.GRAY));
            case INVENTORY_FULL -> player.sendSystemMessage(Component.literal("[Ash Compass] cannot manifest while your inventory is full.")
                    .withStyle(ChatFormatting.RED));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Compass].")
                    .withStyle(ChatFormatting.RED));
            default -> throw new IllegalStateException("Unexpected summon result: " + result);
        }
        return result == MemoryManifestationService.ManifestResult.SUMMONED
                || result == MemoryManifestationService.ManifestResult.ALREADY_SUMMONED
                ? Command.SINGLE_SUCCESS : 0;
    }

    private static int dismissAshCompass(ServerPlayer player) {
        MemoryManifestationService.ManifestResult result = MemoryManifestationService.dismissAshCompass(player);
        switch (result) {
            case DISMISSED -> player.sendSystemMessage(Component.literal("[Ash Compass] dissolves back into your soul.")
                    .withStyle(ChatFormatting.DARK_AQUA));
            case NOT_SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Compass] is not currently manifested.")
                    .withStyle(ChatFormatting.GRAY));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Compass].")
                    .withStyle(ChatFormatting.RED));
            default -> throw new IllegalStateException("Unexpected dismiss result: " + result);
        }
        return result == MemoryManifestationService.ManifestResult.DISMISSED
                || result == MemoryManifestationService.ManifestResult.NOT_SUMMONED
                ? Command.SINGLE_SUCCESS : 0;
    }
}
