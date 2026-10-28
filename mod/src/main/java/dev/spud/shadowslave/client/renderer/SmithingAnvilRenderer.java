package dev.spud.shadowslave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.spud.shadowslave.smithing.SmithingAnvilBlock;
import dev.spud.shadowslave.smithing.SmithingAnvilBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Lays the piece being worked flat on the anvil's face, glowing while hot. */
public final class SmithingAnvilRenderer implements BlockEntityRenderer<SmithingAnvilBlockEntity> {
    private final ItemRenderer items;

    public SmithingAnvilRenderer(BlockEntityRendererProvider.Context context) {
        this.items = context.getItemRenderer();
    }

    @Override
    public void render(SmithingAnvilBlockEntity anvil, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack piece = anvil.piece();
        if (piece.isEmpty()) {
            return;
        }
        float yaw = anvil.getBlockState().getValue(SmithingAnvilBlock.FACING).toYRot();
        pose.pushPose();
        pose.translate(0.5, 14.2 / 16.0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.scale(0.45F, 0.45F, 0.45F);
        // A hot billet lights itself.
        int lit = SmithingAnvilBlockEntityGlow.hot(piece) ? LightTexture.FULL_BRIGHT : light;
        items.renderStatic(piece, ItemDisplayContext.FIXED, lit, overlay, pose, buffers, anvil.getLevel(), 0);
        pose.popPose();
    }
}
