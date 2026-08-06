package dev.spud.shadowslave.command;

import com.mojang.brigadier.Command;
import dev.spud.shadowslave.nightmare.NightmareService;
import dev.spud.shadowslave.nightmare.NightmareStatusReport;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Operator observability for durable successful-completion recovery. */
public final class NightmareCompletionStatusCommand {
    private NightmareCompletionStatusCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_completion_status")
                .requires(source -> source.hasPermission(2))
                .executes(context -> show(context.getSource().getPlayerOrException())));
    }

    private static int show(ServerPlayer player) {
        NightmareStatusReport report = NightmareStatusReport.from(
                NightmareService.activeFor(player),
                NightmareService.successfulCompletionFor(player)
        );
        player.sendSystemMessage(Component.literal("— Nightmare completion state —")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        for (String line : report.lines()) {
            ChatFormatting color = line.contains("CONFLICT")
                    ? ChatFormatting.RED
                    : ChatFormatting.GRAY;
            player.sendSystemMessage(Component.literal(line).withStyle(color));
        }
        return Command.SINGLE_SUCCESS;
    }
}
