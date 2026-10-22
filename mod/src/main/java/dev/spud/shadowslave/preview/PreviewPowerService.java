package dev.spud.shadowslave.preview;

import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Server-authoritative Cold Ash Flaw tick. Kindle moved to the ability framework (ability/KindleAbility). */
public final class PreviewPowerService {
    private static final ResourceLocation COLD_ASH_EFFECT_ID = id("generation/flaw_effect/cold_ash");

    private PreviewPowerService() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        SoulIdentityData identity = SoulIdentityService.get(player);
        if (identity.flaw().isPresent()
                && COLD_ASH_EFFECT_ID.equals(identity.flaw().orElseThrow().effectId())
                && player.isInWaterRainOrBubble()) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, true));
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
