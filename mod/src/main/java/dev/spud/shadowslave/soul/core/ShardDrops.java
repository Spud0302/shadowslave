package dev.spud.shadowslave.soul.core;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.item.ModDataComponents;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.item.ShardContents;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Optional;

/**
 * Soul shards from kills (DESIGN). Each hostile mob has a creature rank:
 * most are Dormant, tougher kinds (armoured mobs, illagers, Nether mobs) are
 * Awakened, a few elites are Fallen, and bosses are Corrupted. Entity type
 * tags under {@code shadowslave:soul_shard/} pick the rank, so a datapack can
 * retune it. Only kills by a player drop shards.
 *
 * <p>A player killed by another player leaves part of their core behind as a
 * shard the killer can absorb, unless the {@link #PVP_SHARDS} game rule is off.</p>
 */
public final class ShardDrops {
    public static final TagKey<EntityType<?>> AWAKENED = tag("awakened");
    public static final TagKey<EntityType<?>> FALLEN = tag("fallen");
    public static final TagKey<EntityType<?>> CORRUPTED = tag("corrupted");
    /** Types that always drop their shard, like mini-bosses. */
    public static final TagKey<EntityType<?>> ALWAYS = tag("always");

    public static final GameRules.Key<GameRules.BooleanValue> PVP_SHARDS = GameRules.register(
            "shadowslavePvpSoulShards", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

    static final float DORMANT_CHANCE = 0.12F;
    static final float AWAKENED_CHANCE = 0.25F;
    static final float FALLEN_CHANCE = 0.5F;

    private ShardDrops() {
    }

    /** Forces the game rule class to load during mod construction, before worlds read their rules. */
    public static void bootstrap() {
    }

    private static TagKey<EntityType<?>> tag(String rank) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "soul_shard/" + rank));
    }

    /** The creature rank of a mob for shard drops; empty for animals and other non-hostiles. */
    public static Optional<ShardRank> rankOf(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        if (type.is(CORRUPTED)) {
            return Optional.of(ShardRank.CORRUPTED);
        }
        if (type.is(FALLEN)) {
            return Optional.of(ShardRank.FALLEN);
        }
        if (type.is(AWAKENED) || (entity instanceof Enemy && wearsArmour(entity))) {
            return Optional.of(ShardRank.AWAKENED);
        }
        return entity instanceof Enemy ? Optional.of(ShardRank.DORMANT) : Optional.empty();
    }

    static float chance(LivingEntity entity, ShardRank rank) {
        if (entity.getType().is(ALWAYS)) {
            return 1.0F;
        }
        return switch (rank) {
            case DORMANT -> DORMANT_CHANCE;
            case AWAKENED -> AWAKENED_CHANCE;
            case FALLEN -> FALLEN_CHANCE;
            case CORRUPTED -> 1.0F;
        };
    }

    private static boolean wearsArmour(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor() && !entity.getItemBySlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || event.isCanceled()) {
            return;
        }
        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer player ? player
                : victim.getKillCredit() instanceof ServerPlayer player ? player : null;
        if (killer == null || killer == victim) {
            return;
        }
        if (victim instanceof ServerPlayer player) {
            dropPlayerShard(level, player, killer);
        } else {
            dropMobShard(level, victim, level.getRandom());
        }
    }

    /** Rolls a mob's shard and drops it where it died. Returns whether one dropped. */
    public static boolean dropMobShard(ServerLevel level, LivingEntity victim, RandomSource random) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            return false;
        }
        Optional<ShardRank> rank = rankOf(victim);
        if (rank.isEmpty() || random.nextFloat() >= chance(victim, rank.get())) {
            return false;
        }
        spawn(level, victim, new ItemStack(ModItems.shardFor(rank.get())));
        return true;
    }

    /** Tears a quarter of the victim's saturation out as a shard. Returns the stack, or empty if none dropped. */
    public static ItemStack dropPlayerShard(ServerLevel level, ServerPlayer victim, ServerPlayer killer) {
        if (!level.getGameRules().getBoolean(PVP_SHARDS)) {
            return ItemStack.EMPTY;
        }
        Optional<SoulRank> rank = CoreService.rank(victim);
        Optional<ShardRank> shardRank = rank.flatMap(ShardRank::ofSoulRank);
        int loss = CoreRules.pvpLoss(CoreService.saturation(victim));
        if (shardRank.isEmpty() || loss <= 0) {
            return ItemStack.EMPTY;
        }
        CoreService.set(victim, CoreService.saturation(victim) - loss);
        ItemStack stack = new ItemStack(ModItems.shardFor(shardRank.get()));
        stack.set(ModDataComponents.SHARD_CONTENTS.get(), new ShardContents(loss, victim.getScoreboardName()));
        spawn(level, victim, stack);
        victim.sendSystemMessage(Component.literal(killer.getScoreboardName() + " tore a shard from your core.")
                .withStyle(ChatFormatting.DARK_RED));
        killer.sendSystemMessage(Component.literal("A shard of " + victim.getScoreboardName() + "'s soul falls free.")
                .withStyle(ChatFormatting.DARK_PURPLE));
        return stack;
    }

    private static void spawn(ServerLevel level, LivingEntity at, ItemStack stack) {
        ItemEntity item = new ItemEntity(level, at.getX(), at.getY() + 0.5, at.getZ(), stack);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }
}
