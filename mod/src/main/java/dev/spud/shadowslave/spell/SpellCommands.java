package dev.spud.shadowslave.spell;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.spud.shadowslave.spell.calling.Calling;
import dev.spud.shadowslave.spell.calling.SpellCallings;
import dev.spud.shadowslave.spell.voice.SpellVoice;
import dev.spud.shadowslave.spell.voice.SpellVoices;
import dev.spud.shadowslave.spell.voice.VoiceRequest;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * {@code /shadowslave spell}: read the Spell's record of you, the server's
 * firsts, and (operators) the world log, test a Calling or preview a line.
 */
public final class SpellCommands {
    private SpellCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave")
                .then(Commands.literal("spell")
                        .then(Commands.literal("record")
                                .executes(context -> record(context.getSource(), context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> record(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("firsts")
                                .executes(context -> firsts(context.getSource())))
                        .then(Commands.literal("log")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> log(context.getSource(), 10))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> log(context.getSource(), IntegerArgumentType.getInteger(context, "count")))))
                        .then(Commands.literal("call")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("calling", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(Calling.values()).map(Calling::id), builder))
                                        .executes(context -> call(context.getSource(), StringArgumentType.getString(context, "calling"),
                                                context.getSource().getPlayerOrException()))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> call(context.getSource(), StringArgumentType.getString(context, "calling"),
                                                        EntityArgument.getPlayer(context, "player"))))))
                        .then(Commands.literal("say")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("topic", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(SpellVoices.lines().topics().keySet(), builder))
                                        .then(Commands.argument("key", StringArgumentType.word())
                                                .executes(context -> say(context.getSource(), StringArgumentType.getString(context, "topic"),
                                                        StringArgumentType.getString(context, "key"))))))
                        .then(Commands.literal("voices")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> voices(context.getSource())))));
    }

    private static int record(CommandSourceStack source, ServerPlayer player) {
        SpellRecord record = SpellRecord.get(source.getServer());
        SpellRecord.Tally tally = record.find(player.getUUID()).orElseGet(SpellRecord.Tally::new);
        source.sendSuccess(() -> Component.literal("The Spell's record of " + player.getGameProfile().getName() + ":")
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        source.sendSuccess(() -> Component.literal("  Creatures slain: " + tally.totalKills() + " (" + tally.uniqueKills() + " unique)"), false);
        tally.kills().forEach((kind, count) -> source.sendSuccess(() -> Component.literal("    " + kind + ": " + count)
                .withStyle(ChatFormatting.GRAY), false));
        source.sendSuccess(() -> Component.literal("  Nightmares conquered " + tally.cleared() + ", failed " + tally.failed()
                + ", left " + tally.fled()), false);
        if (!tally.grades().isEmpty()) {
            source.sendSuccess(() -> Component.literal("  Recent grades: " + String.join(", ", tally.grades())), false);
        }
        source.sendSuccess(() -> Component.literal("  Deaths " + tally.deaths() + ", Citadels claimed " + tally.citadels()), false);
        for (SpellEvent event : record.recent(event -> event.player().filter(player.getUUID()::equals).isPresent(), 5)) {
            source.sendSuccess(() -> Component.literal("  - " + event.describe()).withStyle(ChatFormatting.DARK_GRAY), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int firsts(CommandSourceStack source) {
        Map<String, SpellRecord.First> firsts = SpellRecord.get(source.getServer()).firsts();
        if (firsts.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Nobody under the Spell has done anything first yet."), false);
        }
        firsts.values().forEach(first -> source.sendSuccess(() -> Component.literal(first.name() + " was the first to " + first.what() + "."), false));
        return Command.SINGLE_SUCCESS;
    }

    private static int log(CommandSourceStack source, int count) {
        SpellRecord record = SpellRecord.get(source.getServer());
        List<SpellEvent> events = record.recent(event -> true, count);
        source.sendSuccess(() -> Component.literal(record.size() + " events recorded; newest first:"), false);
        events.forEach(event -> source.sendSuccess(() -> Component.literal("  " + event.describe()), false));
        return Command.SINGLE_SUCCESS;
    }

    private static int call(CommandSourceStack source, String id, ServerPlayer player) {
        Calling calling = Calling.byId(id).orElse(null);
        if (calling == null) {
            source.sendFailure(Component.literal("No Calling called " + id + "."));
            return 0;
        }
        Map<String, String> facts = new java.util.HashMap<>(SpellCallings.playerFacts(player));
        facts.putIfAbsent("region", "The Ashen Expanse");
        facts.putIfAbsent("citadel", "Greywatch");
        facts.putIfAbsent("category", facts.getOrDefault("next_category", "Second"));
        boolean called = SpellCallings.call(player, calling, calling == Calling.IDLE ? "idle" : calling.id(), facts, true);
        source.sendSuccess(() -> Component.literal(called ? "Called " + id + "." : "The voice had no line for " + id + "."), false);
        return called ? Command.SINGLE_SUCCESS : 0;
    }

    private static int say(CommandSourceStack source, String topic, String key) {
        Map<String, String> facts = source.getPlayer() == null ? Map.of() : SpellCallings.playerFacts(source.getPlayer());
        String line = SpellVoices.say(VoiceRequest.of(topic, key, facts, source.getServer().overworld().getRandom().nextLong()),
                "(no line for " + topic + "/" + key + ")");
        source.sendSuccess(() -> Component.literal(line).withStyle(ChatFormatting.ITALIC, ChatFormatting.LIGHT_PURPLE), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int voices(CommandSourceStack source) {
        for (SpellVoice voice : SpellVoices.voices()) {
            source.sendSuccess(() -> Component.literal(voice.name() + (voice.trusted() ? " (written)" : "")), false);
        }
        source.sendSuccess(() -> Component.literal(SpellVoices.lines().count() + " written lines in "
                + SpellVoices.lines().topics().size() + " topics."), false);
        return Command.SINGLE_SUCCESS;
    }
}
