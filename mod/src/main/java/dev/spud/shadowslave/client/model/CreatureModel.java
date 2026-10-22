package dev.spud.shadowslave.client.model;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/**
 * GeckoLib model for one Nightmare Creature: {@code geo/entity/<name>.geo.json},
 * {@code animations/entity/<name>.animation.json} and
 * {@code textures/entity/<name>.png}, all drawn by
 * {@code mod/tools/draw_creatures.py}. The head bone follows where the
 * creature looks.
 */
public final class CreatureModel<T extends GeoAnimatable> extends DefaultedEntityGeoModel<T> {
    public CreatureModel(String name) {
        super(ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, name), true);
    }
}
