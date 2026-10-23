package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Kit effects the client has to apply itself. Stone Sleep: you sink in water,
 * even while swimming up (movement is client-driven). Clouded Eyes: a short,
 * hazy view whenever Foresight isn't open (fog is drawn by the client).
 * Red Haze: the inventory stays shut at full rage (the server shuts chests).
 * Heavy (Anchor): you sink. Leaden: ladders will not hold you.
 */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT)
public final class ClientKitEffects {
    static final double STONE_SINK_PER_TICK = 0.05;
    private static final String STONE_SLEEP = AspectKitCatalog.STONE.flawEffectId().toString();
    private static final String CLOUDED_EYES = AspectKitCatalog.SEER.flawEffectId().toString();
    private static final String FORESIGHT = AspectKitCatalog.SEER.abilityId().toString();
    private static final String LEADEN = AspectKitCatalog.ANCHOR.flawEffectId().toString();
    static final double HEAVY_SINK_PER_TICK = 0.06;
    static final double LEADEN_SLIDE = -0.15;
    static final float CLOUDED_FAR = 36.0F;
    static final float CLOUDED_NEAR = 2.0F;

    private ClientKitEffects() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LocalPlayer player) || player.getAbilities().flying) {
            return;
        }
        if (!player.isInWater()) {
            return;
        }
        // Heavy shows as extra gravity, which the server syncs with the player's other attributes.
        if (player.getAttributeValue(Attributes.GRAVITY) > Attributes.GRAVITY.value().getDefaultValue() * 1.2) {
            player.setDeltaMovement(player.getDeltaMovement().add(0.0, -HEAVY_SINK_PER_TICK, 0.0));
        }
        boolean stoneSleep = ClientSoulState.latest()
                .map(snapshot -> STONE_SLEEP.equals(snapshot.flawEffectId()))
                .orElse(false);
        if (stoneSleep) {
            player.setDeltaMovement(player.getDeltaMovement().add(0.0, -STONE_SINK_PER_TICK, 0.0));
        }
    }

    /** Leaden: a ladder will not lift you; any climb this tick is undone and you slide down. */
    @SubscribeEvent
    public static void onPlayerTickAfter(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer player) || player.getAbilities().flying || !leaden()
                || !player.getInBlockState().is(Blocks.LADDER)) {
            return;
        }
        if (player.getY() > player.yo) {
            player.setPos(player.getX(), player.yo, player.getZ());
        }
        player.setDeltaMovement(player.getDeltaMovement().x, LEADEN_SLIDE, player.getDeltaMovement().z);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (Minecraft.getInstance().player == null || !cloudedEyes() || event.getFarPlaneDistance() <= CLOUDED_FAR) {
            return;
        }
        event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), CLOUDED_NEAR));
        event.setFarPlaneDistance(CLOUDED_FAR);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (ClientAbilityState.status().rage().hazed() && event.getNewScreen() instanceof AbstractContainerScreen<?>
                && !(event.getNewScreen() instanceof CreativeModeInventoryScreen)) {
            event.setCanceled(true);
        }
    }

    static boolean leaden() {
        return ClientSoulState.latest().map(snapshot -> LEADEN.equals(snapshot.flawEffectId())).orElse(false);
    }

    /** Clouded Eyes is on: the player's Flaw, with Foresight not open right now. */
    static boolean cloudedEyes() {
        boolean flawed = ClientSoulState.latest()
                .map(snapshot -> CLOUDED_EYES.equals(snapshot.flawEffectId()))
                .orElse(false);
        return flawed && !foresightOpen(ClientAbilityState.status());
    }

    static boolean foresightOpen(AbilityStatusPayload status) {
        return status.slots().stream().anyMatch(slot -> slot.abilityId().equals(FORESIGHT) && slot.primed());
    }
}
