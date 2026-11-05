package dev.spud.shadowslave.citadel;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.spud.shadowslave.gate.GateForecast;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Optional;

/**
 * {@code /shadowslave citadel}: anyone can read the nearest Citadel.
 * Operators can raise one in front of them, list them all, or strike down
 * a Guardian to open its Gateway; at a claimed Citadel they can set its
 * provisions ({@code feed}, {@code starve}), set an upgrade's tier, and start
 * or lose a raid. For clan wars they can skip the war clock ahead, tick the
 * wars once, cut a Citadel's supply line, or end a clan's wars. To find generated Citadels, use
 * {@code /locate structure shadowslave:citadel} in the Dream Realm.
 */
public final class CitadelCommands {
    private CitadelCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave")
                .then(Commands.literal("citadel")
                        .then(Commands.literal("status").executes(context -> status(context.getSource())))
                        .then(Commands.literal("build")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> build(context.getSource(), NpcCitadels.Kind.WILD))
                                .then(Commands.literal("wild").executes(context -> build(context.getSource(), NpcCitadels.Kind.WILD)))
                                .then(Commands.literal("held").executes(context -> build(context.getSource(), NpcCitadels.Kind.HELD)))
                                .then(Commands.literal("fallen").executes(context -> build(context.getSource(), NpcCitadels.Kind.FALLEN))))
                        .then(Commands.literal("list")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> list(context.getSource())))
                        .then(Commands.literal("conquer")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> conquer(context.getSource())))
                        .then(Commands.literal("feed")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("days", DoubleArgumentType.doubleArg(0, 1000))
                                        .executes(context -> feed(context.getSource(), DoubleArgumentType.getDouble(context, "days")))))
                        .then(Commands.literal("starve")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("days", DoubleArgumentType.doubleArg(0, 1000))
                                        .executes(context -> starve(context.getSource(), DoubleArgumentType.getDouble(context, "days")))))
                        .then(Commands.literal("upgrade")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("upgrade", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                CitadelContentLoader.content().upgrades().keySet(), builder))
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(0, 8))
                                                .executes(context -> setTier(context.getSource(), StringArgumentType.getString(context, "upgrade"),
                                                        IntegerArgumentType.getInteger(context, "tier"))))))
                        .then(Commands.literal("raid")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("category", IntegerArgumentType.integer(1, 5))
                                        .executes(context -> raid(context.getSource(), IntegerArgumentType.getInteger(context, "category"))))
                                .then(Commands.literal("lose").executes(context -> loseRaid(context.getSource()))))
                        .then(Commands.literal("war")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("skip")
                                        .then(Commands.argument("hours", DoubleArgumentType.doubleArg(0, 24 * 365))
                                                .executes(context -> skip(context.getSource(), DoubleArgumentType.getDouble(context, "hours")))))
                                .then(Commands.literal("tick").executes(context -> warTick(context.getSource())))
                                .then(Commands.literal("cut").executes(context -> cut(context.getSource())))
                                .then(Commands.literal("end")
                                        .then(Commands.argument("clan", StringArgumentType.greedyString())
                                                .executes(context -> endWars(context.getSource(), StringArgumentType.getString(context, "clan"))))))));
    }

    private static int status(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!DreamRealm.is(player.level())) {
            source.sendFailure(Component.literal("Citadels stand in the Dream Realm."));
            return 0;
        }
        Optional<CitadelData.Citadel> citadel = CitadelService.near(player.serverLevel(), player.blockPosition(), 256);
        if (citadel.isEmpty()) {
            source.sendFailure(Component.literal("No Citadel you know of stands within 256 blocks."));
            return 0;
        }
        describe(source, citadel.get());
        return 1;
    }

    private static int build(CommandSourceStack source, NpcCitadels.Kind kind) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        if (!DreamRealm.is(level)) {
            source.sendFailure(Component.literal("Citadels stand in the Dream Realm."));
            return 0;
        }
        BlockPos ahead = player.blockPosition().relative(player.getDirection(), 18);
        level.getChunk(ahead);
        BlockPos centre = new BlockPos(ahead.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ahead.getX(), ahead.getZ()), ahead.getZ());
        CitadelService.build(level, centre, 12, CitadelPalette.DEFAULT);
        CitadelData.Citadel citadel = CitadelService.wake(level, CitadelLayout.gateway(centre), kind);
        source.sendSuccess(() -> Component.literal("Raised " + citadel.name() + " at " + citadel.gateway().toShortString() + ", "
                + switch (kind) {
                    case WILD -> "held by a " + citadel.rank().displayName() + " Guardian.";
                    case HELD -> "held by " + NpcCitadels.clanOf(source.getServer(), citadel).map(NpcClanData.NpcClan::name).orElse("an NPC clan") + ".";
                    case FALLEN -> "fallen to ruin.";
                }), true);
        return 1;
    }

    private static int list(CommandSourceStack source) {
        ServerLevel realm = DreamRealm.level(source.getServer());
        if (realm == null) {
            source.sendFailure(Component.literal("The Dream Realm is missing."));
            return 0;
        }
        var citadels = CitadelData.get(realm).citadels();
        if (citadels.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No Citadel has woken yet. Find one with /locate structure shadowslave:citadel."), false);
            return 0;
        }
        citadels.forEach(citadel -> describe(source, citadel));
        return citadels.size();
    }

    private static int conquer(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<CitadelData.Citadel> citadel = CitadelService.near(player.serverLevel(), player.blockPosition(), 64)
                .filter(found -> found.hold() == CitadelData.Hold.GUARDIAN);
        if (citadel.isEmpty() || !DreamRealm.is(player.level())) {
            source.sendFailure(Component.literal("No Guardian holds a Citadel within 64 blocks."));
            return 0;
        }
        Entity guardian = citadel.get().guardian().map(player.serverLevel()::getEntity).orElse(null);
        if (guardian != null) {
            guardian.kill();
        }
        if (citadel.get().hold() == CitadelData.Hold.GUARDIAN) {
            CitadelService.conquer(player.serverLevel(), citadel.get(), Optional.of(player));
        }
        return 1;
    }

    /** The clan Citadel nearest the operator, within 64 blocks. */
    private static Optional<CitadelData.Citadel> claimedNear(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<CitadelData.Citadel> citadel = DreamRealm.is(player.level())
                ? CitadelService.near(player.serverLevel(), player.blockPosition(), 64).filter(found -> found.hold() == CitadelData.Hold.CLAN)
                : Optional.empty();
        if (citadel.isEmpty()) {
            source.sendFailure(Component.literal("No claimed Citadel stands within 64 blocks."));
        }
        return citadel;
    }

    private static int feed(CommandSourceStack source, double days) throws CommandSyntaxException {
        Optional<CitadelData.Citadel> citadel = claimedNear(source);
        if (citadel.isEmpty()) {
            return 0;
        }
        CitadelData.Citadel found = citadel.get();
        found.provisions = 0;
        CitadelLife.feed(source.getServer(), found, CitadelLife.perDay(source.getServer(), found) * days);
        CitadelData.get(source.getPlayerOrException().serverLevel()).setDirty();
        describe(source, found);
        return 1;
    }

    private static int starve(CommandSourceStack source, double days) throws CommandSyntaxException {
        Optional<CitadelData.Citadel> citadel = claimedNear(source);
        if (citadel.isEmpty()) {
            return 0;
        }
        CitadelData.Citadel found = citadel.get();
        found.provisions = 0;
        found.hungryTicks = 0;
        CitadelLife.eat(source.getServer(), found, Math.round(days * CitadelUpkeep.DAY_TICKS));
        CitadelData.get(source.getPlayerOrException().serverLevel()).setDirty();
        describe(source, found);
        return 1;
    }

    private static int setTier(CommandSourceStack source, String id, int tier) throws CommandSyntaxException {
        Optional<CitadelData.Citadel> citadel = claimedNear(source);
        Optional<CitadelUpgrade> upgrade = CitadelContentLoader.content().upgrade(id);
        if (citadel.isEmpty()) {
            return 0;
        }
        if (upgrade.isEmpty()) {
            source.sendFailure(Component.literal("No upgrade is called " + id + "."));
            return 0;
        }
        CitadelData.Citadel found = citadel.get();
        found.tiers.put(id, Math.min(tier, upgrade.get().maxTier()));
        found.damaged.remove(id);
        ServerLevel level = source.getPlayerOrException().serverLevel();
        CitadelData.get(level).setDirty();
        CitadelLife.built(level, found, id);
        describe(source, found);
        return 1;
    }

    private static int raid(CommandSourceStack source, int category) throws CommandSyntaxException {
        Optional<CitadelData.Citadel> citadel = claimedNear(source);
        if (citadel.isEmpty()) {
            return 0;
        }
        if (citadel.get().raid().isPresent()) {
            source.sendFailure(Component.literal(citadel.get().name() + " is already under raid."));
            return 0;
        }
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BlockPos ahead = citadel.get().gateway().relative(player.getDirection(), 32);
        BlockPos at = new BlockPos(ahead.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ahead.getX(), ahead.getZ()) - 1, ahead.getZ());
        if (!CitadelRaids.begin(level, citadel.get(), at, GateForecast.certain(category))) {
            source.sendFailure(Component.literal("Another Gate is open too close by."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("A Category " + category + " raid begins at " + at.toShortString() + "."), true);
        return 1;
    }

    private static int loseRaid(CommandSourceStack source) throws CommandSyntaxException {
        Optional<CitadelData.Citadel> citadel = claimedNear(source);
        if (citadel.isEmpty() || citadel.get().raid().isEmpty()) {
            source.sendFailure(Component.literal("No raid is under way here."));
            return 0;
        }
        CitadelRaids.lose(source.getPlayerOrException().serverLevel(), citadel.get(), "An operator called it.");
        return 1;
    }

    private static int skip(CommandSourceStack source, double hours) {
        CitadelWars.skip(source.getServer(), hours);
        CitadelWars.tick(source.getServer(), false);
        source.sendSuccess(() -> Component.literal("The war clock moves " + hours + " hours ahead."), true);
        return 1;
    }

    private static int warTick(CommandSourceStack source) {
        CitadelWars.tick(source.getServer(), true);
        source.sendSuccess(() -> Component.literal("The wars tick once, and missing Link Anchors rise."), false);
        return 1;
    }

    private static int cut(CommandSourceStack source) throws CommandSyntaxException {
        Optional<CitadelData.Citadel> citadel = claimedNear(source);
        if (citadel.isEmpty()) {
            return 0;
        }
        if (citadel.get().linkAnchor().isEmpty()) {
            source.sendFailure(Component.literal(citadel.get().name() + " has no Link Anchor. Build its Gateway Links first."));
            return 0;
        }
        CitadelWars.cut(source.getPlayerOrException().serverLevel(), citadel.get());
        source.sendSuccess(() -> Component.literal("The supply line of " + citadel.get().name() + " is cut."), true);
        return 1;
    }

    private static int endWars(CommandSourceStack source, String name) {
        Optional<dev.spud.shadowslave.clan.ClanData.Clan> clan = dev.spud.shadowslave.clan.ClanData.get(source.getServer()).named(name);
        if (clan.isEmpty()) {
            source.sendFailure(Component.literal("No clan is called " + name.trim() + "."));
            return 0;
        }
        int ended = CitadelWars.end(source.getServer(), clan.get().id());
        source.sendSuccess(() -> Component.literal("Ended " + ended + (ended == 1 ? " war." : " wars.")), true);
        return ended;
    }

    private static void describe(CommandSourceStack source, CitadelData.Citadel citadel) {
        String holder = switch (citadel.hold()) {
            case GUARDIAN -> "held by its " + citadel.rank().displayName() + " Guardian";
            case CONQUERED -> "conquered, its Gateway open";
            case NPC -> "held by " + NpcCitadels.clanOf(source.getServer(), citadel).map(clan -> clan.name() + " (" + citadel.guards()
                    + " guards, " + citadel.residents() + " residents)").orElse("an NPC clan");
            case CLAN -> "held by " + citadel.clan().flatMap(clan -> ClanService.byId(source.getServer(), clan))
                    .map(clan -> clan.name()).orElse("a clan");
        };
        source.sendSuccess(() -> Component.literal(citadel.name() + " at " + citadel.gateway().getX() + ", " + citadel.gateway().getY()
                + ", " + citadel.gateway().getZ() + ": " + holder).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        if (citadel.hold() != CitadelData.Hold.CLAN) {
            return;
        }
        double perDay = CitadelLife.perDay(source.getServer(), citadel);
        source.sendSuccess(() -> Component.literal("  " + CitadelLife.stage(citadel).displayName() + ": " + Math.round(citadel.provisions())
                + " of " + Math.round(CitadelLife.capacity(source.getServer(), citadel)) + " provisions, eating " + Math.round(perDay)
                + " a day").withStyle(ChatFormatting.GRAY), false);
        java.util.List<String> built = new java.util.ArrayList<>();
        CitadelContentLoader.content().upgrades().forEach((id, upgrade) -> {
            if (citadel.tier(id) > 0) {
                built.add(upgrade.name() + " " + CitadelUpgrade.numeral(citadel.tier(id)) + (citadel.damaged(id) ? " (damaged)" : ""));
            }
        });
        source.sendSuccess(() -> Component.literal("  Built: " + (built.isEmpty() ? "nothing yet" : String.join(", ", built))
                + (citadel.raid().isPresent() ? ". Under raid." : ".")).withStyle(ChatFormatting.GRAY), false);
    }
}
