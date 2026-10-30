package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.trial.content.ScarSettings;
import dev.spud.shadowslave.trial.content.TrialContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * The Nightmare Scar (DESIGN; Jack, 2026-10-29: "leave it as a server option,
 * off by default"). With {@code shadowslaveNightmareScar} on, a death inside a
 * Nightmare cuts a scar into the soul: one stack of this effect, each costing
 * max health and attack damage as {@link ScarSettings} says, up to its
 * {@code max_stacks}. Scars close on their own after a while, and conquering a
 * Nightmare can close one early. The amounts are read from the data pack each
 * time a scar is cut, so a reload retunes the next one.
 */
public final class NightmareScar extends MobEffect {
    static final ResourceLocation HEALTH = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "nightmare_scar_health");
    static final ResourceLocation DAMAGE = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "nightmare_scar_damage");

    NightmareScar() {
        super(MobEffectCategory.HARMFUL, 0x5a1a6e);
        addAttributeModifier(Attributes.MAX_HEALTH, HEALTH, AttributeModifier.Operation.ADD_VALUE,
                level -> -TrialContent.get().scar().healthPerStack() * (level + 1));
        addAttributeModifier(Attributes.ATTACK_DAMAGE, DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                level -> -TrialContent.get().scar().damagePerStack() * (level + 1));
    }

    /** How many scars a player bears now. */
    public static int stacks(ServerPlayer player) {
        MobEffectInstance scar = player.getEffect(holder());
        return scar == null ? 0 : scar.getAmplifier() + 1;
    }

    /** Cuts one more scar, up to the most the settings allow, and restarts the time they take to close. */
    static int cut(ServerPlayer player) {
        ScarSettings settings = TrialContent.get().scar();
        int stacks = Math.min(settings.maxStacks(), stacks(player) + 1);
        set(player, stacks, settings);
        float health = player.getHealth();
        if (health > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
        return stacks;
    }

    /** Closes one scar, if the settings let a conquered Nightmare heal. Returns the scars left. */
    static int heal(ServerPlayer player) {
        ScarSettings settings = TrialContent.get().scar();
        int stacks = stacks(player);
        if (stacks == 0 || !settings.clearedHeals()) {
            return stacks;
        }
        player.removeEffect(holder());
        if (stacks > 1) {
            set(player, stacks - 1, settings);
        }
        return stacks - 1;
    }

    private static void set(ServerPlayer player, int stacks, ScarSettings settings) {
        // Removing first makes the attribute modifiers follow the new number of scars.
        player.removeEffect(holder());
        player.addEffect(new MobEffectInstance(holder(), settings.minutes() * 60 * 20, stacks - 1, false, false, true));
    }

    static Component describe(int stacks) {
        ScarSettings settings = TrialContent.get().scar();
        String count = stacks == 1 ? "a scar" : stacks + " scars";
        return Component.literal("The Nightmare has left " + count + " on your soul. They close in " + settings.minutes() + " minutes"
                + (settings.clearedHeals() ? ", or one at a time as you conquer Nightmares." : "."))
                .withStyle(ChatFormatting.DARK_PURPLE);
    }

    static Holder<MobEffect> holder() {
        return TrialRegistry.NIGHTMARE_SCAR;
    }
}
