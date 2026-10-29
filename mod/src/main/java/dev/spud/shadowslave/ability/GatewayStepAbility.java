package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.DreamCandleRitual;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.citadel.CitadelData;
import dev.spud.shadowslave.citadel.CitadelService;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

/**
 * Keeper, Awakened (movement): standing at a lit Dream Candle, step straight
 * through to the Gateway of the nearest Citadel your clan holds. From the
 * waking world this is a crossing like a dream: your body stays where you
 * stood, and you wake there.
 */
public final class GatewayStepAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.GATEWAY_STEP.abilityId();
    static final int ESSENCE_COST = 50;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.GATEWAY_STEP.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel here = player.serverLevel();
        if (DreamCandleRitual.findLitCandle(here, player.blockPosition()).isEmpty()) {
            AbilityService.feedback(player, "Stand by a lit Dream Candle to step through.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel realm = DreamRealm.level(player.server);
        List<CitadelData.Citadel> held = realm == null ? List.of() : CitadelService.clanCitadels(player, realm, player.blockPosition());
        if (held.isEmpty()) {
            AbilityService.feedback(player, "Your clan holds no Citadel to step through to.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        CitadelData.Citadel citadel = held.get(0);
        here.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.3, 0.8, 0.3, 0.05);
        if (!DreamRealm.is(here)) {
            if (!DreamCrossing.canDream(player) || !DreamCrossing.cross(player, GlobalPos.of(here.dimension(), player.blockPosition()))) {
                AbilityService.feedback(player, "The Gateway will not open to you now.", ChatFormatting.GRAY);
                return Activation.FAILED;
            }
        }
        realm.getChunk(citadel.gateway());
        Vec3 spot = DreamCrossing.standBeside(realm, citadel.gateway());
        player.teleportTo(realm, spot.x, spot.y, spot.z, Set.of(), player.getYRot(), player.getXRot());
        player.resetFallDistance();
        realm.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, spot.x, spot.y + 1.0, spot.z, 40, 0.4, 0.8, 0.4, 0.04);
        realm.playSound(null, citadel.gateway(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.displayClientMessage(Component.literal("You step through the Gateway of " + citadel.name() + ".")
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return Activation.USED;
    }
}
