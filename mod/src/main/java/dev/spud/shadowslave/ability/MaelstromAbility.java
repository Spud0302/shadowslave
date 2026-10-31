package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ability.combat.AbilityCombat;
import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tide, Ascended (control): a whirlpool opens on the spot you look at for six
 * seconds. Hostiles nearby are spun in toward its heart and hurt each second.
 */
public final class MaelstromAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.MAELSTROM.abilityId();
    static final int ESSENCE_COST = 60;
    static final double RADIUS = 6.0;
    static final int DURATION_TICKS = 20 * 6;
    static final float DAMAGE_PER_SECOND = 2.0F;

    private record Whirl(Vec3 centre, long until) {
    }

    private static final Map<UUID, Whirl> WHIRLS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.MAELSTROM.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        Vec3 centre = UndertowAbility.pointOf(player);
        ServerLevel level = player.serverLevel();
        WHIRLS.put(player.getUUID(), new Whirl(centre, level.getGameTime() + DURATION_TICKS));
        level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, SoundSource.PLAYERS, 1.0F, 0.6F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Whirl whirl = WHIRLS.get(player.getUUID());
        if (whirl == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        if (now >= whirl.until()) {
            WHIRLS.remove(player.getUUID());
            return;
        }
        Vec3 c = whirl.centre();
        double spin = now * 0.35;
        for (int arm = 0; arm < 3; arm++) {
            for (int i = 1; i <= 5; i++) {
                double r = RADIUS * i / 5.0;
                double angle = spin + arm * Math.PI * 2.0 / 3.0 + i * 0.5;
                level.sendParticles(ParticleTypes.BUBBLE_POP, c.x + Math.cos(angle) * r, c.y + 0.2, c.z + Math.sin(angle) * r,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        boolean pulse = now % 20 == 0;
        AbilityCombat combat = AbilityCombat.get();
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(RADIUS, 3.0, RADIUS),
                entity -> entity instanceof Enemy && entity.isAlive())) {
            Vec3 toward = c.subtract(mob.position()).multiply(1, 0, 1);
            double distance = toward.length();
            if (distance > 0.4) {
                Vec3 in = toward.scale(0.18 / distance);
                Vec3 around = new Vec3(-toward.z, 0, toward.x).scale(0.22 / distance);
                mob.setDeltaMovement(in.x + around.x, mob.getDeltaMovement().y, in.z + around.z);
                mob.hurtMarked = true;
            }
            if (pulse) {
                combat.damage(player, mob, DAMAGE_PER_SECOND);
            }
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        WHIRLS.remove(player.getUUID());
    }
}
