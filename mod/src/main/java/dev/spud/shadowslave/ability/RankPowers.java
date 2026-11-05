package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.core.ShardDrops;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Optional;

/**
 * What a Master's and a Saint's soul does for every ability they hold (DESIGN,
 * after ch. 744: existing abilities change on Ascension, and section B of the
 * lore research: a Saint's Presence and their pull on the world's essence).
 *
 * <ul>
 *   <li>Mastered: abilities gained below the rank you hold cost a quarter less
 *   essence, and their main number grows by {@link #power}.</li>
 *   <li>Presence: lesser hostile creatures near a Saint falter, slowed and
 *   weakened.</li>
 *   <li>A Saint draws on the world's essence: the pool refills faster, and
 *   faster still in the Dream Realm.</li>
 * </ul>
 */
public final class RankPowers {
    /** Essence a Mastered ability costs, as a share of its usual price. */
    static final float MASTERED_COST = 0.75F;
    /** How much each rank past Awakened adds to an ability's main number. */
    static final float POWER_PER_RANK = 0.25F;
    static final float SAINT_REGEN = 1.5F;
    static final float SAINT_DREAM_REGEN = 2.0F;
    static final double PRESENCE_RADIUS = 10.0;
    static final int PRESENCE_EVERY_TICKS = 20;

    private RankPowers() {
    }

    /** Multiplier for an ability's main number (damage, duration, range): 1 below Master, more at each rank above. */
    public static float power(ServerPlayer player) {
        return powerFor(SoulService.get(player).soulRank());
    }

    static float powerFor(Optional<SoulRank> rank) {
        int above = rank.map(value -> value.ordinal() - SoulRank.AWAKENED.ordinal()).orElse(0);
        return 1.0F + POWER_PER_RANK * Math.max(0, above);
    }

    /** Scales a duration or count in ticks by {@link #power}. */
    public static int scale(ServerPlayer player, int value) {
        return Math.round(value * power(player));
    }

    /** Scales a damage, range or strength by {@link #power}. */
    public static double scale(ServerPlayer player, double value) {
        return value * power(player);
    }

    /** True once this player's soul has outgrown the rank the ability came with. */
    public static boolean mastered(ServerPlayer player, ResourceLocation abilityId) {
        Optional<SoulRank> rank = SoulService.get(player).soulRank();
        Optional<SoulRank> gained = SoulIdentityService.get(player).aspect()
                .flatMap(aspect -> aspect.abilitySet().abilities().stream()
                        .filter(ability -> ability.abilityId().equals(abilityId))
                        .findFirst())
                .flatMap(AspectAbilityData::acquisitionRank);
        return masteredAt(rank, gained);
    }

    static boolean masteredAt(Optional<SoulRank> rank, Optional<SoulRank> gained) {
        if (rank.isEmpty() || rank.get().compareTo(SoulRank.ASCENDED) < 0) {
            return false;
        }
        return gained.map(value -> value.compareTo(rank.get()) < 0).orElse(true);
    }

    /** Essence for one use after Mastery. */
    static int costFor(ServerPlayer player, AspectAbility ability, int cost) {
        return mastered(player, ability.id()) ? Math.max(1, Math.round(cost * MASTERED_COST)) : cost;
    }

    public static boolean saint(ServerPlayer player) {
        return SoulService.get(player).soulRank().filter(rank -> rank.compareTo(SoulRank.TRANSCENDENT) >= 0).isPresent();
    }

    /** A Saint's pull on the world's essence. */
    static float regenFactor(ServerPlayer player) {
        if (!saint(player)) {
            return 1.0F;
        }
        return DreamRealm.is(player.level()) ? SAINT_DREAM_REGEN : SAINT_REGEN;
    }

    /** A Saint's Presence: lesser hostiles nearby falter. */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % PRESENCE_EVERY_TICKS != 0
                || !saint(player) || player.isSpectator()) {
            return;
        }
        presence(player);
    }

    static void presence(ServerPlayer player) {
        int rank = SoulService.get(player).soulRank().map(Enum::ordinal).orElse(0);
        ServerLevel level = player.serverLevel();
        boolean felt = false;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(PRESENCE_RADIUS),
                candidate -> candidate instanceof Enemy && candidate.isAlive())) {
            int mobRank = ShardDrops.rankOf(mob).map(value -> value.tier()).orElse(0);
            if (mobRank >= rank) {
                continue;
            }
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, PRESENCE_EVERY_TICKS + 10, 0, true, false));
            mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, PRESENCE_EVERY_TICKS + 10, 0, true, false));
            felt = true;
        }
        // A faint shimmer marks the Presence for everyone around, felt or not.
        level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.2, player.getZ(), felt ? 10 : 4,
                0.5, 0.7, 0.5, 0.4);
    }
}
