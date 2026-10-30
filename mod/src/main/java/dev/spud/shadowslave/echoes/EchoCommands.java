package dev.spud.shadowslave.echoes;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Optional;

/**
 * Operator shortcuts for testing Echoes: {@code remnant <entity>} hands over
 * the echo remnant that creature would leave, {@code bound <entity>} a bound
 * Echo of it, and {@code clear} empties the soul's Echoes.
 */
public final class EchoCommands {
    private EchoCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_echoes")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("remnant").then(Commands.argument("entity", ResourceLocationArgument.id())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), builder))
                        .executes(context -> give(context.getSource(), ResourceLocationArgument.getId(context, "entity"), false))))
                .then(Commands.literal("bound").then(Commands.argument("entity", ResourceLocationArgument.id())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), builder))
                        .executes(context -> give(context.getSource(), ResourceLocationArgument.getId(context, "entity"), true))))
                .then(Commands.literal("clear").executes(context -> clear(context.getSource()))));
    }

    /** What a freshly spawned {@code entity} would leave, as if it had just been slain. */
    public static Optional<EchoBody> bodyFor(ServerPlayer player, ResourceLocation entity) {
        Optional<Entity> made = BuiltInRegistries.ENTITY_TYPE.getOptional(entity).map(type -> type.create(player.serverLevel()));
        if (made.isEmpty() || !(made.get() instanceof Mob mob)) {
            made.ifPresent(Entity::discard);
            return Optional.empty();
        }
        mob.moveTo(player.getX(), player.getY(), player.getZ());
        mob.finalizeSpawn(player.serverLevel(), player.serverLevel().getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.COMMAND, null);
        Optional<EchoBody> body = EchoRemnants.bodyOf(EchoContent.current(), mob);
        mob.discard();
        return body;
    }

    private static int give(CommandSourceStack source, ResourceLocation entity, boolean bound) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<EchoBody> body = bodyFor(player, entity);
        if (body.isEmpty()) {
            source.sendFailure(Component.literal(entity + " leaves no echo remnant."));
            return 0;
        }
        ItemStack stack = bound ? EchoService.item(EchoRecord.bind(body.get(), player.getGameProfile().getName()))
                : EchoRemnants.remnant(body.get());
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        source.sendSuccess(() -> Component.literal((bound ? "Bound Echo: " : "Echo remnant: ") + body.get().name()
                + " (" + body.get().rankText() + ")").withStyle(ChatFormatting.AQUA), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int clear(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        EchoService.dismissAll(player, true);
        EchoService.setHeld(player, SoulEchoes.EMPTY);
        EchoService.sync(player);
        source.sendSuccess(() -> Component.literal("Your soul's Echoes are cleared.").withStyle(ChatFormatting.AQUA), false);
        return Command.SINGLE_SUCCESS;
    }
}
