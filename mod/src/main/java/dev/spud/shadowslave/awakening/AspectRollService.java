package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityCooldownData;
import dev.spud.shadowslave.ability.FlawStateData;
import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.nightmare.NightmareService;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Objects;

/** Server-side boundary that commits a rolled Aspect to a player and announces it. */
public final class AspectRollService {
    private AspectRollService() {
    }

    public static boolean hasAspect(ServerPlayer player) {
        return SoulIdentityService.get(player).isRevealed();
    }

    /**
     * Rolls and commits a new Aspect, replacing any existing one. Callers decide
     * whether the player may roll (the ritual only rolls once; admins may reroll).
     */
    public static AspectRoller.Roll roll(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        if (NightmareService.activeFor(player).isPresent()) {
            throw new IllegalStateException("Cannot roll an Aspect while inside a Nightmare.");
        }

        AspectRoller.Roll roll = AspectRoller.roll(player.getRandom().nextLong(), PlaystyleEvidence.from(player));
        SoulIdentityData before = SoulIdentityService.get(player);
        SoulIdentityService.replace(player, roll.identity());
        try {
            AspectInstanceData aspect = roll.identity().aspect().orElseThrow();
            FlawInstanceData flaw = roll.identity().flaw().orElseThrow();
            SoulService.awakenThroughRitual(player, aspect.instanceId(), aspect.aspectRank(), flaw.instanceId());
        } catch (RuntimeException exception) {
            SoulIdentityService.replace(player, before);
            throw exception;
        }
        player.setData(ModAttachments.ABILITY_COOLDOWNS, AbilityCooldownData.empty());
        player.setData(ModAttachments.FLAW_STATE, FlawStateData.empty());

        ShadowSlaveMod.LOGGER.info("Dream Candle roll {} for player {}: {} ({}), Flaw {}",
                roll.candidate().generationFingerprint(), player.getScoreboardName(), roll.aspectName(),
                roll.kit().natureName(), roll.kit().flawName());
        return roll;
    }

    /** Placeholder chat reveal; the typed-out Spell overlay replaces it in a later update. */
    public static void reveal(ServerPlayer player, AspectRoller.Roll roll) {
        AspectKit kit = roll.kit();
        player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.6F);
        player.sendSystemMessage(Component.literal("The Spell whispers as you wake...")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(line("Aspect: ", roll.aspectName() + " (Dormant)", ChatFormatting.AQUA));
        player.sendSystemMessage(line("Innate: ", kit.innateName() + " - " + kit.innateDescription(), ChatFormatting.GRAY));
        player.sendSystemMessage(line("Dormant Ability: ", kit.abilityName() + " - " + kit.abilityDescription(),
                ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(line("Flaw: ", kit.flawName() + " - " + kit.flawDescription(), ChatFormatting.RED));
        player.sendSystemMessage(Component.literal("Press O to look at your Soul.").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component line(String label, String value, ChatFormatting colour) {
        return Component.literal(label).withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(value).withStyle(colour));
    }
}
