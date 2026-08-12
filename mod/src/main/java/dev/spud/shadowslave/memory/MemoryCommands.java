package dev.spud.shadowslave.memory;

import com.mojang.brigadier.Command;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Player-facing manifestation controls for implemented Memories. */
public final class MemoryCommands {
    private MemoryCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_memory")
                .then(Commands.literal("summon")
                        .then(Commands.literal("ash_compass").executes(context -> summon(context.getSource().getPlayerOrException(), "Ash Compass", MemoryManifestationService.summonAshCompass(context.getSource().getPlayerOrException()))))
                        .then(Commands.literal("glass_road").executes(context -> summon(context.getSource().getPlayerOrException(), "Glass Road", MemoryManifestationService.summonGlassRoad(context.getSource().getPlayerOrException()))))
                        .then(Commands.literal("stonewake_shield").executes(context -> summon(context.getSource().getPlayerOrException(), "Stonewake Shield", MemoryManifestationService.summonStonewakeShield(context.getSource().getPlayerOrException())))))
                .then(Commands.literal("dismiss")
                        .then(Commands.literal("ash_compass").executes(context -> dismiss(context.getSource().getPlayerOrException(), "Ash Compass", MemoryManifestationService.dismissAshCompass(context.getSource().getPlayerOrException()))))
                        .then(Commands.literal("glass_road").executes(context -> dismiss(context.getSource().getPlayerOrException(), "Glass Road", MemoryManifestationService.dismissGlassRoad(context.getSource().getPlayerOrException()))))
                        .then(Commands.literal("stonewake_shield").executes(context -> dismiss(context.getSource().getPlayerOrException(), "Stonewake Shield", MemoryManifestationService.dismissStonewakeShield(context.getSource().getPlayerOrException()))))));
    }

    private static int summon(ServerPlayer player, String name, MemoryManifestationService.ManifestResult result) {
        switch (result) {
            case SUMMONED -> player.sendSystemMessage(Component.literal("[" + name + "] manifests.").withStyle(ChatFormatting.AQUA));
            case ALREADY_SUMMONED -> player.sendSystemMessage(Component.literal("[" + name + "] is already manifested.").withStyle(ChatFormatting.GRAY));
            case INVENTORY_FULL -> player.sendSystemMessage(Component.literal("[" + name + "] cannot manifest while your inventory is full.").withStyle(ChatFormatting.RED));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [" + name + "].").withStyle(ChatFormatting.RED));
            default -> throw new IllegalStateException("Unexpected summon result: " + result);
        }
        return result == MemoryManifestationService.ManifestResult.SUMMONED || result == MemoryManifestationService.ManifestResult.ALREADY_SUMMONED ? Command.SINGLE_SUCCESS : 0;
    }

    private static int dismiss(ServerPlayer player, String name, MemoryManifestationService.ManifestResult result) {
        switch (result) {
            case DISMISSED -> player.sendSystemMessage(Component.literal("[" + name + "] dissolves back into your soul.").withStyle(ChatFormatting.DARK_AQUA));
            case NOT_SUMMONED -> player.sendSystemMessage(Component.literal("[" + name + "] is not currently manifested.").withStyle(ChatFormatting.GRAY));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [" + name + "].").withStyle(ChatFormatting.RED));
            default -> throw new IllegalStateException("Unexpected dismiss result: " + result);
        }
        return result == MemoryManifestationService.ManifestResult.DISMISSED || result == MemoryManifestationService.ManifestResult.NOT_SUMMONED ? Command.SINGLE_SUCCESS : 0;
    }
}
