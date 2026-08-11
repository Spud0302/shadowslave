package dev.spud.shadowslave.combat;

import com.mojang.brigadier.Command;
import dev.spud.shadowslave.world.entity.ChainbackEntity;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Development-only command surface for physically judging the bounded combat prototype. */
public final class CombatPrototypeCommands {
    private static final double CHAINBACK_SPAWN_DISTANCE = 6.0D;

    private CombatPrototypeCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_combat")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("chainback_slice")
                        .executes(context -> setupChainbackSlice(context.getSource().getPlayerOrException()))));
    }

    private static int setupChainbackSlice(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-6D) {
            horizontal = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            horizontal = horizontal.normalize();
        }

        ChainbackEntity chainback = NightmareCreatureEntities.CHAINBACK.get().create(level);
        if (chainback == null) {
            player.sendSystemMessage(Component.literal("Combat prototype setup failed: Chainback could not be created.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        Vec3 spawn = player.position().add(horizontal.scale(CHAINBACK_SPAWN_DISTANCE));
        chainback.moveTo(spawn.x, player.getY(), spawn.z, player.getYRot() + 180.0F, 0.0F);
        chainback.setPersistenceRequired();
        chainback.setTarget(player);
        level.addFreshEntity(chainback);

        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        player.sendSystemMessage(Component.literal(
                "Combat prototype ready: read Chainback's warning, break range/line of sight, then punish its recovery with the iron sword."
        ).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(
                "Development fixture only: Better Combat owns the ordinary sword swing; Shadow Slave still owns Chainback's special-action state."
        ).withStyle(ChatFormatting.GRAY));
        return Command.SINGLE_SUCCESS;
    }
}
