package dev.spud.shadowslave.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Borrowed Face, as everyone sees it: a disguised player is drawn as the
 * creature whose face they wear. A body of that kind is kept for each of them
 * and posed like the player every frame; it never joins the world.
 */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT)
public final class ClientDisguises {
    /** Disguised player's entity id to the stand-in body drawn in their place. */
    private static final Map<Integer, Entity> BODIES = new ConcurrentHashMap<>();

    private ClientDisguises() {
    }

    static void set(int entityId, String entityType) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation id = entityType.isEmpty() ? null : ResourceLocation.tryParse(entityType);
        Optional<EntityType<?>> type = id == null ? Optional.empty() : BuiltInRegistries.ENTITY_TYPE.getOptional(id);
        if (type.isEmpty() || minecraft.level == null) {
            BODIES.remove(entityId);
            return;
        }
        Entity body = type.get().create(minecraft.level);
        if (body == null) {
            BODIES.remove(entityId);
            return;
        }
        if (body instanceof Mob mob) {
            mob.setNoAi(true);
        }
        BODIES.put(entityId, body);
    }

    public static boolean disguised(int entityId) {
        return BODIES.containsKey(entityId);
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Entity body = BODIES.get(event.getEntity().getId());
        if (body == null) {
            return;
        }
        event.setCanceled(true);
        AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
        float partial = event.getPartialTick();
        pose(body, player);
        @SuppressWarnings("unchecked")
        EntityRenderer<Entity> renderer = (EntityRenderer<Entity>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(body);
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        renderer.render(body, player.getViewYRot(partial), partial, pose, event.getMultiBufferSource(), event.getPackedLight());
        pose.popPose();
    }

    /** Copies where the player is, which way they face and how they walk onto the stand-in. */
    private static void pose(Entity body, AbstractClientPlayer player) {
        body.setPos(player.getX(), player.getY(), player.getZ());
        body.xo = player.xo;
        body.yo = player.yo;
        body.zo = player.zo;
        body.xOld = player.xOld;
        body.yOld = player.yOld;
        body.zOld = player.zOld;
        body.setYRot(player.getYRot());
        body.yRotO = player.yRotO;
        body.setXRot(player.getXRot());
        body.xRotO = player.xRotO;
        body.tickCount = player.tickCount;
        body.setOnGround(player.onGround());
        body.setShiftKeyDown(player.isShiftKeyDown());
        if (body instanceof LivingEntity living) {
            living.yBodyRot = player.yBodyRot;
            living.yBodyRotO = player.yBodyRotO;
            living.yHeadRot = player.yHeadRot;
            living.yHeadRotO = player.yHeadRotO;
            living.hurtTime = player.hurtTime;
            living.swinging = player.swinging;
            living.attackAnim = player.attackAnim;
            living.oAttackAnim = player.oAttackAnim;
            copyWalk(living, player);
        }
    }

    /** The walk cycle has no public setters for all of its state, so it is copied field by field. */
    private static void copyWalk(LivingEntity body, LivingEntity player) {
        for (java.lang.reflect.Field field : net.minecraft.world.entity.WalkAnimationState.class.getDeclaredFields()) {
            if (field.getType() != float.class || java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                field.setFloat(body.walkAnimation, field.getFloat(player.walkAnimation));
            } catch (ReflectiveOperationException ignored) {
                // The legs simply stand still.
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        BODIES.clear();
    }
}
