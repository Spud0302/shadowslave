package dev.spud.shadowslave.smithing;

import dev.spud.shadowslave.ability.KitRules;
import dev.spud.shadowslave.ability.MasterworkAbility;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.weaving.WeavingContent;
import dev.spud.shadowslave.weaving.WeavingData;
import dev.spud.shadowslave.weaving.WeavingRecipe;
import dev.spud.shadowslave.weaving.WeavingRules;
import dev.spud.shadowslave.weaving.WeavingService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Optional;
import java.util.function.LongSupplier;

/**
 * Server side of Smithing: fusing a billet at the hearth, laying it on the
 * anvil, judging each hammer blow, the quench, and stamping the finished
 * piece. Smithing XP, levels, patterns and deeds are the Forging branch of
 * the player's Weaving progress, so nothing earned before is lost.
 */
public final class SmithingService {
    /** Thrifty Hands (Forge): billets cool at this percentage of the usual rate. */
    static final int FORGE_COOL_PERCENT = 70;
    /** Cold Hearth (Forge's flaw): in snow-cold places billets cool this many times as fast. */
    static final int COLD_HEARTH_FACTOR = 2;
    /** Set by the client so tooltips can show a billet's heat now; the server never reads it. */
    public static volatile LongSupplier clientTime = () -> 0L;

    private SmithingService() {
    }

    public static long clientTime() {
        return clientTime.getAsLong();
    }

    /** A short line in the action bar. */
    public static void tell(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    private static Optional<SoulRank> soulRank(ServerPlayer player) {
        return SoulService.get(player).soulRank();
    }

    /** How fast a billet this player handles cools, as a percentage: Thrifty Hands slows it, Cold Hearth speeds it. */
    static int coolPercent(ServerPlayer player, BlockPos pos) {
        if (!KitRules.hasInnate(player, AspectKitCatalog.FORGE.innateId())) {
            return 100;
        }
        boolean cold = player.level().getBiome(pos).value().coldEnoughToSnow(pos)
                && KitRules.hasFlaw(player, AspectKitCatalog.FORGE.flawEffectId());
        return FORGE_COOL_PERCENT * (cold ? COLD_HEARTH_FACTOR : 1);
    }

    // The hearth

    /** Fuses what lies in the crucible into a billet over the fire. Returns what the pattern matcher made of it. */
    public static WeavingRules.Attempt fuse(ServerPlayer player, ForgeHearthMenu menu) {
        WeavingContent content = WeavingContent.current();
        WeavingData before = WeavingService.get(player);
        if (!menu.billet().isEmpty()) {
            WeavingService.report(player, "Take the billet off the fire first.");
            return new WeavingRules.Attempt(WeavingRules.Outcome.EMPTY, Optional.empty(), before, 0, Optional.empty(), java.util.List.of());
        }
        WeavingRules.Boons boons = WeavingService.boons(player);
        WeavingRules.Attempt attempt = WeavingRules.lay(content, before, menu.crucibleAmounts(), soulRank(player), boons);
        WeavingService.set(player, attempt.data());
        Optional<WeavingRecipe> recipe = attempt.recipe().map(content.recipes()::get);
        String name = recipe.map(content::name).orElse("");
        StringBuilder message = new StringBuilder();
        switch (attempt.outcome()) {
            case EMPTY -> message.append("Lay a pattern's materials in the crucible first.");
            case WOVEN, DISCOVERED -> {
                ResourceLocation id = attempt.recipe().orElseThrow();
                WeavingRecipe pattern = recipe.orElseThrow();
                boolean masterwork = boons.tierBonus() > 0;
                int level = WeavingRules.heldLevel(content, attempt.data(), pattern.branch());
                BilletData billet = BilletData.lay(id, name, pattern.forging(), player.level().getGameTime(),
                        SmithingRules.zone(level, masterwork), player.getRandom().nextInt(), masterwork, player.getGameProfile().getName());
                ItemStack stack = new ItemStack(SmithingRegistry.BILLET_ITEM.get());
                stack.set(SmithingRegistry.BILLET.get(), billet);
                for (int i = 0; i < ForgeHearthBlockEntity.CRUCIBLE_SLOTS; i++) {
                    menu.hearth().setItem(i, ItemStack.EMPTY);
                }
                menu.hearth().setItem(ForgeHearthBlockEntity.BILLET_SLOT, stack);
                if (attempt.outcome() == WeavingRules.Outcome.DISCOVERED) {
                    message.append("A new pattern takes shape: ").append(name).append("! ");
                }
                message.append(pattern.forging().cold()
                        ? "The pieces for the " + name + " are laid out. Take them to the anvil."
                        : "A billet for the " + name + " sits in the fire. Heat it to " + SmithingRules.glow(pattern.forging().heatMin()).label() + ".");
                if (masterwork) {
                    MasterworkAbility.spend(player);
                    message.append(" Masterwork set.");
                }
                player.level().playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.6F, 0.8F);
            }
            case DISCOVERED_LOCKED -> message.append("A new pattern takes shape: ").append(name).append(". ")
                    .append(attempt.lock().orElse("")).append(" to forge it.");
            case LOCKED -> message.append(name).append(": ").append(attempt.lock().orElse("")).append('.');
            case UNSHOWN -> message.append("The metal won't take this shape. The Spell has not shown you this pattern.");
            case HINTED -> message.append("It slumps apart, but something almost held. See Weaving on your Soul screen.");
            case NOTHING -> message.append("Nothing in the crucible takes a shape.");
        }
        if (attempt.outcome() == WeavingRules.Outcome.DISCOVERED || attempt.outcome() == WeavingRules.Outcome.DISCOVERED_LOCKED) {
            player.sendSystemMessage(Component.literal("You have learned a Smithing pattern: " + name + ".")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        WeavingService.report(player, message.toString());
        return attempt;
    }

    // The anvil

    static void layOnAnvil(ServerPlayer player, SmithingAnvilBlockEntity anvil, InteractionHand hand) {
        if (!anvil.piece().isEmpty()) {
            tell(player, "Something already lies on the anvil.");
            return;
        }
        ItemStack held = player.getItemInHand(hand);
        BilletData billet = held.get(SmithingRegistry.BILLET.get());
        if (billet == null) {
            return;
        }
        long now = player.level().getGameTime();
        held.set(SmithingRegistry.BILLET.get(), billet.measured(billet.heatAt(now), now, coolPercent(player, anvil.getBlockPos())));
        anvil.setPiece(held.split(1));
        player.level().playSound(null, anvil.getBlockPos(), SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.8F, 0.8F);
        tell(player, billet.shaped() ? "Shaped. Take it to the trough." : "Strike it with a Smith's Hammer when the marker crosses the mark.");
    }

    /** Server side of a left click on an anvil while holding a hammer: one blow. */
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START
                || !(event.getEntity() instanceof ServerPlayer player)
                || !player.getMainHandItem().is(SmithingRegistry.SMITHS_HAMMER.get())
                || !(player.level().getBlockEntity(event.getPos()) instanceof SmithingAnvilBlockEntity anvil)) {
            return;
        }
        event.setCanceled(true);
        long now = player.level().getGameTime();
        BilletData billet = anvil.piece().get(SmithingRegistry.BILLET.get());
        // The client drew the marker a tick behind the server.
        float marker = billet == null ? 0.0F : SmithingRules.marker(now - 1, billet.seed());
        strike(player, anvil, marker);
    }

    /** One hammer blow with the marker at {@code marker}. Returns how it landed, or empty if nothing was struck. */
    public static Optional<SmithingRules.Blow> strike(ServerPlayer player, SmithingAnvilBlockEntity anvil, float marker) {
        ItemStack hammer = player.getMainHandItem();
        if (!hammer.is(SmithingRegistry.SMITHS_HAMMER.get()) || player.getCooldowns().isOnCooldown(hammer.getItem())) {
            return Optional.empty();
        }
        ItemStack piece = anvil.piece();
        BilletData billet = piece.get(SmithingRegistry.BILLET.get());
        ServerLevel level = player.serverLevel();
        BlockPos pos = anvil.getBlockPos();
        if (billet == null) {
            tell(player, piece.isEmpty() ? "Lay a billet on the anvil first." : "That piece is finished. Take it.");
            return Optional.empty();
        }
        if (billet.shaped()) {
            tell(player, "It is shaped. Quench it in a trough.");
            return Optional.empty();
        }
        long now = level.getGameTime();
        int heat = billet.heatAt(now);
        player.getCooldowns().addCooldown(hammer.getItem(), SmithingRules.HAMMER_COOLDOWN);
        if (!billet.workable(now)) {
            level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, 0.6F);
            tell(player, "The metal is too cold to move. Reheat it in the hearth.");
            return Optional.empty();
        }
        SmithingRules.Blow blow = SmithingRules.judge(marker, billet.target(), billet.zone());
        BilletData next = billet.struck(blow, heat, now, coolPercent(player, pos));
        piece.set(SmithingRegistry.BILLET.get(), next);
        anvil.setPiece(piece);
        hammer.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        switch (blow) {
            case CLEAN -> {
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.7F, 1.4F);
                level.sendParticles(ParticleTypes.LAVA, x, y, z, 3, 0.15, 0.05, 0.15, 0.0);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 14, 0.2, 0.1, 0.2, 0.25);
            }
            case ROUGH -> {
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.6F, 0.9F);
                level.sendParticles(ParticleTypes.CRIT, x, y, z, 8, 0.2, 0.1, 0.2, 0.15);
            }
            case MISS -> {
                level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.4F, 0.7F);
                level.sendParticles(ParticleTypes.SMOKE, x, y, z, 6, 0.2, 0.05, 0.2, 0.01);
            }
        }
        if (next.shaped()) {
            if (next.quench()) {
                tell(player, "Shaped. Quench it in a trough while it still glows.");
            } else {
                ItemStack finished = finish(player, next, 1.0F, false);
                anvil.setPiece(finished);
                level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        } else {
            tell(player, switch (blow) {
                case CLEAN -> "Clean blow";
                case ROUGH -> "Rough blow";
                case MISS -> "Missed: a flaw in the metal";
            } + " · " + next.landed() + "/" + next.blows());
        }
        return Optional.of(blow);
    }

    // The quench

    /** Releasing a quench after {@code held} ticks in the trough. */
    static void quench(ServerPlayer player, InteractionHand hand, int held) {
        ItemStack stack = player.getItemInHand(hand);
        BilletData billet = stack.get(SmithingRegistry.BILLET.get());
        if (billet == null || !billet.shaped() || !billet.quench()) {
            return;
        }
        float score = SmithingRules.quench(held, billet.quenchTicks());
        boolean cracked = SmithingRules.cracked(held, billet.quenchTicks());
        player.setItemInHand(hand, finish(player, billet, score, cracked));
        player.level().playSound(null, player.blockPosition(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.2F);
        if (cracked) {
            player.sendSystemMessage(Component.literal("Held too long: the blade turned brittle.").withStyle(ChatFormatting.RED));
        } else if (held < billet.quenchTicks() - SmithingRules.QUENCH_PERFECT) {
            tell(player, "Pulled early: the blade is a little soft.");
        }
    }

    /** Quench (Forge): a shaped blade held in the hand is quenched perfectly. Returns false if there was none. */
    public static boolean quenchInHand(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        BilletData billet = stack.get(SmithingRegistry.BILLET.get());
        if (billet == null || !billet.shaped() || !billet.quench()) {
            return false;
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, finish(player, billet, 1.0F, false));
        return true;
    }

    // Finishing

    /** The finished piece: graded, its stats scaled, the smith's mark stamped, and the Forging XP paid. */
    static ItemStack finish(ServerPlayer player, BilletData billet, float quench, boolean cracked) {
        WeavingContent content = WeavingContent.current();
        WeavingRecipe recipe = content.recipes().get(billet.pattern());
        if (recipe == null) {
            tell(player, "This pattern is no longer known to the world. The billet cools to scrap.");
            return new ItemStack(Items.IRON_NUGGET, 3);
        }
        int score = SmithingRules.score(billet, quench);
        Quality quality = SmithingRules.grade(score, billet.masterwork(), cracked);
        Item item = BuiltInRegistries.ITEM.get(recipe.result());
        ItemStack stack = new ItemStack(item == Items.AIR ? Items.BARRIER : item);
        stamp(stack, new ForgedData(quality, score, billet.smith().isEmpty() ? player.getGameProfile().getName() : billet.smith()));
        WeavingRules.Credit credit = WeavingRules.credit(content, WeavingService.get(player), billet.pattern(), WeavingService.boons(player));
        WeavingService.set(player, credit.data());
        player.sendSystemMessage(Component.literal("You forge a " + quality.displayName() + " " + billet.name()
                + " (score " + score + "). +" + credit.xp() + " " + content.branchName(recipe.branch()) + " XP")
                .withStyle(quality.colour()));
        for (String deed : credit.deeds()) {
            player.sendSystemMessage(Component.literal("Deed done. " + deed).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        }
        WeavingService.sync(player);
        return stack;
    }

    /** Stamps a grade on a piece and scales its damage, armour and durability to match. */
    public static void stamp(ItemStack stack, ForgedData forged) {
        stack.set(SmithingRegistry.FORGED.get(), forged);
        float scale = forged.quality().statPercent() / 100.0F;
        if (scale == 1.0F) {
            return;
        }
        if (stack.isDamageableItem()) {
            stack.set(DataComponents.MAX_DAMAGE, Math.max(1, Math.round(stack.getMaxDamage() * scale)));
        }
        ItemAttributeModifiers base = stack.getAttributeModifiers();
        ItemAttributeModifiers.Builder scaled = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : base.modifiers()) {
            AttributeModifier modifier = entry.modifier();
            boolean stat = entry.attribute().is(Attributes.ATTACK_DAMAGE) || entry.attribute().is(Attributes.ARMOR)
                    || entry.attribute().is(Attributes.ARMOR_TOUGHNESS);
            scaled.add(entry.attribute(), stat
                    ? new AttributeModifier(modifier.id(), modifier.amount() * scale, modifier.operation())
                    : modifier, entry.slot());
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, scaled.build().withTooltip(base.showInTooltip()));
    }
}
