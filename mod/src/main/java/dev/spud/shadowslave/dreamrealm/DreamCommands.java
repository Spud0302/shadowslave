package dev.spud.shadowslave.dreamrealm;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.spud.shadowslave.dreamrealm.seed.SeedData;
import dev.spud.shadowslave.dreamrealm.seed.SeedService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Optional;

/**
 * {@code /shadowslave dream}: anyone can read their Dream Realm status and,
 * when the server makes Seeds opt-in, choose them with {@code seeds on|off}.
 * Operators can send players across and back, and plant, grow or bloom Seeds.
 */
public final class DreamCommands {
    private DreamCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave")
                .then(Commands.literal("dream")
                        .then(Commands.literal("status").executes(context -> status(context.getSource())))
                        .then(Commands.literal("seeds")
                                .then(Commands.literal("on").executes(context -> optIn(context.getSource(), true)))
                                .then(Commands.literal("off").executes(context -> optIn(context.getSource(), false))))
                        .then(Commands.literal("enter")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> enter(context.getSource(), context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> enter(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("wake")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> wake(context.getSource(), context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> wake(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("seed")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("plant").executes(context -> plant(context.getSource())))
                                .then(Commands.literal("grow")
                                        .then(Commands.argument("days", DoubleArgumentType.doubleArg(0.0, 100.0))
                                                .executes(context -> grow(context.getSource(), DoubleArgumentType.getDouble(context, "days")))))
                                .then(Commands.literal("bloom").executes(context -> bloom(context.getSource()))))));
    }

    private static int status(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        DreamData data = DreamCrossing.data(player);
        source.sendSuccess(() -> Component.literal(!data.isDreaming() ? "You are awake."
                : data.isLost() ? "You are Lost: your body is dead. Rise to Ascended to remake it."
                : data.dreaming().map(dreaming -> DreamCrossing.waitLeft(player, dreaming)).filter(left -> left > 0)
                        .map(left -> "You are dreaming. You can wake in " + DreamCrossing.duration(left) + ".")
                        .orElse("You are dreaming. A lit Dream Candle or your clan's Gateway will wake you.")), false);
        source.sendSuccess(() -> Component.literal(data.anchor().map(anchor -> "Dream Anchor: " + anchor.toShortString())
                .orElse("No Dream Anchor yet. Sleep beside a lit Dream Candle once you have Awakened.")), false);
        source.sendSuccess(() -> Component.literal("Ratio 1:" + DreamRealmRules.ratio(source.getServer())
                + ". Natural Seeds: " + (!DreamRealmRules.seeds(source.getServer()) ? "off"
                : DreamRealmRules.seedsOptIn(source.getServer()) ? "opt-in, you are " + (data.seedsOptIn() ? "in" : "out")
                : "on") + "."), false);
        if (DreamRealm.is(player.level())) {
            DreamRealmContent.get().regionAt(player.blockPosition()).ifPresent(region ->
                    source.sendSuccess(() -> Component.literal("Region: " + region.getValue().displayName()), false));
            SeedService.nearest(player.serverLevel(), player.blockPosition()).ifPresent(seed ->
                    source.sendSuccess(() -> Component.literal("Nearest Seed: " + seed.pos().toShortString()), false));
        }
        return 1;
    }

    private static int optIn(CommandSourceStack source, boolean on) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        DreamCrossing.setData(player, DreamCrossing.data(player).withSeedsOptIn(on));
        source.sendSuccess(() -> Component.literal(on ? "Nightmare Seeds may now grow near you in the Dream Realm."
                : "Nightmare Seeds will no longer grow near you."), false);
        return 1;
    }

    private static int enter(CommandSourceStack source, ServerPlayer player) {
        if (DreamRealm.is(player.level())) {
            source.sendFailure(Component.literal(player.getGameProfile().getName() + " is already in the Dream Realm."));
            return 0;
        }
        if (!DreamCrossing.cross(player, GlobalPos.of(player.level().dimension(), player.blockPosition()))) {
            source.sendFailure(Component.literal("Could not cross into the Dream Realm."));
            return 0;
        }
        return 1;
    }

    private static int wake(CommandSourceStack source, ServerPlayer player) {
        if (!DreamCrossing.wake(player, DreamCrossing.Reason.COMMAND)) {
            source.sendFailure(Component.literal(player.getGameProfile().getName() + " is not dreaming."));
            return 0;
        }
        return 1;
    }

    private static int plant(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!DreamRealm.is(player.level())) {
            source.sendFailure(Component.literal("Seeds only grow in the Dream Realm."));
            return 0;
        }
        BlockPos at = player.blockPosition().relative(player.getDirection(), 3);
        for (int dy = 2; dy >= -3; dy--) {
            Optional<SeedData.Seed> seed = SeedService.plant(player.serverLevel(), at.above(dy), player);
            if (seed.isPresent()) {
                source.sendSuccess(() -> Component.literal("Planted a Nightmare Seed at " + seed.get().pos().toShortString() + "."), true);
                return 1;
            }
        }
        source.sendFailure(Component.literal("No room for a Seed there, or another grows too close."));
        return 0;
    }

    private static int grow(CommandSourceStack source, double days) throws CommandSyntaxException {
        return nearestSeed(source).map(seed -> {
            SeedService.addGrowth(source.getLevel(), seed, (long) (days * 24000));
            source.sendSuccess(() -> Component.literal("The Seed at " + seed.pos().toShortString() + " grew " + days + " days."), true);
            return 1;
        }).orElse(0);
    }

    private static int bloom(CommandSourceStack source) throws CommandSyntaxException {
        return nearestSeed(source).map(seed -> {
            if (!SeedService.bloomNow(source.getLevel(), seed)) {
                source.sendFailure(Component.literal("The Seed could not bloom yet (it is being cleansed, or a Gate is too close)."));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("The Seed at " + seed.pos().toShortString() + " bloomed."), true);
            return 1;
        }).orElse(0);
    }

    private static Optional<SeedData.Seed> nearestSeed(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        if (!DreamRealm.is(level)) {
            source.sendFailure(Component.literal("Seeds only grow in the Dream Realm."));
            return Optional.empty();
        }
        Optional<SeedData.Seed> seed = SeedService.nearest(level, BlockPos.containing(source.getPosition()));
        if (seed.isEmpty()) {
            source.sendFailure(Component.literal("No Nightmare Seed grows in the Dream Realm."));
        }
        return seed;
    }
}
