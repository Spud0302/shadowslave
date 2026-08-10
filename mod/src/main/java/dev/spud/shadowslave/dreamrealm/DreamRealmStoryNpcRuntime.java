package dev.spud.shadowslave.dreamrealm;

import com.mojang.brigadier.Command;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.StringJoiner;

/** Removable NeoForge execution adapter for one authored Dream Realm settlement NPC. */
public final class DreamRealmStoryNpcRuntime {
    static final String STORY_NPC_TAG = "shadowslave_story_npc";
    static final String ASHEN_WATCH_TAG = "shadowslave_story_module_ashen_watch";
    static final String WATCH_CAPTAIN_TAG = "shadowslave_story_archetype_watch_captain";

    private DreamRealmStoryNpcRuntime() {}

    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_storynpc")
                .then(Commands.literal("spawn_ashen_watch_captain")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> spawnNear(context.getSource().getPlayerOrException())))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource().getPlayerOrException()))));
    }

    public static void ensureAshenWatchCaptain(ServerLevel level, BlockPos pos) {
        AABB search = new AABB(pos).inflate(8.0D);
        boolean exists = level.getEntitiesOfClass(Villager.class, search, DreamRealmStoryNpcRuntime::isAshenWatchCaptain).stream()
                .findFirst().isPresent();
        if (!exists && !level.addFreshEntity(createCaptain(level, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 180.0F))) {
            throw new IllegalStateException("Could not place the Ashen Watch NPC placeholder at Cinder Rest");
        }
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getTarget().getTags().contains(STORY_NPC_TAG)) return;

        if (!isAshenWatchCaptain(event.getTarget())) {
            player.sendSystemMessage(Component.literal("This story NPC body has no resolvable Java-owned identity.").withStyle(ChatFormatting.RED));
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        var binding = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        player.sendSystemMessage(Component.literal(binding.archetypeDisplayName() + " — " + binding.factionName()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(binding.settlementName() + " / " + binding.moduleDisplayName()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal(binding.arrivalCue()).withStyle(ChatFormatting.DARK_GRAY));
        player.sendSystemMessage(Component.literal("Available here: " + String.join(", ", binding.serviceLabels())).withStyle(ChatFormatting.YELLOW));
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static boolean isAshenWatchCaptain(net.minecraft.world.entity.Entity entity) {
        return entity.getTags().contains(STORY_NPC_TAG)
                && entity.getTags().contains(ASHEN_WATCH_TAG)
                && entity.getTags().contains(WATCH_CAPTAIN_TAG);
    }

    private static Villager createCaptain(ServerLevel level, double x, double y, double z, float yRot) {
        var binding = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        Villager villager = new Villager(EntityType.VILLAGER, level);
        villager.moveTo(x, y, z, yRot, 0.0F);
        villager.setCustomName(Component.literal(binding.factionName() + " " + binding.archetypeDisplayName()));
        villager.setCustomNameVisible(true);
        villager.setPersistenceRequired();
        villager.setNoAi(true);
        villager.setInvulnerable(true);
        villager.addTag(STORY_NPC_TAG);
        villager.addTag(ASHEN_WATCH_TAG);
        villager.addTag(WATCH_CAPTAIN_TAG);
        return villager;
    }

    private static int spawnNear(ServerPlayer player) {
        var villager = createCaptain(player.serverLevel(), player.getX() + 1.5D, player.getY(), player.getZ() + 1.5D, player.getYRot() + 180.0F);
        if (!player.serverLevel().addFreshEntity(villager)) return 0;
        player.sendSystemMessage(Component.literal("Placed Grey Lanterns Watch Captain placeholder.").withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private static int showStatus(ServerPlayer player) {
        var binding = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        StringJoiner services = new StringJoiner(", ");
        binding.serviceLabels().forEach(services::add);
        player.sendSystemMessage(Component.literal("Story NPC execution: " + binding.moduleDisplayName()).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(binding.settlementName() + " | " + binding.factionName() + " | " + binding.archetypeDisplayName()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Services: " + services).withStyle(ChatFormatting.YELLOW));
        return Command.SINGLE_SUCCESS;
    }
}
