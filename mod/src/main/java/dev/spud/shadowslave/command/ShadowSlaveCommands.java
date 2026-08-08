package dev.spud.shadowslave.command;

import com.mojang.brigadier.Command;
import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.migration.DatapackMigrationOutcome;
import dev.spud.shadowslave.migration.DatapackMigrationService;
import dev.spud.shadowslave.migration.ImportedIdentityData;
import dev.spud.shadowslave.migration.ImportedIdentityService;
import dev.spud.shadowslave.network.SoulSyncService;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.nightmare.NightmareService;
import dev.spud.shadowslave.preview.PreviewPowerService;
import dev.spud.shadowslave.preview.PreviewResetService;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SpellState;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Player preview controls plus operator-facing architecture and migration commands. */
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
                .then(Commands.literal("preview_begin")
                        .executes(context -> previewBegin(context.getSource().getPlayerOrException())))
                .then(Commands.literal("preview_reset")
                        .executes(context -> previewReset(context.getSource().getPlayerOrException())))
                .then(Commands.literal("nightmare_enter")
                        .executes(context -> enterNightmare(context.getSource().getPlayerOrException())))
                .then(Commands.literal("nightmare_status")
                        .executes(context -> nightmareStatus(context.getSource().getPlayerOrException())))
                .then(Commands.literal("nightmare_recover")
                        .executes(context -> recoverNightmare(context.getSource().getPlayerOrException())))
                .then(Commands.literal("kindle")
                        .executes(context -> kindle(context.getSource().getPlayerOrException())))
                .then(Commands.literal("migrate_datapack")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> migrateDatapack(context.getSource().getPlayerOrException())))
                .then(Commands.literal("infect")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> infect(context.getSource().getPlayerOrException())))
                .then(Commands.literal("begin_first_nightmare_test")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> beginFirstNightmare(context.getSource().getPlayerOrException())))
                .then(Commands.literal("complete_first_nightmare_test")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> completeFirstNightmare(context.getSource().getPlayerOrException())))
                .then(Commands.literal("reset")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> previewReset(context.getSource().getPlayerOrException()))));
    }

    private static int showSoul(ServerPlayer player) {
        SoulData soul = SoulService.get(player);
        SoulIdentityData identity = SoulIdentityService.get(player);
        ImportedIdentityData importedIdentity = ImportedIdentityService.get(player);
        String aspect = identity.aspect()
                .map(value -> value.formalName().orElse("<name unrevealed>") + " [" + value.instanceId() + "]")
                .orElseGet(() -> importedIdentity.aspect()
                        .map(value -> value.formalName() + " [" + value.instanceId() + "]")
                        .orElseGet(() -> soul.aspectId().map(ResourceLocation::toString).orElse("—")));
        String flaw = identity.flaw()
                .map(value -> value.formalName().orElse("<name unrevealed>") + " [" + value.instanceId() + "]")
                .orElseGet(() -> importedIdentity.flaw()
                        .map(value -> value.formalName() + " [" + value.instanceId() + "]")
                        .orElseGet(() -> soul.flawId().map(ResourceLocation::toString).orElse("—")));

        player.sendSystemMessage(Component.literal("— Soul —").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("Status: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(soul.spellState().serializedName()).withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("Path: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(soul.awakeningPath().serializedName()).withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("Soul Rank: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(soul.soulRank().map(SoulRank::serializedName).orElse("—"))
                        .withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("Aspect: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(aspect).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("Aspect Rank: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(soul.aspectRank().map(SoulRank::serializedName).orElse("—"))
                        .withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("Flaw: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(flaw).withStyle(ChatFormatting.RED)));
        player.sendSystemMessage(Component.literal("Schema: " + soul.schemaVersion() + " / migration: " + soul.migrationVersion())
                .withStyle(ChatFormatting.DARK_GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int openSoulScreen(ServerPlayer player) {
        SoulSyncService.openScreen(player);
        return Command.SINGLE_SUCCESS;
    }

    private static int previewBegin(ServerPlayer player) {
        try {
            SoulData soul = SoulService.get(player);
            if (soul.spellState() == SpellState.UNINFECTED) {
                SoulService.infect(player);
                player.sendSystemMessage(Component.literal(
                        "DEVELOPMENT PREVIEW: infection is granted by this command. The command is not a canonical infection cause."
                ).withStyle(ChatFormatting.YELLOW));
            }
            NightmareService.tryEnter(player);
            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException exception) {
            player.sendSystemMessage(Component.literal(exception.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int previewReset(ServerPlayer player) {
        PreviewResetService.reset(player);
        player.sendSystemMessage(Component.literal("Preview state reset to uninfected (Mundane description).")
                .withStyle(ChatFormatting.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int enterNightmare(ServerPlayer player) {
        try {
            NightmareService.tryEnter(player);
            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException exception) {
            player.sendSystemMessage(Component.literal(exception.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int nightmareStatus(ServerPlayer player) {
        NightmareInstance instance = NightmareService.activeFor(player).orElse(null);
        if (instance == null) {
            player.sendSystemMessage(Component.literal("No active Nightmare instance.").withStyle(ChatFormatting.GRAY));
            return Command.SINGLE_SUCCESS;
        }
        player.sendSystemMessage(Component.literal("Active Nightmare: " + instance.scenarioId())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal("Historical role: " + instance.historicalRoleId())
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Instance: " + instance.instanceId() + " / slot " + instance.slot())
                .withStyle(ChatFormatting.DARK_GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int recoverNightmare(ServerPlayer player) {
        try {
            NightmareService.technicalRecover(player);
            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException exception) {
            player.sendSystemMessage(Component.literal(exception.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int kindle(ServerPlayer player) {
        return PreviewPowerService.activateKindle(player) ? Command.SINGLE_SUCCESS : 0;
    }

    private static int migrateDatapack(ServerPlayer player) {
        try {
            DatapackMigrationOutcome outcome = DatapackMigrationService.migrate(player);
            ChatFormatting color = switch (outcome.status()) {
                case MIGRATED_CARRIER, MIGRATED_DREAMER -> ChatFormatting.GREEN;
                case ALREADY_MIGRATED -> ChatFormatting.AQUA;
                case NO_LEGACY_STATE -> ChatFormatting.GRAY;
            };
            player.sendSystemMessage(Component.literal(outcome.status() + ": " + outcome.detail()).withStyle(color));
            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException exception) {
            player.sendSystemMessage(Component.literal("Migration refused: " + exception.getMessage())
                    .withStyle(ChatFormatting.RED));
            ShadowSlaveMod.LOGGER.warn("Datapack migration refused for {}", player.getScoreboardName(), exception);
            return 0;
        }
    }

    private static int infect(ServerPlayer player) {
        SoulData before = SoulService.get(player);
        SoulData after = SoulService.infect(player);
        if (after == before) {
            player.sendSystemMessage(Component.literal("The Spell has already touched your soul.")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            player.sendSystemMessage(Component.literal("The Nightmare Spell marks you as a Carrier.")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int beginFirstNightmare(ServerPlayer player) {
        try {
            SoulService.beginFirstNightmare(player);
            player.sendSystemMessage(Component.literal("Test transition complete: Aspirant / Dormant Soul Core.")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            return Command.SINGLE_SUCCESS;
        } catch (IllegalStateException exception) {
            player.sendSystemMessage(Component.literal(exception.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int completeFirstNightmare(ServerPlayer player) {
        try {
            SoulService.completeFirstNightmare(player, TEST_ASPECT, SoulRank.DORMANT, TEST_FLAW);
            player.sendSystemMessage(Component.literal("Test appraisal complete: Dreamer (Sleeper) / Dormant.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            return Command.SINGLE_SUCCESS;
        } catch (IllegalStateException exception) {
            player.sendSystemMessage(Component.literal(exception.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }
}