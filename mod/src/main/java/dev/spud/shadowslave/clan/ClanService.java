package dev.spud.shadowslave.clan;

import dev.spud.shadowslave.citadel.CitadelContentLoader;
import dev.spud.shadowslave.fieldguide.FieldGuideData;
import dev.spud.shadowslave.fieldguide.FieldGuideService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Clans (DESIGN, Jack 2026-10-29; after ch. 365, where great clans own the
 * main Citadels and lesser clans or lone Awakened hold the rest). A clan has
 * one leader, who invites and removes members. Anyone without a clan who
 * claims a Citadel becomes a clan of one. When the last member leaves, the
 * clan is gone; if they leave to join another clan, its Citadels and Field
 * Guide board go with them.
 */
public final class ClanService {
    private static final Pattern NAME = Pattern.compile("[\\p{L}\\p{N}][\\p{L}\\p{N} '\\-]*");
    private static final List<Listener> LISTENERS = new ArrayList<>();

    /** What else follows a clan: Citadels pass on or fall open with it. */
    public interface Listener {
        void dissolved(MinecraftServer server, UUID clan);

        void merged(MinecraftServer server, UUID from, UUID into);
    }

    /** The outcome of a clan action, with the line the player reads. */
    public record Result(boolean ok, Component message) {
        static Result ok(String message) {
            return new Result(true, Component.literal(message).withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        static Result fail(String message) {
            return new Result(false, Component.literal(message).withStyle(ChatFormatting.GRAY));
        }
    }

    private ClanService() {
    }

    public static void addListener(Listener listener) {
        LISTENERS.add(listener);
    }

    public static Optional<ClanData.Clan> clanOf(MinecraftServer server, UUID player) {
        return ClanData.get(server).of(player);
    }

    public static Optional<ClanData.Clan> byId(MinecraftServer server, UUID clan) {
        return ClanData.get(server).byId(clan);
    }

    // Founding and joining

    public static Result create(ServerPlayer player, String rawName) {
        ClanData data = ClanData.get(player.server);
        if (data.of(player.getUUID()).isPresent()) {
            return Result.fail("You already belong to a clan. Leave it first, or rename it with /clan rename.");
        }
        Optional<String> problem = nameProblem(data, rawName);
        if (problem.isPresent()) {
            return Result.fail(problem.get());
        }
        ClanData.Clan clan = found(data, rawName.trim(), player);
        return Result.ok("You founded the clan " + clan.name + ".");
    }

    /** The player's clan, founding a clan of one in their name if they have none. */
    public static ClanData.Clan ensureClan(ServerPlayer player) {
        ClanData data = ClanData.get(player.server);
        Optional<ClanData.Clan> existing = data.of(player.getUUID());
        if (existing.isPresent()) {
            return existing.get();
        }
        String base = player.getGameProfile().getName() + "'s clan";
        String name = base;
        for (int i = 2; data.named(name).isPresent(); i++) {
            name = base + " " + i;
        }
        ClanData.Clan clan = found(data, name, player);
        player.sendSystemMessage(Component.literal("You stand as a clan of one: " + clan.name
                + ". Rename it with /clan rename, or invite others with /clan invite.").withStyle(ChatFormatting.LIGHT_PURPLE));
        return clan;
    }

    private static ClanData.Clan found(ClanData data, String name, ServerPlayer player) {
        ClanData.Clan clan = new ClanData.Clan(UUID.randomUUID(), name, player.getUUID());
        clan.members.put(player.getUUID(), player.getGameProfile().getName());
        data.clans.put(clan.id, clan);
        data.setDirty();
        return clan;
    }

    public static Result rename(ServerPlayer player, String rawName) {
        ClanData data = ClanData.get(player.server);
        Optional<ClanData.Clan> clan = data.of(player.getUUID());
        if (clan.isEmpty()) {
            return Result.fail("You have no clan to rename.");
        }
        if (!clan.get().leader.equals(player.getUUID())) {
            return Result.fail("Only " + clan.get().memberName(clan.get().leader) + ", who leads the clan, can rename it.");
        }
        Optional<String> problem = nameProblem(data, rawName);
        if (problem.isPresent() && !rawName.trim().equalsIgnoreCase(clan.get().name)) {
            return Result.fail(problem.get());
        }
        String old = clan.get().name;
        clan.get().name = rawName.trim();
        data.setDirty();
        tell(player.server, clan.get(), "The clan " + old + " is now called " + clan.get().name + ".");
        return Result.ok("Renamed.");
    }

    public static Result invite(ServerPlayer leader, ServerPlayer target) {
        ClanData data = ClanData.get(leader.server);
        Optional<ClanData.Clan> clan = data.of(leader.getUUID());
        if (clan.isEmpty()) {
            return Result.fail("Found a clan first: /clan create <name>.");
        }
        if (!clan.get().leader.equals(leader.getUUID())) {
            return Result.fail("Only the clan's leader can invite.");
        }
        if (clan.get().has(target.getUUID())) {
            return Result.fail(target.getGameProfile().getName() + " is already in your clan.");
        }
        clan.get().invites.add(target.getUUID());
        data.setDirty();
        target.sendSystemMessage(Component.literal(leader.getGameProfile().getName() + " invites you to the clan " + clan.get().name
                + ". Accept with /clan join " + clan.get().name + ".").withStyle(ChatFormatting.LIGHT_PURPLE));
        return Result.ok("Invited " + target.getGameProfile().getName() + " to " + clan.get().name + ".");
    }

    public static Result join(ServerPlayer player, String name) {
        ClanData data = ClanData.get(player.server);
        Optional<ClanData.Clan> clan = data.named(name);
        if (clan.isEmpty()) {
            return Result.fail("No clan is called " + name.trim() + ".");
        }
        ClanData.Clan joining = clan.get();
        if (joining.has(player.getUUID())) {
            return Result.fail("You already belong to " + joining.name + ".");
        }
        if (!joining.invited(player.getUUID())) {
            return Result.fail(joining.name + " has not invited you.");
        }
        Optional<ClanData.Clan> old = data.of(player.getUUID());
        if (old.isPresent()) {
            ClanData.Clan leaving = old.get();
            removeMember(player.server, data, leaving, player.getUUID(), Optional.of(joining));
        }
        joining.invites.remove(player.getUUID());
        joining.members.put(player.getUUID(), player.getGameProfile().getName());
        data.setDirty();
        tell(player.server, joining, player.getGameProfile().getName() + " has joined " + joining.name + ".");
        return Result.ok("You are now of the clan " + joining.name + ".");
    }

    public static Result leave(ServerPlayer player) {
        ClanData data = ClanData.get(player.server);
        Optional<ClanData.Clan> clan = data.of(player.getUUID());
        if (clan.isEmpty()) {
            return Result.fail("You belong to no clan.");
        }
        boolean last = clan.get().members.size() == 1;
        removeMember(player.server, data, clan.get(), player.getUUID(), Optional.empty());
        return Result.ok(last ? "You left " + clan.get().name + ". With no one left, the clan is no more, and its Citadels lie open."
                : "You left " + clan.get().name + ".");
    }

    public static Result kick(ServerPlayer leader, UUID target, String targetName) {
        ClanData data = ClanData.get(leader.server);
        Optional<ClanData.Clan> clan = data.of(leader.getUUID());
        if (clan.isEmpty() || !clan.get().leader.equals(leader.getUUID())) {
            return Result.fail("Only a clan's leader can send members away.");
        }
        if (target.equals(leader.getUUID())) {
            return Result.fail("Use /clan leave to leave your own clan.");
        }
        if (!clan.get().has(target)) {
            clan.get().invites.remove(target);
            data.setDirty();
            return Result.fail(targetName + " is not in your clan.");
        }
        removeMember(leader.server, data, clan.get(), target, Optional.empty());
        Optional.ofNullable(leader.server.getPlayerList().getPlayer(target)).ifPresent(kicked ->
                kicked.sendSystemMessage(Component.literal("You are no longer of the clan " + clan.get().name + ".").withStyle(ChatFormatting.GRAY)));
        return Result.ok(targetName + " is no longer of " + clan.get().name + ".");
    }

    /**
     * Takes a member out of a clan. The leader's place passes to the
     * longest-standing member. If no one is left the clan ends: what it
     * held goes to {@code into} if the last member is joining another clan,
     * and otherwise falls open.
     */
    private static void removeMember(MinecraftServer server, ClanData data, ClanData.Clan clan, UUID member,
                                     Optional<ClanData.Clan> into) {
        String name = clan.memberName(member);
        clan.members.remove(member);
        data.setDirty();
        if (clan.members.isEmpty()) {
            data.clans.remove(clan.id);
            if (into.isPresent()) {
                into.get().board.learnFrom(clan.board, FieldGuideService.maxChunks(), FieldGuideService.maxPathCells(), false);
                LISTENERS.forEach(listener -> listener.merged(server, clan.id, into.get().id));
            } else {
                LISTENERS.forEach(listener -> listener.dissolved(server, clan.id));
            }
            return;
        }
        if (clan.leader.equals(member)) {
            clan.leader = clan.members.keySet().iterator().next();
            tell(server, clan, name + " left; " + clan.memberName(clan.leader) + " now leads " + clan.name + ".");
        } else {
            tell(server, clan, name + " left " + clan.name + ".");
        }
    }

    /** Keeps a member's name current, for clan lists read while they are away. */
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ClanData data = ClanData.get(player.server);
            data.of(player.getUUID()).ifPresent(clan -> {
                if (!player.getGameProfile().getName().equals(clan.memberName(player.getUUID()))) {
                    clan.members.put(player.getUUID(), player.getGameProfile().getName());
                    data.setDirty();
                }
            });
        }
    }

    // The Field Guide board

    /**
     * A member's visit to the board: the board takes what they know, then
     * they take what the board knows. Returns what the member learned.
     */
    public static FieldGuideData.Learned shareBoard(ServerPlayer player, ClanData.Clan clan) {
        int maxChunks = FieldGuideService.maxChunks();
        FieldGuideData mine = FieldGuideService.get(player);
        int maxPaths = FieldGuideService.maxPathCells();
        clan.board.learnFrom(mine, maxChunks, maxPaths, false);
        FieldGuideData.Learned learned = mine.learnFrom(clan.board, maxChunks, maxPaths, false);
        ClanData.get(player.server).setDirty();
        if (learned.any()) {
            FieldGuideService.sendWholeMap(player);
            FieldGuideService.sendWholePaths(player);
            FieldGuideService.sendStatus(player, false);
        }
        return learned;
    }

    // Helpers

    static Optional<String> nameProblem(ClanData data, String rawName) {
        String name = rawName.trim();
        int max = CitadelContentLoader.settings().clanNameMax();
        if (name.length() < 3 || name.length() > max) {
            return Optional.of("A clan's name is 3 to " + max + " characters long.");
        }
        if (!NAME.matcher(name).matches()) {
            return Optional.of("A clan's name uses letters, numbers, spaces, hyphens and apostrophes.");
        }
        if (data.named(name).isPresent()) {
            return Optional.of("There is already a clan called " + name + ".");
        }
        return Optional.empty();
    }

    /** Tells every member online. */
    public static void tell(MinecraftServer server, ClanData.Clan clan, String line) {
        Component message = Component.literal(line).withStyle(ChatFormatting.LIGHT_PURPLE);
        for (UUID member : clan.members.keySet()) {
            ServerPlayer online = server.getPlayerList().getPlayer(member);
            if (online != null) {
                online.sendSystemMessage(message);
            }
        }
    }
}
