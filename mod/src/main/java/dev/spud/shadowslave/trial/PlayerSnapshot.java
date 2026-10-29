package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.network.SoulSyncService;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.soul.refinement.RefinementService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * The whole of a player as they were when they entered a Nightmare: their
 * inventory and Ender chest, experience, health and hunger, effects, and every
 * piece of soul data (core, essence, Aspect, Memories). A death inside puts all
 * of it back, so everything gained in the Nightmare is lost (Jack, 2026-10-29).
 *
 * <p>The snapshot is the player's own save data, so it survives a crash or a
 * restart in {@link TrialData}. Restoring it keeps where the player stands; the
 * caller moves them.</p>
 */
public final class PlayerSnapshot {
    private PlayerSnapshot() {
    }

    public static CompoundTag capture(ServerPlayer player) {
        return player.saveWithoutId(new CompoundTag());
    }

    public static void restore(ServerPlayer player, CompoundTag snapshot) {
        CompoundTag tag = snapshot.copy();
        // Stay where we are: the caller teleports the player home afterwards.
        ListTag pos = new ListTag();
        pos.add(DoubleTag.valueOf(player.getX()));
        pos.add(DoubleTag.valueOf(player.getY()));
        pos.add(DoubleTag.valueOf(player.getZ()));
        tag.put("Pos", pos);
        ListTag rotation = new ListTag();
        rotation.add(FloatTag.valueOf(player.getYRot()));
        rotation.add(FloatTag.valueOf(player.getXRot()));
        tag.put("Rotation", rotation);
        tag.remove("Motion");
        float health = tag.contains("Health") ? tag.getFloat("Health") : player.getMaxHealth();

        if (player.isPassenger()) {
            player.stopRiding();
        }
        player.removeAllEffects();
        player.load(tag);
        player.deathTime = 0;
        player.hurtTime = 0;
        player.setRemainingFireTicks(0);
        player.resetFallDistance();
        player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);

        CoreService.refresh(player);
        RefinementService.refresh(player);
        player.setHealth(Math.max(1.0F, Math.min(health, player.getMaxHealth())));
        resync(player);
    }

    /** Tells the client everything a load changed behind its back. */
    static void resync(ServerPlayer player) {
        if (player.connection == null) {
            return;
        }
        player.server.getPlayerList().sendAllPlayerInfo(player);
        player.onUpdateAbilities();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effect, false));
        }
        SoulSyncService.sync(player);
        AbilityService.syncStatus(player);
    }
}
