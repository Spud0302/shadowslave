package dev.spud.shadowslave.client.model;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.world.entity.AshBurrowerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib visual model binding for the hostile Ash Burrower executor.
 *
 * <p>The geometry, animations, and texture are Shadow Slave project presentation assets. They are
 * deliberately isolated from creature identity/gameplay so presentation can be replaced without
 * migrating Java-owned state.</p>
 */
public final class AshBurrowerModel extends GeoModel<AshBurrowerEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            ShadowSlaveMod.MOD_ID, "geo/ash_burrower.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(
            ShadowSlaveMod.MOD_ID, "animations/ash_burrower.animation.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ShadowSlaveMod.MOD_ID, "textures/entity/ash_burrower.png");

    @Override
    public ResourceLocation getModelResource(AshBurrowerEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AshBurrowerEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AshBurrowerEntity animatable) {
        return ANIMATIONS;
    }
}
