package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * What laid strands do (DESIGN). A strand works from the slot its piece is
 * meant for (a blade in the main hand, a helmet on the head) and, if tuned,
 * only while its condition holds. Every kind's number comes from the
 * strand's file through {@link StrandRules#strength}.
 */
public final class StrandEffects {
    /** The equipment slots a strand can work from. */
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET};
    /** A guard only answers hits at least this heavy, then rests this many ticks. */
    static final float GUARD_THRESHOLD = 5.0F;
    static final int GUARD_REST = 100;
    /** Resistances from several pieces together never take off more than this. */
    static final float RESIST_CAP = 0.8F;
    /** Sneaking is never quieter than this share of normal. */
    static final float HUSH_FLOOR = 0.2F;
    /** Roughly the hunger a sprint adds each tick (0.1 a metre at sprint speed). */
    static final float SPRINT_EXHAUSTION = 0.028F;
    /** Lesser creatures are those this frail or frailer; after hesitating once they won't again for this long. */
    static final float DETER_MAX_HEALTH = 40.0F;
    static final int DETER_REST = 600;
    /** The echo of a cut lands this many ticks later. */
    static final int ECHO_DELAY = 10;

    private static final Map<UUID, Long> GUARD_RESTING = new ConcurrentHashMap<>();
    private static final Map<UUID, Hesitation> HESITATING = new ConcurrentHashMap<>();
    private static final List<Pending> PENDING = new CopyOnWriteArrayList<>();

    private StrandEffects() {
    }

    /** One laid strand at work. */
    record Active(StrandPattern strand, WovenStrands.Laid laid, ItemStack stack, float strength) {
    }

    private record Hesitation(long until, long rest) {
    }

    private record Pending(ResourceKey<Level> dimension, UUID target, UUID attacker, long due, StrandPattern.Kind kind, float amount) {
    }

    /** Every strand of this kind working for {@code holder} right now. */
    static List<Active> active(LivingEntity holder, StrandPattern.Kind kind) {
        WeavingContent content = WeavingContent.current();
        List<Active> found = new ArrayList<>();
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = holder.getItemBySlot(slot);
            WovenStrands strands = stack.get(WeavingRegistry.WOVEN_STRANDS.get());
            if (strands == null || strands.isEmpty()) {
                continue;
            }
            Optional<StrandFit> fit = StrandFit.of(stack);
            if (fit.isEmpty() || fit.get().slot() != slot) {
                continue;
            }
            for (WovenStrands.Laid laid : strands.strands()) {
                if (laid.restriction().filter(restriction -> !restriction.holds(holder)).isPresent()) {
                    continue;
                }
                // A strand of a Weaver's own making works as each of its parts, at the fusing share.
                for (StrandFusion.Part part : StrandFusion.parts(content, laid)) {
                    if (part.strand().effect().kind() == kind && part.strand().fits(fit.get())) {
                        found.add(new Active(part.strand(), part.laid(), stack, StrandRules.strength(part.strand(), part.laid()) * part.share()));
                    }
                }
            }
        }
        return found;
    }

    static float total(LivingEntity holder, StrandPattern.Kind kind) {
        float sum = 0.0F;
        for (Active active : active(holder, kind)) {
            sum += active.strength();
        }
        return sum;
    }

    /** A wear-saving strand gives back the point of durability the hit or break is about to take. */
    private static void spare(LivingEntity holder, ItemStack stack, float chance) {
        if (stack.isDamaged() && holder.getRandom().nextFloat() < chance) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    // Attributes

    /** Attribute strands add their bonus to the piece's own modifiers, in the slot the piece is for. */
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        WovenStrands strands = stack.get(WeavingRegistry.WOVEN_STRANDS.get());
        if (strands == null || strands.isEmpty()) {
            return;
        }
        Optional<StrandFit> fit = StrandFit.of(stack);
        if (fit.isEmpty()) {
            return;
        }
        WeavingContent content = WeavingContent.current();
        for (int i = 0; i < strands.size(); i++) {
            WovenStrands.Laid laid = strands.strands().get(i);
            for (StrandFusion.Part part : StrandFusion.parts(content, laid)) {
                StrandPattern strand = part.strand();
                if (strand.effect().kind() != StrandPattern.Kind.ATTRIBUTE || !strand.fits(fit.get())) {
                    continue;
                }
                Optional<Holder.Reference<Attribute>> attribute = strand.effect().attribute()
                        .flatMap(id -> BuiltInRegistries.ATTRIBUTE.getHolder(ResourceKey.create(Registries.ATTRIBUTE, id)));
                if (attribute.isEmpty()) {
                    continue;
                }
                ResourceLocation modifier = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID,
                        "strand/" + part.laid().id().getPath() + "/" + i);
                event.addModifier(attribute.get(), new AttributeModifier(modifier, StrandRules.strength(strand, part.laid()) * part.share(),
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(fit.get().slot()));
            }
        }
    }

    // Taking hits

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || event.getAmount() <= 0) {
            return;
        }
        DamageSource source = event.getSource();
        float resisted = 0.0F;
        for (Active active : active(target, StrandPattern.Kind.RESIST)) {
            ResourceLocation tag = active.strand().effect().damageTag().orElseThrow();
            if (source.is(TagKey.create(Registries.DAMAGE_TYPE, tag))) {
                resisted += active.strength();
            }
        }
        if (resisted > 0.0F) {
            event.setAmount(event.getAmount() * (1.0F - Math.min(RESIST_CAP, resisted)));
        }
        if (event.getAmount() >= GUARD_THRESHOLD && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            long now = target.level().getGameTime();
            if (GUARD_RESTING.getOrDefault(target.getUUID(), Long.MIN_VALUE) <= now) {
                float guard = 0.0F;
                for (Active active : active(target, StrandPattern.Kind.GUARD)) {
                    guard = Math.max(guard, active.strength());
                }
                if (guard > 0.0F) {
                    event.setAmount(event.getAmount() * (1.0F - Math.min(RESIST_CAP, guard)));
                    GUARD_RESTING.put(target.getUUID(), now + GUARD_REST);
                    target.level().playSound(null, target.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.0F, 0.7F);
                }
            }
        }
    }

    // Landing hits

    public static void onDamaged(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (event.getNewDamage() <= 0 || !(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker
                || attacker == event.getEntity() || !(source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK))) {
            return;
        }
        LivingEntity target = event.getEntity();
        long now = target.level().getGameTime();
        for (Active active : active(attacker, StrandPattern.Kind.KEEN)) {
            spare(attacker, active.stack(), active.strength());
        }
        float pull = total(attacker, StrandPattern.Kind.PULL);
        if (pull > 0.0F) {
            // A tick later, so the hit's own knockback has already been applied.
            PENDING.add(new Pending(target.level().dimension(), target.getUUID(), attacker.getUUID(), now + 1, StrandPattern.Kind.PULL, pull));
        }
        float echo = total(attacker, StrandPattern.Kind.ECHO);
        if (echo > 0.0F) {
            PENDING.add(new Pending(target.level().dimension(), target.getUUID(), attacker.getUUID(), now + ECHO_DELAY,
                    StrandPattern.Kind.ECHO, event.getNewDamage() * echo));
        }
    }

    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide) {
            return;
        }
        for (Active active : active(player, StrandPattern.Kind.KEEN)) {
            spare(player, active.stack(), active.strength());
        }
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 1200 == 0) {
            long now = server.overworld().getGameTime();
            HESITATING.values().removeIf(hesitation -> hesitation.rest() < now);
            GUARD_RESTING.values().removeIf(rest -> rest < now);
        }
        if (PENDING.isEmpty()) {
            return;
        }
        for (Iterator<Pending> it = PENDING.iterator(); it.hasNext(); ) {
            Pending pending = it.next();
            ServerLevel level = server.getLevel(pending.dimension());
            if (level == null) {
                PENDING.remove(pending);
                continue;
            }
            if (level.getGameTime() < pending.due()) {
                continue;
            }
            PENDING.remove(pending);
            Entity target = level.getEntity(pending.target());
            Entity attacker = level.getEntity(pending.attacker());
            if (!(target instanceof LivingEntity living) || !living.isAlive() || attacker == null) {
                continue;
            }
            if (pending.kind() == StrandPattern.Kind.PULL) {
                Vec3 toward = attacker.position().subtract(living.position()).multiply(1.0, 0.0, 1.0);
                if (toward.lengthSqr() > 1.0E-4) {
                    Vec3 pull = toward.normalize().scale(pending.amount());
                    living.setDeltaMovement(pull.x, Math.max(living.getDeltaMovement().y, 0.1), pull.z);
                    living.hurtMarked = true;
                }
            } else if (pending.kind() == StrandPattern.Kind.ECHO) {
                living.invulnerableTime = 0;
                living.hurt(level.damageSources().indirectMagic(attacker, attacker), pending.amount());
                level.playSound(null, living.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 1.6F);
            }
        }
    }

    // Moving and being seen

    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        LivingEntity seen = event.getEntity();
        if (!seen.isCrouching()) {
            return;
        }
        float hush = total(seen, StrandPattern.Kind.HUSH);
        if (hush > 0.0F) {
            event.modifyVisibility(Math.max(HUSH_FLOOR, 1.0F - hush));
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isSprinting()) {
            return;
        }
        float stride = total(player, StrandPattern.Kind.STRIDE);
        if (stride > 0.0F) {
            float exhaustion = player.getFoodData().getExhaustionLevel();
            player.getFoodData().setExhaustion(Math.max(0.0F, exhaustion - SPRINT_EXHAUSTION * Math.min(1.0F, stride)));
        }
    }

    /** A lesser creature turning on someone wearing a deterring strand stops short for a few seconds, once in a while. */
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob instanceof Enemy) || mob.level().isClientSide
                || mob.getMaxHealth() > DETER_MAX_HEALTH || !(event.getNewAboutToBeSetTarget() instanceof LivingEntity target)) {
            return;
        }
        long now = mob.level().getGameTime();
        Hesitation hesitation = HESITATING.get(mob.getUUID());
        if (hesitation != null && now < hesitation.until()) {
            event.setCanceled(true);
            return;
        }
        if (hesitation != null && now < hesitation.rest()) {
            return;
        }
        float seconds = 0.0F;
        for (Active active : active(target, StrandPattern.Kind.DETER)) {
            seconds = Math.max(seconds, active.strength());
        }
        if (seconds > 0.0F) {
            long until = now + Math.round(seconds * 20);
            HESITATING.put(mob.getUUID(), new Hesitation(until, until + DETER_REST));
            event.setCanceled(true);
        }
    }

    /** Clears what the effects remember, for tests and when the server stops. */
    public static void clear() {
        GUARD_RESTING.clear();
        HESITATING.clear();
        PENDING.clear();
    }
}
