package dev.spud.shadowslave.echoes;

import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.creature.goal.SpawnLesserCopiesGoal;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

/**
 * Everything the game does that Echoes answer to: who an Echo may fight,
 * what happens when it, its owner or a carried Echo meets its end, and
 * handing an Echo to another player.
 */
public final class EchoEvents {
    private EchoEvents() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % EchoService.TICK_INTERVAL == 0) {
            EchoService.tick(player);
        }
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide() && mob.getTags().contains(EchoService.BODY_TAG)) {
            EchoService.shadeFromSun(mob);
        }
    }

    // Who an Echo fights

    /**
     * An Echo picks its fights only through its owner's orders, its own
     * defence and its owner's: the creature's own AI can't set it on anyone,
     * least of all its owner. Clearing a target is always allowed.
     */
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() == null || EchoService.ordering()) {
            return;
        }
        if (EchoService.bond(event.getEntity()).isPresent()) {
            event.setCanceled(true);
        }
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        Optional<EchoBond> victimBond = EchoService.bond(victim);
        Optional<EchoBond> attackerBond = attacker == null ? Optional.empty() : EchoService.bond(attacker);
        if (victimBond.isPresent() && attacker != null) {
            EchoBond bond = victimBond.get();
            if (attacker.getUUID().equals(bond.owner())
                    || attackerBond.filter(other -> other.owner().equals(bond.owner())).isPresent()) {
                event.setCanceled(true);
                return;
            }
            if (attacker instanceof LivingEntity living && victim instanceof Mob echo
                    && (echo.getTarget() == null || !echo.getTarget().isAlive())) {
                EchoService.aim(echo, living);
            }
        }
        if (attackerBond.isPresent()) {
            EchoBond bond = attackerBond.get();
            if (victim.getUUID().equals(bond.owner())) {
                event.setCanceled(true);
                return;
            }
            // Kills an Echo makes count as its owner's, for shards, remnants and loot.
            EchoService.owner(attacker).ifPresent(victim::setLastHurtByPlayer);
        }
        if (victim instanceof ServerPlayer owner && attacker instanceof LivingEntity living && !EchoService.isAlly(owner, living)) {
            guard(owner, living);
        }
        if (attacker instanceof ServerPlayer owner && !EchoService.isAlly(owner, victim)) {
            guard(owner, victim);
        }
    }

    /** Under Guard, idle Echoes take on whatever hurt their owner or their owner hurt. */
    private static void guard(ServerPlayer owner, LivingEntity foe) {
        if (EchoService.summonedCount(owner) == 0 || EchoService.held(owner).order() != EchoCommand.GUARD) {
            return;
        }
        for (Mob echo : EchoService.bodies(owner)) {
            if (echo.getTarget() == null || !echo.getTarget().isAlive()) {
                EchoService.aim(echo, foe);
            }
        }
    }

    // Endings

    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity dead = event.getEntity();
        Optional<EchoBond> bond = EchoService.bond(dead);
        if (bond.isPresent() && dead instanceof Mob mob && !bond.get().lesser()) {
            EchoService.destroyed(mob, bond.get());
        } else if (dead instanceof ServerPlayer player) {
            // The owner falls; their Echoes are not killed but return to the soul.
            EchoService.dismissAll(player, true);
        }
    }

    /** An Echo leaves nothing behind: no loot, no gear, no experience. */
    public static void onDrops(LivingDropsEvent event) {
        if (EchoService.bond(event.getEntity()).isPresent()) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            keepCarried(player, event);
        }
    }

    public static void onExperience(LivingExperienceDropEvent event) {
        if (EchoService.bond(event.getEntity()).isPresent()) {
            event.setCanceled(true);
        }
    }

    /** An Echo's body stays what it is: no turning into a drowned or a stray, no splitting, no young. */
    public static void onConversion(LivingConversionEvent.Pre event) {
        if (EchoService.bond(event.getEntity()).isPresent()) {
            event.setCanceled(true);
        }
    }

    public static void onSplit(MobSplitEvent event) {
        if (EchoService.bond(event.getParent()).isPresent()) {
            event.setCanceled(true);
        }
    }

    public static void onBreed(BabyEntitySpawnEvent event) {
        if (EchoService.bond(event.getParentA()).isPresent() || EchoService.bond(event.getParentB()).isPresent()) {
            event.setCanceled(true);
        }
    }

    /**
     * A body that joins the world must be a summon its owner is expecting;
     * anything else (left over from a crash) is sent home. A Tyrant Echo's
     * lesser copies fight for its owner too.
     */
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        Optional<EchoBond> bond = EchoService.bond(mob);
        if (bond.isEmpty() && mob instanceof NightmareCreature copy) {
            UUID spawner = SpawnLesserCopiesGoal.spawnerOf(copy);
            Entity maker = spawner == null || !(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) ? null : level.getEntity(spawner);
            Optional<EchoBond> makerBond = maker == null ? Optional.empty() : EchoService.bond(maker);
            if (makerBond.isPresent()) {
                bond = Optional.of(new EchoBond(makerBond.get().owner(), makerBond.get().echo(), true));
                mob.setData(EchoesRegistry.ECHO_BOND.get(), bond);
                mob.addTag(EchoService.BODY_TAG);
            }
        }
        if (bond.isEmpty()) {
            return;
        }
        if (!EchoService.expected(mob, bond.get())) {
            event.setCanceled(true);
            return;
        }
        EchoService.prepare(mob);
    }

    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EchoService.dismissAll(player, true);
            EchoService.forget(player.getUUID());
        }
    }

    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EchoService.sync(player);
        }
    }

    /** Echoes don't cross between worlds with their owner; they return to the soul and can be called again. */
    public static void onTravel(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && EchoService.dismissAll(player, true) > 0) {
            player.sendSystemMessage(Component.literal("Your Echoes return to your soul as you cross over.").withStyle(ChatFormatting.AQUA));
        } else if (EchoService.bond(event.getEntity()).isPresent()) {
            event.setCanceled(true);
        }
    }

    // Carrying and handing over

    /**
     * Bound Echoes a player carries sink into their soul as they fall, even
     * past its slots (lore: Echoes are kept in the soul); the soul just can't
     * take more until some are drawn out.
     */
    private static void keepCarried(ServerPlayer player, LivingDropsEvent event) {
        SoulEchoes held = EchoService.held(player);
        int kept = 0;
        for (Iterator<ItemEntity> drops = event.getDrops().iterator(); drops.hasNext(); ) {
            EchoRecord echo = drops.next().getItem().get(EchoesRegistry.ECHO.get());
            if (echo != null) {
                held = held.with(echo);
                drops.remove();
                kept++;
            }
        }
        if (kept > 0) {
            EchoService.setHeld(player, held);
            player.sendSystemMessage(Component.literal(kept == 1 ? "An Echo you carried sinks into your soul as you fall."
                    : kept + " Echoes you carried sink into your soul as you fall.").withStyle(ChatFormatting.AQUA));
        }
    }

    /** An Echo is never left on the ground (lore: it passes by touch); tossing it keeps it in the pack. */
    public static void onToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (!(event.getPlayer() instanceof ServerPlayer player) || !stack.has(EchoesRegistry.ECHO.get())) {
            return;
        }
        if (player.getInventory().add(stack.copy())) {
            event.setCanceled(true);
            EchoService.tell(player, EchoService.Outcome.REFUSED, "An Echo won't lie on the ground. Sneak and use another player to hand it over.");
        }
    }

    /** Sneak and use another player while holding a bound Echo, and it passes to them (lore: Echoes can be transferred, ch. 47). */
    public static void onInteractPlayer(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer giver) || !(event.getTarget() instanceof ServerPlayer taker)
                || !giver.isShiftKeyDown() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        ItemStack hand = giver.getMainHandItem();
        EchoRecord echo = hand.get(EchoesRegistry.ECHO.get());
        if (echo == null) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!taker.getInventory().add(hand.copy())) {
            EchoService.tell(giver, EchoService.Outcome.REFUSED, taker.getGameProfile().getName() + "'s hands and pack are full.");
            return;
        }
        giver.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        giver.level().playSound(null, taker.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.4F);
        EchoService.tell(giver, EchoService.Outcome.DONE, "You hand " + echo.name() + " to " + taker.getGameProfile().getName() + ".");
        EchoService.tell(taker, EchoService.Outcome.DONE, giver.getGameProfile().getName() + " hands you " + echo.name()
                + ". Use it to take it into your soul.");
    }
}
