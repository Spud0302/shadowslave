package dev.spud.shadowslave.echoes;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.network.payload.EchoStatusPayload;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Echoes for every player (DESIGN, after ch. 47): taking a bound Echo into
 * the soul, summoning it out at an essence cost, the upkeep that keeps it
 * out, standing orders, and sending it back. The soul's list is the truth;
 * a summoned body in the world is only where the Echo is right now, so a
 * body with no summon behind it (after a crash, say) is sent home on sight.
 */
public final class EchoService {
    static final String BODY_TAG = "shadowslave_soul_echo";
    /** How often summoned Echoes are steered, charged and read back. */
    static final int TICK_INTERVAL = 10;

    /** Where each player's summoned Echoes are: owner, then Echo id. Not saved; logging out sends them home. */
    private static final Map<UUID, Map<UUID, Summoned>> SUMMONED = new ConcurrentHashMap<>();
    /** Essence owed toward upkeep that has not yet come to a whole point. */
    private static final Map<UUID, Float> DEBT = new ConcurrentHashMap<>();
    /** Set while this class picks an Echo's target, so the creature's own AI can't pick one. */
    private static boolean ordering;

    private EchoService() {
    }

    /** One summoned Echo: its body, the level it is in, where it was last seen, and its post under Stay. */
    static final class Summoned {
        final UUID entity;
        final ResourceKey<Level> level;
        BlockPos seen;
        BlockPos post;

        Summoned(UUID entity, ResourceKey<Level> level, BlockPos seen) {
            this.entity = entity;
            this.level = level;
            this.seen = seen;
        }
    }

    // The soul's Echoes

    public static SoulEchoes held(ServerPlayer player) {
        return player.getData(EchoesRegistry.SOUL_ECHOES.get());
    }

    static void setHeld(ServerPlayer player, SoulEchoes echoes) {
        player.setData(EchoesRegistry.SOUL_ECHOES.get(), echoes);
    }

    public static int slots(ServerPlayer player) {
        return EchoRules.slots(EchoContent.current().settings(), SoulService.get(player).soulRank());
    }

    static Map<UUID, Summoned> summonedOf(ServerPlayer player) {
        return SUMMONED.computeIfAbsent(player.getUUID(), id -> new LinkedHashMap<>());
    }

    public static boolean isSummoned(ServerPlayer player, UUID echo) {
        Map<UUID, Summoned> out = SUMMONED.get(player.getUUID());
        return out != null && out.containsKey(echo);
    }

    public static int summonedCount(ServerPlayer player) {
        Map<UUID, Summoned> out = SUMMONED.get(player.getUUID());
        return out == null ? 0 : out.size();
    }

    public enum Outcome { DONE, REFUSED, NONE }

    /** Takes the bound Echo in {@code hand} into the soul, if it has room. */
    public static Outcome absorb(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        EchoRecord echo = stack.get(EchoesRegistry.ECHO.get());
        if (echo == null) {
            return Outcome.NONE;
        }
        SoulEchoes held = held(player);
        int slots = slots(player);
        if (held.size() >= slots) {
            return tell(player, Outcome.REFUSED, slots == 0
                    ? "Your soul can't hold an Echo yet. An Awakened soul holds one."
                    : "Your soul holds " + held.size() + (held.size() == 1 ? " Echo" : " Echoes") + " already. A higher Rank holds more.");
        }
        player.setItemInHand(hand, ItemStack.EMPTY);
        setHeld(player, held.with(echo));
        player.level().playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
        sync(player);
        return tell(player, Outcome.DONE, echo.name() + " sinks into your soul (" + held(player).size() + "/" + slots + ").");
    }

    /** Draws an Echo out of the soul into the hand, to hand it to someone else. A summoned Echo is sent home first. */
    public static Outcome drawOut(ServerPlayer player, int index) {
        SoulEchoes held = held(player);
        if (index < 0 || index >= held.size()) {
            return Outcome.NONE;
        }
        if (!player.getMainHandItem().isEmpty() && player.getInventory().getFreeSlot() < 0) {
            return tell(player, Outcome.REFUSED, "Your hands and pack are full.");
        }
        UUID id = held.held().get(index).id();
        if (isSummoned(player, id)) {
            dismiss(player, id, true);
        }
        EchoRecord echo = held(player).find(id).orElseThrow();
        setHeld(player, held(player).without(id));
        ItemStack stack = item(echo);
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        } else {
            player.getInventory().add(stack);
        }
        sync(player);
        return tell(player, Outcome.DONE, echo.name() + " takes the shape of a sliver of light in your hand. Hand it over, or use it to take it back.");
    }

    public static ItemStack item(EchoRecord echo) {
        ItemStack stack = new ItemStack(EchoesRegistry.BOUND_ECHO.get());
        stack.set(EchoesRegistry.ECHO.get(), echo);
        return stack;
    }

    // Summoning and dismissing

    /** The essence summoning an Echo costs: nothing to a soul with no essence core. */
    public static int summonCost(ServerPlayer player, EchoRecord echo) {
        return EssenceService.capacity(player) > 0 ? EchoContent.current().settings().summonEssence(echo.body().rank()) : 0;
    }

    public static Outcome summon(ServerPlayer player, int index) {
        SoulEchoes held = held(player);
        if (index < 0 || index >= held.size()) {
            return Outcome.NONE;
        }
        EchoRecord echo = held.held().get(index);
        if (isSummoned(player, echo.id())) {
            return tell(player, Outcome.REFUSED, echo.name() + " is already out.");
        }
        if (echo.health() <= 0.5F) {
            return tell(player, Outcome.REFUSED, echo.name() + " is still mending in your soul.");
        }
        int cost = summonCost(player, echo);
        if (cost > 0 && !EssenceService.covers(player, cost)) {
            return tell(player, Outcome.REFUSED, "Summoning " + echo.name() + " takes " + cost + " essence.");
        }
        Optional<Mob> body = spawn(player, echo);
        if (body.isEmpty()) {
            return tell(player, Outcome.REFUSED, echo.name() + " can't take shape here.");
        }
        if (cost > 0) {
            EssenceService.spend(player, cost);
        }
        sync(player);
        return tell(player, Outcome.DONE, echo.name() + " rises from your soul.");
    }

    /** Builds the Echo's body beside its owner and sends it into the world. */
    static Optional<Mob> spawn(ServerPlayer player, EchoRecord echo) {
        ServerLevel level = player.serverLevel();
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(echo.body().entity());
        if (type.isEmpty()) {
            return Optional.empty();
        }
        CompoundTag tag = echo.body().tag();
        tag.putString("id", echo.body().entity().toString());
        Entity entity = EntityType.loadEntityRecursive(tag, level, loaded -> loaded);
        if (!(entity instanceof Mob mob)) {
            if (entity != null) {
                entity.discard();
            }
            return Optional.empty();
        }
        Vec3 at = besideOwner(player);
        mob.moveTo(at.x, at.y, at.z, player.getYRot() + 180.0F, 0.0F);
        mob.setHealth(Math.max(1.0F, Math.min(mob.getMaxHealth(), echo.health())));
        mob.setPersistenceRequired();
        mob.setCanPickUpLoot(false);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            mob.setDropChance(slot, 0.0F);
        }
        mob.setCustomName(Component.literal(echo.name()).withStyle(ChatFormatting.AQUA));
        mob.setCustomNameVisible(true);
        mob.addTag(BODY_TAG);
        mob.setData(EchoesRegistry.ECHO_BOND.get(), Optional.of(new EchoBond(player.getUUID(), echo.id(), false)));
        // Listed before it joins the level, so the join check knows it is meant to be here.
        Summoned summoned = new Summoned(mob.getUUID(), level.dimension(), mob.blockPosition());
        if (held(player).order() == EchoCommand.STAY) {
            summoned.post = player.blockPosition();
        }
        summonedOf(player).put(echo.id(), summoned);
        if (!level.addFreshEntity(mob)) {
            summonedOf(player).remove(echo.id());
            return Optional.empty();
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                30, mob.getBbWidth() * 0.5, mob.getBbHeight() * 0.5, mob.getBbWidth() * 0.5, 0.01);
        level.sendParticles(ParticleTypes.END_ROD, mob.getX(), mob.getY() + 0.1, mob.getZ(), 12, 0.4, 0.05, 0.4, 0.02);
        level.playSound(null, mob.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.6F);
        return Optional.of(mob);
    }

    private static Vec3 besideOwner(ServerPlayer player) {
        Vec3 look = player.getLookAngle().multiply(1.0, 0.0, 1.0);
        Vec3 side = look.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : look.normalize().yRot((float) (Math.PI / 2));
        return player.position().add(look.normalize().scale(1.5)).add(side.scale(1.2));
    }

    /** Sends the Echo with id {@code echo} home to the soul, keeping its wounds. */
    public static boolean dismiss(ServerPlayer player, UUID echo, boolean quiet) {
        Map<UUID, Summoned> out = summonedOf(player);
        Summoned summoned = out.remove(echo);
        if (summoned == null) {
            return false;
        }
        Optional<Mob> body = body(player.getServer(), summoned);
        SoulEchoes held = held(player);
        Optional<EchoRecord> record = held.find(echo);
        if (body.isPresent()) {
            Mob mob = body.get();
            if (record.isPresent()) {
                setHeld(player, held.updated(record.get().withHealth(mob.getHealth())));
            }
            fade(mob);
            sendLesserHome(mob, echo);
            mob.discard();
        }
        if (out.isEmpty()) {
            DEBT.remove(player.getUUID());
        }
        sync(player);
        if (!quiet) {
            record.ifPresent(found -> tell(player, Outcome.DONE, found.name() + " returns to your soul."));
        }
        return true;
    }

    public static int dismissAll(ServerPlayer player, boolean quiet) {
        List<UUID> out = new ArrayList<>(summonedOf(player).keySet());
        out.forEach(echo -> dismiss(player, echo, true));
        if (!quiet && !out.isEmpty()) {
            tell(player, Outcome.DONE, out.size() == 1 ? "Your Echo returns to your soul." : "Your Echoes return to your soul.");
        }
        return out.size();
    }

    /** The Echo key: send every summoned Echo home, or else call out every Echo the soul holds. */
    public static Outcome call(ServerPlayer player) {
        if (summonedCount(player) > 0) {
            dismissAll(player, false);
            return Outcome.DONE;
        }
        SoulEchoes held = held(player);
        if (held.isEmpty()) {
            return Outcome.NONE;
        }
        Outcome result = Outcome.REFUSED;
        for (int i = 0; i < held.size(); i++) {
            if (summon(player, i) == Outcome.DONE) {
                result = Outcome.DONE;
            }
        }
        return result;
    }

    private static void fade(Mob mob) {
        if (mob.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SOUL, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                    16, mob.getBbWidth() * 0.4, mob.getBbHeight() * 0.4, mob.getBbWidth() * 0.4, 0.03);
            level.playSound(null, mob.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 0.7F);
        }
    }

    /** Lesser copies an Echo Tyrant made leave with it. */
    static void sendLesserHome(Mob echo, UUID id) {
        echo.level().getEntitiesOfClass(Mob.class, echo.getBoundingBox().inflate(64.0),
                other -> bond(other).filter(bond -> bond.lesser() && bond.echo().equals(id)).isPresent()).forEach(Entity::discard);
    }

    // Orders

    public static Outcome order(ServerPlayer player, EchoCommand command) {
        Map<UUID, Summoned> out = summonedOf(player);
        if (command == EchoCommand.ATTACK) {
            if (out.isEmpty()) {
                return tell(player, Outcome.REFUSED, "No Echo is out to send.");
            }
            Optional<LivingEntity> target = lookTarget(player, EchoContent.current().settings().attackReach());
            if (target.isEmpty()) {
                return tell(player, Outcome.REFUSED, "Look at what your Echoes should attack.");
            }
            bodies(player).forEach(mob -> aim(mob, target.get()));
            return tell(player, Outcome.DONE, (out.size() == 1 ? "Your Echo goes" : "Your Echoes go") + " for the "
                    + target.get().getName().getString() + ".");
        }
        setHeld(player, held(player).ordered(command));
        for (Summoned summoned : out.values()) {
            summoned.post = null;
            if (command == EchoCommand.STAY) {
                body(player.getServer(), summoned).ifPresent(mob -> summoned.post = mob.blockPosition());
            }
        }
        if (command != EchoCommand.GUARD) {
            // Follow and Stay drop a fight picked for the owner; one the Echo is in for itself goes on.
            bodies(player).forEach(mob -> {
                if (mob.getTarget() != null && mob.getLastHurtByMob() != mob.getTarget()) {
                    aim(mob, null);
                }
            });
        }
        sync(player);
        return tell(player, Outcome.DONE, "Echoes: " + command.label() + ".");
    }

    /** What the player is looking at, within {@code reach} and not behind a wall. */
    static Optional<LivingEntity> lookTarget(ServerPlayer player, double reach) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(reach));
        HitResult blocks = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blocks.getType() != HitResult.Type.MISS) {
            end = blocks.getLocation();
        }
        AABB box = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, box,
                entity -> entity instanceof LivingEntity living && living.isAlive() && !entity.isSpectator() && !isAlly(player, entity),
                eye.distanceToSqr(end));
        return hit == null ? Optional.empty() : Optional.of((LivingEntity) hit.getEntity());
    }

    /** Sets an Echo's target, which the creature's own AI is not allowed to do. */
    static void aim(Mob echo, LivingEntity target) {
        ordering = true;
        try {
            echo.setTarget(target);
        } finally {
            ordering = false;
        }
    }

    static boolean ordering() {
        return ordering;
    }

    // Bonds

    public static Optional<EchoBond> bond(Entity entity) {
        return entity instanceof Mob mob && mob.hasData(EchoesRegistry.ECHO_BOND.get())
                ? mob.getData(EchoesRegistry.ECHO_BOND.get()) : Optional.empty();
    }

    static Optional<ServerPlayer> owner(Entity echo) {
        MinecraftServer server = echo.getServer();
        return server == null ? Optional.empty() : bond(echo).map(bond -> server.getPlayerList().getPlayer(bond.owner()));
    }

    /** The owner themself, or one of their Echoes or its lesser copies. */
    static boolean isAlly(ServerPlayer owner, Entity entity) {
        return entity == owner || bond(entity).filter(bond -> bond.owner().equals(owner.getUUID())).isPresent();
    }

    /** Where an Echo under Stay holds, if it is under Stay. */
    static Optional<BlockPos> post(Mob echo) {
        Optional<EchoBond> bond = bond(echo);
        if (bond.isEmpty()) {
            return Optional.empty();
        }
        Map<UUID, Summoned> out = SUMMONED.get(bond.get().owner());
        Summoned summoned = out == null ? null : out.get(bond.get().echo());
        return summoned == null ? Optional.empty() : Optional.ofNullable(summoned.post);
    }

    /** Moves a far-off Echo back beside its owner. */
    static void bringTo(Mob echo, ServerPlayer owner) {
        Vec3 at = besideOwner(owner);
        echo.getNavigation().stop();
        echo.teleportTo(at.x, at.y, at.z);
        if (echo.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SOUL, at.x, at.y + 0.5, at.z, 10, 0.3, 0.4, 0.3, 0.02);
        }
    }

    static Optional<Mob> body(MinecraftServer server, Summoned summoned) {
        if (server == null) {
            return Optional.empty();
        }
        ServerLevel level = server.getLevel(summoned.level);
        Entity entity = level == null ? null : level.getEntity(summoned.entity);
        return entity instanceof Mob mob && mob.isAlive() ? Optional.of(mob) : Optional.empty();
    }

    static List<Mob> bodies(ServerPlayer player) {
        List<Mob> bodies = new ArrayList<>();
        Map<UUID, Summoned> out = SUMMONED.get(player.getUUID());
        if (out != null) {
            out.values().forEach(summoned -> body(player.getServer(), summoned).ifPresent(bodies::add));
        }
        return bodies;
    }

    /** Whether {@code body} is the summoned body of the Echo its bond names. */
    static boolean expected(Entity body, EchoBond bond) {
        Map<UUID, Summoned> out = SUMMONED.get(bond.owner());
        if (out == null) {
            return false;
        }
        Summoned summoned = out.get(bond.echo());
        return summoned != null && (bond.lesser() || summoned.entity.equals(body.getUUID()));
    }

    /** Readies a body to be an Echo: it forgets whom its kind hunts and takes its owner's will instead. */
    static void prepare(Mob mob) {
        mob.targetSelector.removeAllGoals(goal -> true);
        mob.goalSelector.removeAllGoals(goal -> goal instanceof EchoMindGoal);
        mob.goalSelector.addGoal(0, new EchoMindGoal(mob));
        mob.setPersistenceRequired();
        aim(mob, null);
    }

    // Upkeep, steering and resting

    /** Every half second: charge upkeep, steer the summoned, let resting Echoes mend. */
    static void tick(ServerPlayer player) {
        EchoSettings settings = EchoContent.current().settings();
        Map<UUID, Summoned> out = summonedOf(player);
        SoulEchoes held = held(player);
        boolean changed = false;
        List<EchoRecord> summonedRecords = new ArrayList<>();
        for (Map.Entry<UUID, Summoned> entry : new ArrayList<>(out.entrySet())) {
            Summoned summoned = entry.getValue();
            Optional<EchoRecord> record = held.find(entry.getKey());
            if (record.isEmpty()) {
                // Taken off the soul's list some other way; nothing should still be out for it.
                body(player.getServer(), summoned).ifPresent(Entity::discard);
                out.remove(entry.getKey());
                continue;
            }
            Optional<Mob> body = body(player.getServer(), summoned);
            if (body.isEmpty()) {
                ServerLevel level = player.getServer().getLevel(summoned.level);
                if (level == null || level.isLoaded(summoned.seen)) {
                    // Its body is gone without dying (a silverfish into stone, a despawn): it comes home as it was.
                    out.remove(entry.getKey());
                    changed = true;
                } else {
                    // It waits at its post in an unloaded chunk, and still costs upkeep.
                    summonedRecords.add(record.get());
                }
                continue;
            }
            Mob mob = body.get();
            summoned.seen = mob.blockPosition();
            summonedRecords.add(record.get());
            steer(player, mob, held.order(), settings);
            if (Math.abs(record.get().health() - mob.getHealth()) >= 0.5F) {
                held = held.updated(record.get().withHealth(mob.getHealth()));
                changed = true;
            }
        }
        if (!summonedRecords.isEmpty() && !payUpkeep(player, EchoRules.upkeep(settings, summonedRecords) * TICK_INTERVAL / 20.0F)) {
            setHeld(player, held);
            dismissAll(player, true);
            tell(player, Outcome.REFUSED, "Your essence runs dry, and your Echoes slip back into your soul.");
            return;
        }
        if (player.tickCount % 20 == 0) {
            for (EchoRecord echo : held.held()) {
                if (!out.containsKey(echo.id()) && echo.health() < echo.body().maxHealth()) {
                    held = held.updated(EchoRules.rested(settings, echo, 1));
                    changed = true;
                }
            }
        }
        if (changed) {
            setHeld(player, held);
        }
        if (changed || (!out.isEmpty() && player.tickCount % 20 == 0)) {
            sync(player);
        }
    }

    /** Takes upkeep from the essence pool, whole points at a time. Returns false when the pool can't cover it. */
    static boolean payUpkeep(ServerPlayer player, float amount) {
        if (EssenceService.capacity(player) <= 0 || amount <= 0) {
            return true;
        }
        float owed = DEBT.getOrDefault(player.getUUID(), 0.0F) + amount;
        int whole = (int) owed;
        if (whole > 0) {
            if (!EssenceService.covers(player, whole)) {
                DEBT.remove(player.getUUID());
                return false;
            }
            EssenceService.spend(player, whole);
            owed -= whole;
        }
        DEBT.put(player.getUUID(), owed);
        return true;
    }

    /** Drops targets an Echo should not hold, and under Guard picks up whatever is hunting its owner. */
    static void steer(ServerPlayer player, Mob mob, EchoCommand order, EchoSettings settings) {
        LivingEntity target = mob.getTarget();
        double leash = settings.guardRadius() * 2.0;
        if (target != null && (!target.isAlive() || isAlly(player, target) || target.level() != mob.level()
                || (order != EchoCommand.STAY && target.distanceToSqr(player) > leash * leash))) {
            aim(mob, null);
            target = null;
        }
        if (target == null && order == EchoCommand.GUARD && mob.level() == player.level()) {
            double radius = settings.guardRadius();
            mob.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius),
                            hunter -> hunter.getTarget() == player && hunter.isAlive() && !isAlly(player, hunter))
                    .stream().min(java.util.Comparator.comparingDouble(hunter -> hunter.distanceToSqr(mob)))
                    .ifPresent(hunter -> aim(mob, hunter));
        }
        // Undead Echoes are soulless copies and don't catch fire in daylight; real fire and lava still burn them.
        if (mob.isOnFire() && mob.isInvertedHealAndHarm() && !mob.isInLava() && mob.level().isDay()
                && mob.level().canSeeSky(mob.blockPosition())
                && mob.level().getBlockStatesIfLoaded(mob.getBoundingBox()).noneMatch(state -> state.is(net.minecraft.tags.BlockTags.FIRE))) {
            mob.clearFire();
        }
    }

    // Death of an Echo

    /** An Echo that dies is gone for good (lore: a destroyed Echo is lost permanently, ch. 47). */
    static void destroyed(Mob mob, EchoBond bond) {
        MinecraftServer server = mob.getServer();
        ServerPlayer owner = server == null ? null : server.getPlayerList().getPlayer(bond.owner());
        sendLesserHome(mob, bond.echo());
        Map<UUID, Summoned> out = SUMMONED.get(bond.owner());
        if (out != null) {
            out.remove(bond.echo());
        }
        if (owner == null) {
            ShadowSlaveMod.LOGGER.warn("An Echo died while its owner {} was away; it stays in their soul", bond.owner());
            return;
        }
        Optional<EchoRecord> record = held(owner).find(bond.echo());
        setHeld(owner, held(owner).without(bond.echo()));
        owner.sendSystemMessage(Component.literal(record.map(EchoRecord::name).orElse("Your Echo")
                + " is destroyed. It will not return.").withStyle(ChatFormatting.DARK_RED));
        owner.level().playSound(null, mob.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 0.5F);
        if (mob.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SOUL, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                    40, mob.getBbWidth() * 0.5, mob.getBbHeight() * 0.5, mob.getBbWidth() * 0.5, 0.06);
        }
        sync(owner);
    }

    static void forget(UUID player) {
        SUMMONED.remove(player);
        DEBT.remove(player);
    }

    // The client's view

    public static void sync(ServerPlayer player) {
        // Fake and game-test players have no negotiated mod channel to send on.
        if (player.connection == null || !player.connection.hasChannel(EchoStatusPayload.TYPE)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, status(player));
    }

    public static EchoStatusPayload status(ServerPlayer player) {
        EchoSettings settings = EchoContent.current().settings();
        SoulEchoes held = held(player);
        List<EchoStatusPayload.Echo> echoes = new ArrayList<>();
        List<EchoRecord> out = new ArrayList<>();
        for (EchoRecord echo : held.held()) {
            boolean summoned = isSummoned(player, echo.id());
            if (summoned) {
                out.add(echo);
            }
            echoes.add(new EchoStatusPayload.Echo(echo.name(), echo.body().entity().toString(), echo.body().rankText(),
                    echo.health(), echo.body().maxHealth(), summoned, settings.upkeep(echo.body().rank()), echo.binder()));
        }
        return new EchoStatusPayload(slots(player), held.order().getSerializedName(), EchoRules.upkeep(settings, out), echoes,
                EchoBinding.preview(player));
    }

    static Outcome tell(ServerPlayer player, Outcome outcome, String message) {
        player.displayClientMessage(Component.literal(message)
                .withStyle(outcome == Outcome.REFUSED ? ChatFormatting.RED : ChatFormatting.AQUA), true);
        return outcome;
    }
}
