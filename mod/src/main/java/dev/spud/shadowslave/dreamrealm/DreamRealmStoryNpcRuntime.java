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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        boolean exists = level.getEntitiesOfClass(Pillager.class, search, DreamRealmStoryNpcRuntime::isAshenWatchCaptain).stream()
                .findFirst().isPresent();
        if (!exists && !level.addFreshEntity(createCaptain(level, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 180.0F))) {
            throw new IllegalStateException("Could not place the Ashen Watch NPC presentation at Cinder Rest");
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
        if (player.isShiftKeyDown()) {
            player.sendSystemMessage(Component.literal(binding.standingRule()).withStyle(ChatFormatting.AQUA));
        } else {
            player.sendSystemMessage(Component.literal(binding.arrivalCue()).withStyle(ChatFormatting.DARK_GRAY));
            player.sendSystemMessage(Component.literal("Available here: " + String.join(", ", binding.serviceLabels())).withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.literal("Sneak-interact to inspect the local standing rule.").withStyle(ChatFormatting.DARK_GRAY));
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static boolean isAshenWatchCaptain(net.minecraft.world.entity.Entity entity) {
        return entity.getTags().contains(STORY_NPC_TAG)
                && entity.getTags().contains(ASHEN_WATCH_TAG)
                && entity.getTags().contains(WATCH_CAPTAIN_TAG);
    }

    private static Pillager createCaptain(ServerLevel level, double x, double y, double z, float yRot) {
        var binding = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        Pillager captain = EntityType.PILLAGER.create(level);
        if (captain == null) {
            throw new IllegalStateException("Could not create Watch Captain presentation body");
        }
        captain.moveTo(x, y, z, yRot, 0.0F);
        captain.setCustomName(Component.literal(binding.factionName() + " " + binding.archetypeDisplayName()));
        captain.setCustomNameVisible(true);
        captain.setPersistenceRequired();
        captain.setNoAi(true);
        captain.setInvulnerable(true);
        captain.setCanPickUpLoot(false);
        captain.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        captain.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SPYGLASS));
        captain.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SOUL_LANTERN));
        captain.addTag(STORY_NPC_TAG);
        captain.addTag(ASHEN_WATCH_TAG);
        captain.addTag(WATCH_CAPTAIN_TAG);
        return captain;
    }

    private static int spawnNear(ServerPlayer player) {
        var captain = createCaptain(player.serverLevel(), player.getX() + 1.5D, player.getY(), player.getZ() + 1.5D, player.getYRot() + 180.0F);
        if (!player.serverLevel().addFreshEntity(captain)) return 0;
        player.sendSystemMessage(Component.literal("Placed Grey Lanterns Watch Captain presentation.").withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private static int showStatus(ServerPlayer player) {
        var binding = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        StringJoiner services = new StringJoiner(", ");
        binding.serviceLabels().forEach(services::add);
        player.sendSystemMessage(Component.literal("Story NPC execution: " + binding.moduleDisplayName()).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(binding.settlementName() + " | " + binding.factionName() + " | " + binding.archetypeDisplayName()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Services: " + services).withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("Standing rule: " + binding.standingRule()).withStyle(ChatFormatting.AQUA));
        return Command.SINGLE_SUCCESS;
    }
}
