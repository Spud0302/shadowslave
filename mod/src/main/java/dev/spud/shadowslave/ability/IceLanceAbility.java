package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Rime's Dormant ability: a lance of ice at the creature you look at (within
 * {@link #REACH} blocks). It hurts and leaves two Frost Marks.
 */
public final class IceLanceAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.RIME.abilityId();
    static final int ESSENCE_COST = 25;
    static final double REACH = 24.0;
    static final float DAMAGE = 5.0F;
    static final int MARKS = 2;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.RIME.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity target = AbilityTargets.lookedAt(player, REACH,
                entity -> entity != player && !CarryTheFlameAbility.isAlly(player, entity));
        if (target == null) {
            AbilityService.feedback(player, "There is nothing there to strike.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        Vec3 from = player.getEyePosition().add(player.getLookAngle().scale(0.6)).add(0, -0.2, 0);
        Vec3 to = target.position().add(0, target.getBbHeight() * 0.6, 0);
        AbilityTargets.beam(level, ParticleTypes.SNOWFLAKE, from, to);
        AbilityTargets.beam(level, ParticleTypes.ITEM_SNOWBALL, from, to);
        target.hurt(level.damageSources().indirectMagic(player, player), DAMAGE);
        SignatureKitPassives.mark(player, target, MARKS);
        level.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 1.6F);
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.8F, 1.3F);
        return Activation.USED;
    }
}
