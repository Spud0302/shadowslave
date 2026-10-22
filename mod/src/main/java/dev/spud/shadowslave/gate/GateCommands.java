package dev.spud.shadowslave.gate;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Operator controls for Gates: {@code /shadowslave gate open <category>}
 * opens one where you stand (a certain Category, no Lure needed),
 * {@code /shadowslave gate close} closes the nearest, and
 * {@code /shadowslave gate status} reports it.
 */
public final class GateCommands {
    private GateCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave")
                .then(Commands.literal("gate")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("open")
                                .then(Commands.argument("category", IntegerArgumentType.integer(GateCategory.MIN, GateCategory.MAX))
                                        .executes(context -> open(context.getSource(),
                                                IntegerArgumentType.getInteger(context, "category")))))
                        .then(Commands.literal("close").executes(context -> close(context.getSource())))
                        .then(Commands.literal("status").executes(context -> status(context.getSource())))));
    }

    private static int open(CommandSourceStack source, int category) {
        ServerLevel level = source.getLevel();
        BlockPos at = BlockPos.containing(source.getPosition()).below();
        if (GateService.open(level, at, GateTrigger.COMMAND, GateForecast.certain(category)).isEmpty()) {
            source.sendFailure(Component.literal("Another Gate is already open within 64 blocks."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Opening a Category " + category + " Gate."), true);
        return 1;
    }

    private static int close(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        return GateService.nearest(level, BlockPos.containing(source.getPosition()))
                .map(gate -> {
                    GateService.close(level, gate.id());
                    source.sendSuccess(() -> Component.literal("Closed the Gate at " + gate.center().toShortString() + "."), true);
                    return 1;
                })
                .orElseGet(() -> {
                    source.sendFailure(Component.literal("No Gate is open in this dimension."));
                    return 0;
                });
    }

    private static int status(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        return GateService.nearest(level, BlockPos.containing(source.getPosition()))
                .map(gate -> {
                    source.sendSuccess(() -> Component.literal("Gate at " + gate.center().toShortString() + ": "
                            + gate.phase().getSerializedName() + ", Category " + gate.category() + ", wave " + gate.wave()
                            + ", opened by " + gate.trigger().getSerializedName()), false);
                    return 1;
                })
                .orElseGet(() -> {
                    source.sendFailure(Component.literal("No Gate is open in this dimension."));
                    return 0;
                });
    }
}
