package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.citadel.WarNatures;
import dev.spud.shadowslave.fieldguide.FieldGuideService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saboteur (Run I, clan wars): Line Sense shows other clans' supply lines,
 * Undermine lays a slow charge, Cut the Lines makes both faster, False Trail
 * hides your way and lays a false one, and Marked makes you glow after you
 * break an enemy's structure.
 */
public final class SaboteurCraft {
    static final double SENSE_RADIUS = 48.0;
    static final int SENSE_INTERVAL = 40;
    static final int FUSE_TICKS = 200;
    static final int MARKED_TICKS = 600;
    /** Blocks this hard or harder (obsidian and up) do not crack. */
    static final float MAX_HARDNESS = 50.0F;
    static final int TRAIL_TICKS = 1200;
    static final double TRAIL_LENGTH = 24.0;
    static final double TRAIL_LURE_RADIUS = 24.0;
    static final int TRAIL_LURE_TICKS = 160;

    /** A dark smudge of earth, for footprints on the false trail. */
    private static final net.minecraft.core.particles.DustParticleOptions FOOTPRINT =
            new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.22F, 0.15F, 0.08F), 1.6F);

    record Charge(ResourceKey<Level> level, BlockPos pos, net.minecraft.core.Direction face, long goes) {
    }

    record Trail(ResourceKey<Level> level, Vec3 from, Vec3 to, long until) {
    }

    private static final Map<UUID, Charge> CHARGES = new ConcurrentHashMap<>();
    private static final Map<UUID, Trail> TRAILS = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<UUID>> SENSED = new ConcurrentHashMap<>();

    private SaboteurCraft() {
    }

    static boolean isSaboteur(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.SABOTEUR.innateId());
    }

    /** Cut the Lines: anchors fall twice as fast to you, and your charges burn in half the time. */
    public static boolean cutsTheLines(ServerPlayer player) {
        return KitRules.hasInnate(player, KitLadder.CUT_THE_LINES.abilityId());
    }

    /** Marked: after breaking an enemy's structure, you glow. */
    public static void marked(ServerPlayer player) {
        if (!KitRules.hasFlaw(player, AspectKitCatalog.SABOTEUR.flawEffectId())) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, MARKED_TICKS, 0, false, false, true));
        AbilityService.feedback(player, "Marked: the Spell shows everyone what you broke.", ChatFormatting.RED);
    }

    // ---- Line Sense ------------------------------------------------------------------------------------------------

    /** Shows the Saboteur, and only them, the Link Anchors and Gateways of other clans nearby. */
    static void sense(ServerPlayer player) {
        List<WarNatures.Sensed> found = WarNatures.sense(player, SENSE_RADIUS);
        Set<UUID> before = SENSED.getOrDefault(player.getUUID(), Set.of());
        Set<UUID> now = new HashSet<>();
        ServerLevel level = player.serverLevel();
        for (WarNatures.Sensed sensed : found) {
            now.add(sensed.id());
            ParticleOptions mark = sensed.enemy() ? ParticleTypes.FLAME : ParticleTypes.ELECTRIC_SPARK;
            sensed.anchor().ifPresent(anchor -> column(level, player, anchor, mark, 6));
            column(level, player, sensed.gateway(), ParticleTypes.WAX_ON, 4);
            if (!before.contains(sensed.id())) {
                BlockPos target = sensed.anchor().orElse(sensed.gateway());
                String what = sensed.anchor().isPresent() ? "its Link Anchor" : "its Gateway";
                AbilityService.feedback(player, "You sense the supply line of " + sensed.name() + ": " + what + " lies "
                                + Math.round(Math.sqrt(target.distSqr(player.blockPosition()))) + " blocks " + direction(player, target) + ".",
                        sensed.enemy() ? ChatFormatting.RED : ChatFormatting.GOLD);
            }
        }
        if (now.isEmpty()) {
            SENSED.remove(player.getUUID());
        } else {
            SENSED.put(player.getUUID(), now);
        }
    }

    /** A column of particles over a block, sent to one player alone. */
    private static void column(ServerLevel level, ServerPlayer player, BlockPos pos, ParticleOptions particle, int height) {
        for (int i = 0; i < height; i++) {
            level.sendParticles(player, particle, true, pos.getX() + 0.5, pos.getY() + 1.0 + i * 0.8, pos.getZ() + 0.5,
                    2, 0.1, 0.2, 0.1, 0.0);
        }
    }

    static String direction(ServerPlayer player, BlockPos target) {
        double dx = target.getX() + 0.5 - player.getX();
        double dz = target.getZ() + 0.5 - player.getZ();
        String[] names = {"south", "south-west", "west", "north-west", "north", "north-east", "east", "south-east"};
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        return names[Math.floorMod(Math.round((float) (angle / 45.0)), 8)];
    }

    // ---- Undermine -------------------------------------------------------------------------------------------------

    static int fuse(ServerPlayer player) {
        return cutsTheLines(player) ? FUSE_TICKS / 2 : FUSE_TICKS;
    }

    static void plant(ServerPlayer player, BlockPos pos, net.minecraft.core.Direction face) {
        CHARGES.put(player.getUUID(), new Charge(player.level().dimension(), pos.immutable(), face, player.server.getTickCount() + fuse(player)));
    }

    /** Sets the player's charge off at once. */
    static Optional<WarNatures.Undermined> goNow(ServerPlayer player) {
        Charge charge = CHARGES.remove(player.getUUID());
        ServerLevel level = charge == null ? null : player.server.getLevel(charge.level());
        return level == null ? Optional.empty() : Optional.of(detonate(player, level, charge.pos()));
    }

    static Optional<BlockPos> charge(ServerPlayer player) {
        return Optional.ofNullable(CHARGES.get(player.getUUID())).map(Charge::pos);
    }

    /** Burns every charge down, and sets off those whose fuse is spent. */
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (CHARGES.isEmpty() && TRAILS.isEmpty()) {
            return;
        }
        long now = server.getTickCount();
        for (Map.Entry<UUID, Charge> entry : List.copyOf(CHARGES.entrySet())) {
            Charge charge = entry.getValue();
            ServerLevel level = server.getLevel(charge.level());
            if (level == null) {
                CHARGES.remove(entry.getKey());
                continue;
            }
            if (charge.goes() > now) {
                if (now % 5 == 0) {
                    // The fuse burns on the face it was set on, where anyone near can see it.
                    Vec3 at = Vec3.atCenterOf(charge.pos()).add(Vec3.atLowerCornerOf(charge.face().getNormal()).scale(0.6));
                    level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 3, 0.12, 0.12, 0.12, 0.01);
                    level.sendParticles(ParticleTypes.SMALL_FLAME, at.x, at.y, at.z, 2, 0.08, 0.08, 0.08, 0.0);
                    level.sendParticles(ParticleTypes.LAVA, at.x, at.y, at.z, 1, 0.05, 0.05, 0.05, 0.0);
                }
                if (now % 20 == 0) {
                    level.playSound(null, charge.pos(), SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 0.3F, 1.6F);
                }
                continue;
            }
            CHARGES.remove(entry.getKey());
            ServerPlayer owner = KitRules.online(server, entry.getKey());
            detonate(owner, level, charge.pos());
        }
        for (Map.Entry<UUID, Trail> entry : List.copyOf(TRAILS.entrySet())) {
            Trail trail = entry.getValue();
            ServerLevel level = server.getLevel(trail.level());
            if (level == null || trail.until() <= now) {
                TRAILS.remove(entry.getKey());
                continue;
            }
            if (now % 20 == 0) {
                footprints(level, trail);
            }
        }
    }

    /** The charge goes: on a Citadel's land the war decides what it does; in the wild it cracks the stone around it. */
    static WarNatures.Undermined detonate(ServerPlayer owner, ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8F, 1.3F);
        level.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.3, 0.3, 0.3, 0.0);
        if (owner == null) {
            return WarNatures.Undermined.HELD;
        }
        WarNatures.Undermined result = WarNatures.undermine(owner, level, pos);
        switch (result) {
            case WILD -> {
                int cracked = crack(level, owner, pos);
                AbilityService.feedback(owner, cracked == 0 ? "Your charge goes off, but nothing here cracks."
                        : "Your charge cracks " + cracked + (cracked == 1 ? " block." : " blocks."), ChatFormatting.GOLD);
            }
            case HELD -> AbilityService.feedback(owner, "The Spell holds that ground: your charge fizzles.", ChatFormatting.GRAY);
            case DEFUSED -> AbilityService.feedback(owner, "Your charge was stamped out by those who keep that ground.", ChatFormatting.GRAY);
            case CUT, RAIDED -> marked(owner);
        }
        return result;
    }

    /** Breaks the blocks within a block of the charge, as the Saboteur breaking them, up to obsidian's hardness. */
    static int crack(ServerLevel level, ServerPlayer player, BlockPos centre) {
        int cracked = 0;
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-1, -1, -1), centre.offset(1, 1, 1))) {
            if (pos.distSqr(centre) > 2) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            float hardness = state.getDestroySpeed(level, pos);
            if (state.isAir() || !state.getFluidState().isEmpty() && state.getFluidState().isSource()
                    || hardness < 0 || hardness >= MAX_HARDNESS || !player.mayInteract(level, pos)) {
                continue;
            }
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos.immutable(), state, player);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                continue;
            }
            if (level.destroyBlock(pos.immutable(), true, player)) {
                cracked++;
            }
        }
        return cracked;
    }

    // ---- False Trail -----------------------------------------------------------------------------------------------

    /** False Trail: hide your way for a minute and lay a false one where you look. */
    static Vec3 layTrail(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle().multiply(1.0, 0.0, 1.0);
        look = look.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : look.normalize();
        Vec3 from = player.position();
        Vec3 flat = from.add(look.scale(TRAIL_LENGTH));
        Vec3 to = new Vec3(flat.x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(flat.x), (int) Math.floor(flat.z)), flat.z);
        Trail trail = new Trail(level.dimension(), from, to, player.server.getTickCount() + TRAIL_TICKS);
        TRAILS.put(player.getUUID(), trail);
        FieldGuideService.layFalseTrail(player, from, to);
        footprints(level, trail);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(player.blockPosition()).inflate(TRAIL_LURE_RADIUS),
                mob -> mob.isAlive() && mob instanceof Enemy && mob.getTarget() == player)) {
            MasqueFace.lure(mob, to, TRAIL_LURE_TICKS);
        }
        return to;
    }

    /** Whether the player is on a False Trail, leaving no path behind them. */
    public static boolean hidden(ServerPlayer player) {
        Trail trail = TRAILS.get(player.getUUID());
        return trail != null && trail.until() > player.server.getTickCount();
    }

    /** Footprints along the false trail, for anyone to see. */
    private static void footprints(ServerLevel level, Trail trail) {
        Vec3 step = trail.to().subtract(trail.from());
        int steps = (int) Math.ceil(step.horizontalDistance() / 1.5);
        for (int i = 1; i <= steps; i++) {
            Vec3 at = trail.from().add(step.scale(i / (double) steps));
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(at.x), (int) Math.floor(at.z));
            double side = (i % 2 == 0 ? 0.2 : -0.2);
            level.sendParticles(FOOTPRINT, at.x + side, y + 0.1, at.z, 4, 0.08, 0.0, 0.08, 0.0);
        }
    }

    // ---- ticks and cleanup -----------------------------------------------------------------------------------------

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % SENSE_INTERVAL != 0) {
            return;
        }
        if (isSaboteur(player)) {
            sense(player);
        }
    }

    static void forget(UUID player) {
        CHARGES.remove(player);
        TRAILS.remove(player);
        SENSED.remove(player);
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        CHARGES.clear();
        TRAILS.clear();
        SENSED.clear();
    }
}
