package dev.spud.shadowslave.weaving;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Operator shortcuts for testing Weaving: grant XP, teach a pattern, or wipe progress. */
public final class WeavingCommands {
    private WeavingCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_weaving")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("xp")
                        .then(Commands.argument("branch", ResourceLocationArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(WeavingContent.current().branches().keySet(), builder))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> xp(context.getSource(), ResourceLocationArgument.getId(context, "branch"),
                                                IntegerArgumentType.getInteger(context, "amount"))))))
                .then(Commands.literal("learn")
                        .then(Commands.argument("pattern", ResourceLocationArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(WeavingContent.current().recipes().keySet(), builder))
                                .executes(context -> learn(context.getSource(), ResourceLocationArgument.getId(context, "pattern")))))
                .then(Commands.literal("reset").executes(context -> reset(context.getSource()))));
    }

    private static int xp(CommandSourceStack source, ResourceLocation branch, int amount) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (WeavingContent.current().branch(branch).isEmpty()) {
            source.sendFailure(Component.literal("No Weaving branch " + branch));
            return 0;
        }
        WeavingService.set(player, WeavingService.get(player).withXp(branch, amount));
        WeavingService.sync(player);
        source.sendSuccess(() -> Component.literal("+" + amount + " " + WeavingContent.current().branchName(branch) + " XP")
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learn(CommandSourceStack source, ResourceLocation pattern) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!WeavingContent.current().recipes().containsKey(pattern)) {
            source.sendFailure(Component.literal("No Weaving pattern " + pattern));
            return 0;
        }
        WeavingService.set(player, WeavingService.get(player).withLearned(pattern));
        WeavingService.sync(player);
        source.sendSuccess(() -> Component.literal("Learned " + pattern).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int reset(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        WeavingService.set(player, WeavingData.empty());
        WeavingService.sync(player);
        source.sendSuccess(() -> Component.literal("Weaving progress wiped").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return Command.SINGLE_SUCCESS;
    }
}
