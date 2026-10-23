package dev.spud.shadowslave.gate;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.creature.NightmareRules;
import dev.spud.shadowslave.creature.goal.SpawnLesserCopiesGoal;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Runs Nightmare Gates (DESIGN, after ch. 484). {@link #open} is the one
 * entry point: the Gate Lure, the operator command and, later, natural
 * world events all start a Gate the same way. A Gate warns nearby players
 * with a countdown and the Category forecast, opens at a rolled Category,
 * pours out waves, then sends out its Guardian; killing the Guardian closes
 * it and the creatures it let out crumble.
 *
 * <p>A Gate only advances while a player is within {@link #ACTIVE_RANGE}, so
 * nobody comes back to a finished fight they never saw.</p>
 */
public final class GateService {
    public static final String GATE_TAG = "shadowslave_gate";
    static final double WARN_RANGE = 96.0;
    static final double ACTIVE_RANGE = 160.0;
    static final double MIN_SPACING = 64.0;
    static final int SPAWN_RADIUS = 6;
    static final int MAX_WAVE_OVERTIME = 90 * 20;
    static final Map<String, Supplier<EntityType<? extends NightmareCreature>>> CREATURES = Map.of(
            "ash_burrower", NightmareCreatureEntities.ASH_BURROWER::get,
            "chainback", NightmareCreatureEntities.CHAINBACK::get,
            "hollow_mimic", NightmareCreatureEntities.HOLLOW_MIMIC::get);

    private static final Map<UUID, ServerBossEvent> BARS = new HashMap<>();

    private GateService() {
    }

    /**
     * Starts a Gate at {@code center} (the block the rift stands on). Returns
     * empty if another Gate is already open within {@link #MIN_SPACING}.
     */
    public static Optional<GateEvent> open(ServerLevel level, BlockPos center, GateTrigger trigger, GateForecast forecast) {
        if (!canOpen(level, center)) {
            return Optional.empty();
        }
        GateData data = GateData.get(level);
        GateEvent gate = new GateEvent(UUID.randomUUID(), center, trigger, forecast);
        data.gates.add(gate);
        data.setDirty();
        ensureRift(level, gate);
        level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.5F, 0.5F);
        for (ServerPlayer player : playersNear(level, gate, WARN_RANGE)) {
            spell(player, Component.literal("A Nightmare Gate is opening nearby.").withStyle(ChatFormatting.DARK_PURPLE));
            spell(player, Component.literal("Forecast: " + forecast.describe() + "."));
            spell(player, Component.literal("It opens in " + GateEvent.WARNING_TICKS / 20 + " seconds."));
            title(player, Component.literal("Nightmare Gate").withStyle(ChatFormatting.DARK_PURPLE),
                    Component.literal("Opens in " + GateEvent.WARNING_TICKS / 20 + " seconds · " + forecast.describeLikeliest())
                            .withStyle(ChatFormatting.GRAY));
        }
        return Optional.of(gate);
    }

    /** Whether a Gate may open here: no other Gate within {@link #MIN_SPACING}. */
    public static boolean canOpen(ServerLevel level, BlockPos center) {
        return GateData.get(level).gates.stream()
                .noneMatch(other -> other.center.distSqr(center) < MIN_SPACING * MIN_SPACING);
    }

    /** Closes a Gate at once, as if its Guardian fell; for operators. */
    public static boolean close(ServerLevel level, UUID id) {
        for (GateEvent gate : GateData.get(level).gates) {
            if (gate.id.equals(id)) {
                finish(level, gate, false);
                return true;
            }
        }
        return false;
    }

    public static Optional<GateEvent> nearest(ServerLevel level, BlockPos pos) {
        return GateData.get(level).gates.stream()
                .min((a, b) -> Double.compare(a.center.distSqr(pos), b.center.distSqr(pos)));
    }

    /**
     * A Seer's Foresight on the nearest Gate within {@code range}: a Gate
     * still in its warning has its Category rolled now, so what the Seer sees
     * is what opens. Returns the Gate, if one was in reach.
     */
    public static Optional<GateEvent> foresee(ServerLevel level, BlockPos from, double range) {
        Optional<GateEvent> gate = nearest(level, from)
                .filter(found -> found.center.distSqr(from) <= range * range && found.phase != GateEvent.Phase.CLOSED);
        gate.ifPresent(found -> {
            if (found.phase == GateEvent.Phase.WARNING && !found.foreseen) {
                found.category = found.forecast.roll(level.getRandom()::nextInt);
                found.foreseen = true;
                GateData.get(level).setDirty();
            }
        });
        return gate;
    }

    /** The creatures still to come out of a Gate, wave by wave, then its Guardian, as lines of text. */
    public static List<String> roster(GateEvent gate) {
        List<List<GateCategory.Spawn>> waves = GateCategory.waves(gate.category);
        List<String> lines = new java.util.ArrayList<>();
        int first = gate.phase == GateEvent.Phase.WARNING ? 0 : Math.min(gate.wave, waves.size());
        for (int i = first; i < waves.size(); i++) {
            String wave = waves.get(i).stream()
                    .map(spawn -> spawn.count() + " " + creatureName(spawn.creature()) + " (" + spawn.rank().displayName()
                            + " " + spawn.creatureClass().displayName() + ")")
                    .collect(java.util.stream.Collectors.joining(", "));
            lines.add("Wave " + (i + 1) + ": " + wave);
        }
        if (gate.phase != GateEvent.Phase.GUARDIAN) {
            lines.add("Guardian: Ashen Tyrant (" + GateCategory.guardianRank(gate.category).displayName() + " Tyrant)");
        }
        return lines;
    }

    private static String creatureName(String key) {
        String[] words = key.split("_");
        StringBuilder name = new StringBuilder();
        for (String word : words) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            GateData data = GateData.get(level);
            if (data.gates.isEmpty()) {
                continue;
            }
            Iterator<GateEvent> gates = data.gates.iterator();
            while (gates.hasNext()) {
                GateEvent gate = gates.next();
                tick(level, gate);
                if (gate.phase == GateEvent.Phase.CLOSED) {
                    gates.remove();
                    ServerBossEvent bar = BARS.remove(gate.id);
                    if (bar != null) {
                        bar.removeAllPlayers();
                    }
                }
            }
            data.setDirty();
        }
    }

    /** Advances one Gate by a tick. Package-private for game tests. */
    static void tick(ServerLevel level, GateEvent gate) {
        List<ServerPlayer> near = playersNear(level, gate, ACTIVE_RANGE);
        ServerBossEvent bar = BARS.computeIfAbsent(gate.id, id -> new ServerBossEvent(Component.empty(),
                BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS));
        for (ServerPlayer player : List.copyOf(bar.getPlayers())) {
            if (!near.contains(player) || gate.phase == GateEvent.Phase.GUARDIAN) {
                bar.removePlayer(player);
            }
        }
        if (near.isEmpty() || !level.isLoaded(gate.center)) {
            return;
        }
        if (gate.phase != GateEvent.Phase.GUARDIAN) {
            near.forEach(bar::addPlayer);
        }
        ensureRift(level, gate);
        gate.phaseTicks++;
        switch (gate.phase) {
            case WARNING -> tickWarning(level, gate, bar, near);
            case OPEN -> tickOpen(level, gate, bar);
            case GUARDIAN -> {
                if (gate.guardian != null && level.getEntity(gate.guardian) == null && gate.phaseTicks > 40
                        && level.isLoaded(gate.center)) {
                    // The Guardian is gone without dying (unloaded or removed): send another.
                    spawnGuardian(level, gate);
                }
            }
            case CLOSED -> {
            }
        }
    }

    private static void tickWarning(ServerLevel level, GateEvent gate, ServerBossEvent bar, List<ServerPlayer> near) {
        int left = Math.max(0, GateEvent.WARNING_TICKS - gate.phaseTicks);
        int seconds = (left + 19) / 20;
        bar.setColor(BossEvent.BossBarColor.PURPLE);
        bar.setProgress(left / (float) GateEvent.WARNING_TICKS);
        bar.setName(Component.literal("Nightmare Gate · opens in " + seconds + "s · " + (gate.foreseen ? "Category " + gate.category + " (foreseen)" : gate.forecast.describeLikeliest())));
        if (left % 20 == 0 && (seconds == 20 || seconds == 10 || seconds <= 5) && seconds > 0) {
            for (ServerPlayer player : near) {
                player.displayClientMessage(Component.literal("The Gate opens in " + seconds + "...")
                        .withStyle(ChatFormatting.DARK_PURPLE), true);
            }
            level.playSound(null, gate.center, SoundEvents.NOTE_BLOCK_BASEDRUM.value(), SoundSource.HOSTILE, 2.0F, 0.5F);
        }
        if (left > 0) {
            return;
        }
        if (!gate.foreseen) {
            gate.category = gate.forecast.roll(level.getRandom()::nextInt);
        }
        gate.phase = GateEvent.Phase.OPEN;
        gate.phaseTicks = 0;
        gate.wave = 0;
        gate.nextWaveIn = 40;
        setRiftPhase(level, gate);
        level.playSound(null, gate.center, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5F, 0.6F);
        if (NightmareRules.blockDamage(level)) {
            Vec3 at = Vec3.atBottomCenterOf(gate.center.above());
            level.explode(null, at.x, at.y, at.z, 2.5F, false, Level.ExplosionInteraction.BLOCK);
        } else {
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, gate.center.getX() + 0.5, gate.center.getY() + 1.0,
                    gate.center.getZ() + 0.5, 1, 0, 0, 0, 0);
        }
        for (ServerPlayer player : playersNear(level, gate, WARN_RANGE)) {
            spell(player, Component.literal("The Nightmare Gate has opened. Category " + gate.category + ".")
                    .withStyle(ChatFormatting.DARK_RED));
            title(player, Component.literal("Category " + gate.category).withStyle(ChatFormatting.DARK_RED),
                    Component.literal("The Gate has opened").withStyle(ChatFormatting.GRAY));
        }
    }

    private static void tickOpen(ServerLevel level, GateEvent gate, ServerBossEvent bar) {
        List<List<GateCategory.Spawn>> waves = GateCategory.waves(gate.category);
        bar.setColor(BossEvent.BossBarColor.RED);
        bar.setProgress(1.0F - (float) gate.wave / waves.size());
        bar.setName(Component.literal("Nightmare Gate · Category " + gate.category + " · wave "
                + Math.max(1, gate.wave) + " of " + waves.size()));
        if (gate.wave < waves.size()) {
            if (--gate.nextWaveIn <= 0) {
                for (GateCategory.Spawn spawn : waves.get(gate.wave)) {
                    for (int i = 0; i < spawn.count(); i++) {
                        spawnCreature(level, gate, CREATURES.get(spawn.creature()).get(), spawn.rank(), spawn.creatureClass());
                    }
                }
                gate.wave++;
                gate.nextWaveIn = GateCategory.waveInterval(gate.category);
                level.playSound(null, gate.center, SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 1.5F, 0.8F);
            }
            return;
        }
        gate.spawned.removeIf(uuid -> !(level.getEntity(uuid) instanceof LivingEntity living) || !living.isAlive());
        if (gate.spawned.size() <= 1 || gate.phaseTicks > waves.size() * GateCategory.waveInterval(gate.category) + MAX_WAVE_OVERTIME) {
            spawnGuardian(level, gate);
            gate.phase = GateEvent.Phase.GUARDIAN;
            gate.phaseTicks = 0;
            setRiftPhase(level, gate);
            for (ServerPlayer player : playersNear(level, gate, WARN_RANGE)) {
                spell(player, Component.literal("The Gate Guardian emerges.").withStyle(ChatFormatting.DARK_RED));
            }
        }
    }

    private static void spawnGuardian(ServerLevel level, GateEvent gate) {
        NightmareCreature guardian = spawnCreature(level, gate, NightmareCreatureEntities.ASHEN_TYRANT.get(),
                GateCategory.guardianRank(gate.category), CreatureClass.TYRANT);
        if (guardian != null) {
            gate.guardian = guardian.getUUID();
            guardian.getPersistentData().putBoolean("shadowslave_guardian", true);
            level.playSound(null, gate.center, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.0F, 0.6F);
        }
    }

    private static NightmareCreature spawnCreature(ServerLevel level, GateEvent gate, EntityType<? extends NightmareCreature> type,
                                                   dev.spud.shadowslave.creature.CreatureRank rank, CreatureClass creatureClass) {
        NightmareCreature creature = type.create(level);
        if (creature == null) {
            return null;
        }
        double angle = level.getRandom().nextDouble() * Math.PI * 2;
        double distance = 2.0 + level.getRandom().nextDouble() * (SPAWN_RADIUS - 2);
        int x = (int) Math.floor(gate.center.getX() + 0.5 + Math.cos(angle) * distance);
        int z = (int) Math.floor(gate.center.getZ() + 0.5 + Math.sin(angle) * distance);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (Math.abs(y - gate.center.getY()) > 6) {
            y = gate.center.getY() + 1;
            x = gate.center.getX();
            z = gate.center.getZ();
        }
        creature.moveTo(x + 0.5, y, z + 0.5, (float) Math.toDegrees(angle) - 90.0F, 0);
        creature.finalizeSpawn(level, level.getCurrentDifficultyAt(gate.center), MobSpawnType.EVENT, null);
        creature.setRankAndClass(rank, creatureClass);
        creature.getPersistentData().putUUID(GATE_TAG, gate.id);
        Player prey = level.getNearestPlayer(creature, 48.0);
        if (prey != null && !prey.isCreative() && !prey.isSpectator()) {
            creature.setTarget(prey);
        }
        level.addFreshEntity(creature);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, creature.getX(), creature.getY() + 0.5, creature.getZ(),
                30, 0.4, 0.6, 0.4, 0.05);
        gate.spawned.add(creature.getUUID());
        return creature;
    }

    /** A Guardian's death closes its Gate. */
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof NightmareCreature creature) || !(creature.level() instanceof ServerLevel level)
                || !creature.getPersistentData().hasUUID(GATE_TAG)) {
            return;
        }
        UUID id = creature.getPersistentData().getUUID(GATE_TAG);
        for (GateEvent gate : GateData.get(level).gates) {
            if (gate.id.equals(id) && creature.getUUID().equals(gate.guardian)) {
                finish(level, gate, true);
            }
        }
    }

    private static void finish(ServerLevel level, GateEvent gate, boolean guardianSlain) {
        gate.phase = GateEvent.Phase.CLOSED;
        setRiftPhase(level, gate);
        for (UUID uuid : gate.spawned) {
            Entity entity = level.getEntity(uuid);
            if (entity instanceof NightmareCreature creature && creature.isAlive() && !uuid.equals(gate.guardian)) {
                crumble(level, creature);
            }
        }
        if (gate.guardian != null) {
            for (NightmareCreature lesser : level.getEntitiesOfClass(NightmareCreature.class,
                    new net.minecraft.world.phys.AABB(gate.center).inflate(96),
                    other -> gate.guardian.equals(SpawnLesserCopiesGoal.spawnerOf(other)))) {
                crumble(level, lesser);
            }
            Entity guardian = level.getEntity(gate.guardian);
            if (!guardianSlain && guardian != null) {
                guardian.discard();
            }
        }
        level.playSound(null, gate.center, SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 2.0F, 0.5F);
        for (ServerPlayer player : playersNear(level, gate, WARN_RANGE)) {
            spell(player, Component.literal(guardianSlain ? "The Guardian has fallen. The Nightmare Gate closes."
                    : "The Nightmare Gate closes.").withStyle(ChatFormatting.DARK_PURPLE));
        }
        GateData.get(level).setDirty();
    }

    /** Creatures left behind when a Gate closes turn to ash and leave nothing. */
    private static void crumble(ServerLevel level, NightmareCreature creature) {
        level.sendParticles(ParticleTypes.ASH, creature.getX(), creature.getY() + creature.getBbHeight() / 2, creature.getZ(),
                40, creature.getBbWidth() / 2, creature.getBbHeight() / 2, creature.getBbWidth() / 2, 0.02);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, creature.getX(), creature.getY() + 0.5, creature.getZ(),
                8, 0.3, 0.3, 0.3, 0.01);
        creature.discard();
    }

    private static void ensureRift(ServerLevel level, GateEvent gate) {
        if (gate.rift != null && level.getEntity(gate.rift) instanceof GateRiftEntity) {
            return;
        }
        GateRiftEntity rift = NightmareCreatureEntities.GATE_RIFT.get().create(level);
        if (rift == null) {
            return;
        }
        rift.moveTo(gate.center.getX() + 0.5, gate.center.getY() + 1.0, gate.center.getZ() + 0.5, 0, 0);
        rift.setPhase(gate.phase);
        level.addFreshEntity(rift);
        gate.rift = rift.getUUID();
    }

    private static void setRiftPhase(ServerLevel level, GateEvent gate) {
        if (gate.rift != null && level.getEntity(gate.rift) instanceof GateRiftEntity rift) {
            rift.setPhase(gate.phase);
        }
    }

    private static List<ServerPlayer> playersNear(ServerLevel level, GateEvent gate, double range) {
        Vec3 center = Vec3.atCenterOf(gate.center);
        return level.players().stream().filter(player -> player.position().distanceToSqr(center) < range * range).toList();
    }

    /** A line in the Spell's voice: grey and italic. */
    private static void spell(ServerPlayer player, Component line) {
        player.sendSystemMessage(line.copy().withStyle(style -> style.withItalic(true)
                .withColor(line.getStyle().getColor() == null ? net.minecraft.network.chat.TextColor.fromLegacyFormat(ChatFormatting.GRAY)
                        : line.getStyle().getColor())));
    }

    private static void title(ServerPlayer player, Component title, Component subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
    }
}
