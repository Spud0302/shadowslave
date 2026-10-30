package dev.spud.shadowslave.echoes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.Optional;

/**
 * An Echo's will while it has nothing to fight: keep to its owner (Follow,
 * Guard) or to its spot (Stay). It sits ahead of the creature's own goals so
 * it never wanders off, and gives way the moment the Echo has a target, so
 * the creature's own attacks take over: an Echo keeps its creature's moves.
 */
final class EchoMindGoal extends Goal {
    private static final double SPEED = 1.2;
    private final Mob echo;
    private int repath;

    EchoMindGoal(Mob echo) {
        this.echo = echo;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return EchoService.bond(echo).isPresent() && (echo.getTarget() == null || !echo.getTarget().isAlive());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        repath = 0;
    }

    @Override
    public void tick() {
        Optional<ServerPlayer> owner = EchoService.owner(echo);
        if (owner.isEmpty() || owner.get().level() != echo.level()) {
            echo.getNavigation().stop();
            return;
        }
        ServerPlayer player = owner.get();
        EchoSettings settings = EchoContent.current().settings();
        Optional<BlockPos> post = EchoService.post(echo);
        if (post.isPresent()) {
            BlockPos at = post.get();
            if (echo.blockPosition().distSqr(at) > 4 && --repath <= 0) {
                repath = 10;
                echo.getNavigation().moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, SPEED);
            }
            return;
        }
        double distance = echo.distanceToSqr(player);
        if (distance > settings.teleportDistance() * settings.teleportDistance()) {
            EchoService.bringTo(echo, player);
        } else if (distance > settings.followDistance() * settings.followDistance()) {
            if (--repath <= 0) {
                repath = 10;
                echo.getNavigation().moveTo(player, SPEED);
            }
        } else {
            echo.getNavigation().stop();
            echo.getLookControl().setLookAt(player, 10.0F, echo.getMaxHeadXRot());
        }
    }

    @Override
    public void stop() {
        echo.getNavigation().stop();
    }
}
