package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.network.payload.DisguisePayload;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.spell.SpellJudge;
import dev.spud.shadowslave.trial.content.NightmarePack;
import dev.spud.shadowslave.trial.content.TrialSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The always-on halves of Masque and the states its abilities leave behind:
 * Role-Worn (a Nightmare trusts the role you were given), Borrowed Face (the
 * creatures of your rank or lower take you for one of their own), Stolen
 * Voice's lures, and Hollow Reflection (still water and glass give you away).
 */
public final class MasqueFace {
    /** Borrowed Face lasts this long unless you act first. */
    static final int FACE_TICKS = 20 * 60;
    /** Role-Worn: an ending that rewards your role rewards you this much more of it. */
    static final float ROLE_SCORE_SHARE = 0.5F;
    /** Hollow Reflection: how far still water or glass can see you, and what being seen costs. */
    static final int REFLECTION_REACH = 2;
    static final float REVEALED_DAMAGE = 1.25F;
    static final int REVEALED_GLOW_TICKS = 30;

    /** Disguised player to the face they wear and when it slips on its own. */
    private static final Map<UUID, Face> FACES = new ConcurrentHashMap<>();
    /** Lured creature to where Stolen Voice is calling it from. */
    private static final Map<UUID, Lure> LURES = new ConcurrentHashMap<>();

    record Face(ResourceLocation type, long until) {
    }

    record Lure(ResourceKey<Level> level, Vec3 point, long until) {
    }

    private MasqueFace() {
    }

    static boolean roleWorn(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.MASQUE.innateId());
    }

    // ---- Role-Worn -----------------------------------------------------------------------------------------------

    /** The kit a Nightmare hands this player for their role: one more of everything for a Masque. */
    public static List<ItemStack> roleKit(ServerPlayer player, List<ItemStack> kit) {
        if (!roleWorn(player)) {
            return kit;
        }
        List<ItemStack> more = new ArrayList<>();
        for (ItemStack stack : kit) {
            more.add(stack.copyWithCount(Math.min(stack.getMaxStackSize(), stack.getCount() + 1)));
        }
        return more;
    }

    /** The Judge's verdict, with half again of the role's reward when a Masque's role was the one the ending wanted. */
    public static SpellJudge.Judgement trusted(ServerPlayer player, NightmarePack.Ending ending, SpellJudge.Run run,
                                               SpellJudge.Judgement judgement, TrialSettings settings) {
        if (!roleWorn(player) || ending.roleScore() <= 0 || ending.role().filter(run.roleId()::equals).isEmpty()) {
            return judgement;
        }
        int score = judgement.score() + Math.round(ending.roleScore() * ROLE_SCORE_SHARE);
        List<String> deeds = new ArrayList<>(judgement.deeds());
        deeds.add("You wore your role so well that the Nightmare believed it.");
        return SpellJudge.Judgement.of(deeds, score, judgement.behaviour(), settings,
                dev.spud.shadowslave.trial.content.TrialContent.get().appraisal());
    }

    // ---- Borrowed Face -------------------------------------------------------------------------------------------

    public static Optional<ResourceLocation> face(ServerPlayer player) {
        Face face = FACES.get(player.getUUID());
        return face == null ? Optional.empty() : Optional.of(face.type());
    }

    static boolean disguised(ServerPlayer player) {
        return FACES.containsKey(player.getUUID());
    }

    /** Puts on the face of {@code type}: everything of the wearer's rank or lower that hunted them lets go. */
    static void wear(ServerPlayer player, EntityType<?> type) {
        FACES.put(player.getUUID(), new Face(BuiltInRegistries.ENTITY_TYPE.getKey(type), player.serverLevel().getGameTime() + FACE_TICKS));
        for (Mob hunter : player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(48),
                mob -> mob.getTarget() == player && fooled(mob, player))) {
            hunter.setTarget(null);
        }
        sync(player);
    }

    /** Takes the borrowed face off, with a word to the wearer when {@code why} is not null. */
    static void drop(ServerPlayer player, String why) {
        if (FACES.remove(player.getUUID()) == null) {
            return;
        }
        sync(player);
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.8F, 1.2F);
        if (why != null) {
            AbilityService.feedback(player, why, ChatFormatting.GRAY);
        }
    }

    /** Whether {@code mob} takes the disguised {@code player} for one of its own: it is of their rank or lower. */
    static boolean fooled(Mob mob, ServerPlayer player) {
        CreatureRank rank = mob instanceof NightmareCreature creature ? creature.rank() : CreatureRank.DORMANT;
        return !SpellJudge.aboveRank(rank, CoreService.rank(player));
    }

    /** Tells the wearer and everyone who can see them which face to draw. */
    static void sync(ServerPlayer player) {
        DisguisePayload payload = new DisguisePayload(player.getId(), face(player).map(ResourceLocation::toString).orElse(""));
        for (ServerPlayer viewer : player.serverLevel().players()) {
            if ((viewer == player || viewer.distanceToSqr(player) < 160 * 160)
                    && viewer.connection != null && viewer.connection.hasChannel(DisguisePayload.TYPE)) {
                PacketDistributor.sendToPlayer(viewer, payload);
            }
        }
    }

    /** A newcomer to a disguised player's sight learns their face. */
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer worn && event.getEntity() instanceof ServerPlayer viewer && disguised(worn)
                && viewer.connection != null && viewer.connection.hasChannel(DisguisePayload.TYPE)) {
            PacketDistributor.sendToPlayer(viewer, new DisguisePayload(worn.getId(), face(worn).map(ResourceLocation::toString).orElse("")));
        }
    }

    /** Using any ability but Borrowed Face itself gives the wearer away. */
    static void onAbilityUsed(ServerPlayer player, AspectAbility ability) {
        if (!ability.id().equals(BorrowedFaceAbility.ID)) {
            drop(player, "You act, and the borrowed face slips.");
        }
    }

    // ---- Stolen Voice --------------------------------------------------------------------------------------------

    static void lure(Mob mob, Vec3 point, int ticks) {
        LURES.put(mob.getUUID(), new Lure(mob.level().dimension(), point, mob.level().getGameTime() + ticks));
        mob.setTarget(null);
        mob.getNavigation().moveTo(point.x, point.y, point.z, 1.1);
    }

    static boolean lured(Entity mob) {
        Lure lure = LURES.get(mob.getUUID());
        return lure != null && lure.until() > mob.level().getGameTime();
    }

    /** Keeps lured creatures walking to the call, once a second. */
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (LURES.isEmpty() || server.getTickCount() % 20 != 0) {
            return;
        }
        for (Map.Entry<UUID, Lure> entry : List.copyOf(LURES.entrySet())) {
            Lure lure = entry.getValue();
            ServerLevel level = server.getLevel(lure.level());
            Entity entity = level == null ? null : level.getEntity(entry.getKey());
            if (!(entity instanceof Mob mob) || !mob.isAlive() || lure.until() <= level.getGameTime()) {
                LURES.remove(entry.getKey());
                continue;
            }
            if (mob.position().distanceToSqr(lure.point()) > 2.0) {
                mob.getNavigation().moveTo(lure.point().x, lure.point().y, lure.point().z, 1.1);
            }
        }
    }

    // ---- Targets -------------------------------------------------------------------------------------------------

    /** Fooled creatures can't take a disguised player as a target; lured ones can't take anyone. */
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide || event.getNewAboutToBeSetTarget() == null) {
            return;
        }
        if (lured(mob)) {
            event.setCanceled(true);
            return;
        }
        if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player && disguised(player) && fooled(mob, player)) {
            event.setCanceled(true);
        }
    }

    // ---- Hollow Reflection ---------------------------------------------------------------------------------------

    static boolean reflected(ServerPlayer player) {
        return KitRules.hasFlaw(player, AspectKitCatalog.MASQUE.flawEffectId());
    }

    /** Still water (a source block, not a current) or glass within reach of the player's feet. */
    static boolean revealed(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos feet = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(feet.offset(-REFLECTION_REACH, -1, -REFLECTION_REACH),
                feet.offset(REFLECTION_REACH, 1, REFLECTION_REACH))) {
            BlockState state = level.getBlockState(pos);
            FluidState fluid = state.getFluidState();
            if (fluid.is(FluidTags.WATER) && fluid.isSource() && level.getFluidState(pos.above()).isEmpty()) {
                return true;
            }
            if (state.is(Tags.Blocks.GLASS_BLOCKS) || state.is(Tags.Blocks.GLASS_PANES)) {
                return true;
            }
        }
        return false;
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 10 != 0) {
            return;
        }
        Face face = FACES.get(player.getUUID());
        if (face != null && (face.until() <= player.serverLevel().getGameTime() || !player.isAlive())) {
            drop(player, "Your borrowed face fades.");
        }
        if (reflected(player) && revealed(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, REVEALED_GLOW_TICKS, 0, true, false, true));
            drop(player, "Your reflection shows what you really are.");
        }
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        // Striking anything gives a disguised player away.
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != event.getEntity() && disguised(attacker)) {
            drop(attacker, "You strike, and the borrowed face slips.");
        }
        if (event.getEntity() instanceof ServerPlayer victim && reflected(victim) && revealed(victim)) {
            event.setAmount(event.getAmount() * REVEALED_DAMAGE);
        }
    }

    // ---- Cleanup -------------------------------------------------------------------------------------------------

    static void forget(UUID player) {
        FACES.remove(player);
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        FACES.clear();
        LURES.clear();
    }
}
