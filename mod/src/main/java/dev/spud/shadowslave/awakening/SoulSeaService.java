package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.block.ModBlocks;
import dev.spud.shadowslave.network.payload.RefineRequestPayload;
import dev.spud.shadowslave.network.payload.SoulSeaPayload;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.soul.refinement.RefinementData;
import dev.spud.shadowslave.soul.refinement.RefinementService;
import dev.spud.shadowslave.soul.refinement.RefinementStat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Meditation at the Dream Candle (DESIGN): sneak and use a lit candle to look
 * inward into the Soul Sea, where rank-ups are chosen. Choices made there only
 * count while the player stays beside a still-lit candle.
 */
public final class SoulSeaService {
    /** How far a meditating player may drift from the candle before the Soul Sea closes to them. */
    static final double REACH = 6.0;

    private static final Map<UUID, GlobalPos> MEDITATING = new ConcurrentHashMap<>();

    private SoulSeaService() {
    }

    /** Sneak-use on a lit Dream Candle meditates instead of snuffing it or placing what is in hand. */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown() || !isLitCandle(event.getLevel().getBlockState(event.getPos()))) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getEntity() instanceof ServerPlayer player) {
            meditate(player, event.getPos());
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MEDITATING.remove(event.getEntity().getUUID());
    }

    public static void meditate(ServerPlayer player, BlockPos candle) {
        MEDITATING.put(player.getUUID(), GlobalPos.of(player.level().dimension(), candle.immutable()));
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, candle.getX() + 0.5, candle.getY() + 0.6, candle.getZ() + 0.5,
                12, 0.15, 0.25, 0.15, 0.01);
        level.playSound(null, candle, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 0.6F);
        send(player, true);
    }

    /** True while the player is beside the lit candle they meditated at. */
    public static boolean meditating(ServerPlayer player) {
        GlobalPos candle = MEDITATING.get(player.getUUID());
        if (candle == null || !candle.dimension().equals(player.level().dimension())) {
            return false;
        }
        Level level = player.level();
        return player.position().closerThan(candle.pos().getCenter(), REACH)
                && level.isLoaded(candle.pos())
                && isLitCandle(level.getBlockState(candle.pos()));
    }

    /** Awakening as chosen in the Soul Sea, which must be open beside a lit candle. */
    public static AwakeningService.Result requestAwakening(ServerPlayer player, String abilityId) {
        if (!meditating(player)) {
            player.sendSystemMessage(Component.literal("The Soul Sea closes. Meditate beside a lit Dream Candle to choose.")
                    .withStyle(ChatFormatting.GRAY));
            return AwakeningService.Result.NOT_READY;
        }
        ResourceLocation id = ResourceLocation.tryParse(abilityId);
        AwakeningService.Result result = id == null ? AwakeningService.Result.NOT_A_CHOICE : AwakeningService.awaken(player, id);
        if (result == AwakeningService.Result.NOT_READY) {
            player.sendSystemMessage(Component.literal("Your soul is not ready to rise.").withStyle(ChatFormatting.GRAY));
        }
        send(player, false);
        return result;
    }

    /** A refinement choice from the Soul Sea: a stat name to place a point, or "respec". */
    public static boolean requestRefinement(ServerPlayer player, String action) {
        if (!meditating(player)) {
            player.sendSystemMessage(Component.literal("The Soul Sea closes. Meditate beside a lit Dream Candle to refine.")
                    .withStyle(ChatFormatting.GRAY));
            return false;
        }
        boolean changed;
        if (RefineRequestPayload.RESPEC.equals(action)) {
            changed = RefinementService.respec(player);
        } else {
            changed = Arrays.stream(RefinementStat.values())
                    .filter(stat -> stat.serializedName().equals(action))
                    .findFirst()
                    .map(stat -> RefinementService.allocate(player, stat))
                    .orElse(false);
        }
        if (changed) {
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_PLACE,
                    SoundSource.PLAYERS, 0.8F, 1.2F);
        }
        send(player, false);
        return changed;
    }

    public static SoulSeaPayload status(ServerPlayer player, boolean open) {
        String nextRank = AwakeningService.nextRank(player).map(SoulRank::serializedName).orElse("");
        String requirement = AwakeningService.nextRank(player)
                .flatMap(RankUpRequirements::forRank)
                .map(value -> value.describe(player))
                .orElse("");
        List<SoulSeaPayload.Choice> choices = AwakeningService.choices(player).stream()
                .map(variant -> new SoulSeaPayload.Choice(variant.abilityId().toString(), variant.name(), variant.role(),
                        variant.description()))
                .toList();
        RefinementData refinement = RefinementService.get(player);
        SoulSeaPayload.Refinement points = new SoulSeaPayload.Refinement(RefinementService.unspent(player),
                refinement.body(), refinement.soul(), refinement.aspect(), RefinementService.canRespec(player));
        return new SoulSeaPayload(CoreService.status(player), nextRank, requirement, AwakeningService.ready(player),
                choices, points, open);
    }

    private static void send(ServerPlayer player, boolean open) {
        // Fake and game-test players have no negotiated mod channel to send on.
        if (player.connection != null && player.connection.hasChannel(SoulSeaPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, status(player, open));
        }
    }

    static boolean isLitCandle(BlockState state) {
        return state.is(ModBlocks.DREAM_CANDLE.get()) && state.getValue(CandleBlock.LIT);
    }
}
