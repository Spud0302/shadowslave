package dev.spud.shadowslave.combat;

import com.mojang.brigadier.Command;
import dev.spud.shadowslave.world.entity.ChainbackEntity;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Development-only command surface for physically judging the bounded combat prototype. */
public final class CombatPrototypeCommands {
    static final String PROTOTYPE_CHAINBACK_TAG = "shadowslave_combat_prototype";
    private static final String BETTER_COMBAT_MOD_ID = "bettercombat";
    private static final double CHAINBACK_SPAWN_DISTANCE = 6.0D;
    private static final double STATUS_RADIUS = 64.0D;
    private static final Map<UUID, HealthProbeBaseline> HEALTH_PROBES = new ConcurrentHashMap<>();

    private CombatPrototypeCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_combat")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("chainback_slice")
                        .executes(context -> setupChainbackSlice(context.getSource().getPlayerOrException())))
                .then(Commands.literal("status")
                        .executes(context -> reportPrototypeStatus(context.getSource().getPlayerOrException())))
                .then(Commands.literal("reset")
                        .executes(context -> resetPrototype(context.getSource().getPlayerOrException()))));
    }

    /**
     * Development-only observer: arm the one-shot health baseline as soon as the tagged Chainback
     * enters the longer recovery earned by a clean evade. This avoids requiring a command during
     * the 18-tick punish window and does not observe, modify, or cancel damage events.
     */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ChainbackEntity chainback = findNearestTaggedPrototypeChainback(player);
        if (chainback == null || !chainback.isInEvadedDisplacementOpening()) {
            return;
        }

        HealthProbeBaseline baseline = HEALTH_PROBES.get(player.getUUID());
        if (baseline != null && baseline.chainbackId().equals(chainback.getUUID())) {
            return;
        }
        if (!player.getMainHandItem().is(Items.IRON_SWORD)) {
            return;
        }

        HEALTH_PROBES.put(player.getUUID(), new HealthProbeBaseline(chainback.getUUID(), chainback.getHealth()));
        player.sendSystemMessage(Component.literal(
                "Combat prototype OPEN: clean evade confirmed and health probe armed. Commit one iron-sword swing, then use /shadowslave_combat status after the exchange."
        ).withStyle(ChatFormatting.GREEN));
    }

    private static int setupChainbackSlice(ServerPlayer player) {
        if (!ModList.get().isLoaded(BETTER_COMBAT_MOD_ID)) {
            player.sendSystemMessage(Component.literal(
                    "Combat prototype setup refused: Better Combat is not loaded, so an ordinary sword swing would not prove the dependency spike."
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        int removed = removeTaggedPrototypeChainbacks(player);
        HEALTH_PROBES.remove(player.getUUID());

        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-6D) {
            horizontal = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            horizontal = horizontal.normalize();
        }

        ChainbackEntity chainback = NightmareCreatureEntities.CHAINBACK.get().create(level);
        if (chainback == null) {
            player.sendSystemMessage(Component.literal("Combat prototype setup failed: Chainback could not be created.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        Vec3 spawn = player.position().add(horizontal.scale(CHAINBACK_SPAWN_DISTANCE));
        chainback.moveTo(spawn.x, player.getY(), spawn.z, player.getYRot() + 180.0F, 0.0F);
        chainback.setPersistenceRequired();
        chainback.addTag(PROTOTYPE_CHAINBACK_TAG);
        chainback.setTarget(player);
        level.addFreshEntity(chainback);

        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        player.sendSystemMessage(Component.literal(
                "Combat prototype ready: Better Combat is loaded. Read Chainback's warning, break range/line of sight, then punish its earned opening with the iron sword."
        ).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(
                "A clean evade now arms the one-shot health probe automatically. After one punish, use /shadowslave_combat status to consume the damage verdict."
        ).withStyle(ChatFormatting.YELLOW));
        if (removed > 0) {
            player.sendSystemMessage(Component.literal(
                    "Removed " + removed + " previous tagged prototype Chainback(s) so this run has exactly one test target."
            ).withStyle(ChatFormatting.GRAY));
        }
        player.sendSystemMessage(Component.literal(
                "Development fixture only: Better Combat owns the ordinary sword swing; Shadow Slave still owns Chainback's special-action state."
        ).withStyle(ChatFormatting.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int reportPrototypeStatus(ServerPlayer player) {
        ChainbackEntity chainback = findNearestTaggedPrototypeChainback(player);

        if (chainback == null) {
            player.sendSystemMessage(Component.literal(
                    "Combat prototype status: no tagged Chainback found within 64 blocks. Run /shadowslave_combat chainback_slice first."
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        boolean telegraph = chainback.isInDisplacementTelegraph();
        int telegraphTicks = chainback.displacementTelegraphTicks();
        boolean recovery = chainback.isInDisplacementRecovery();
        boolean opening = chainback.isInEvadedDisplacementOpening();
        int recoveryTicks = chainback.displacementRecoveryTicks();
        String phase = telegraph ? "TELEGRAPH" : opening ? "OPEN" : recovery ? "RECOVERY" : "NEUTRAL";
        HealthProbeBaseline baseline = HEALTH_PROBES.get(player.getUUID());
        String probeStatus;

        if (baseline == null || !baseline.chainbackId().equals(chainback.getUUID())) {
            probeStatus = opening
                    ? " | probe awaiting automatic arm; hold the reference iron sword"
                    : " | probe unarmed; earn OPEN with a clean evade";
        } else {
            float healthDelta = baseline.health() - chainback.getHealth();
            if (healthDelta > 0.0F) {
                HEALTH_PROBES.remove(player.getUUID());
                probeStatus = String.format(
                        " | health delta %.1f since earned OPEN baseline | verdict DAMAGE OBSERVED | probe CONSUMED",
                        healthDelta
                );
            } else if (opening) {
                probeStatus = String.format(
                        " | probe ARMED at %.1f health | commit one iron-sword swing before OPEN closes",
                        baseline.health()
                );
            } else {
                HEALTH_PROBES.remove(player.getUUID());
                probeStatus = " | health delta 0.0 since earned OPEN baseline | verdict NO DAMAGE OBSERVED | probe CONSUMED";
            }
        }

        player.sendSystemMessage(Component.literal(String.format(
                "Combat prototype status: Chainback health %.1f/%.1f | phase %s | telegraph %d ticks | recovery %d ticks%s",
                chainback.getHealth(),
                chainback.getMaxHealth(),
                phase,
                telegraphTicks,
                recoveryTicks,
                probeStatus
        )).withStyle(opening ? ChatFormatting.GREEN : telegraph ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int resetPrototype(ServerPlayer player) {
        int removed = removeTaggedPrototypeChainbacks(player);
        HEALTH_PROBES.remove(player.getUUID());
        player.sendSystemMessage(Component.literal(
                "Combat prototype reset: removed " + removed + " tagged Chainback(s) and cleared the transient health probe."
        ).withStyle(ChatFormatting.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static ChainbackEntity findNearestTaggedPrototypeChainback(ServerPlayer player) {
        return player.serverLevel()
                .getEntitiesOfClass(
                        ChainbackEntity.class,
                        player.getBoundingBox().inflate(STATUS_RADIUS),
                        entity -> entity.getTags().contains(PROTOTYPE_CHAINBACK_TAG))
                .stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }

    private static int removeTaggedPrototypeChainbacks(ServerPlayer player) {
        var tagged = player.serverLevel().getEntitiesOfClass(
                ChainbackEntity.class,
                player.getBoundingBox().inflate(STATUS_RADIUS),
                entity -> entity.getTags().contains(PROTOTYPE_CHAINBACK_TAG));
        tagged.forEach(ChainbackEntity::discard);
        return tagged.size();
    }

    private record HealthProbeBaseline(UUID chainbackId, float health) {
    }
}