package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.spell.SpellJudge;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Scribe (Field Guide): Quick Study fills creature pages faster and copies
 * Field Notes without ink, Ward Script writes a rune lesser creatures cannot
 * cross, Lorebind studies a kind for the clan's blows, Living Page keeps
 * written notes current, and Inkbound makes every ability spend paper or ink.
 */
public final class ScribeLore {
    static final double WARD_RADIUS = 4.0;
    static final int WARD_TICKS = 200;
    static final int LOREBIND_TICKS = 1200;
    static final double LOREBIND_SHARE = 0.25;
    static final double LOREBIND_CLAN_RADIUS = 32.0;
    /** What Inkbound spends, first found first spent. */
    static final List<Item> INK = List.of(Items.PAPER, Items.INK_SAC, Items.GLOW_INK_SAC);

    record Ward(ResourceKey<Level> level, Vec3 centre, long until) {
    }

    record Study(EntityType<?> kind, long until) {
    }

    private static final Map<UUID, Ward> WARDS = new ConcurrentHashMap<>();
    private static final Map<UUID, Study> STUDIES = new ConcurrentHashMap<>();

    private ScribeLore() {
    }

    public static boolean isScribe(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.SCRIBE.innateId());
    }

    /** Living Page: notes this player writes keep up with their guide. */
    public static boolean writesLivingPages(ServerPlayer player) {
        return KitRules.hasInnate(player, KitLadder.LIVING_PAGE.abilityId());
    }

    // ---- Inkbound --------------------------------------------------------------------------------------------------

    static boolean inkbound(ServerPlayer player) {
        return KitRules.hasFlaw(player, AspectKitCatalog.SCRIBE.flawEffectId());
    }

    /** Whether an Inkbound Scribe has what an ability needs; anyone else always does. */
    static boolean inked(ServerPlayer player) {
        if (!inkbound(player) || player.getAbilities().instabuild) {
            return true;
        }
        return INK.stream().anyMatch(item -> player.getInventory().countItem(item) > 0);
    }

    /** Spends one sheet of paper or one ink sac for an Inkbound Scribe's ability. */
    static void spendInk(ServerPlayer player) {
        if (!inkbound(player) || player.getAbilities().instabuild) {
            return;
        }
        for (Item item : INK) {
            if (player.getInventory().countItem(item) > 0) {
                player.getInventory().clearOrCountMatchingItems(stack -> stack.is(item), 1, player.inventoryMenu.getCraftSlots());
                player.inventoryMenu.broadcastChanges();
                return;
            }
        }
    }

    // ---- Ward Script -----------------------------------------------------------------------------------------------

    static void write(ServerPlayer player) {
        WARDS.put(player.getUUID(), new Ward(player.level().dimension(), player.position(), player.server.getTickCount() + WARD_TICKS));
        guard(player.server);
    }

    static Optional<Vec3> ward(ServerPlayer player) {
        Ward ward = WARDS.get(player.getUUID());
        return ward == null || ward.until() <= player.server.getTickCount() ? Optional.empty() : Optional.of(ward.centre());
    }

    /** Whether the ward holds this creature back: hostile, and not above its writer's rank. */
    static boolean barred(Mob mob, ServerPlayer writer) {
        if (!(mob instanceof Enemy) || !mob.isAlive()) {
            return false;
        }
        CreatureRank rank = mob instanceof NightmareCreature creature ? creature.rank() : CreatureRank.DORMANT;
        return !SpellJudge.aboveRank(rank, CoreService.rank(writer));
    }

    /** Pushes every barred creature back out of every ward. */
    static void guard(MinecraftServer server) {
        long now = server.getTickCount();
        for (Map.Entry<UUID, Ward> entry : List.copyOf(WARDS.entrySet())) {
            Ward ward = entry.getValue();
            ServerLevel level = server.getLevel(ward.level());
            ServerPlayer writer = KitRules.online(server, entry.getKey());
            if (level == null || writer == null || ward.until() <= now) {
                WARDS.remove(entry.getKey());
                continue;
            }
            Vec3 centre = ward.centre();
            for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(centre, centre).inflate(WARD_RADIUS + 1.0),
                    mob -> barred(mob, writer))) {
                Vec3 out = mob.position().subtract(centre).multiply(1.0, 0.0, 1.0);
                double distance = out.length();
                if (distance >= WARD_RADIUS) {
                    continue;
                }
                Vec3 push = distance < 1.0E-3 ? new Vec3(1.0, 0.0, 0.0) : out.scale(1.0 / distance);
                Vec3 edge = centre.add(push.scale(WARD_RADIUS + 0.2));
                mob.teleportTo(edge.x, mob.getY(), edge.z);
                mob.setDeltaMovement(push.scale(0.3));
                mob.getNavigation().stop();
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, edge.x, mob.getY() + 1.0, edge.z, 4, 0.2, 0.4, 0.2, 0.05);
            }
            if (now % 4 == 0) {
                for (int i = 0; i < 24; i++) {
                    double angle = Math.PI * 2 * i / 24 + now * 0.02;
                    level.sendParticles(ParticleTypes.ENCHANT, centre.x + Math.cos(angle) * WARD_RADIUS, centre.y + 0.1,
                            centre.z + Math.sin(angle) * WARD_RADIUS, 1, 0.0, 0.1, 0.0, 0.0);
                }
            }
        }
    }

    // ---- Lorebind --------------------------------------------------------------------------------------------------

    static void study(ServerPlayer player, EntityType<?> kind) {
        STUDIES.put(player.getUUID(), new Study(kind, player.server.getTickCount() + LOREBIND_TICKS));
    }

    static Optional<EntityType<?>> studied(ServerPlayer player) {
        Study study = STUDIES.get(player.getUUID());
        return study == null || study.until() <= player.server.getTickCount() ? Optional.empty() : Optional.of(study.kind());
    }

    /** Whether the attacker, or a clanmate of theirs nearby, has studied this creature's kind. */
    static boolean lorebound(ServerPlayer attacker, LivingEntity victim) {
        if (STUDIES.isEmpty()) {
            return false;
        }
        long now = attacker.server.getTickCount();
        for (Map.Entry<UUID, Study> entry : STUDIES.entrySet()) {
            if (entry.getValue().until() <= now || entry.getValue().kind() != victim.getType()) {
                continue;
            }
            if (entry.getKey().equals(attacker.getUUID())) {
                return true;
            }
            ServerPlayer scholar = KitRules.online(attacker.server, entry.getKey());
            if (scholar != null && scholar.level() == attacker.level()
                    && scholar.distanceToSqr(attacker) <= LOREBIND_CLAN_RADIUS * LOREBIND_CLAN_RADIUS
                    && ClanService.clanOf(attacker.server, scholar.getUUID()).filter(clan -> clan.has(attacker.getUUID())).isPresent()) {
                return true;
            }
        }
        return false;
    }

    public static void onDamage(LivingDamageEvent.Pre event) {
        Entity source = event.getSource().getEntity();
        if (source instanceof ServerPlayer attacker && lorebound(attacker, event.getEntity())) {
            event.setNewDamage(event.getNewDamage() * (float) (1.0 + LOREBIND_SHARE));
        }
    }

    // ---- ticks and cleanup -----------------------------------------------------------------------------------------

    public static void onServerTick(ServerTickEvent.Post event) {
        if (!WARDS.isEmpty()) {
            guard(event.getServer());
        }
    }

    /** Tells an Inkbound Scribe why their ability would not answer. */
    static void dry(ServerPlayer player) {
        AbilityService.feedback(player, "Inkbound: you have no paper or ink to write it with.", ChatFormatting.GRAY);
    }

    static void forget(UUID player) {
        WARDS.remove(player);
        STUDIES.remove(player);
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        WARDS.clear();
        STUDIES.clear();
    }
}
