package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

/**
 * One of the Awakened who live in an NPC clan's Citadel (DESIGN, Jack
 * 2026-10-31; CANON, ch. 398: residents keep watch on the walls and share
 * the work). A guard fights Nightmare Creatures and anyone the clan has
 * turned against; a resident goes about the courtyard; the trader deals in
 * soul shards; the lord is the Saint who binds the Citadel. They only stand
 * in the world while a player is near: {@link NpcCitadels} counts the rest.
 */
public final class CitadelResident extends PathfinderMob implements Merchant {
    /** What a resident does in their Citadel. */
    public enum Role {
        GUARD, RESIDENT, TRADER, LORD;

        static Role byId(int id) {
            return id >= 0 && id < values().length ? values()[id] : RESIDENT;
        }
    }

    private static final EntityDataAccessor<Integer> ROLE = SynchedEntityData.defineId(CitadelResident.class, EntityDataSerializers.INT);

    private UUID citadel;
    private UUID clan;
    private BlockPos post;
    private Player trading;
    private MerchantOffers offers;

    public CitadelResident(EntityType<? extends CitadelResident> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setDropChance(slot, 0.0F);
        }
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    /** Makes a resident of {@code role} for a held Citadel, dressed in its clan's colour and posted at {@code post}. */
    static CitadelResident create(ServerLevel level, CitadelData.Citadel citadel, NpcClanData.NpcClan clan, Role role, String name, BlockPos post) {
        CitadelResident resident = CitadelRegistry.CITADEL_RESIDENT.get().create(level);
        if (resident == null) {
            throw new IllegalStateException("Could not create a Citadel resident");
        }
        resident.entityData.set(ROLE, role.ordinal());
        resident.citadel = citadel.id;
        resident.clan = clan.id;
        resident.post = post.immutable();
        resident.dress(role, clan.colour);
        if (role == Role.LORD) {
            resident.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
            resident.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(14.0);
            resident.getAttribute(Attributes.ARMOR).setBaseValue(16.0);
        } else if (role == Role.GUARD) {
            resident.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0);
            resident.getAttribute(Attributes.ARMOR).setBaseValue(10.0);
        }
        resident.setHealth(resident.getMaxHealth());
        resident.setCustomName(title(role, name, clan));
        resident.setCustomNameVisible(role == Role.LORD || role == Role.TRADER);
        resident.moveTo(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        resident.restrictTo(role == Role.RESIDENT || role == Role.TRADER ? citadel.gateway : post, roam(role));
        return resident;
    }

    static Component title(Role role, String name, NpcClanData.NpcClan clan) {
        String text = switch (role) {
            case LORD -> "Saint " + name + " of " + clan.name;
            case GUARD -> name + ", Guard of " + clan.name;
            case TRADER -> name + ", Trader";
            case RESIDENT -> name + " of " + clan.name;
        };
        return Component.literal(text).withStyle(role == Role.LORD ? ChatFormatting.GOLD : ChatFormatting.WHITE);
    }

    private static int roam(Role role) {
        return switch (role) {
            case LORD -> 3;
            case GUARD -> 6;
            case TRADER -> 6;
            case RESIDENT -> 9;
        };
    }

    private void dress(Role role, DyeColor colour) {
        ItemStack tunic = new ItemStack(Items.LEATHER_CHESTPLATE);
        tunic.set(DataComponents.DYED_COLOR, new DyedItemColor(colour.getTextureDiffuseColor(), false));
        setItemSlot(EquipmentSlot.CHEST, tunic);
        switch (role) {
            case GUARD -> {
                setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
            }
            case LORD -> {
                setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            }
            case TRADER -> setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.DORMANT_SOUL_SHARD.get()));
            case RESIDENT -> {
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ROLE, Role.RESIDENT.ordinal());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1, true) {
            @Override
            public boolean canUse() {
                return role() != Role.RESIDENT && role() != Role.TRADER && super.canUse();
            }
        });
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, NightmareCreature.class, 10.0F, 0.8, 1.1) {
            @Override
            public boolean canUse() {
                return (role() == Role.RESIDENT || role() == Role.TRADER) && super.canUse();
            }
        });
        goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6) {
            @Override
            public boolean canUse() {
                return role() != Role.LORD && super.canUse();
            }
        });
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(CitadelResident.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, NightmareCreature.class, true) {
            @Override
            public boolean canUse() {
                return role() == Role.GUARD && super.canUse();
            }
        });
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                target -> target instanceof ServerPlayer player && clan != null && NpcCitadels.hostile(player, clan)) {
            @Override
            public boolean canUse() {
                return role() == Role.GUARD && super.canUse();
            }
        });
    }

    public Role role() {
        return Role.byId(entityData.get(ROLE));
    }

    public Optional<UUID> citadel() {
        return Optional.ofNullable(citadel);
    }

    public Optional<UUID> clan() {
        return Optional.ofNullable(clan);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && post != null && !hasRestriction()) {
            restrictTo(post, roam(role()));
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer server) || clan == null) {
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (NpcCitadels.hostile(server, clan)) {
            server.displayClientMessage(Component.literal(getName().getString() + " will not speak with you.").withStyle(ChatFormatting.DARK_RED), true);
            return InteractionResult.CONSUME;
        }
        if (role() == Role.TRADER && hand == InteractionHand.MAIN_HAND && trading == null && isAlive()) {
            setTradingPlayer(server);
            openTradingScreen(server, getName(), 0);
            return InteractionResult.CONSUME;
        }
        NpcCitadels.greet(server, this);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide && source.getEntity() instanceof ServerPlayer player && clan != null) {
            NpcCitadels.struck(player, this, isDeadOrDying());
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel server) {
            NpcCitadels.lost(server, this);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // Trading (CANON, ch. 1599: soul shards were the Citadels' first coin)

    @Override
    public void setTradingPlayer(Player player) {
        trading = player;
    }

    @Override
    public Player getTradingPlayer() {
        return trading;
    }

    @Override
    public MerchantOffers getOffers() {
        if (offers == null) {
            offers = new MerchantOffers();
            var shard = ModItems.DORMANT_SOUL_SHARD.get();
            offers.add(new MerchantOffer(new ItemCost(shard, 2), new ItemStack(Items.COOKED_MUTTON, 4), 12, 1, 0.05F));
            offers.add(new MerchantOffer(new ItemCost(shard, 3), new ItemStack(Items.ARROW, 16), 8, 1, 0.05F));
            offers.add(new MerchantOffer(new ItemCost(shard, 4), new ItemStack(Items.SOUL_LANTERN, 2), 8, 1, 0.05F));
            offers.add(new MerchantOffer(new ItemCost(shard, 6), new ItemStack(Items.SHIELD), 4, 1, 0.05F));
            offers.add(new MerchantOffer(new ItemCost(Items.IRON_INGOT, 6), new ItemStack(shard, 2), 12, 1, 0.05F));
            offers.add(new MerchantOffer(new ItemCost(Items.LEATHER, 8), new ItemStack(shard, 1), 12, 1, 0.05F));
            offers.add(new MerchantOffer(new ItemCost(Items.COAL, 16), new ItemStack(shard, 1), 12, 1, 0.05F));
        }
        return offers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        if (trading instanceof ServerPlayer player && clan != null) {
            NpcCitadels.traded(player, clan);
        }
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return level().isClientSide;
    }

    // Saving

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("role", role().ordinal());
        if (citadel != null) {
            tag.putUUID("citadel", citadel);
        }
        if (clan != null) {
            tag.putUUID("clan", clan);
        }
        if (post != null) {
            tag.put("post", NbtUtils.writeBlockPos(post));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(ROLE, tag.getInt("role"));
        citadel = tag.hasUUID("citadel") ? tag.getUUID("citadel") : null;
        clan = tag.hasUUID("clan") ? tag.getUUID("clan") : null;
        post = NbtUtils.readBlockPos(tag, "post").orElse(null);
    }
}
