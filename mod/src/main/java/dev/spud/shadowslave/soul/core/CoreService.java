package dev.spud.shadowslave.soul.core;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.item.SoulShardItem;
import dev.spud.shadowslave.network.payload.CoreStatusPayload;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

/** Server-side soul core: absorbing shards, saturation, and the small health bonus it gives. */
public final class CoreService {
    static final ResourceLocation VIGOR_ID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "core_vigor");

    private CoreService() {
    }

    /** The rank of the player's core, empty before the Spell gives them one. */
    public static Optional<SoulRank> rank(ServerPlayer player) {
        return SoulService.get(player).soulRank();
    }

    public static int saturation(ServerPlayer player) {
        return player.getData(ModAttachments.CORE).saturation();
    }

    public static int limit(ServerPlayer player) {
        return rank(player).map(CoreRules::limit).orElse(0);
    }

    public static boolean saturated(ServerPlayer player) {
        return rank(player).map(rank -> CoreRules.saturated(saturation(player), rank)).orElse(false);
    }

    /** Sets saturation directly (rank-ups, admin tools), clamped to the core's limit. */
    public static void set(ServerPlayer player, int saturation) {
        int limit = limit(player);
        player.setData(ModAttachments.CORE, new CoreData(Math.min(limit, Math.max(0, saturation))));
        refresh(player);
    }

    /**
     * Adds a shard's saturation to the core. Returns the units added, or 0 if
     * the player has no core or it is already saturated (the shard stays).
     */
    public static int absorb(ServerPlayer player, ShardRank shard, int units) {
        Optional<SoulRank> rank = rank(player);
        if (rank.isEmpty()) {
            return 0;
        }
        int before = saturation(player);
        int limit = CoreRules.limit(rank.get());
        if (before >= limit) {
            return 0;
        }
        int after = Math.min(limit, before + CoreRules.absorbValue(shard, units, rank.get()));
        player.setData(ModAttachments.CORE, new CoreData(after));
        refresh(player);
        if (after >= limit) {
            player.sendSystemMessage(Component.literal("Your core is saturated. It can hold no more at this Rank.")
                    .withStyle(ChatFormatting.AQUA));
        }
        return after - before;
    }

    /** Absorbs one shard from the stack, or as many as fit when {@code all} is set. Returns shards consumed. */
    public static int absorbFromStack(ServerPlayer player, ItemStack stack, ShardRank shard, boolean all) {
        if (rank(player).isEmpty()) {
            player.displayClientMessage(Component.literal("You have no soul core to hold it.")
                    .withStyle(ChatFormatting.GRAY), true);
            return 0;
        }
        int units = SoulShardItem.units(stack, shard);
        int consumed = 0;
        while (!stack.isEmpty() && absorb(player, shard, units) > 0) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            consumed++;
            if (!all || player.getAbilities().instabuild && consumed >= 64) {
                break;
            }
        }
        if (consumed == 0) {
            player.displayClientMessage(Component.literal("Your core is saturated.").withStyle(ChatFormatting.GRAY), true);
            return 0;
        }
        player.serverLevel().sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(),
                8 + 4 * Math.min(consumed, 6), 0.3, 0.5, 0.3, 0.02);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.8F, 0.6F + player.getRandom().nextFloat() * 0.2F);
        player.displayClientMessage(Component.literal("Core " + Math.round(100.0F * saturation(player) / limit(player)) + "%")
                .withStyle(ChatFormatting.AQUA), true);
        return consumed;
    }

    /** Reapplies the health bonus and tells the client. Call after any change to rank or saturation. */
    public static void refresh(ServerPlayer player) {
        applyVigor(player);
        sync(player);
    }

    private static void applyVigor(ServerPlayer player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        double bonus = rank(player).map(rank -> CoreRules.healthBonus(saturation(player), rank)).orElse(0.0);
        if (bonus <= 0.0) {
            maxHealth.removeModifier(VIGOR_ID);
        } else {
            maxHealth.addOrUpdateTransientModifier(new AttributeModifier(VIGOR_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public static CoreStatusPayload status(ServerPlayer player) {
        return rank(player)
                .map(rank -> new CoreStatusPayload(rank.serializedName(), saturation(player), CoreRules.limit(rank)))
                .orElse(CoreStatusPayload.NONE);
    }

    public static void sync(ServerPlayer player) {
        if (player.connection != null && player.connection.hasChannel(CoreStatusPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, status(player));
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refresh(player);
        }
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refresh(player);
            player.setHealth(player.getMaxHealth());
        }
    }
}
