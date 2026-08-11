package dev.spud.shadowslave.dreamrealm;

import com.mojang.brigadier.Command;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Development commands for bounded Dream Realm physical slices. */
public final class DreamRealmCommands {
    private DreamRealmCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_dreamrealm")
                .then(Commands.literal("enter")
                        .executes(context -> enter(context.getSource().getPlayerOrException())))
                .then(Commands.literal("enter_storm_lantern")
                        .executes(context -> enterStormLantern(context.getSource().getPlayerOrException())))
                .then(Commands.literal("enter_drowned_bell_history")
                        .executes(context -> enterDrownedBellHistory(context.getSource().getPlayerOrException())))
                .then(Commands.literal("exit")
                        .executes(context -> exit(context.getSource().getPlayerOrException())))
                .then(Commands.literal("status")
                        .executes(context -> status(context.getSource().getPlayerOrException()))));
    }

    private static int enter(ServerPlayer player) {
        try {
            DreamRealmPreviewService.enter(player);
            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException exception) {
            player.sendSystemMessage(Component.literal(exception.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int enterStormLantern(ServerPlayer player) {
        try {
            StormLanternCoastPreviewService.enter(player);
            var level = player.serverLevel();
            var sitePlan = StormLanternCoastSitePlan.drownedBellLater(level.getSeed());
            StormLanternCoastEncounterService.populate(player, sitePlan);
            player.sendSystemMessage(Component.literal("The coast is not empty. Disturbed ground, damaged stone and abandoned chainwork may warn of nearby pressure if you notice them.")
                    .withStyle(ChatFormatting.DARK_RED));
            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException exception) {
            player.sendSystemMessage(Component.literal(exception.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int enterDrownedBellHistory(ServerPlayer player) {
        try {
            DrownedBellHistoricalPreviewService.enter(player);
            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException exception) {
            player.sendSystemMessage(Component.literal(exception.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int exit(ServerPlayer player) {
        if (!DreamRealmPreviewService.isInside(player)) {
            player.sendSystemMessage(Component.literal("You are not inside the Dream Realm development slice.")
                    .withStyle(ChatFormatting.GRAY));
            return Command.SINGLE_SUCCESS;
        }
        DreamRealmPreviewService.exit(player);
        return Command.SINGLE_SUCCESS;
    }

    private static int status(ServerPlayer player) {
        var slice = DreamRealmVerticalSliceDefinition.ashenExpanse();
        player.sendSystemMessage(Component.literal("Dream Realm regression slice: " + slice.region().displayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal("Landmarks: " + String.join(", ", slice.region().landmarkHooks()))
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Resource hooks: " + String.join(", ", slice.region().resourceHooks()))
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Era-linked pair: historical Drowned Bell <-> later Storm Lantern Coast ruins")
                .withStyle(ChatFormatting.DARK_AQUA));
        player.sendSystemMessage(Component.literal("Physical executor: "
                        + (DreamRealmPreviewService.isInside(player) ? "inside Dream Realm" : "outside Dream Realm"))
                .withStyle(ChatFormatting.DARK_GRAY));
        return Command.SINGLE_SUCCESS;
    }
}
