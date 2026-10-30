package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Fortune, Awakened (utility): mark the creature you look at. If it dies
 * within thirty seconds, by anyone's hand, it drops twice as much and gives
 * up twice the experience. One mark at a time.
 */
public final class BorrowedLuckAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.BORROWED_LUCK.abilityId();
    static final int ESSENCE_COST = 30;
    static final double REACH = 20.0;
    static final int MARK_TICKS = 20 * 30;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.BORROWED_LUCK.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity target = AbilityTargets.lookedAt(player, REACH,
                entity -> !(entity instanceof Player) && !CarryTheFlameAbility.isAlly(player, entity));
        if (target == null) {
            AbilityService.feedback(player, "Look at a creature to borrow against.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        FortuneLuck.mark(player, target, MARK_TICKS);
        ServerLevel level = player.serverLevel();
        AbilityTargets.beam(level, ParticleTypes.WAX_OFF, player.getEyePosition(), target.position().add(0, target.getBbHeight() * 0.6, 0));
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.2F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        if (player.serverLevel().getGameTime() % 10 != 0) {
            return;
        }
        Optional<UUID> marked = FortuneLuck.marked(player);
        Entity target = marked.map(id -> player.serverLevel().getEntity(id)).orElse(null);
        if (target != null && target.isAlive()) {
            player.serverLevel().sendParticles(ParticleTypes.WAX_OFF, target.getX(), target.getY() + target.getBbHeight() + 0.4,
                    target.getZ(), 3, 0.2, 0.1, 0.2, 0.0);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        FortuneLuck.unmark(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return FortuneLuck.marked(player).isPresent();
    }
}
