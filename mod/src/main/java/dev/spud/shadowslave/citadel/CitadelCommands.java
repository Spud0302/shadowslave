package dev.spud.shadowslave.citadel;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
 * a Guardian to open its Gateway. To find generated Citadels, use
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
                                .executes(context -> build(context.getSource())))
                        .then(Commands.literal("list")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> list(context.getSource())))
                        .then(Commands.literal("conquer")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> conquer(context.getSource())))));
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

    private static int build(CommandSourceStack source) throws CommandSyntaxException {
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
        CitadelData.Citadel citadel = CitadelService.wake(level, CitadelLayout.gateway(centre));
        source.sendSuccess(() -> Component.literal("Raised " + citadel.name() + " at " + citadel.gateway().toShortString()
                + ", held by a " + citadel.rank().displayName() + " Guardian."), true);
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

    private static void describe(CommandSourceStack source, CitadelData.Citadel citadel) {
        String holder = switch (citadel.hold()) {
            case GUARDIAN -> "held by its " + citadel.rank().displayName() + " Guardian";
            case CONQUERED -> "conquered, its Gateway open";
            case CLAN -> "held by " + citadel.clan().flatMap(clan -> ClanService.byId(source.getServer(), clan))
                    .map(clan -> clan.name()).orElse("a clan");
        };
        source.sendSuccess(() -> Component.literal(citadel.name() + " at " + citadel.gateway().getX() + ", " + citadel.gateway().getY()
                + ", " + citadel.gateway().getZ() + ": " + holder).withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }
}
