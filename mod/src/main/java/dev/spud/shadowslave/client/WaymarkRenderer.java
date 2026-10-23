package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.network.payload.WaymarkPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Draws the Waymarks the server says are near: a slim pillar of light over
 * each, seen through walls. Your own are gold, other Wayfarers' pale blue.
 * Marks fade if the server stops mentioning them for a few seconds.
 */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT)
public final class WaymarkRenderer {
    static final int FORGET_TICKS = 60;

    record Shown(List<BlockPos> marks, long seen) {
    }

    private static final Map<Integer, Shown> SHOWN = new ConcurrentHashMap<>();
    private static ClientLevel shownIn;

    private WaymarkRenderer() {
    }

    static void show(WaymarkPayload payload, long now) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != shownIn) {
            SHOWN.clear();
            shownIn = level;
        }
        if (payload.marks().isEmpty()) {
            SHOWN.remove(payload.ownerId());
        } else {
            SHOWN.put(payload.ownerId(), new Shown(payload.marks(), now));
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || SHOWN.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.level != shownIn) {
            SHOWN.clear();
            return;
        }
        long now = minecraft.level.getGameTime();
        SHOWN.values().removeIf(shown -> now - shown.seen() > FORGET_TICKS || now < shown.seen());
        int self = minecraft.player.getId();
        net.minecraft.world.phys.Vec3 camera = event.getCamera().getPosition();
        XrayLines.draw(event, (pose, lines) -> SHOWN.forEach((owner, shown) -> {
            boolean mine = owner == self;
            float r = mine ? 1.0F : 0.6F;
            float g = mine ? 0.82F : 0.85F;
            float b = mine ? 0.35F : 1.0F;
            for (int i = 0; i < shown.marks().size(); i++) {
                BlockPos pos = shown.marks().get(i);
                if (Math.abs(pos.getX() + 0.5 - camera.x) < 1.0 && Math.abs(pos.getZ() + 0.5 - camera.z) < 1.0) {
                    continue; // standing in it: its lines would sweep across the whole view
                }
                // The newest mark, the one a snap returns to, stands tallest.
                double height = i == shown.marks().size() - 1 ? 3.0 : 2.0;
                double x = pos.getX() + 0.5;
                double z = pos.getZ() + 0.5;
                LevelRenderer.renderLineBox(pose, lines, new AABB(x - 0.15, pos.getY(), z - 0.15, x + 0.15, pos.getY() + height, z + 0.15),
                        r, g, b, 0.95F);
                LevelRenderer.renderLineBox(pose, lines, new AABB(x - 0.45, pos.getY(), z - 0.45, x + 0.45, pos.getY() + 0.05, z + 0.45),
                        r, g, b, 0.7F);
            }
        }));
    }
}
