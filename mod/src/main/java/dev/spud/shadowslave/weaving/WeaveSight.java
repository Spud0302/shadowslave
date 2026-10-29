package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.ability.KitRules;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A Seer sees weaves in the world (DESIGN, plan section 2b): looking at a
 * creature, a player or an item on the ground shows the strands in what it
 * holds and wears, without a Weaving Table.
 */
public final class WeaveSight {
    static final double REACH = 16.0;
    static final int CHECK_TICKS = 10;
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private WeaveSight() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().getGameTime() % CHECK_TICKS != 0
                || !KitRules.hasInnate(player, AspectKitCatalog.SEER.innateId())) {
            return;
        }
        Entity seen = looking(player);
        if (seen == null) {
            return;
        }
        List<String> pieces = read(seen);
        if (!pieces.isEmpty()) {
            player.displayClientMessage(Component.literal(String.join("  ·  ", pieces)).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
    }

    /** What the Seer is looking at, within reach and in plain sight. */
    static Entity looking(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(REACH));
        double blocked = player.level().clip(new net.minecraft.world.level.ClipContext(eye, end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player))
                .getLocation().distanceToSqr(eye);
        AABB box = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, box,
                entity -> entity != player && (entity instanceof LivingEntity || entity instanceof ItemEntity), blocked);
        return hit == null ? null : hit.getEntity();
    }

    /** "Piece: Strand I, Strand II" for each woven piece the entity holds or wears, or the item it is. */
    static List<String> read(Entity entity) {
        List<String> pieces = new ArrayList<>();
        if (entity instanceof ItemEntity item) {
            describe(item.getItem(), pieces);
        } else if (entity instanceof LivingEntity living) {
            for (EquipmentSlot slot : SLOTS) {
                describe(living.getItemBySlot(slot), pieces);
            }
        }
        return pieces;
    }

    private static void describe(ItemStack stack, List<String> pieces) {
        WovenStrands strands = StrandService.strands(stack);
        if (strands.isEmpty()) {
            return;
        }
        WeavingContent content = WeavingContent.current();
        List<String> names = new ArrayList<>();
        for (WovenStrands.Laid laid : strands.strands()) {
            names.add(StrandService.name(content, laid) + " " + WeavingRules.tierName(laid.tier()));
        }
        pieces.add(stack.getHoverName().getString() + ": " + String.join(", ", names));
    }
}
