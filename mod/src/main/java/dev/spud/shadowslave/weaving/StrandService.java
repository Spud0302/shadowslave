package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.ability.KitRules;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.item.SoulShardItem;
import dev.spud.shadowslave.smithing.ForgedData;
import dev.spud.shadowslave.smithing.SmithingRegistry;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.core.ShardRank;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The server side of laying strands (DESIGN, after Sunny's Weaving): studying
 * a weave to learn a strand, tracing a learned strand onto a soul-shard
 * anchor to lay it into a piece, and the Forger's work of unravelling a
 * strand into a Woven Thread and laying that thread elsewhere.
 */
public final class StrandService {
    public static final ResourceLocation PATTERNS = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "patterns");
    public static final ResourceLocation FORGERY = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "forgery");
    /** Forgery XP for each Tier of a strand unravelled, and for laying a thread. */
    static final int UNRAVEL_XP = 10;
    static final int LAY_XP = 5;

    /** Who is studying what, and since when. Not saved: a study is a few seconds at the table. */
    private static final Map<UUID, Study> STUDYING = new ConcurrentHashMap<>();

    private StrandService() {
    }

    private record Study(ResourceLocation strand, long started) {
    }

    // What the table holds

    public static int patternsLevel(ServerPlayer player) {
        return WeavingRules.heldLevel(WeavingContent.current(), WeavingService.get(player), PATTERNS);
    }

    public static int forgeryLevel(ServerPlayer player) {
        return WeavingRules.heldLevel(WeavingContent.current(), WeavingService.get(player), FORGERY);
    }

    /** The share of Weaving's essence costs this player pays: Wellspring pays less. */
    public static int essencePercent(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.WELLSPRING.innateId()) ? StrandRules.WELLSPRING_PERCENT : 100;
    }

    /** A Seer sees weaves plainly: studying one costs them no essence. */
    static boolean seer(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.SEER.innateId());
    }

    static boolean knows(ServerPlayer player, ResourceLocation id) {
        return WeavingContent.current().strand(id)
                .map(strand -> StrandRules.knows(WeavingService.get(player).studied(), id, strand)).orElse(false);
    }

    /** Whether the piece holds a strand of {@code kind}. */
    public static boolean holds(WeavingContent content, ItemStack stack, StrandPattern.Kind kind) {
        for (WovenStrands.Laid laid : strands(stack).strands()) {
            if (content.strand(laid.id()).map(strand -> strand.effect().kind() == kind).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    static Optional<ShardRank> anchorRank(ItemStack anchor) {
        return anchor.getItem() instanceof SoulShardItem shard && !anchor.isEmpty() ? Optional.of(shard.rank()) : Optional.empty();
    }

    /** Essence one shard of the anchor stack is worth when burned. */
    static int anchorValue(ItemStack anchor) {
        return anchorRank(anchor).map(rank -> StrandRules.shardEssence(rank, SoulShardItem.isFaint(anchor))).orElse(0);
    }

    /** How many strands a piece holds: its forged grade's threads, or one for a plain piece a strand fits. */
    public static int threads(ItemStack stack) {
        ForgedData forged = stack.get(SmithingRegistry.FORGED.get());
        return StrandRules.threads(Optional.ofNullable(forged).map(ForgedData::quality), StrandFit.of(stack).isPresent());
    }

    public static WovenStrands strands(ItemStack stack) {
        WovenStrands strands = stack.get(WeavingRegistry.WOVEN_STRANDS.get());
        return strands == null ? WovenStrands.EMPTY : strands;
    }

    /**
     * The strands a piece can teach, in id order: those laid into it, and
     * any whose study sources name the item or an enchantment it carries.
     */
    public static List<ResourceLocation> teaches(WeavingContent content, ItemStack stack) {
        TreeSet<ResourceLocation> found = new TreeSet<>(java.util.Comparator.comparing(ResourceLocation::toString));
        if (stack.isEmpty()) {
            return List.of();
        }
        strands(stack).strands().stream().map(WovenStrands.Laid::id).filter(content.strands()::containsKey).forEach(found::add);
        ResourceLocation item = BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<ResourceLocation> enchantments = enchantments(stack);
        content.strands().forEach((id, strand) -> {
            for (StrandPattern.Source source : strand.study()) {
                if (source.item().filter(item::equals).isPresent()
                        || source.enchantment().filter(enchantments::contains).isPresent()) {
                    found.add(id);
                }
            }
        });
        return List.copyOf(found);
    }

    private static List<ResourceLocation> enchantments(ItemStack stack) {
        List<ResourceLocation> ids = new ArrayList<>();
        for (ItemEnchantments enchantments : new ItemEnchantments[]{stack.get(DataComponents.ENCHANTMENTS), stack.get(DataComponents.STORED_ENCHANTMENTS)}) {
            if (enchantments != null) {
                for (Holder<Enchantment> enchantment : enchantments.keySet()) {
                    enchantment.unwrapKey().ifPresent(key -> ids.add(key.location()));
                }
            }
        }
        return ids;
    }

    // Study

    /** Starts studying strand {@code index} of what the centre piece teaches. */
    static void startStudy(ServerPlayer player, WeavingMenu menu, int index) {
        List<ResourceLocation> lessons = teaches(WeavingContent.current(), menu.centre());
        if (index < 0 || index >= lessons.size()) {
            return;
        }
        STUDYING.put(player.getUUID(), new Study(lessons.get(index), player.level().getGameTime()));
    }

    /** Finishes the study begun with {@link #startStudy}, if it was held long enough. */
    static void finishStudy(ServerPlayer player, WeavingMenu menu, int index) {
        Study study = STUDYING.remove(player.getUUID());
        List<ResourceLocation> lessons = teaches(WeavingContent.current(), menu.centre());
        if (study == null || index < 0 || index >= lessons.size() || !lessons.get(index).equals(study.strand())) {
            return;
        }
        study(player, menu, study.strand(), player.level().getGameTime() - study.started());
    }

    /**
     * Picks {@code strand} out of the weave in the centre piece after
     * {@code held} ticks of study (lore: Sunny studied many weaves and picked
     * out single patterns, ch. 2048). Burns a little essence, or shards from
     * the anchor slot. Returns whether it was learned.
     */
    public static boolean study(ServerPlayer player, WeavingMenu menu, ResourceLocation strand, long held) {
        WeavingContent content = WeavingContent.current();
        WeavingData data = WeavingService.get(player);
        Optional<StrandPattern> pattern = content.strand(strand);
        if (pattern.isEmpty() || !teaches(content, menu.centre()).contains(strand)) {
            WeavingService.report(player, "There is no such weave in this piece.");
            return false;
        }
        if (StrandRules.knows(data.studied(), strand, pattern.get())) {
            WeavingService.report(player, "You already know " + pattern.get().name() + ".");
            return false;
        }
        if (held < StrandRules.STUDY_TICKS) {
            WeavingService.report(player, "The pattern slips away. Hold your attention on it a little longer.");
            return false;
        }
        int cost = seer(player) ? 0 : StrandRules.discounted(StrandRules.STUDY_ESSENCE, essencePercent(player));
        Optional<StrandRules.Payment> payment = StrandRules.pay(cost, (int) EssenceService.current(player),
                menu.anchor().getCount(), anchorValue(menu.anchor()));
        if (payment.isEmpty()) {
            WeavingService.report(player, "Studying a weave takes " + cost
                    + " essence. Rest, or lay soul shards in the anchor slot to burn.");
            return false;
        }
        pay(player, menu, payment.get());
        WeavingService.set(player, data.withStudied(strand));
        WeavingService.report(player, "You pick " + pattern.get().name() + " out of the weave. You can trace it now.");
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 1.3F);
        return true;
    }

    private static void pay(ServerPlayer player, WeavingMenu menu, StrandRules.Payment payment) {
        if (payment.essence() > 0) {
            EssenceService.spend(player, payment.essence());
        }
        if (payment.shards() > 0) {
            menu.anchor().shrink(payment.shards());
            menu.table().setChanged();
        }
    }

    // Laying a traced strand

    /** Why {@code strand} at {@code tier} can't be laid into the centre piece now, or empty if it can (before the trace). */
    static Optional<String> lock(ServerPlayer player, WeavingMenu menu, ResourceLocation id, int tier,
                                 Optional<StrandRestriction> restriction) {
        WeavingContent content = WeavingContent.current();
        Optional<StrandPattern> found = content.strand(id);
        if (found.isEmpty() || !knows(player, id)) {
            return Optional.of("You haven't studied that strand.");
        }
        StrandPattern strand = found.get();
        ItemStack piece = menu.centre();
        Optional<StrandFit> fit = StrandFit.of(piece);
        if (fit.isEmpty()) {
            return Optional.of("Lay a weapon, tool or piece of armour in the centre.");
        }
        if (!strand.fits(fit.get())) {
            return Optional.of(strand.name() + " doesn't suit " + fit.get().plural() + ".");
        }
        WovenStrands laid = strands(piece);
        if (laid.strands().stream().anyMatch(existing -> existing.id().equals(id))) {
            return Optional.of("This piece already holds " + strand.name() + ".");
        }
        if (laid.size() >= threads(piece)) {
            return Optional.of("Every thread of this piece is taken. A finer forging holds more.");
        }
        if (strand.effect().kind() == StrandPattern.Kind.BOUND && !holds(content, piece, StrandPattern.Kind.MEMORY)) {
            return Optional.of("Only a Memory can be Bound. Lay the Memory weave first.");
        }
        Optional<SoulRank> soul = SoulService.get(player).soulRank();
        if (strand.minRank() != SoulRank.DORMANT && soul.map(rank -> rank.ordinal() < strand.minRank().ordinal()).orElse(true)) {
            return Optional.of(strand.name() + " needs " + WeavingRules.rankName(strand.minRank()) + " soul to hold it.");
        }
        Optional<ShardRank> anchor = anchorRank(menu.anchor());
        if (anchor.isEmpty()) {
            return Optional.of("Lay a soul shard in the anchor slot.");
        }
        if (tier < 1 || tier > strand.maxTier()) {
            return Optional.of(strand.name() + " has no Tier " + WeavingRules.tierName(tier) + ".");
        }
        int cap = StrandRules.tierCap(soul, patternsLevel(player), anchor);
        if (tier > cap) {
            return Optional.of("Tier " + WeavingRules.tierName(tier) + " is beyond you here: your soul, your Patterns level and the anchor allow Tier "
                    + WeavingRules.tierName(Math.max(1, cap)) + ".");
        }
        if (restriction.isPresent() && (!strand.tunable() || patternsLevel(player) < 2)) {
            return Optional.of(strand.tunable() ? "Tuning a strand needs Patterns level 2." : strand.name() + " can't be tuned.");
        }
        int material = strand.material().count(tier);
        if (menu.materialAmounts().getOrDefault(strand.material().item(), 0) < material) {
            return Optional.of("Spinning it takes " + material + " × " + itemName(strand.material().item()) + ".");
        }
        return Optional.empty();
    }

    static String itemName(ResourceLocation item) {
        return BuiltInRegistries.ITEM.get(item).getDescription().getString();
    }

    /**
     * Lays {@code strand} at {@code tier} into the centre piece along a traced
     * pattern (lore: spin essence into strings, recreate the pattern, anchor
     * it in a soul shard, ch. 3024). The essence is spent however the trace
     * goes; a fizzle keeps the anchor and materials. Returns the potency laid,
     * or 0 if nothing was.
     */
    public static int weave(ServerPlayer player, WeavingMenu menu, ResourceLocation id, int tier,
                            Optional<StrandRestriction> restriction, float[] trace) {
        WeavingContent content = WeavingContent.current();
        Optional<String> lock = lock(player, menu, id, tier, restriction);
        if (lock.isPresent()) {
            WeavingService.report(player, lock.get());
            return 0;
        }
        StrandPattern strand = content.strand(id).orElseThrow();
        int cost = StrandRules.discounted(strand.essence().get(tier - 1), essencePercent(player));
        // The anchor shard itself stays to hold the strand; any others in the slot can be burned.
        Optional<StrandRules.Payment> payment = StrandRules.pay(cost, (int) EssenceService.current(player),
                menu.anchor().getCount() - 1, anchorValue(menu.anchor()));
        if (payment.isEmpty()) {
            WeavingService.report(player, "Tier " + WeavingRules.tierName(tier) + " takes " + cost
                    + " essence. Rest, or lay more soul shards in the anchor slot to burn.");
            return 0;
        }
        pay(player, menu, payment.get());
        float score = StrandRules.score(StrandRules.points(id, strand), trace, StrandRules.tolerance(patternsLevel(player), tier));
        StrandRules.Trace judged = StrandRules.judge(score);
        if (judged == StrandRules.Trace.FIZZLE) {
            WeavingService.report(player, "The strings fray before they hold (" + Math.round(score * 100)
                    + "%). The essence is spent; the anchor and materials are not.");
            player.level().playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6F, 1.4F);
            return 0;
        }
        int potency = StrandRules.potency(score);
        menu.anchor().shrink(1);
        menu.takeMaterial(strand.material().item(), strand.material().count(tier));
        ItemStack piece = menu.centre();
        piece.set(WeavingRegistry.WOVEN_STRANDS.get(), strands(piece).with(
                new WovenStrands.Laid(id, tier, potency, restriction, player.getGameProfile().getName())));
        menu.table().setChanged();
        WeavingRules.Credit credit = StrandRules.credit(content, WeavingService.get(player), id, strand, tier, PATTERNS);
        WeavingService.set(player, credit.data());
        StringBuilder message = new StringBuilder(judged == StrandRules.Trace.CLEAN
                ? strand.name() + " " + WeavingRules.tierName(tier) + " holds, clean and whole."
                : strand.name() + " " + WeavingRules.tierName(tier) + " holds, loosely, at " + potency + "%.");
        if (strand.effect().kind() == StrandPattern.Kind.MEMORY) {
            message.append(" It is a Memory now: dismiss it into your soul and summon it back with the Memory key.");
        } else if (strand.effect().kind() == StrandPattern.Kind.BOUND) {
            message.append(" It will bond to the next hand that holds it.");
        }
        message.append(" +").append(credit.xp()).append(" Patterns XP.");
        for (String deed : credit.deeds()) {
            message.append(" Deed done: ").append(deed).append('.');
        }
        WeavingService.report(player, message.toString());
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.9F);
        return potency;
    }

    // Forgery

    /** Pulls strand {@code index} out of the centre piece into a Woven Thread in the output slot. */
    public static boolean unravel(ServerPlayer player, WeavingMenu menu, int index) {
        ItemStack piece = menu.centre();
        WovenStrands laid = strands(piece);
        if (index < 0 || index >= laid.size()) {
            WeavingService.report(player, "There is no such strand in this piece.");
            return false;
        }
        if (!menu.output().isEmpty()) {
            WeavingService.report(player, "Take the last thread out first.");
            return false;
        }
        WovenStrands.Laid strand = laid.strands().get(index);
        WeavingContent content = WeavingContent.current();
        StrandPattern.Kind kind = content.strand(strand.id()).map(pattern -> pattern.effect().kind()).orElse(StrandPattern.Kind.ATTRIBUTE);
        if (kind.foundational()) {
            return unweave(player, menu, index, kind);
        }
        int cost = StrandRules.discounted(StrandRules.UNRAVEL_ESSENCE * strand.tier(), essencePercent(player));
        Optional<StrandRules.Payment> payment = StrandRules.pay(cost, (int) EssenceService.current(player),
                menu.anchor().getCount(), anchorValue(menu.anchor()));
        if (payment.isEmpty()) {
            WeavingService.report(player, "Unravelling it takes " + cost + " essence. Rest, or lay soul shards in the anchor slot to burn.");
            return false;
        }
        pay(player, menu, payment.get());
        int forgery = forgeryLevel(player);
        WovenStrands rest = laid.without(index);
        if (rest.isEmpty()) {
            piece.remove(WeavingRegistry.WOVEN_STRANDS.get());
        } else {
            piece.set(WeavingRegistry.WOVEN_STRANDS.get(), rest);
        }
        ItemStack thread = new ItemStack(WeavingRegistry.WOVEN_THREAD.get());
        thread.set(WeavingRegistry.WOVEN_STRANDS.get(), new WovenStrands(List.of(strand.withPotency(StrandRules.unravelled(strand.potency(), forgery)))));
        menu.table().setItem(WeavingMenu.OUTPUT_SLOT, thread);
        WeavingService.set(player, WeavingService.get(player).withXp(FORGERY, UNRAVEL_XP * strand.tier()));
        String name = WeavingContent.current().strand(strand.id()).map(StrandPattern::name).orElse(strand.id().getPath());
        WeavingService.report(player, name + " comes loose as a Woven Thread" + (forgery >= 2 ? ", whole." : ", a little frayed."));
        player.level().playSound(null, player.blockPosition(), SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }

    /**
     * Takes the Memory weave or Bound out of a piece. Neither comes loose as a
     * thread: the Memory weave is simply undone, and unbinding needs an
     * Awakened soul shard or better in the anchor slot, which it uses up. A
     * Bound piece must be unbound before it stops being a Memory.
     */
    private static boolean unweave(ServerPlayer player, WeavingMenu menu, int index, StrandPattern.Kind kind) {
        ItemStack piece = menu.centre();
        WeavingContent content = WeavingContent.current();
        if (kind == StrandPattern.Kind.MEMORY && holds(content, piece, StrandPattern.Kind.BOUND)) {
            WeavingService.report(player, "Unbind it first: a Bound piece stays a Memory.");
            return false;
        }
        boolean unbinding = kind == StrandPattern.Kind.BOUND;
        if (unbinding && anchorRank(menu.anchor()).map(rank -> rank.ordinal() < ShardRank.AWAKENED.ordinal()).orElse(true)) {
            WeavingService.report(player, "Unbinding takes an Awakened soul shard or better in the anchor slot.");
            return false;
        }
        int cost = StrandRules.discounted(StrandRules.UNRAVEL_ESSENCE, essencePercent(player));
        Optional<StrandRules.Payment> payment = StrandRules.pay(cost, (int) EssenceService.current(player),
                menu.anchor().getCount() - (unbinding ? 1 : 0), anchorValue(menu.anchor()));
        if (payment.isEmpty()) {
            WeavingService.report(player, (unbinding ? "Unbinding" : "Undoing the Memory weave") + " takes " + cost + " essence.");
            return false;
        }
        pay(player, menu, payment.get());
        if (unbinding) {
            menu.anchor().shrink(1);
            piece.remove(WeavingRegistry.BOUND_TO.get());
        }
        WovenStrands rest = strands(piece).without(index);
        if (rest.isEmpty()) {
            piece.remove(WeavingRegistry.WOVEN_STRANDS.get());
        } else {
            piece.set(WeavingRegistry.WOVEN_STRANDS.get(), rest);
        }
        menu.table().setChanged();
        WeavingService.set(player, WeavingService.get(player).withXp(FORGERY, UNRAVEL_XP));
        WeavingService.report(player, unbinding ? "The bond is cut. Anyone may wield it now." : "The Memory weave comes undone. It is only a piece again.");
        player.level().playSound(null, player.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }

    /** Lays the first Woven Thread in the material slots into the centre piece. */
    public static boolean layThread(ServerPlayer player, WeavingMenu menu) {
        int slot = -1;
        for (int i = WeavingMenu.FIRST_MATERIAL; i < WeavingMenu.OUTPUT_SLOT; i++) {
            if (menu.table().getItem(i).is(WeavingRegistry.WOVEN_THREAD.get())) {
                slot = i;
                break;
            }
        }
        if (slot < 0) {
            WeavingService.report(player, "Lay a Woven Thread in a material slot first.");
            return false;
        }
        ItemStack threadStack = menu.table().getItem(slot);
        WovenStrands carried = strands(threadStack);
        if (carried.isEmpty()) {
            WeavingService.report(player, "This thread is empty.");
            return false;
        }
        WovenStrands.Laid strand = carried.strands().getFirst();
        Optional<StrandPattern> pattern = WeavingContent.current().strand(strand.id());
        ItemStack piece = menu.centre();
        Optional<StrandFit> fit = StrandFit.of(piece);
        String name = pattern.map(StrandPattern::name).orElse(strand.id().getPath());
        if (pattern.isEmpty() || fit.isEmpty() || !pattern.get().fits(fit.get())) {
            WeavingService.report(player, fit.isEmpty() ? "Lay a weapon, tool or piece of armour in the centre."
                    : name + " doesn't suit " + fit.get().plural() + ".");
            return false;
        }
        WovenStrands laid = strands(piece);
        if (laid.strands().stream().anyMatch(existing -> existing.id().equals(strand.id()))) {
            WeavingService.report(player, "This piece already holds " + name + ".");
            return false;
        }
        if (laid.size() >= threads(piece)) {
            WeavingService.report(player, "Every thread of this piece is taken.");
            return false;
        }
        int cost = StrandRules.discounted(StrandRules.LAY_ESSENCE, essencePercent(player));
        Optional<StrandRules.Payment> payment = StrandRules.pay(cost, (int) EssenceService.current(player),
                menu.anchor().getCount(), anchorValue(menu.anchor()));
        if (payment.isEmpty()) {
            WeavingService.report(player, "Laying a thread takes " + cost + " essence.");
            return false;
        }
        pay(player, menu, payment.get());
        piece.set(WeavingRegistry.WOVEN_STRANDS.get(), laid.with(strand));
        threadStack.shrink(1);
        menu.table().setChanged();
        WeavingService.set(player, WeavingService.get(player).withXp(FORGERY, LAY_XP));
        WeavingService.report(player, name + " is laid into the piece.");
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
        return true;
    }

    public static void forget(UUID player) {
        STUDYING.remove(player);
    }
}
