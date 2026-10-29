package dev.spud.shadowslave.clan;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.authlib.GameProfile;
import dev.spud.shadowslave.citadel.CitadelService;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code /clan}: found, name and lead a clan, invite and remove members,
 * join a clan that invited you, or leave. With no arguments it shows your
 * clan, its members and the Citadels it holds.
 */
public final class ClanCommands {
    private ClanCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("clan")
                .executes(context -> info(context.getSource()))
                .then(Commands.literal("info").executes(context -> info(context.getSource())))
                .then(Commands.literal("list").executes(context -> list(context.getSource())))
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(context -> report(context.getSource(), ClanService.create(
                                        context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "name"))))))
                .then(Commands.literal("rename")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(context -> report(context.getSource(), ClanService.rename(
                                        context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "name"))))))
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> report(context.getSource(), ClanService.invite(
                                        context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "player"))))))
                .then(Commands.literal("join")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        ClanData.get(context.getSource().getServer()).clans().stream().map(ClanData.Clan::name), builder))
                                .executes(context -> report(context.getSource(), ClanService.join(
                                        context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "name"))))))
                .then(Commands.literal("leave")
                        .executes(context -> report(context.getSource(), ClanService.leave(context.getSource().getPlayerOrException()))))
                .then(Commands.literal("kick")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(context -> kick(context.getSource(), GameProfileArgument.getGameProfiles(context, "player"))))));
    }

    private static int report(CommandSourceStack source, ClanService.Result result) {
        if (result.ok()) {
            source.sendSuccess(result::message, false);
            return 1;
        }
        source.sendFailure(result.message());
        return 0;
    }

    private static int kick(CommandSourceStack source, Collection<GameProfile> profiles) throws CommandSyntaxException {
        ServerPlayer leader = source.getPlayerOrException();
        int done = 0;
        for (GameProfile profile : profiles) {
            done += report(source, ClanService.kick(leader, profile.getId(), profile.getName()));
        }
        return done;
    }

    private static int info(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<ClanData.Clan> found = ClanService.clanOf(source.getServer(), player.getUUID());
        if (found.isEmpty()) {
            source.sendSuccess(() -> Component.literal("You belong to no clan. Found one with /clan create <name>, "
                    + "or claim a Citadel and stand as a clan of one.").withStyle(ChatFormatting.GRAY), false);
            return 1;
        }
        ClanData.Clan clan = found.get();
        source.sendSuccess(() -> Component.literal("Clan " + clan.name()).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
        StringBuilder members = new StringBuilder();
        for (UUID member : clan.members()) {
            if (!members.isEmpty()) {
                members.append(", ");
            }
            members.append(clan.memberName(member));
            if (member.equals(clan.leader())) {
                members.append(" (leader)");
            }
        }
        source.sendSuccess(() -> Component.literal("Members: " + members), false);
        var citadels = CitadelService.heldBy(source.getServer(), clan.id());
        source.sendSuccess(() -> Component.literal(citadels.isEmpty() ? "Citadels: none yet."
                : "Citadels: " + String.join(", ", citadels)), false);
        return 1;
    }

    private static int list(CommandSourceStack source) {
        var clans = ClanData.get(source.getServer()).clans();
        if (clans.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No clans yet.").withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
        for (ClanData.Clan clan : clans) {
            int held = CitadelService.heldBy(source.getServer(), clan.id()).size();
            source.sendSuccess(() -> Component.literal(clan.name() + " · " + clan.members().size()
                    + (clan.members().size() == 1 ? " member" : " members") + " · " + held + (held == 1 ? " Citadel" : " Citadels")), false);
        }
        return clans.size();
    }
}
