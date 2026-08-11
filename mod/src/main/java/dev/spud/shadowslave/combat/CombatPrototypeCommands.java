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
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Development-only command surface for physically judging the bounded combat prototype. */
public final class CombatPrototypeCommands {
    static final String PROTOTYPE_CHAINBACK_TAG = "shadowslave_combat_prototype";
    private static final double CHAINBACK_SPAWN_DISTANCE = 6.0D;
    private static final double STATUS_RADIUS = 64.0D;
    private static final Map<UUID, PrototypeTelemetry> PROTOTYPE_TELEMETRY = new HashMap<>();

    private CombatPrototypeCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowslave_combat")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("chainback_slice")
                        .executes(context -> setupChainbackSlice(context.getSource().getPlayerOrException())))
                .then(Commands.literal("status")
                        .executes(context -> reportPrototypeStatus(context.getSource().getPlayerOrException()))));
    }

    /**
     * Passive development telemetry only. LivingDamageEvent.Post observes immutable final damage
     * after health has been modified; it never changes the amount, cancels the event, or becomes
     * canonical combat state.
     */
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ChainbackEntity chainback)
                || !chainback.getTags().contains(PROTOTYPE_CHAINBACK_TAG)
                || !(event.getSource().getEntity() instanceof ServerPlayer)) {
            return;
        }

        PROTOTYPE_TELEMETRY.compute(
                chainback.getUUID(),
                (ignored, previous) -> (previous == null ? PrototypeTelemetry.empty() : previous)
                        .recordHit(event.getHealthDamage(), chainback.isInDisplacementRecovery()));
    }

    private static int setupChainbackSlice(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
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
        PROTOTYPE_TELEMETRY.put(chainback.getUUID(), PrototypeTelemetry.empty());

        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        player.sendSystemMessage(Component.literal(
                "Combat prototype ready: read Chainback's warning, break range/line of sight, then punish its recovery with the iron sword."
        ).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(
                "Use /shadowslave_combat status after a punish: opening hits counts server damage that landed while Chainback was in recovery."
        ).withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal(
                "Development fixture only: Better Combat owns the ordinary sword swing; Shadow Slave still owns Chainback's special-action state."
        ).withStyle(ChatFormatting.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int reportPrototypeStatus(ServerPlayer player) {
        ChainbackEntity chainback = player.serverLevel()
                .getEntitiesOfClass(
                        ChainbackEntity.class,
                        player.getBoundingBox().inflate(STATUS_RADIUS),
                        entity -> entity.getTags().contains(PROTOTYPE_CHAINBACK_TAG))
                .stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);

        if (chainback == null) {
            player.sendSystemMessage(Component.literal(
                    "Combat prototype status: no tagged Chainback found within 64 blocks. Run /shadowslave_combat chainback_slice first."
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        boolean opening = chainback.isInDisplacementRecovery();
        int openingTicks = chainback.displacementRecoveryTicks();
        PrototypeTelemetry telemetry = PROTOTYPE_TELEMETRY.getOrDefault(chainback.getUUID(), PrototypeTelemetry.empty());
        String lastHit = telemetry.playerHits() == 0
                ? "none"
                : String.format("%.1f (%s)", telemetry.lastDamage(), telemetry.lastHitDuringOpening() ? "during opening" : "outside opening");
        player.sendSystemMessage(Component.literal(String.format(
                "Combat prototype status: Chainback health %.1f/%.1f | opening %s | recovery %d ticks | player hits %d | opening hits %d | last %s",
                chainback.getHealth(),
                chainback.getMaxHealth(),
                opening ? "OPEN" : "closed",
                openingTicks,
                telemetry.playerHits(),
                telemetry.openingHits(),
                lastHit
        )).withStyle(opening ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    record PrototypeTelemetry(int playerHits, int openingHits, float lastDamage, boolean lastHitDuringOpening) {
        static PrototypeTelemetry empty() {
            return new PrototypeTelemetry(0, 0, 0.0F, false);
        }

        PrototypeTelemetry recordHit(float damage, boolean duringOpening) {
            return new PrototypeTelemetry(
                    playerHits + 1,
                    openingHits + (duringOpening ? 1 : 0),
                    damage,
                    duringOpening);
        }
    }
}
