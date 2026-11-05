package dev.spud.shadowslave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.spud.shadowslave.citadel.CitadelResident;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws the Awakened of an NPC clan's Citadel as people: one of the default
 * player skins, fixed by who they are, in their clan's coloured tunic and
 * whatever their role carries.
 */
public final class CitadelResidentRenderer extends LivingEntityRenderer<CitadelResident, PlayerModel<CitadelResident>> {
    private final PlayerModel<CitadelResident> wide;
    private final PlayerModel<CitadelResident> slim;

    public CitadelResidentRenderer(EntityRendererProvider.Context context) {
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
    public void render(CitadelResident resident, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        this.model = skin(resident).model() == PlayerSkin.Model.SLIM ? slim : wide;
        this.model.attackTime = resident.getAttackAnim(partialTick);
        super.render(resident, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(CitadelResident resident) {
        return skin(resident).texture();
    }

    @Override
    protected boolean shouldShowName(CitadelResident resident) {
        return super.shouldShowName(resident) && (resident.shouldShowName() || resident == this.entityRenderDispatcher.crosshairPickEntity);
    }

    private static PlayerSkin skin(CitadelResident resident) {
        return DefaultPlayerSkin.get(resident.getUUID());
    }
}
