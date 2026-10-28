package dev.spud.shadowslave.smithing;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A piece being forged (see {@link BilletData}). Heat it in a Forge Hearth,
 * shape it on a Smithing Anvil, and if its pattern calls for it, quench it:
 * use it on a Quench Trough and hold, letting go as the steam hisses.
 */
public final class BilletItem extends Item {
    static final int USE_TICKS = 72_000;

    public BilletItem(Properties properties) {
        super(properties);
    }

    public static BilletData data(ItemStack stack) {
        return stack.get(SmithingRegistry.BILLET.get());
    }

    @Override
    public Component getName(ItemStack stack) {
        BilletData billet = data(stack);
        if (billet == null) {
            return super.getName(stack);
        }
        return Component.literal((billet.shaped() ? "Shaped " : "Unfinished ") + billet.name());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BilletData billet = data(context.getItemInHand());
        Player player = context.getPlayer();
        if (billet == null || player == null || !context.getLevel().getBlockState(context.getClickedPos()).is(SmithingRegistry.QUENCH_TROUGH.get())) {
            return InteractionResult.PASS;
        }
        if (!billet.quench() || !billet.shaped()) {
            if (player instanceof ServerPlayer serverPlayer) {
                SmithingService.tell(serverPlayer, billet.quench() ? "Shape it on the anvil before you quench it."
                        : "This piece takes no quench.");
            }
            return InteractionResult.FAIL;
        }
        if (billet.heatAt(context.getLevel().getGameTime()) < SmithingRules.QUENCH_MIN) {
            if (player instanceof ServerPlayer serverPlayer) {
                SmithingService.tell(serverPlayer, "Too cold to quench. Bring it back to a cherry glow in the hearth.");
            }
            return InteractionResult.FAIL;
        }
        player.startUsingItem(context.getHand());
        context.getLevel().playSound(player, context.getClickedPos(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 0.6F, 1.6F);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        BilletData billet = data(stack);
        if (billet == null) {
            return;
        }
        int held = USE_TICKS - remaining;
        if (level.isClientSide) {
            if (held % 2 == 0) {
                var look = entity.getLookAngle();
                level.addParticle(ParticleTypes.CLOUD, entity.getX() + look.x * 1.2, entity.getEyeY() - 0.9, entity.getZ() + look.z * 1.2,
                        0.0, 0.08, 0.0);
            }
        } else if (held == billet.quenchTicks()) {
            level.playSound(null, entity.blockPosition(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (held % 8 == 0) {
            level.playSound(null, entity.blockPosition(), SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.PLAYERS, 0.4F, 1.4F);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (entity instanceof ServerPlayer player) {
            SmithingService.quench(player, player.getUsedItemHand(), USE_TICKS - remaining);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        BilletData billet = data(stack);
        if (billet == null) {
            return;
        }
        if (!billet.cold()) {
            long now = SmithingService.clientTime();
            int heat = billet.heatAt(now);
            SmithingRules.Glow glow = SmithingRules.glow(heat);
            tooltip.add(Component.literal(heat + "° · " + glow.label()).withColor(glow == SmithingRules.Glow.COLD ? 0xA0A0A0 : glow.colour()));
            tooltip.add(Component.literal("Works between " + billet.heatMin() + "° and " + billet.heatMax() + "°")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.literal("Cold work: no heat needed").withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.literal("Blows " + Math.min(billet.landed(), billet.blows()) + "/" + billet.blows()
                + (billet.misses() > 0 ? " · " + billet.misses() + " missed" : "")).withStyle(ChatFormatting.GRAY));
        if (billet.shaped() && billet.quench()) {
            tooltip.add(Component.literal("Shaped. Quench it in a trough.").withStyle(ChatFormatting.AQUA));
        }
        if (billet.burned()) {
            tooltip.add(Component.literal("Burned in the fire").withStyle(ChatFormatting.RED));
        }
        if (billet.masterwork()) {
            tooltip.add(Component.literal("Masterwork").withStyle(ChatFormatting.GOLD));
        }
    }
}
