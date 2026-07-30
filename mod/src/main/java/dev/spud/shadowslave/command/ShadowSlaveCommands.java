package dev.spud.shadowslave.command;

import com.mojang.brigadier.Command;
import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.network.SoulSyncService;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Operator-facing commands for Java architecture and migration smoke tests. */
public final class ShadowSlaveCommands {
    private static final ResourceLocation TEST_ASPECT =
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "prototype/veiled_witness");
    private static final ResourceLocation TEST_FLAW =
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "prototype/heavy_step");

    private ShadowSlaveCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave")
                .then(Commands.literal("soul")
                        .executes(context -> showSoul(context.getSource().getPlayerOrException())))
                .then(Commands.literal("soul_screen")
                        .executes(context -> openSoulScreen(context.getSource().getPlayerOrException())))
                .then(Commands.literal("infect")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> infect(context.getSource().getPlayerOrException())))
                .then(Commands.literal("complete_first_nightmare_test")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> completeFirstNightmare(context.getSource().getPlayerOrException())))
                .then(Commands.literal("reset")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> reset(context.getSource().getPlayerOrException()))));
    }

    private static int showSoul(ServerPlayer player) {
        SoulData soul = SoulService.get(player);
        player.sendSystemMessage(Component.literal("— Soul —").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("Spell state: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(soul.spellState().serializedName()).withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("Soul Rank: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(soul.soulRank().serializedName()).withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("Aspect: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(soul.aspectId().map(ResourceLocation::toString).orElse("—")).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("Flaw: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(soul.flawId().map(ResourceLocation::toString).orElse("—")).withStyle(ChatFormatting.RED)));
        player.sendSystemMessage(Component.literal("Schema: " + soul.schemaVersion() + " / migration: " + soul.migrationVersion())
                .withStyle(ChatFormatting.DARK_GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int openSoulScreen(ServerPlayer player) {
        SoulSyncService.openScreen(player);
        return Command.SINGLE_SUCCESS;
    }

    private static int infect(ServerPlayer player) {
        SoulData before = SoulService.get(player);
        SoulData after = SoulService.infect(player);
        if (after == before) {
            player.sendSystemMessage(Component.literal("The Spell has already touched your soul.").withStyle(ChatFormatting.GRAY));
        } else {
            player.sendSystemMessage(Component.literal("The Nightmare Spell marks you as a Carrier.")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int completeFirstNightmare(ServerPlayer player) {
        try {
            SoulService.completeFirstNightmare(player, TEST_ASPECT, TEST_FLAW);
            player.sendSystemMessage(Component.literal("Test transition complete: Sleeper / Dormant.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            return Command.SINGLE_SUCCESS;
        } catch (IllegalStateException exception) {
            player.sendSystemMessage(Component.literal(exception.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int reset(ServerPlayer player) {
        SoulService.reset(player);
        player.sendSystemMessage(Component.literal("Soul state reset to Mundane.").withStyle(ChatFormatting.GRAY));
        return Command.SINGLE_SUCCESS;
    }
}
