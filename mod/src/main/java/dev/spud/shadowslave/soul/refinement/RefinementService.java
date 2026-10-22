package dev.spud.shadowslave.soul.refinement;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Optional;

/** Server-side refinement: placing points, respeccing, and applying what the points do. */
public final class RefinementService {
    static final ResourceLocation BODY_HEALTH = id("refine_body_health");
    static final ResourceLocation BODY_SPEED = id("refine_body_speed");
    static final ResourceLocation BODY_DAMAGE = id("refine_body_damage");

    private RefinementService() {
    }

    public static RefinementData get(ServerPlayer player) {
        return player.getData(ModAttachments.REFINEMENT);
    }

    private static Optional<SoulRank> rank(ServerPlayer player) {
        return SoulService.get(player).soulRank();
    }

    public static int unspent(ServerPlayer player) {
        return RefinementRules.unspent(rank(player), get(player));
    }

    public static boolean canRespec(ServerPlayer player) {
        return RefinementRules.canRespec(rank(player), get(player));
    }

    /** Places one unspent point. False if there is none to place. */
    public static boolean allocate(ServerPlayer player, RefinementStat stat) {
        if (unspent(player) <= 0) {
            return false;
        }
        player.setData(ModAttachments.REFINEMENT, get(player).plus(stat));
        refresh(player);
        return true;
    }

    /** Takes every point back, losing one for good. False if nothing is placed. */
    public static boolean respec(ServerPlayer player) {
        if (!canRespec(player)) {
            return false;
        }
        player.setData(ModAttachments.REFINEMENT, get(player).respecced());
        refresh(player);
        return true;
    }

    /** Reapplies the Body modifiers and tells the HUD about the essence pool and costs. */
    public static void refresh(ServerPlayer player) {
        RefinementData data = get(player);
        modifier(player, Attributes.MAX_HEALTH, BODY_HEALTH, data.body() * RefinementRules.HEALTH_PER_BODY,
                AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.MOVEMENT_SPEED, BODY_SPEED, data.body() * RefinementRules.SPEED_PER_BODY,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        modifier(player, Attributes.ATTACK_DAMAGE, BODY_DAMAGE, data.body() * RefinementRules.DAMAGE_PER_BODY,
                AttributeModifier.Operation.ADD_VALUE);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
        AbilityService.syncStatus(player);
    }

    private static void modifier(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, double amount,
                                 AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (amount == 0.0) {
            instance.removeModifier(id);
        } else {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
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

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, path);
    }
}
