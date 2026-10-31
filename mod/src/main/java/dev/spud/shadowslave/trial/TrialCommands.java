package dev.spud.shadowslave.trial;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.seed.SeedService;
import dev.spud.shadowslave.trial.content.EchoSite;
import dev.spud.shadowslave.trial.content.TrialContent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@code /shadowslave trial}: anyone can read their Nightmare record with
 * {@code status}. Operators can list and visit echo sites, grow a Seed at one,
 * start a Nightmare directly, bloom a player's First Nightmare, skip its clock, force an ending, end it, and
 * forget a player's record.
 */
public final class TrialCommands {
    private static final SuggestionProvider<CommandSourceStack> SITES = (context, builder) ->
            SharedSuggestionProvider.suggestResource(TrialContent.get().sites().keySet(), builder);
    private static final SuggestionProvider<CommandSourceStack> NIGHTMARES = (context, builder) ->
            SharedSuggestionProvider.suggestResource(TrialContent.get().nightmares().keySet(), builder);

    private TrialCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave")
                .then(Commands.literal("trial")
                        .then(Commands.literal("status")
                                .executes(context -> status(context.getSource(), context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> status(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("sites")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> sites(context.getSource())))
                        .then(Commands.literal("site")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("site", ResourceLocationArgument.id()).suggests(SITES)
                                        .then(Commands.literal("visit").executes(context -> visit(context.getSource(), site(context))))
                                        .then(Commands.literal("grow").executes(context -> grow(context.getSource(), site(context))))))
                        .then(Commands.literal("start")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("nightmare", ResourceLocationArgument.id()).suggests(NIGHTMARES)
                                        .executes(context -> start(context.getSource(), nightmare(context), 2,
                                                List.of(context.getSource().getPlayerOrException())))
                                        .then(Commands.argument("category", IntegerArgumentType.integer(1, 5))
                                                .executes(context -> start(context.getSource(), nightmare(context),
                                                        IntegerArgumentType.getInteger(context, "category"),
                                                        List.of(context.getSource().getPlayerOrException())))
                                                .then(Commands.argument("players", EntityArgument.players())
                                                        .executes(context -> start(context.getSource(), nightmare(context),
                                                                IntegerArgumentType.getInteger(context, "category"),
                                                                EntityArgument.getPlayers(context, "players")))))))
                        .then(Commands.literal("first")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> first(context.getSource(), context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> first(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("skip")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("seconds", IntegerArgumentType.integer(1, 3600))
                                        .executes(context -> skip(context.getSource(), IntegerArgumentType.getInteger(context, "seconds")))))
                        .then(Commands.literal("end")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("ending", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(endings(context.getSource()), builder))
                                        .executes(context -> end(context.getSource(), StringArgumentType.getString(context, "ending")))))
                        .then(Commands.literal("abort")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> abort(context.getSource())))
                        .then(Commands.literal("forget")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> forget(context.getSource(), EntityArgument.getPlayer(context, "player")))))));
    }

    /** Blooms a player's First Nightmare now, whatever their rank or the rule says. */
    private static int first(CommandSourceStack source, ServerPlayer player) {
        Optional<TrialInstance> run = FirstNightmare.bloom(player);
        if (run.isEmpty()) {
            source.sendFailure(Component.literal("No First Nightmare could start for " + player.getScoreboardName() + "."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(player.getScoreboardName() + "'s First Nightmare bloomed: "
                + run.get().pack().displayName() + "."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static ResourceLocation site(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        return ResourceLocationArgument.getId(context, "site");
    }

    private static ResourceLocation nightmare(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        return ResourceLocationArgument.getId(context, "nightmare");
    }

    private static int status(CommandSourceStack source, ServerPlayer player) {
        TrialData.Record record = TrialData.get(source.getServer()).record(player.getUUID());
        Optional<TrialInstance> instance = TrialService.instanceOf(player);
        source.sendSuccess(() -> Component.literal(instance
                .map(found -> "In the Nightmare " + found.pack().displayName() + ", "
                        + (found.isLong() ? "day " + found.day() : found.ticks() / 20 + " s in") + ", as ["
                        + found.member(player.getUUID()).map(member -> member.role().name()).orElse("?") + "].")
                .orElse("Not in a Nightmare.")), false);
        if (instance.isEmpty()) {
            LongNightmareData.get(source.getServer()).runOf(player.getUUID()).ifPresent(run -> source.sendSuccess(() -> Component.literal(
                    "Asleep inside the Long Nightmare " + run.pack().displayName() + ", day " + run.day() + "."), false));
        }
        int scars = NightmareScar.stacks(player);
        if (scars > 0) {
            source.sendSuccess(() -> Component.literal("Nightmare Scars: " + scars), false);
        }
        for (int category = 1; category <= 5; category++) {
            int clears = record.clears(category);
            if (clears > 0) {
                int shown = category;
                source.sendSuccess(() -> Component.literal("Category " + shown + " Nightmares conquered: " + clears), false);
            }
        }
        record.bestGrades().forEach((id, grade) -> source.sendSuccess(() -> Component.literal("  "
                + TrialContent.get().nightmare(id).map(pack -> pack.displayName()).orElse(id.toString()) + ": best grade " + grade), false));
        if (record.clears.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No Nightmares conquered yet."), false);
        }
        if (!record.judgedBy.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Seeds that have judged you: " + record.judgedBy.size()), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int sites(CommandSourceStack source) {
        if (TrialContent.get().sites().isEmpty()) {
            source.sendSuccess(() -> Component.literal("No echo sites are loaded."), false);
        }
        TrialContent.get().sites().forEach((id, site) -> source.sendSuccess(() -> Component.literal(id + ": " + site.displayName()
                + " at " + site.x() + ", " + site.z() + ", weight " + site.weight() + " (Category "
                + TrialContent.get().category(site) + "), Nightmare " + site.nightmare()), false));
        return Command.SINGLE_SUCCESS;
    }

    private static Optional<EchoSite> lookUp(CommandSourceStack source, ResourceLocation id) {
        Optional<EchoSite> site = TrialContent.get().site(id);
        if (site.isEmpty()) {
            source.sendFailure(Component.literal("No echo site called " + id + "."));
        }
        return site;
    }

    private static Optional<ServerLevel> realm(CommandSourceStack source) {
        Optional<ServerLevel> realm = SeedService.realm(source.getServer());
        if (realm.isEmpty()) {
            source.sendFailure(Component.literal("The Dream Realm is missing."));
        }
        return realm;
    }

    private static int visit(CommandSourceStack source, ResourceLocation id) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<EchoSite> site = lookUp(source, id);
        Optional<ServerLevel> realm = realm(source);
        if (site.isEmpty() || realm.isEmpty()) {
            return 0;
        }
        EchoSites.raise(realm.get(), id, site.get());
        int x = site.get().x() + site.get().seedOffsetX();
        int z = site.get().z() + site.get().seedOffsetZ() + 6;
        realm.get().getChunk(x >> 4, z >> 4);
        int y = realm.get().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (!DreamCrossing.data(player).isDreaming()) {
            source.sendSuccess(() -> Component.literal("You are not dreaming: you will not wake in a bed from here."), false);
        }
        player.teleportTo(realm.get(), x + 0.5, y, z + 0.5, Set.of(), 180.0F, 10.0F);
        return Command.SINGLE_SUCCESS;
    }

    private static int grow(CommandSourceStack source, ResourceLocation id) {
        Optional<EchoSite> site = lookUp(source, id);
        Optional<ServerLevel> realm = realm(source);
        if (site.isEmpty() || realm.isEmpty()) {
            return 0;
        }
        EchoSites.raise(realm.get(), id, site.get());
        EchoSites.wake(realm.get(), id);
        var seed = EchoSites.seedAt(realm.get(), id).or(() -> EchoSites.grow(realm.get(), id, site.get()));
        if (seed.isEmpty()) {
            source.sendFailure(Component.literal("No Seed could take root at " + site.get().displayName() + "."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("A Seed grows at " + site.get().displayName() + ", "
                + seed.get().pos().toShortString() + "."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int start(CommandSourceStack source, ResourceLocation nightmare, int category, Collection<ServerPlayer> players) {
        List<ServerPlayer> cohort = players.stream().filter(player -> !TrialService.inside(player)).toList();
        Optional<TrialInstance> instance = TrialService.begin(cohort, nightmare, Optional.empty(), Optional.empty(), category);
        if (instance.isEmpty()) {
            source.sendFailure(Component.literal("Could not start " + nightmare + "; see the log."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Started " + instance.get().pack().displayName() + " with "
                + cohort.size() + " challenger(s)."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static Optional<TrialInstance> current(CommandSourceStack source) {
        Optional<TrialInstance> instance = source.getPlayer() == null ? Optional.empty() : TrialService.instanceOf(source.getPlayer());
        if (instance.isEmpty()) {
            source.sendFailure(Component.literal("You are not in a Nightmare."));
        }
        return instance;
    }

    private static List<String> endings(CommandSourceStack source) {
        return source.getPlayer() == null ? List.of() : TrialService.instanceOf(source.getPlayer())
                .map(instance -> instance.pack().endings().stream().map(ending -> ending.id()).toList())
                .orElse(List.of());
    }

    private static int skip(CommandSourceStack source, int seconds) {
        Optional<TrialInstance> instance = current(source);
        ServerLevel level = TrialWorld.level(source.getServer());
        if (instance.isEmpty() || level == null) {
            return 0;
        }
        TrialService.skip(level, instance.get(), seconds);
        source.sendSuccess(() -> Component.literal("Skipped " + seconds + " s."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int end(CommandSourceStack source, String ending) {
        Optional<TrialInstance> instance = current(source);
        ServerLevel level = TrialWorld.level(source.getServer());
        if (instance.isEmpty() || level == null) {
            return 0;
        }
        if (!TrialService.forceEnding(level, instance.get(), ending)) {
            source.sendFailure(Component.literal("This Nightmare has no ending called " + ending + "."));
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int abort(CommandSourceStack source) {
        Optional<TrialInstance> instance = current(source);
        if (instance.isEmpty()) {
            return 0;
        }
        TrialService.abort(source.getServer(), instance.get());
        return Command.SINGLE_SUCCESS;
    }

    private static int forget(CommandSourceStack source, ServerPlayer player) {
        TrialData.get(source.getServer()).forget(player.getUUID());
        source.sendSuccess(() -> Component.literal("Forgot " + player.getScoreboardName() + "'s Nightmares."), true);
        return Command.SINGLE_SUCCESS;
    }
}
