package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The always-on halves of the Awakening-inspired Natures, each built on one
 * signature rule: Gloam grows stronger the darker it is (Deepening Dark,
 * Sunstruck), and Rime's Frost Marks keep working after the hit (Frost Mark,
 * Thin Blood). Fury's rage meter lives in {@link FuryRage}.
 */
public final class SignatureKitPassives {
    /** Light levels (sky dimmed by the time of day, or a block's own light) that count as dark and as dim. */
    static final int DARK_LIGHT = 4;
    static final int DIM_LIGHT = 10;
    /** Deepening Dark: in the dark, extra melee damage and a faster refill. */
    static final float DARK_BLOW = 2.0F;
    static final float DARK_REGEN = 1.5F;
    /** Frost Mark: at most five stacks; each bites for half a heart-point a second and one melts off every few seconds. */
    static final int MAX_MARKS = 5;
    static final float MARK_DAMAGE = 0.5F;
    static final int MARK_MELT_TICKS = 60;
    /** Thin Blood: how near heat stops the refill (and melts marks twice as fast), and how near it burns. */
    static final double HEAT_NEAR = 6.0;
    static final double HEAT_CLOSE = 2.0;
    static final float HEAT_BURN = 1.0F;

    enum Light { DARK, DIM, BRIGHT }

    enum Heat { NONE, NEAR, CLOSE }

    record Mark(ResourceKey<Level> dimension, UUID owner, int stacks, long meltsAt) {
    }

    private static final Map<UUID, Mark> MARKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Heat> HEAT = new ConcurrentHashMap<>();

    private SignatureKitPassives() {
    }

    // ---- Gloam ---------------------------------------------------------------------------------------------------

    /** How dark it is at a spot, with the sky dimmed by the time of day and the weather. */
    static Light lightAt(Level level, BlockPos pos) {
        int light = level.getMaxLocalRawBrightness(pos);
        return light <= DARK_LIGHT ? Light.DARK : light <= DIM_LIGHT ? Light.DIM : Light.BRIGHT;
    }

    static Light lightAt(LivingEntity entity) {
        return lightAt(entity.level(), BlockPos.containing(entity.getEyePosition()));
    }

    /** Sunstruck: daylight falling straight on you, with no cloud or roof between. */
    public static boolean sunstruck(ServerPlayer player) {
        Level level = player.level();
        return level.dimensionType().hasSkyLight() && level.isDay() && !level.isRaining()
                && level.canSeeSky(BlockPos.containing(player.getEyePosition()));
    }

    /** The refill share from these kits' rules: Deepening Dark, Sunstruck and Thin Blood. */
    public static float regenFactor(ServerPlayer player) {
        float factor = 1.0F;
        if (KitRules.hasFlaw(player, AspectKitCatalog.GLOAM.flawEffectId()) && sunstruck(player)) {
            return 0.0F;
        }
        if (KitRules.hasInnate(player, AspectKitCatalog.GLOAM.innateId()) && lightAt(player) == Light.DARK) {
            factor *= DARK_REGEN;
        }
        if (heat(player) != Heat.NONE && KitRules.hasFlaw(player, AspectKitCatalog.RIME.flawEffectId())) {
            return 0.0F;
        }
        return factor;
    }

    // ---- Rime ----------------------------------------------------------------------------------------------------

    /** Thin Blood: how close the nearest heat is, as last measured (once a second). */
    static Heat heat(ServerPlayer player) {
        return HEAT.getOrDefault(player.getUUID(), Heat.NONE);
    }

    /** Looks for fire, lava, magma, a lit campfire or a lit furnace around the player. */
    static Heat measureHeat(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos centre = BlockPos.containing(player.position().add(0, 0.5, 0));
        int reach = (int) Math.ceil(HEAT_NEAR);
        double nearest = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-reach, -reach, -reach), centre.offset(reach, reach, reach))) {
            if (hot(level.getBlockState(pos)) || level.getFluidState(pos).is(FluidTags.LAVA)) {
                nearest = Math.min(nearest, pos.getCenter().distanceTo(player.position().add(0, 0.5, 0)));
            }
        }
        return nearest <= HEAT_CLOSE + 0.5 ? Heat.CLOSE : nearest <= HEAT_NEAR + 0.5 ? Heat.NEAR : Heat.NONE;
    }

    static boolean hot(BlockState state) {
        if (state.is(BlockTags.FIRE) || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.LAVA)) {
            return true;
        }
        if (state.getBlock() instanceof CampfireBlock || state.getBlock() instanceof AbstractFurnaceBlock) {
            return state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT);
        }
        return false;
    }

    /** Puts {@code stacks} more Frost Marks on a creature, up to five, and resets the melt. */
    static void mark(ServerPlayer owner, LivingEntity target, int stacks) {
        if (!target.isAlive() || target == owner) {
            return;
        }
        Mark old = MARKS.get(target.getUUID());
        int total = Math.min(MAX_MARKS, (old == null ? 0 : old.stacks()) + stacks);
        MARKS.put(target.getUUID(), new Mark(target.level().dimension(), owner.getUUID(), total,
                target.level().getGameTime() + meltTicks(owner)));
        chill(target, total);
        ((ServerLevel) target.level()).sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + target.getBbHeight() * 0.6,
                target.getZ(), 4 + 2 * stacks, 0.3, 0.3, 0.3, 0.02);
    }

    public static int marks(Entity entity) {
        Mark mark = MARKS.get(entity.getUUID());
        return mark == null ? 0 : mark.stacks();
    }

    /** Marks melt twice as fast while their Rime stands near heat (Thin Blood). */
    private static int meltTicks(ServerPlayer owner) {
        return heat(owner) != Heat.NONE && KitRules.hasFlaw(owner, AspectKitCatalog.RIME.flawEffectId())
                ? MARK_MELT_TICKS / 2 : MARK_MELT_TICKS;
    }

    /** Slows a marked creature, more for each mark, and frosts it over. */
    private static void chill(LivingEntity target, int stacks) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, stacks - 1, false, false));
        target.setTicksFrozen(Math.max(target.getTicksFrozen(), Math.min(target.getTicksRequiredToFreeze(), 30 * stacks)));
    }

    // ---- Events --------------------------------------------------------------------------------------------------

    /** Deepening Dark and Gloam Edge add to melee blows; frozen (Encase) creatures land none. */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Mob attacker && EncaseAbility.encased(attacker)) {
            event.setCanceled(true);
            return;
        }
        LivingEntity target = event.getEntity();
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || target == player) {
            return;
        }
        if (KitRules.hasInnate(player, AspectKitCatalog.GLOAM.innateId()) && lightAt(player) == Light.DARK) {
            event.setAmount(event.getAmount() + DARK_BLOW);
        }
        if (GloamEdgeAbility.active(player)) {
            event.setAmount(event.getAmount() + GloamEdgeAbility.cut(player, target));
        }
    }

    /** Frost Mark: a melee blow that lands leaves a mark. */
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (event.getNewDamage() > 0 && event.getSource().is(DamageTypes.PLAYER_ATTACK)
                && event.getSource().getEntity() instanceof ServerPlayer player && event.getEntity() != player
                && KitRules.hasInnate(player, AspectKitCatalog.RIME.innateId())) {
            mark(player, event.getEntity(), 1);
        }
    }

    /** Once a second for Rimes with Thin Blood: measure the heat around them, and burn them if it is close. */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        if (!KitRules.hasFlaw(player, AspectKitCatalog.RIME.flawEffectId())) {
            HEAT.remove(player.getUUID());
            return;
        }
        Heat heat = measureHeat(player);
        HEAT.put(player.getUUID(), heat);
        if (heat == Heat.CLOSE && player.tickCount % 40 == 0) {
            player.hurt(player.damageSources().inFire(), HEAT_BURN);
        }
    }

    /** Encase holds its prisoners; Frost Marks bite once a second and melt away one at a time. */
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        EncaseAbility.tickShells(server);
        if (MARKS.isEmpty()) {
            return;
        }
        boolean bite = server.getTickCount() % 20 == 0;
        for (Iterator<Map.Entry<UUID, Mark>> it = MARKS.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Mark> entry = it.next();
            Mark mark = entry.getValue();
            ServerLevel level = server.getLevel(mark.dimension());
            Entity entity = level == null ? null : level.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
                it.remove();
                continue;
            }
            long now = level.getGameTime();
            if (mark.meltsAt() <= now) {
                if (mark.stacks() <= 1) {
                    it.remove();
                    continue;
                }
                ServerPlayer owner = server.getPlayerList().getPlayer(mark.owner());
                mark = new Mark(mark.dimension(), mark.owner(), mark.stacks() - 1,
                        now + (owner == null ? MARK_MELT_TICKS : meltTicks(owner)));
                entry.setValue(mark);
            }
            if (bite) {
                chill(living, mark.stacks());
                living.hurt(level.damageSources().freeze(), MARK_DAMAGE * mark.stacks());
                // The bite should not shield the creature from the next real blow.
                living.invulnerableTime = 0;
                level.sendParticles(ParticleTypes.SNOWFLAKE, living.getX(), living.getY() + living.getBbHeight() * 0.6, living.getZ(),
                        mark.stacks(), 0.25, 0.3, 0.25, 0.01);
            }
        }
    }

    /** Puts back every light Snuff put out, so a shutdown never loses torches. */
    public static void onServerStopping(ServerStoppingEvent event) {
        SnuffAbility.restoreAll(event.getServer());
        EncaseAbility.thawAll(event.getServer());
    }

    public static void forget(UUID player) {
        HEAT.remove(player);
    }

    /** Clears every Frost Mark, for tests. */
    static void clearMarks() {
        MARKS.clear();
    }

    /** Sets the measured heat directly, for tests. */
    static void heat(ServerPlayer player, Heat heat) {
        HEAT.put(player.getUUID(), heat);
    }
}
