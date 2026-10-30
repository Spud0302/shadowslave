package dev.spud.shadowslave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.spud.shadowslave.trial.LingeringChallenger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws a challenger who lingers in a Long Nightmare as they were: their own
 * skin, their armour and what they held. Above them only their role's name
 * shows, as with every challenger inside.
 */
public final class LingeringChallengerRenderer extends LivingEntityRenderer<LingeringChallenger, PlayerModel<LingeringChallenger>> {
    private final PlayerModel<LingeringChallenger> wide;
    private final PlayerModel<LingeringChallenger> slim;

    public LingeringChallengerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.wide = this.model;
        this.slim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(LingeringChallenger figure, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        this.model = skin(figure).model() == PlayerSkin.Model.SLIM ? slim : wide;
        this.model.attackTime = figure.getAttackAnim(partialTick);
        super.render(figure, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(LingeringChallenger figure) {
        return skin(figure).texture();
    }

    private static PlayerSkin skin(LingeringChallenger figure) {
        return figure.owner().map(owner -> {
            var connection = Minecraft.getInstance().getConnection();
            PlayerInfo info = connection == null ? null : connection.getPlayerInfo(owner);
            return info != null ? info.getSkin() : DefaultPlayerSkin.get(owner);
        }).orElseGet(() -> DefaultPlayerSkin.get(figure.getUUID()));
    }
}
