package dev.spud.shadowslave.creature;

import dev.spud.shadowslave.creature.goal.ClassBehaviours;
import dev.spud.shadowslave.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Optional;

/**
 * A Nightmare Creature (DESIGN): a mob with a Rank and a Class. Rank sets its
 * health, damage, armour and shard rank; Class sets how many shards it drops
 * (one per core) and adds its behaviour package from {@link ClassBehaviours}.
 * Each creature declares its stats at its own native Rank and Class, and a
 * copy can be moved to another, like the lesser spawn of a Tyrant.
 *
 * <p>Movement and fighting run on vanilla goals. Subclasses add their own
 * signature goals in {@link #registerSignatureGoals()} and their GeckoLib
 * controllers.</p>
 */
public abstract class NightmareCreature extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Byte> RANK = SynchedEntityData.defineId(NightmareCreature.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> CLASS = SynchedEntityData.defineId(NightmareCreature.class, EntityDataSerializers.BYTE);
    private static boolean alertingPack;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private boolean statsApplied;

    protected NightmareCreature(EntityType<? extends NightmareCreature> type, Level level) {
        super(type, level);
        this.entityData.set(RANK, (byte) nativeRank().ordinal());
        this.entityData.set(CLASS, (byte) nativeClass().ordinal());
        this.xpReward = 5;
    }

    /** The Rank this creature is authored at. */
    public abstract CreatureRank nativeRank();

    /** The Class this creature is authored at. */
    public abstract CreatureClass nativeClass();

    /** Health, damage, armour and speed at the native Rank and Class. */
    public abstract CreatureStats nativeStats();

    /** Attributes for the entity type, from a creature's native stats. */
    public static AttributeSupplier.Builder attributes(CreatureStats stats) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, stats.health())
                .add(Attributes.ATTACK_DAMAGE, stats.damage())
                .add(Attributes.ARMOR, stats.armour())
                .add(Attributes.MOVEMENT_SPEED, stats.speed())
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RANK, (byte) 0);
        builder.define(CLASS, (byte) 0);
    }

    public CreatureRank rank() {
        return CreatureRank.byOrdinal(this.entityData.get(RANK));
    }

    public CreatureClass creatureClass() {
        return CreatureClass.byOrdinal(this.entityData.get(CLASS));
    }

    /** Moves this creature to another Rank and Class and rescales its stats; health refills. */
    public void setRankAndClass(CreatureRank rank, CreatureClass creatureClass) {
        this.entityData.set(RANK, (byte) rank.ordinal());
        this.entityData.set(CLASS, (byte) creatureClass.ordinal());
        applyStats();
        setHealth(getMaxHealth());
    }

    /** "Awakened Monster" */
    public Component rankAndClassName() {
        return Component.literal(rank().displayName() + " " + creatureClass().displayName());
    }

    private void applyStats() {
        CreatureStats stats = nativeStats().scaled(nativeRank(), nativeClass(), rank(), creatureClass());
        setBase(Attributes.MAX_HEALTH, stats.health());
        setBase(Attributes.ATTACK_DAMAGE, stats.damage());
        setBase(Attributes.ARMOR, stats.armour());
        this.xpReward = 5 * (1 + rank().ordinal()) * creatureClass().cores();
        ClassBehaviours.configure(this);
        this.statsApplied = true;
    }

    private void setBase(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    /** Shrinks or grows the body, e.g. for a Tyrant's lesser spawn. */
    public void setBodyScale(double scale) {
        setBase(Attributes.SCALE, scale);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (!this.statsApplied) {
            applyStats();
            setHealth(getMaxHealth());
        }
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.statsApplied && !level().isClientSide) {
            applyStats();
        }
    }

    @Override
    protected final void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        registerSignatureGoals();
        ClassBehaviours.registerGoals(this, this.goalSelector, this.targetSelector);
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(NightmareCreature.class));
    }

    /** A creature's own goals, ahead of its Class package. Priorities 1 and 2 are free for them. */
    protected abstract void registerSignatureGoals();

    /**
     * A Devil's one supernatural power (DESIGN hook): called by the Devil
     * package on a cooldown while the creature has a target. Return whether it
     * was used. No creature in the first roster is a Devil yet.
     */
    public boolean useSignaturePower(LivingEntity target) {
        return false;
    }

    /** Whether the Class package may pick targets by sight; a creature with its own senses returns false. */
    public boolean usesSightTargeting() {
        return true;
    }

    /** Called when a Tyrant starts spawning lesser copies, for its animation and sound. */
    public void onSpawnLesserWindup() {
    }

    /** Beasts hunt in packs: when one finds prey, the same kind nearby join in. */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity previous = getTarget();
        super.setTarget(target);
        if (alertingPack || target == null || previous == target || creatureClass() != CreatureClass.BEAST) {
            return;
        }
        alertingPack = true;
        try {
            for (NightmareCreature packmate : level().getEntitiesOfClass(NightmareCreature.class,
                    getBoundingBox().inflate(16.0), other -> other != this && other.getType() == getType()
                            && other.getTarget() == null && other.isAlive())) {
                packmate.setTarget(target);
            }
        } finally {
            alertingPack = false;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("creature_rank", rank().getSerializedName());
        tag.putString("creature_class", creatureClass().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CreatureRank rank = parse(CreatureRank.values(), tag.getString("creature_rank")).orElse(nativeRank());
        CreatureClass creatureClass = parse(CreatureClass.values(), tag.getString("creature_class")).orElse(nativeClass());
        this.entityData.set(RANK, (byte) rank.ordinal());
        this.entityData.set(CLASS, (byte) creatureClass.ordinal());
        float health = getHealth();
        applyStats();
        setHealth(Math.min(health, getMaxHealth()));
    }

    private static <E extends Enum<E> & net.minecraft.util.StringRepresentable> Optional<E> parse(E[] values, String name) {
        for (E value : values) {
            if (value.getSerializedName().equals(name.toLowerCase(Locale.ROOT))) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /**
     * Breaks the creature's cores into shards where it died, one per core, and
     * tells the killer in the Spell's words (CANON format, ch. 1: "You have
     * slain ..."). Returns the number of shards dropped.
     */
    public int dropCoreShards(ServerLevel level, @Nullable ServerPlayer killer) {
        if (killer != null) {
            String rankAndClass = rank().displayName() + " " + creatureClass().displayName();
            String article = "AEIOU".indexOf(rankAndClass.charAt(0)) >= 0 ? "an " : "a ";
            killer.sendSystemMessage(Component.literal("You have slain " + article + rankAndClass + ", ")
                    .append(getType().getDescription()).append(".")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT) || rank().shard().isEmpty()) {
            return 0;
        }
        int shards = creatureClass().cores();
        for (int i = 0; i < shards; i++) {
            ItemEntity item = new ItemEntity(level, getX(), getY() + 0.5, getZ(),
                    new ItemStack(ModItems.shardFor(rank().shard().get())));
            item.setDeltaMovement(level.random.triangle(0.0, 0.15), 0.25, level.random.triangle(0.0, 0.15));
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
        }
        if (killer != null) {
            killer.sendSystemMessage(Component.literal(shards == 1 ? "Its soul shard falls free."
                            : "Its " + shards + " soul shards fall free.")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
        return shards;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
