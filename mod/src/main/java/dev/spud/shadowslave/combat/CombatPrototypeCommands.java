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
    private static final String BETTER_COMBAT_DISABLED_TAG = "bettercombat_disabled";
    private static final double CHAINBACK_SPAWN_DISTANCE = 3.5D;
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
     * Development-only observer: keep Chainback's existing committed telegraph/recovery readable during
     * the physical exchange, arm a one-shot health baseline when a clean evade earns OPEN, then resolve
     * it automatically when OPEN closes. This samples only server-owned state and never modifies attack
     * timing, hit selection, damage, recovery, or canonical Shadow Slave data.
     */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ChainbackEntity chainback = findNearestTaggedPrototypeChainback(player);
        HealthProbeBaseline baseline = HEALTH_PROBES.get(player.getUUID());
        if (chainback == null) {
            if (baseline != null) {
                HEALTH_PROBES.remove(player.getUUID());
                player.sendSystemMessage(Component.literal(
                        "Combat prototype verdict INVALID: the tagged Chainback disappeared while the OPEN health probe was armed. Repeat the slice; do not count this run as Better Combat evidence."
                ).withStyle(ChatFormatting.RED));
                player.displayClientMessage(Component.literal(
                        "TARGET LOST • verdict invalid • reset / repeat"
                ).withStyle(ChatFormatting.RED), true);
            }
            return;
        }

        if (chainback.isInDisplacementTelegraph()) {
            player.displayClientMessage(Component.literal(
                    "TELEGRAPH • " + chainback.displacementTelegraphTicks() + "t • break range / line of sight"
            ).withStyle(ChatFormatting.YELLOW), true);
        }

        boolean opening = chainback.isInEvadedDisplacementOpening();
        boolean recovery = chainback.isInDisplacementRecovery();
        if (opening
                && baseline != null
                && baseline.chainbackId().equals(chainback.getUUID())
                && !player.getMainHandItem().is(Items.IRON_SWORD)) {
            HEALTH_PROBES.remove(player.getUUID());
            player.sendSystemMessage(Component.literal(
                    "Combat prototype verdict INVALID: the reference iron sword left the main hand after OPEN was armed. Repeat the slice; do not count damage from another item or empty hand as Better Combat moveset evidence."
            ).withStyle(ChatFormatting.RED));
            player.displayClientMessage(Component.literal(
                    "WEAPON CHANGED • verdict invalid • reset / repeat"
            ).withStyle(ChatFormatting.RED), true);
            return;
        }
        if (opening && BetterCombatSpikeAdapter.isAttackDisabled(player)) {
            if (baseline != null && baseline.chainbackId().equals(chainback.getUUID())) {
                HEALTH_PROBES.remove(player.getUUID());
            }
            player.sendSystemMessage(Component.literal(
                    "Combat prototype verdict INVALID: Better Combat attacks became disabled before the earned OPEN could be judged. Repeat after re-enabling Better Combat; do not count a vanilla fallback swing as spike evidence."
            ).withStyle(ChatFormatting.RED));
            player.displayClientMessage(Component.literal(
                    "ATTACKS DISABLED • verdict invalid • re-enable / repeat"
            ).withStyle(ChatFormatting.RED), true);
            return;
        }
        if (!opening) {
            if (baseline != null && baseline.chainbackId().equals(chainback.getUUID())) {
                float healthDelta = baseline.health() - chainback.getHealth();
                float extraDamage = baseline.extraDamageSinceFirstObservation(chainback.getHealth());
                HEALTH_PROBES.remove(player.getUUID());
                String verdict = healthDelta > 0.0F ? "DAMAGE OBSERVED" : "NO DAMAGE OBSERVED";
                ChatFormatting verdictColor = healthDelta > 0.0F ? ChatFormatting.GREEN : ChatFormatting.RED;
                if (extraDamage > 0.0F) {
                    verdict = "EXTRA DAMAGE OBSERVED";
                    verdictColor = ChatFormatting.RED;
                }
                String firstDropTiming = baseline.firstObservedRecoveryTicks() == null
                        ? "no health drop during OPEN"
                        : baseline.ticksToFirstObservedDrop() + "t to first health drop";
                player.sendSystemMessage(Component.literal(String.format(
                        "Combat prototype OPEN closed: health delta %.1f | opened at %.1f blocks | %s | verdict %s | probe CONSUMED. Reposition before Chainback resumes pressure.",
                        healthDelta,
                        baseline.openingDistance(),
                        firstDropTiming,
                        verdict
                )).withStyle(verdictColor));
                if (extraDamage > 0.0F) {
                    player.displayClientMessage(Component.literal(String.format(
                            "EXTRA DAMAGE • %.1f after first observed drop • reject/defer spike",
                            extraDamage
                    )).withStyle(ChatFormatting.RED), true);
                } else {
                    player.displayClientMessage(Component.literal(healthDelta > 0.0F
                            ? String.format(
                                    "HIT CONFIRMED • %.1f damage • %dt into OPEN • reposition",
                                    healthDelta,
                                    baseline.ticksToFirstObservedDrop()
                            )
                            : "MISS • opening closed • reposition"
                    ).withStyle(verdictColor), true);
                }
            }
            if (recovery) {
                player.displayClientMessage(Component.literal(
                        "RECOVERY • " + chainback.displacementRecoveryTicks() + "t • Chainback connected • reposition"
                ).withStyle(ChatFormatting.RED), true);
            }
            return;
        }

        double currentDistance = Math.sqrt(player.distanceToSqr(chainback));
        if (baseline != null && baseline.chainbackId().equals(chainback.getUUID())) {
            float healthDelta = baseline.health() - chainback.getHealth();
            if (healthDelta > 0.0F) {
                HealthProbeBaseline observed = baseline.observeFirstHealthDrop(
                        chainback.getHealth(),
                        chainback.displacementRecoveryTicks()
                );
                if (observed != baseline) {
                    HEALTH_PROBES.put(player.getUUID(), observed);
                    baseline = observed;
                }
                float extraDamage = baseline.extraDamageSinceFirstObservation(chainback.getHealth());
                if (extraDamage > 0.0F) {
                    player.displayClientMessage(Component.literal(String.format(
                            "EXTRA DAMAGE • %.1f after first observed drop • stop / reject spike",
                            extraDamage
                    )).withStyle(ChatFormatting.RED), true);
                } else {
                    player.displayClientMessage(Component.literal(String.format(
                            "HIT • %.1f damage • %dt into OPEN • recover / reposition",
                            baseline.firstObservedDamage(),
                            baseline.ticksToFirstObservedDrop()
                    )).withStyle(ChatFormatting.GREEN), true);
                }
            } else {
                player.displayClientMessage(Component.literal(String.format(
                        "OPEN • %dt • %.1f blocks • commit one iron-sword swing",
                        chainback.displacementRecoveryTicks(),
                        currentDistance
                )).withStyle(ChatFormatting.GREEN), true);
            }
            return;
        }
        if (!player.getMainHandItem().is(Items.IRON_SWORD)) {
            return;
        }

        HEALTH_PROBES.put(player.getUUID(), new HealthProbeBaseline(
                chainback.getUUID(),
                chainback.getHealth(),
                currentDistance,
                chainback.displacementRecoveryTicks()
        ));
        player.sendSystemMessage(Component.literal(String.format(
                "Combat prototype OPEN: clean evade confirmed and health probe armed at %.1f blocks. Commit one iron-sword swing before the opening closes.",
                currentDistance
        )).withStyle(ChatFormatting.GREEN));
        player.displayClientMessage(Component.literal(String.format(
                "OPEN • %dt • %.1f blocks • commit one iron-sword swing",
                chainback.displacementRecoveryTicks(),
                currentDistance
        )).withStyle(ChatFormatting.GREEN), true);
    }

    private static int setupChainbackSlice(ServerPlayer player) {
        if (!ModList.get().isLoaded(BETTER_COMBAT_MOD_ID)) {
            player.sendSystemMessage(Component.literal(
                    "Combat prototype setup refused: Better Combat is not loaded, so an ordinary sword swing would not prove the dependency spike."
            ).withStyle(ChatFormatting.RED));
            return 0;
        }
        if (player.getTags().contains(BETTER_COMBAT_DISABLED_TAG)) {
            player.sendSystemMessage(Component.literal(
                    "Combat prototype setup refused: this player has Better Combat's persistent bettercombat_disabled tag, so the next sword swing would use vanilla combat. Remove the tag before judging the spike."
            ).withStyle(ChatFormatting.RED));
            return 0;
        }
        if (BetterCombatSpikeAdapter.isAttackDisabled(player)) {
            player.sendSystemMessage(Component.literal(
                    "Combat prototype setup refused: Better Combat's CombatFlags API reports attacks disabled for this player by another mod/runtime flag, so the next sword swing would use vanilla combat. Re-enable Better Combat before judging the spike."
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
                "Combat prototype ready: Better Combat is loaded and CombatFlags confirms attacks are enabled for this player. Chainback starts inside displacement range; read its warning, break range/line of sight, then punish the earned opening with the iron sword."
        ).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(
                "TELEGRAPH, connected RECOVERY, and earned OPEN stay visible in the action bar; OPEN reports live Chainback distance, and a successful punish reports ticks-to-first-health-drop so move fit can be judged separately from hit plumbing."
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
            if (opening) {
                float extraDamage = baseline.extraDamageSinceFirstObservation(chainback.getHealth());
                probeStatus = extraDamage > 0.0F
                        ? String.format(" | EXTRA DAMAGE %.1f after first observed drop | reject/defer spike | probe remains ARMED", extraDamage)
                        : healthDelta > 0.0F
                        ? String.format(
                                " | health delta %.1f observed %dt into OPEN | opened at %.1f blocks | final verdict pending until OPEN closes | probe remains ARMED",
                                healthDelta,
                                baseline.ticksToFirstObservedDrop(),
                                baseline.openingDistance()
                        )
                        : String.format(
                                " | probe ARMED at %.1f health | opened at %.1f blocks | commit one iron-sword swing before OPEN closes",
                                baseline.health(),
                                baseline.openingDistance()
                        );
            } else {
                HEALTH_PROBES.remove(player.getUUID());
                String timing = baseline.firstObservedRecoveryTicks() == null
                        ? "no health drop during OPEN"
                        : baseline.ticksToFirstObservedDrop() + "t to first health drop";
                probeStatus = String.format(
                        " | health delta %.1f since earned OPEN baseline | opened at %.1f blocks | %s | verdict %s | probe CONSUMED",
                        healthDelta,
                        baseline.openingDistance(),
                        timing,
                        healthDelta > 0.0F ? "DAMAGE OBSERVED" : "NO DAMAGE OBSERVED"
                );
            }
        }

        player.sendSystemMessage(Component.literal(String.format(
                "Combat prototype status: Chainback health %.1f/%.1f | distance %.1f blocks | phase %s | telegraph %d ticks | recovery %d ticks%s",
                chainback.getHealth(), chainback.getMaxHealth(), Math.sqrt(player.distanceToSqr(chainback)), phase,
                telegraphTicks, recoveryTicks, probeStatus
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
        return player.serverLevel().getEntitiesOfClass(
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

    private record HealthProbeBaseline(
            UUID chainbackId,
            float health,
            double openingDistance,
            int openingTicksAtArm,
            Float firstObservedHealth,
            Integer firstObservedRecoveryTicks
    ) {
        private HealthProbeBaseline(UUID chainbackId, float health, double openingDistance, int openingTicksAtArm) {
            this(chainbackId, health, openingDistance, openingTicksAtArm, null, null);
        }

        private HealthProbeBaseline observeFirstHealthDrop(float currentHealth, int currentRecoveryTicks) {
            if (firstObservedHealth != null || currentHealth >= health) {
                return this;
            }
            return new HealthProbeBaseline(
                    chainbackId,
                    health,
                    openingDistance,
                    openingTicksAtArm,
                    currentHealth,
                    currentRecoveryTicks
            );
        }

        private float firstObservedDamage() {
            return firstObservedHealth == null ? 0.0F : health - firstObservedHealth;
        }

        private int ticksToFirstObservedDrop() {
            if (firstObservedRecoveryTicks == null) {
                return -1;
            }
            return Math.max(0, openingTicksAtArm - firstObservedRecoveryTicks);
        }

        private float extraDamageSinceFirstObservation(float currentHealth) {
            return firstObservedHealth == null ? 0.0F : Math.max(0.0F, firstObservedHealth - currentHealth);
        }
    }
}
