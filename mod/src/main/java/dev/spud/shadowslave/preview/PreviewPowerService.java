package dev.spud.shadowslave.preview;

import dev.spud.shadowslave.appraisal.PreviewAppraisalService;
import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Objects;

/** Server-authoritative mechanics for the fixed preview Aspect and Flaw. */
public final class PreviewPowerService {
    private static final int KINDLE_DURATION_TICKS = 20 * 10;
    private static final int KINDLE_COOLDOWN_TICKS = 20 * 30;

    private PreviewPowerService() {
    }

    public static boolean activateKindle(ServerPlayer player) {
        SoulIdentityData identity = SoulIdentityService.get(player);
        if (!possessesAspectAbility(identity, PreviewAppraisalService.ABILITY_ID)) {
            player.sendSystemMessage(Component.literal("Your revealed Aspect does not possess the preview Kindle ability."));
            return false;
        }

        long now = player.serverLevel().getGameTime();
        PreviewPowerData power = player.getData(ModAttachments.PREVIEW_POWER);
        if (power.kindleCooldownUntil() > now) {
            long remainingSeconds = (power.kindleCooldownUntil() - now + 19) / 20;
            player.sendSystemMessage(Component.literal("Kindle is recovering: " + remainingSeconds + "s."));
            return false;
        }

        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, KINDLE_DURATION_TICKS, 0, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, KINDLE_DURATION_TICKS, 0, false, true));
        player.setData(ModAttachments.PREVIEW_POWER, new PreviewPowerData(now + KINDLE_COOLDOWN_TICKS));
        player.sendSystemMessage(Component.literal("[Last Light] Kindle — the dark yields for a moment."));
        return true;
    }

    static boolean possessesAspectAbility(SoulIdentityData identity, ResourceLocation abilityId) {
        Objects.requireNonNull(identity, "identity");
        ResourceLocation checkedAbilityId = Objects.requireNonNull(abilityId, "abilityId");
        return identity.aspect()
                .map(aspect -> aspect.abilitySet().contains(checkedAbilityId))
                .orElse(false);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        SoulIdentityData identity = SoulIdentityService.get(player);
        if (identity.flaw().isPresent()
                && PreviewAppraisalService.FLAW_EFFECT_ID.equals(identity.flaw().orElseThrow().effectId())
                && player.isInWaterRainOrBubble()) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, true));
        }
    }
}
