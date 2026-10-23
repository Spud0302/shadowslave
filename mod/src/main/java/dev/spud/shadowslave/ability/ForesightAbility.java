package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.gate.GateEvent;
import dev.spud.shadowslave.gate.GateService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Seer's Dormant ability: 30 seconds of clear sight. Hostile creatures
 * nearby glow while it lasts, Clouded Eyes lifts, and a Nightmare Gate in
 * reach shows its Category and the creatures still to come. Foreseeing a Gate
 * before it opens fixes its Category: what the Seer sees is what comes out.
 */
public final class ForesightAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.SEER.abilityId();
    static final int ESSENCE_COST = 30;
    static final int DURATION_TICKS = 20 * 30;
    static final double GLOW_RANGE = 24.0;
    static final double GATE_RANGE = 128.0;

    private static final Map<UUID, Long> ACTIVE_UNTIL = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.SEER.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (active(player)) {
            AbilityService.feedback(player, "Your Foresight is already open.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        ACTIVE_UNTIL.put(player.getUUID(), level.getGameTime() + DURATION_TICKS);
        revealHostiles(player);
        level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getEyeY() + 1.2, player.getZ(), 40, 0.6, 0.15, 0.6, 0.3);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.4F);
        foreseeGate(player);
        return Activation.USED;
    }

    private static void foreseeGate(ServerPlayer player) {
        Optional<GateEvent> gate = GateService.foresee(player.serverLevel(), player.blockPosition(), GATE_RANGE);
        if (gate.isEmpty()) {
            return;
        }
        GateEvent found = gate.get();
        String state = found.phase() == GateEvent.Phase.WARNING ? "will open at" : "is";
        player.sendSystemMessage(Component.literal("Foresight: the Gate " + state + " Category " + found.category() + ".")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        for (String line : GateService.roster(found)) {
            player.sendSystemMessage(Component.literal("  " + line).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    private static void revealHostiles(ServerPlayer player) {
        for (LivingEntity mob : player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(GLOW_RANGE), AbilityTargets::isHostile)) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, false, false));
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        if (until == null) {
            return;
        }
        if (until <= player.serverLevel().getGameTime()) {
            ACTIVE_UNTIL.remove(player.getUUID());
            AbilityService.feedback(player, "Your Foresight closes.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
            return;
        }
        if (player.tickCount % 40 == 0) {
            revealHostiles(player);
        }
    }

    /** Lit on the HUD while Foresight is open; the client lifts Clouded Eyes on it. */
    @Override
    public boolean primed(ServerPlayer player) {
        return active(player);
    }

    @Override
    public void clear(ServerPlayer player) {
        ACTIVE_UNTIL.remove(player.getUUID());
    }

    public static boolean active(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        return until != null && until > player.serverLevel().getGameTime();
    }
}
