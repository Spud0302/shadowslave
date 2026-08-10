package dev.spud.shadowslave.dreamrealm;

import com.mojang.brigadier.Command;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.StringJoiner;

/**
 * Removable NeoForge execution adapter for one authored Dream Realm settlement NPC.
 * The Villager body, tags, command and chat presentation are placeholders and own no canonical state.
 */
public final class DreamRealmStoryNpcRuntime {
    static final String STORY_NPC_TAG = "shadowslave_story_npc";
    static final String ASHEN_WATCH_TAG = "shadowslave_story_module_ashen_watch";
    static final String WATCH_CAPTAIN_TAG = "shadowslave_story_archetype_watch_captain";

    private DreamRealmStoryNpcRuntime() {
    }

    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_storynpc")
                .then(Commands.literal("spawn_ashen_watch_captain")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> spawnAshenWatchCaptain(context.getSource().getPlayerOrException())))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource().getPlayerOrException()))));
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!event.getTarget().getTags().contains(STORY_NPC_TAG)) {
            return;
        }

        // This adapter currently owns exactly one physical archetype. Unknown tagged bodies fail closed.
        if (!event.getTarget().getTags().contains(ASHEN_WATCH_TAG)
                || !event.getTarget().getTags().contains(WATCH_CAPTAIN_TAG)) {
            player.sendSystemMessage(Component.literal("This story NPC body has no resolvable Java-owned identity.")
                    .withStyle(ChatFormatting.RED));
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        DreamRealmStoryNpcExecutionBinding.Binding binding = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        player.sendSystemMessage(Component.literal(binding.archetypeDisplayName() + " — " + binding.factionName())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(binding.settlementName() + " / " + binding.moduleDisplayName())
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal(binding.arrivalCue()).withStyle(ChatFormatting.DARK_GRAY));
        player.sendSystemMessage(Component.literal("Available here: " + String.join(", ", binding.serviceLabels()))
                .withStyle(ChatFormatting.YELLOW));

        // Suppress vanilla Villager trading. The placeholder body must not manufacture economy or relationship state.
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static int spawnAshenWatchCaptain(ServerPlayer player) {
        DreamRealmStoryNpcExecutionBinding.Binding binding = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        ServerLevel level = player.serverLevel();
        Villager villager = new Villager(EntityType.VILLAGER, level);
        villager.moveTo(player.getX() + 1.5D, player.getY(), player.getZ() + 1.5D, player.getYRot() + 180.0F, 0.0F);
        villager.setCustomName(Component.literal(binding.factionName() + " " + binding.archetypeDisplayName()));
        villager.setCustomNameVisible(true);
        villager.setPersistenceRequired();
        villager.setNoAi(true);
        villager.setInvulnerable(true);
        villager.addTag(STORY_NPC_TAG);
        villager.addTag(ASHEN_WATCH_TAG);
        villager.addTag(WATCH_CAPTAIN_TAG);

        if (!level.addFreshEntity(villager)) {
            player.sendSystemMessage(Component.literal("Could not place the Ashen Watch NPC placeholder.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        player.sendSystemMessage(Component.literal("Placed " + binding.factionName() + " " + binding.archetypeDisplayName()
                        + " at " + binding.settlementName() + " execution seam.")
                .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("PLACEHOLDER: vanilla Villager body; right-click for Java-owned story presentation.")
                .withStyle(ChatFormatting.DARK_GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int showStatus(ServerPlayer player) {
        DreamRealmStoryNpcExecutionBinding.Binding binding = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        StringJoiner services = new StringJoiner(", ");
        binding.serviceLabels().forEach(services::add);
        player.sendSystemMessage(Component.literal("Story NPC execution: " + binding.moduleDisplayName())
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(binding.settlementName() + " | " + binding.factionName()
                        + " | " + binding.archetypeDisplayName())
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Services: " + services).withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("No trades, standing, quests, rewards, progression, or world authority are executed here.")
                .withStyle(ChatFormatting.DARK_GRAY));
        return Command.SINGLE_SUCCESS;
    }
}
