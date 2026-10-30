package dev.spud.shadowslave.echoes;

import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.soul.core.ShardDrops;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.Optional;

/**
 * Echo remnants (DESIGN, after ch. 47: Echoes are far rarer than Memories).
 * Any creature a player slays can, rarely, leave the echo of itself behind;
 * the chance grows with its rank. Animals count as Dormant unless the
 * settings turn them off, and the {@code shadowslave:echo/no_remnant} tag
 * and per-creature files say who never leaves one.
 */
public final class EchoRemnants {
    /** What a saved body forgets: where and how it died, who it was angry at, and anything another system put on it. */
    private static final List<String> FORGOTTEN = List.of(
            "UUID", "Pos", "Motion", "Rotation", "FallDistance", "Fire", "Air", "OnGround", "PortalCooldown",
            "Health", "HurtTime", "HurtByTimestamp", "DeathTime", "AbsorptionAmount", "active_effects", "FallFlying",
            "Brain", "AngerTime", "AngryAt", "Tags", "CustomName", "CustomNameVisible", "PersistenceRequired",
            "DeathLootTable", "DeathLootTableSeed", "Leash", "Owner", "Sitting", "InLove", "LoveCause", "Age", "ForcedAge",
            "NeoForgeData", "neoforge:attachments", "Invulnerable", "Silent", "NoAI", "CanPickUpLoot", "SleepingX",
            "SleepingY", "SleepingZ", "TicksFrozen", "HasVisualFire", "Glowing");

    private EchoRemnants() {
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob victim) || !(victim.level() instanceof ServerLevel level) || event.isCanceled()
                || EchoService.bond(victim).isPresent()) {
            return;
        }
        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer player ? player
                : victim.getKillCredit() instanceof ServerPlayer player ? player : null;
        if (killer != null) {
            drop(level, victim, killer, level.getRandom());
        }
    }

    /** Rolls a remnant for a slain creature and leaves it where it fell. Returns the remnant, or empty. */
    public static ItemStack drop(ServerLevel level, Mob victim, ServerPlayer killer, RandomSource random) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            return ItemStack.EMPTY;
        }
        EchoContent content = EchoContent.current();
        Optional<EchoBody> body = bodyOf(content, victim);
        if (body.isEmpty() || random.nextFloat() >= EchoRules.dropChance(content.settings(), body.get().rank(),
                content.creature(typeId(victim)))) {
            return ItemStack.EMPTY;
        }
        ItemStack remnant = remnant(body.get());
        ItemEntity item = new ItemEntity(level, victim.getX(), victim.getY() + 0.5, victim.getZ(), remnant.copy());
        item.setDefaultPickUpDelay();
        item.setGlowingTag(true);
        level.addFreshEntity(item);
        level.sendParticles(ParticleTypes.SOUL, victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(),
                24, victim.getBbWidth() * 0.4, victim.getBbHeight() * 0.4, victim.getBbWidth() * 0.4, 0.02);
        level.playSound(null, victim.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.NEUTRAL, 1.4F, 0.8F);
        killer.sendSystemMessage(Component.literal("An echo of the " + body.get().name() + " lingers where it fell.")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        return remnant;
    }

    public static ItemStack remnant(EchoBody body) {
        ItemStack stack = new ItemStack(EchoesRegistry.ECHO_REMNANT.get());
        stack.set(EchoesRegistry.REMNANT_OF.get(), body);
        return stack;
    }

    /** What an Echo of {@code victim} would be made of, or empty if it can leave no remnant. */
    public static Optional<EchoBody> bodyOf(EchoContent content, Mob victim) {
        ResourceLocation id = typeId(victim);
        Optional<EchoCreature> override = content.creature(id);
        if (victim.getType().is(EchoesRegistry.NO_REMNANT) || override.map(creature -> !creature.remnant()).orElse(false)) {
            return Optional.empty();
        }
        Optional<CreatureRank> rank = override.flatMap(EchoCreature::rank).or(() -> rankOf(content, victim));
        if (rank.isEmpty()) {
            return Optional.empty();
        }
        String kind = victim instanceof NightmareCreature creature ? creature.creatureClass().displayName() : "";
        return Optional.of(new EchoBody(id, victim.getType().getDescription().getString(), rank.get(), kind,
                savedBody(victim), victim.getMaxHealth()));
    }

    private static Optional<CreatureRank> rankOf(EchoContent content, LivingEntity victim) {
        if (victim instanceof NightmareCreature creature) {
            return Optional.of(creature.rank());
        }
        Optional<CreatureRank> graded = ShardDrops.rankOf(victim).map(CreatureRank::of);
        return graded.isPresent() || !content.settings().passiveCreatures() ? graded : Optional.of(CreatureRank.DORMANT);
    }

    /** The creature's body as saved, without what an Echo should not carry over. */
    static CompoundTag savedBody(Mob victim) {
        CompoundTag tag = victim.saveWithoutId(new CompoundTag());
        FORGOTTEN.forEach(tag::remove);
        return tag;
    }

    static ResourceLocation typeId(LivingEntity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
    }
}
