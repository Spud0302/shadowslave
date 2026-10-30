package dev.spud.shadowslave.spell.truename;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.spud.shadowslave.fieldguide.FieldGuideRegistry;
import dev.spud.shadowslave.fieldguide.WrittenName;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Map;
import java.util.Optional;

/**
 * {@code /shadowslave truename}: read your True Name and the names you know,
 * show or hide yours as a title, speak it to someone close, write a name onto a
 * Field Note, speak a known name to sense its bearer, or bind the Memory in your
 * hand with it. Operators can grant, take away and list deeds.
 */
public final class TrueNameCommands {
    private TrueNameCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave")
                .then(Commands.literal("truename")
                        .executes(context -> info(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.literal("show")
                                .executes(context -> shown(context.getSource(), true)))
                        .then(Commands.literal("hide")
                                .executes(context -> shown(context.getSource(), false)))
                        .then(Commands.literal("reveal")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> TrueNames.reveal(context.getSource().getPlayerOrException(),
                                                EntityArgument.getPlayer(context, "player")) ? Command.SINGLE_SUCCESS : 0)))
                        .then(Commands.literal("write")
                                .executes(context -> write(context.getSource(), ""))
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(knownNames(context.getSource()), builder))
                                        .executes(context -> write(context.getSource(), StringArgumentType.getString(context, "name")))))
                        .then(Commands.literal("sense")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(knownNames(context.getSource()), builder))
                                        .executes(context -> TrueNames.sense(context.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(context, "name")) == TrueNames.Sensed.SENSED ? Command.SINGLE_SUCCESS : 0)))
                        .then(Commands.literal("bind")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    return TrueNameBinding.bind(player, player.getMainHandItem()) == TrueNameBinding.Outcome.BOUND ? Command.SINGLE_SUCCESS : 0;
                                }))
                        .then(Commands.literal("grant")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> grant(context.getSource(), EntityArgument.getPlayer(context, "player"), "steadfast"))
                                        .then(Commands.argument("deed", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(TrueNameContent.get().ids(), builder))
                                                .executes(context -> grant(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                                        StringArgumentType.getString(context, "deed"))))))
                        .then(Commands.literal("forget")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            TrueNames.forget(EntityArgument.getPlayer(context, "player"));
                                            context.getSource().sendSuccess(() -> Component.literal("The True Name is gone."), true);
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(Commands.literal("names")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> names(context.getSource())))));
    }

    private static Iterable<String> knownNames(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return player == null ? java.util.List.of() : TrueNames.data(player).known().stream().map(TrueNameData.Known::name).toList();
    }

    private static int info(CommandSourceStack source, ServerPlayer player) {
        TrueNameData data = TrueNames.data(player);
        if (data.named()) {
            source.sendSuccess(() -> Component.literal("Your True Name is " + data.name() + ".").withStyle(ChatFormatting.GOLD), false);
            source.sendSuccess(() -> Component.literal("  " + data.deedLine()).withStyle(ChatFormatting.GRAY), false);
            source.sendSuccess(() -> Component.literal(data.shown() ? "  It shows as your title." : "  It is hidden from others.")
                    .withStyle(ChatFormatting.DARK_GRAY), false);
        } else {
            source.sendSuccess(() -> Component.literal("You have no True Name.").withStyle(ChatFormatting.GRAY), false);
        }
        long now = player.server.overworld().getGameTime();
        for (TrueNameData.Known known : data.known()) {
            long ready = TrueNames.readyAt(known);
            source.sendSuccess(() -> Component.literal("  You know " + known.name() + ", the True Name of " + known.bearerName()
                    + (now < ready ? " (quiet for " + TrueNames.minutes(ready - now) + ")" : "")).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int shown(CommandSourceStack source, boolean shown) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        if (!TrueNames.data(player).named()) {
            source.sendFailure(Component.literal("You have no True Name."));
            return 0;
        }
        if (!TrueNames.setShown(player, shown)) {
            source.sendSuccess(() -> Component.literal(shown ? "It shows already." : "It is hidden already."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal(shown ? "Your True Name shows as your title. Everyone who reads it knows it now."
                : "Your True Name is hidden. Those who learned it still know it.").withStyle(ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    /** Writes a name onto a Field Note made from the paper in hand: your own, or one you know. */
    private static int write(CommandSourceStack source, String spoken) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        Optional<WrittenName> written = spoken.isBlank()
                ? TrueNames.nameOf(player).map(name -> new WrittenName(player.getUUID(), player.getGameProfile().getName(), name))
                : TrueNames.data(player).knowsName(spoken).map(known -> new WrittenName(known.bearer(), known.bearerName(), known.name()));
        if (written.isEmpty()) {
            source.sendFailure(Component.literal(spoken.isBlank() ? "You have no True Name to write." : "You know no True Name like that."));
            return 0;
        }
        ItemStack hand = player.getMainHandItem();
        if (!hand.is(Items.PAPER)) {
            source.sendFailure(Component.literal("Hold a sheet of paper to write on."));
            return 0;
        }
        hand.shrink(1);
        ItemStack note = new ItemStack(FieldGuideRegistry.FIELD_NOTE_ITEM.get());
        note.set(FieldGuideRegistry.WRITTEN_NAME.get(), written.get());
        if (hand.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, note);
        } else if (!player.getInventory().add(note)) {
            player.drop(note, false);
        }
        source.sendSuccess(() -> Component.literal("You write a True Name on a Field Note. Whoever reads it will know it.")
                .withStyle(ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int grant(CommandSourceStack source, ServerPlayer player, String deedId) {
        Optional<TrueNameDeed> deed = TrueNameContent.get().deed(deedId);
        if (deed.isEmpty()) {
            source.sendFailure(Component.literal("No True Name deed called " + deedId + "."));
            return 0;
        }
        if (TrueNames.data(player).named()) {
            TrueNames.forget(player);
        }
        Optional<TrueNames.Grant> grant = TrueNames.grant(player, deedId, deed.get(), true);
        if (grant.isEmpty()) {
            source.sendFailure(Component.literal("The Spell found no free name for that deed."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Granted " + player.getGameProfile().getName() + " the True Name " + grant.get().name() + "."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int names(CommandSourceStack source) {
        Map<java.util.UUID, String> names = dev.spud.shadowslave.spell.SpellRecord.get(source.getServer()).trueNames();
        source.sendSuccess(() -> Component.literal(names.size() + " True Names granted in this world."), false);
        names.forEach((id, name) -> {
            ServerPlayer online = source.getServer().getPlayerList().getPlayer(id);
            String who = online == null ? id.toString() : online.getGameProfile().getName();
            source.sendSuccess(() -> Component.literal("  " + who + ": " + name).withStyle(ChatFormatting.GRAY), false);
        });
        return Command.SINGLE_SUCCESS;
    }
}
