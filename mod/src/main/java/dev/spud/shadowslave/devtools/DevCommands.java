package dev.spud.shadowslave.devtools;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.AspectRoller;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.ShardRank;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@code /shadowslave dev ...}: the dev menu's commands, for operators (or
 * cheats on in single player). The menu sends these; they work typed too.
 */
public final class DevCommands {
    private DevCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave")
                .then(Commands.literal("dev")
                        .requires(source -> source.hasPermission(DevTools.PERMISSION))
                        .then(Commands.literal("nature")
                                .then(Commands.argument("nature", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                AspectKitCatalog.all().stream().map(kit -> kit.natureName().toLowerCase(Locale.ROOT)), builder))
                                        .executes(context -> nature(context.getSource(), StringArgumentType.getString(context, "nature")))))
                        .then(Commands.literal("ability")
                                .then(Commands.literal("dormant")
                                        .then(Commands.argument("ability", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        AspectKitCatalog.all().stream().map(kit -> DevTools.abilityKey(kit.abilityId())), builder))
                                                .executes(context -> dormant(context.getSource(), StringArgumentType.getString(context, "ability")))))
                                .then(Commands.literal("awakened")
                                        .then(Commands.argument("ability", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(Stream.concat(Stream.of("none"),
                                                        KitLadder.all().stream().map(variant -> DevTools.abilityKey(variant.abilityId()))), builder))
                                                .executes(context -> awakened(context.getSource(), StringArgumentType.getString(context, "ability"))))))
                        .then(Commands.literal("rank")
                                .then(Commands.literal("dormant").executes(context -> rank(context.getSource(), SoulRank.DORMANT)))
                                .then(Commands.literal("awakened").executes(context -> rank(context.getSource(), SoulRank.AWAKENED))))
                        .then(Commands.literal("shards")
                                .then(Commands.argument("rank", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(ShardRank.values()).map(ShardRank::getSerializedName), builder))
                                        .executes(context -> shards(context.getSource(), StringArgumentType.getString(context, "rank"), 16))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(context -> shards(context.getSource(), StringArgumentType.getString(context, "rank"),
                                                        IntegerArgumentType.getInteger(context, "count"))))))
                        .then(Commands.literal("restore").executes(context -> restore(context.getSource())))
                        .then(Commands.literal("free_casting").executes(context -> freeCasting(context.getSource())))));
    }

    private static int nature(CommandSourceStack source, String name) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<AspectKit> kit = AspectKitCatalog.all().stream().filter(value -> value.natureName().equalsIgnoreCase(name)).findFirst();
        if (kit.isEmpty()) {
            source.sendFailure(Component.literal("No Nature called " + name + "."));
            return 0;
        }
        try {
            AspectRoller.Roll roll = DevTools.swapNature(player, kit.get());
            source.sendSuccess(() -> Component.literal("Now " + roll.aspectName() + " (" + kit.get().natureName() + ")."), true);
            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException exception) {
            source.sendFailure(Component.literal(String.valueOf(exception.getMessage())));
            return 0;
        }
    }

    private static int dormant(CommandSourceStack source, String key) throws CommandSyntaxException {
        Optional<ResourceLocation> ability = DevTools.dormantAbility(key);
        if (ability.isEmpty() || !DevTools.putDormant(source.getPlayerOrException(), ability.get())) {
            source.sendFailure(Component.literal(ability.isEmpty() ? "No Dormant ability called " + key + "." : "You have no Aspect yet."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("First key: " + nameOf(ability.get()) + "."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int awakened(CommandSourceStack source, String key) throws CommandSyntaxException {
        Optional<ResourceLocation> ability = key.equals("none") ? Optional.empty() : DevTools.awakenedAbility(key);
        if (!key.equals("none") && ability.isEmpty()) {
            source.sendFailure(Component.literal("No Awakened ability called " + key + "."));
            return 0;
        }
        if (!DevTools.putAwakened(source.getPlayerOrException(), ability)) {
            source.sendFailure(Component.literal("You have no Aspect yet."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Second key: " + ability.map(DevCommands::nameOf).orElse("empty") + "."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int rank(CommandSourceStack source, SoulRank rank) throws CommandSyntaxException {
        if (!DevTools.setRank(source.getPlayerOrException(), rank)) {
            source.sendFailure(Component.literal("Needs a named Aspect first (pick a Nature)."));
            return 0;
        }
        String name = rank == SoulRank.DORMANT ? "Dormant" : "Awakened";
        source.sendSuccess(() -> Component.literal("Soul set to " + name + "."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int shards(CommandSourceStack source, String rank, int count) throws CommandSyntaxException {
        Optional<ShardRank> shard = Arrays.stream(ShardRank.values()).filter(value -> value.getSerializedName().equals(rank)).findFirst();
        if (shard.isEmpty()) {
            source.sendFailure(Component.literal("No shard rank called " + rank + "."));
            return 0;
        }
        DevTools.giveShards(source.getPlayerOrException(), shard.get(), count);
        source.sendSuccess(() -> Component.literal("Gave " + count + " " + rank + " soul shards."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int restore(CommandSourceStack source) throws CommandSyntaxException {
        DevTools.restore(source.getPlayerOrException());
        source.sendSuccess(() -> Component.literal("Health, food and essence restored."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int freeCasting(CommandSourceStack source) throws CommandSyntaxException {
        boolean on = DevTools.toggleFreeCasting(source.getPlayerOrException());
        source.sendSuccess(() -> Component.literal("Free casting " + (on ? "on: abilities cost no essence." : "off.")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static String nameOf(ResourceLocation abilityId) {
        return KitLadder.byAbility(abilityId).map(variant -> variant.name())
                .or(() -> AspectKitCatalog.all().stream().filter(kit -> kit.abilityId().equals(abilityId)).map(AspectKit::abilityName).findFirst())
                .orElse(abilityId.toString());
    }
}
