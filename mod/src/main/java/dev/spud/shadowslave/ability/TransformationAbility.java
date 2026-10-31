package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AbilityVariant;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Saint's Transformation Ability (CANON: Transcendence brings one, and its
 * form and purpose vary a great deal; section B of the lore research). DESIGN:
 * press to take the form, press again to leave it. Taking it costs essence up
 * front and holding it drains the pool each second; the form ends on its own
 * when the pool runs dry. Each form is a set of attribute changes, an aura,
 * and the hooks a subclass overrides for its signature effect.
 */
public abstract class TransformationAbility implements AspectAbility {
    static final int TICKS_PER_DRAIN = 20;
    static final int AURA_EVERY_TICKS = 4;

    /** One attribute change the form holds while it lasts. */
    protected record FormModifier(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        static FormModifier add(Holder<Attribute> attribute, double amount) {
            return new FormModifier(attribute, amount, AttributeModifier.Operation.ADD_VALUE);
        }

        static FormModifier multiply(Holder<Attribute> attribute, double amount) {
            return new FormModifier(attribute, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    /** Ticks each player has held the form. */
    private final Map<UUID, Integer> held = new ConcurrentHashMap<>();
    private final AbilityVariant variant;

    protected TransformationAbility(AbilityVariant variant) {
        this.variant = variant;
    }

    @Override
    public ResourceLocation id() {
        return variant.abilityId();
    }

    @Override
    public String displayName() {
        return variant.name();
    }

    /** Essence each second the form is held. */
    public abstract int drainPerSecond();

    protected abstract List<FormModifier> modifiers();

    protected abstract ParticleOptions aura();

    protected SoundEvent shiftSound() {
        return SoundEvents.BEACON_ACTIVATE;
    }

    /** Called when the form is taken. */
    protected void onEnter(ServerPlayer player) {
    }

    /** Called every tick while the form is held. */
    protected void onFormTick(ServerPlayer player, int ticksHeld) {
    }

    /** Called when the form ends, however it ends. */
    protected void onLeave(ServerPlayer player) {
    }

    /** Harm about to reach the transformed player; cancel it to let it pass through. */
    protected void onIncoming(ServerPlayer player, LivingIncomingDamageEvent event) {
    }

    /** Harm the transformed player is about to deal; cancel it to hold the blow back. */
    protected void onOutgoing(ServerPlayer player, LivingIncomingDamageEvent event) {
    }

    /** Harm the transformed player has taken, before armour; change the amount to soften or sharpen it. */
    protected void onHurt(ServerPlayer player, LivingDamageEvent.Pre event) {
    }

    /** Harm the transformed player is dealing, before the target's armour. */
    protected void onHit(ServerPlayer player, LivingEntity target, LivingDamageEvent.Pre event) {
    }

    public boolean transformed(ServerPlayer player) {
        return held.containsKey(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        // The HUD lights the slot while the form holds, and a press to leave it needs no essence.
        return transformed(player);
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (transformed(player)) {
            leave(player, "You return to your own shape.");
            return Activation.FAILED;
        }
        held.put(player.getUUID(), 0);
        applyModifiers(player, true);
        ServerLevel level = player.serverLevel();
        level.sendParticles(aura(), player.getX(), player.getY() + 1.0, player.getZ(), 60, 0.6, 1.0, 0.6, 0.05);
        level.playSound(null, player.blockPosition(), shiftSound(), SoundSource.PLAYERS, 1.0F, 0.8F);
        onEnter(player);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Integer ticks = held.get(player.getUUID());
        if (ticks == null) {
            return;
        }
        if (!player.isAlive()) {
            leave(player, "");
            return;
        }
        int next = ticks + 1;
        held.put(player.getUUID(), next);
        if (next % TICKS_PER_DRAIN == 0) {
            int drain = drainPerSecond();
            if (!EssenceService.covers(player, drain)) {
                leave(player, "Your essence runs dry and the form falls away.");
                return;
            }
            EssenceService.spend(player, drain);
            AbilityService.syncStatus(player);
        }
        // Re-applied every tick so a respawned or reloaded body takes the form back.
        applyModifiers(player, true);
        if (next % AURA_EVERY_TICKS == 0) {
            player.serverLevel().sendParticles(aura(), player.getX(), player.getY() + player.getBbHeight() * 0.5,
                    player.getZ(), 3, player.getBbWidth() * 0.6, player.getBbHeight() * 0.4, player.getBbWidth() * 0.6, 0.01);
        }
        onFormTick(player, next);
    }

    @Override
    public void clear(ServerPlayer player) {
        if (transformed(player)) {
            leave(player, "");
        }
    }

    void leave(ServerPlayer player, String message) {
        if (held.remove(player.getUUID()) == null) {
            return;
        }
        applyModifiers(player, false);
        onLeave(player);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.0F);
        if (!message.isEmpty()) {
            AbilityService.feedback(player, message, ChatFormatting.GRAY);
        }
        AbilityService.syncStatus(player);
    }

    private void applyModifiers(ServerPlayer player, boolean on) {
        List<FormModifier> list = modifiers();
        for (int i = 0; i < list.size(); i++) {
            FormModifier modifier = list.get(i);
            KitModifiers.keep(player, modifier.attribute(), modifierId(i), on ? modifier.amount() : 0.0, modifier.operation());
        }
    }

    private ResourceLocation modifierId(int index) {
        return ResourceLocation.fromNamespaceAndPath(id().getNamespace(), "form/" + id().getPath().replace('/', '_') + "_" + index);
    }
}
