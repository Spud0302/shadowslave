package dev.spud.shadowslave.ability;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/** Keeps a kit's attribute modifier at the amount its rule calls for, adding, updating or removing it. */
final class KitModifiers {
    private KitModifiers() {
    }

    static void keep(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, double amount) {
        keep(entity, attribute, id, amount, AttributeModifier.Operation.ADD_VALUE);
    }

    /** A zero amount takes the modifier off. */
    static void keep(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, double amount,
                     AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier current = instance.getModifier(id);
        if (amount == 0.0) {
            if (current != null) {
                instance.removeModifier(id);
            }
        } else if (current == null || Math.abs(current.amount() - amount) > 1.0e-3 || current.operation() != operation) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }
}
