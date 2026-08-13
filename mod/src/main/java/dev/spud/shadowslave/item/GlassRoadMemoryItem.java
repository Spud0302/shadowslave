package dev.spud.shadowslave.item;

import dev.spud.combatcore.api.CombatPhase;
import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import dev.spud.shadowslave.memory.MemoryOwnershipService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Comparator;
import java.util.Optional;

/** Physical executor for Glass Road's authored clean-edge precision strike. */
public final class GlassRoadMemoryItem extends Item {
    public static final ResourceLocation MEMORY_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory/glass_road");
    public static final ResourceLocation CLEAN_EDGE_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory_enchantment/clean_edge");
    static final int WINDUP_TICKS = 10;
    static final int RECOVERY_TICKS = 16;
    static final double REACH = 4.5D;
    static final double HIT_RADIUS = 0.8D;
    static final float CLEAN_EDGE_DAMAGE = 6.0F;

    public GlassRoadMemoryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.sidedSuccess(stack, true);
        if (!MemoryOwnershipService.owns(serverPlayer, MEMORY_ID)) {
            serverPlayer.sendSystemMessage(Component.literal("The imitation carries no Glass Road bound to your soul.").withStyle(ChatFormatting.DARK_GRAY));
            return InteractionResultHolder.fail(stack);
        }

        long now = serverPlayer.serverLevel().getGameTime();
        GlassRoadCombatData state = serverPlayer.getData(ModAttachments.GLASS_ROAD_COMBAT);
        CombatPhase phase = state.phaseAt(now);
        if (phase == CombatPhase.WINDUP || phase == CombatPhase.ACTIVE) {
            serverPlayer.sendSystemMessage(Component.literal("Glass Road is already committed to the line.").withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }
        if (phase == CombatPhase.RECOVERY) {
            serverPlayer.sendSystemMessage(Component.literal("Glass Road has not yet recovered its edge.").withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }
        if (!state.start(now)) return InteractionResultHolder.fail(stack);

        serverPlayer.sendSystemMessage(Component.literal("Glass Road stills for a precise cut...").withStyle(ChatFormatting.AQUA));
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        long now = player.serverLevel().getGameTime();
        GlassRoadCombatData state = player.getData(ModAttachments.GLASS_ROAD_COMBAT);
        state.resolve(now, () -> resolveCleanEdge(player));
    }

    private static void resolveCleanEdge(ServerPlayer player) {
        boolean manifested = player.getMainHandItem().is(ModItems.GLASS_ROAD_MEMORY.get()) || player.getOffhandItem().is(ModItems.GLASS_ROAD_MEMORY.get());
        boolean owned = MemoryOwnershipService.owns(player, MEMORY_ID);
        Optional<LivingEntity> target = manifested && owned ? findCleanEdgeTarget(player) : Optional.empty();
        if (target.isPresent()) {
            target.orElseThrow().hurt(player.damageSources().playerAttack(player), CLEAN_EDGE_DAMAGE);
            player.sendSystemMessage(Component.literal("Glass Road finds the opening.").withStyle(ChatFormatting.AQUA));
        } else {
            player.sendSystemMessage(Component.literal("The committed cut finds only empty road.").withStyle(ChatFormatting.GRAY));
        }
    }

    private static Optional<LivingEntity> findCleanEdgeTarget(ServerPlayer player) {
        Vec3 origin = player.getEyePosition();
        Vec3 direction = player.getLookAngle().normalize();
        AABB search = player.getBoundingBox().expandTowards(direction.scale(REACH)).inflate(1.0D);
        return player.serverLevel().getEntitiesOfClass(LivingEntity.class, search,
                        target -> target != player && target.isAlive() && player.hasLineOfSight(target))
                .stream()
                .filter(target -> onPrecisionLine(origin, direction, target.getBoundingBox().getCenter()))
                .min(Comparator.comparingDouble(target -> target.distanceToSqr(player)));
    }

    static boolean onPrecisionLine(Vec3 origin, Vec3 direction, Vec3 point) {
        if (!finite(origin) || !finite(direction) || !finite(point) || direction.lengthSqr() < 1.0E-8D) return false;
        Vec3 unit = direction.normalize();
        Vec3 offset = point.subtract(origin);
        double forward = offset.dot(unit);
        if (forward < 0.0D || forward > REACH) return false;
        double lateralSquared = Math.max(0.0D, offset.lengthSqr() - forward * forward);
        return lateralSquared <= HIT_RADIUS * HIT_RADIUS;
    }

    private static boolean finite(Vec3 value) {
        return Double.isFinite(value.x) && Double.isFinite(value.y) && Double.isFinite(value.z);
    }

    static boolean bindsExistingCatalogueDefinition() {
        MemoryContentCatalog.MemoryProfile memory = MemoryContentCatalog.waveOne().memories().stream()
                .filter(profile -> profile.id().equals(MEMORY_ID)).findFirst().orElseThrow();
        return memory.enchantments().stream().anyMatch(enchantment -> enchantment.id().equals(CLEAN_EDGE_ID));
    }
}
