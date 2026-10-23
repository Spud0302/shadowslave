package dev.spud.shadowslave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.spud.shadowslave.dreamrealm.SleepingBodyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws a sleeping body as its dreamer, in their own skin, lying in the bed.
 * The vanilla living renderer already lays a sleeping entity along its bed.
 */
public final class SleepingBodyRenderer extends LivingEntityRenderer<SleepingBodyEntity, PlayerModel<SleepingBodyEntity>> {
    private final PlayerModel<SleepingBodyEntity> wide;
    private final PlayerModel<SleepingBodyEntity> slim;

    public SleepingBodyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.0F);
        this.wide = this.model;
        this.slim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public void render(SleepingBodyEntity body, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        this.model = skin(body).model() == PlayerSkin.Model.SLIM ? slim : wide;
        super.render(body, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(SleepingBodyEntity body) {
        return skin(body).texture();
    }

    private static PlayerSkin skin(SleepingBodyEntity body) {
        return body.owner().map(owner -> {
            var connection = Minecraft.getInstance().getConnection();
            PlayerInfo info = connection == null ? null : connection.getPlayerInfo(owner);
            return info != null ? info.getSkin() : DefaultPlayerSkin.get(owner);
        }).orElseGet(() -> DefaultPlayerSkin.get(body.getUUID()));
    }

    @Override
    protected boolean shouldShowName(SleepingBodyEntity body) {
        return false;
    }
}
