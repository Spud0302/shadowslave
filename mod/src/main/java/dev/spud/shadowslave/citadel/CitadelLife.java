package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.clan.ClanData;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.fieldguide.FieldGuideData;
import dev.spud.shadowslave.fieldguide.FieldGuideService;
import dev.spud.shadowslave.fieldguide.MapSampler;
import dev.spud.shadowslave.network.payload.GatewayActionPayload;
import dev.spud.shadowslave.network.payload.GatewayStatusPayload;
import dev.spud.shadowslave.smithing.SmithingRegistry;
import dev.spud.shadowslave.weaving.WeavingRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Citadel life (DESIGN, Run C, Jack 2026-10-30): what keeps a claimed Citadel
 * standing and what a clan builds there. The Gateway hungers, more for a
 * bigger clan, and members feed it soul shards and materials; neglect drops
 * its wards, then slows its workshops, then silences the Guardian's echo, but
 * never loses the Citadel. Members pay for upgrades at the Gateway, each read
 * from data by its id:
 * <ul>
 *     <li>{@code wards}: no hostile mob spawns within {@code radius} of the
 *     Gateway, an intruder {@code alarm}, and at the top tier intruders
 *     are {@code mark}ed with a glow;</li>
 *     <li>{@code workshops}: a Forge Hearth and a Weaving Table in the
 *     courtyard, with {@code forge_score} added to pieces forged on the
 *     clan's land and Weaving's essence at {@code weave_essence_percent};</li>
 *     <li>{@code hall}: how many members may {@code anchors} to the Gateway;</li>
 *     <li>{@code archive}: the Field Guide board reaches members away from the
 *     Citadel every {@code remote_seconds}, and in the {@code waking} world
 *     at the top tier;</li>
 *     <li>{@code lanterns}: lantern posts on a {@code ring} around the walls,
 *     and the board charts {@code reveal_chunks} around the Citadel;</li>
 *     <li>{@code gateway_links}: instant travel to the clan's other Citadels
 *     for {@code essence} a trip.</li>
 * </ul>
 * Raids are in {@link CitadelRaids}.
 */
public final class CitadelLife {
    public static final String WARDS = "wards";
    public static final String WORKSHOPS = "workshops";
    public static final String HALL = "hall";
    public static final String ARCHIVE = "archive";
    public static final String LANTERNS = "lanterns";
    public static final String LINKS = "gateway_links";

    /** How often the Gateways eat, in ticks. */
    static final int UPKEEP_INTERVAL = 100;
    /** How often the lanterns' map reveal is sampled, in ticks. */
    static final int REVEAL_INTERVAL = 600;
    /** How close to the Gateway a player must stand to use its screen or its links. */
    static final double REACH = 8.0;
    static final long ALARM_COOLDOWN = 60 * 20;
    static final int ANCHOR_SLOTS_DEFAULT = 4;

    /** When each Citadel last raised the alarm about each intruder; not saved. */
    private static final Map<String, Long> ALARMS = new HashMap<>();
    /** When each player last shared with the board from afar; not saved. */
    private static final Map<UUID, Long> REMOTE_SHARES = new HashMap<>();

    private CitadelLife() {
    }

    static CitadelContentLoader.Content content() {
        return CitadelContentLoader.content();
    }

    // ---- upkeep --------------------------------------------------------------------------------------------------

    static int members(MinecraftServer server, CitadelData.Citadel citadel) {
        return Optional.ofNullable(citadel.clan).flatMap(id -> ClanService.byId(server, id)).map(clan -> clan.members().size()).orElse(1);
    }

    public static double perDay(MinecraftServer server, CitadelData.Citadel citadel) {
        return content().upkeep().perDay(members(server, citadel));
    }

    public static double capacity(MinecraftServer server, CitadelData.Citadel citadel) {
        return content().upkeep().capacity(members(server, citadel));
    }

    public static Neglect stage(CitadelData.Citadel citadel) {
        return content().upkeep().stage(citadel.provisions, citadel.hungryTicks);
    }

    /** A freshly claimed Citadel starts with a few days of provisions and nothing built over again. */
    static void claimed(MinecraftServer server, CitadelData.Citadel citadel) {
        citadel.provisions = perDay(server, citadel) * content().upkeep().claimDays();
        citadel.hungryTicks = 0;
        citadel.stage = Neglect.FED;
        citadel.anchored.clear();
        citadel.raid = null;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel realm = DreamRealm.level(server);
        if (realm == null) {
            return;
        }
        int tick = server.getTickCount();
        CitadelData data = CitadelData.get(realm);
        boolean changed = false;
        for (CitadelData.Citadel citadel : data.citadels) {
            if (citadel.hold != CitadelData.Hold.CLAN) {
                continue;
            }
            if (tick % UPKEEP_INTERVAL == 0) {
                eat(server, citadel, UPKEEP_INTERVAL);
                changed = true;
            }
            if (tick % REVEAL_INTERVAL == 0) {
                reveal(realm, citadel);
            }
            if (citadel.raid != null && tick % CitadelRaids.TICK_INTERVAL == 0) {
                CitadelRaids.tick(realm, citadel);
                changed = true;
            }
        }
        if (changed) {
            data.setDirty();
        }
    }

    /** The Gateway eats {@code ticks}' worth of provisions; at none, neglect deepens, and the clan hears of each new stage. */
    static void eat(MinecraftServer server, CitadelData.Citadel citadel, long ticks) {
        double meal = perDay(server, citadel) * ticks / CitadelUpkeep.DAY_TICKS * CitadelWars.drain(citadel);
        if (citadel.provisions > meal) {
            citadel.provisions -= meal;
        } else {
            // Only the time after the last meal ran out counts as hunger.
            long fed = meal <= 0 ? ticks : Math.round(ticks * citadel.provisions / meal);
            citadel.provisions = 0;
            citadel.hungryTicks += ticks - fed;
        }
        announce(server, citadel);
    }

    /** Tells the clan when the Gateway's stage of neglect has changed since they last heard. */
    static void announce(MinecraftServer server, CitadelData.Citadel citadel) {
        Neglect now = stage(citadel);
        if (now == citadel.stage) {
            return;
        }
        Neglect was = citadel.stage;
        citadel.stage = now;
        String line = switch (now) {
            case FED -> "The Gateway of " + citadel.name + " is fed again" + (was.ordinal() > Neglect.HUNGRY.ordinal()
                    ? ". Everything it had let fall stands again." : ". Its wards rise.");
            case HUNGRY -> "The Gateway of " + citadel.name + " has eaten the last of its provisions. Its wards go dark.";
            case STARVING -> "The Gateway of " + citadel.name + " is starving. Its workshops grow cold.";
            case FORSAKEN -> "The Gateway of " + citadel.name + " is forsaken. The Guardian's echo no longer answers for it.";
        };
        Optional.ofNullable(citadel.clan).flatMap(id -> ClanService.byId(server, id)).ifPresent(clan -> ClanService.tell(server, clan, line));
    }

    /**
     * Feeds the Gateway what the player holds in their main hand, as much as it
     * has room for. Returns whether the item feeds a Gateway at all, so the
     * caller knows the use was spent on feeding.
     */
    public static boolean feedFromHand(ServerPlayer player, CitadelData.Citadel citadel) {
        ItemStack hand = player.getMainHandItem();
        Optional<Double> worth = content().upkeep().worth(hand);
        if (worth.isEmpty()) {
            return false;
        }
        if (citadel.linkCut) {
            player.displayClientMessage(Component.literal("The supply line of " + citadel.name + " is cut. Mend its Link Anchor before"
                    + " the Gateway can eat.").withStyle(ChatFormatting.DARK_RED), true);
            return true;
        }
        double room = capacity(player.server, citadel) - citadel.provisions;
        if (room < worth.get() / 2) {
            player.displayClientMessage(Component.literal("The Gateway of " + citadel.name + " is full.").withStyle(ChatFormatting.GRAY), true);
            return true;
        }
        int eaten = Math.max(1, Math.min(hand.getCount(), (int) Math.ceil(room / worth.get())));
        feed(player.server, citadel, eaten * worth.get());
        if (!player.getAbilities().instabuild) {
            hand.shrink(eaten);
        }
        ServerLevel level = player.serverLevel();
        level.playSound(null, citadel.gateway, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.8F, 1.2F);
        level.sendParticles(ParticleTypes.SOUL, citadel.gateway.getX() + 0.5, citadel.gateway.getY() + 0.9, citadel.gateway.getZ() + 0.5,
                12 + eaten, 0.3, 0.4, 0.3, 0.03);
        player.displayClientMessage(Component.literal("The Gateway eats " + eaten + " " + hand.getHoverName().getString() + ": "
                + describeDays(citadel.provisions / perDay(player.server, citadel)) + " of provisions.")
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        CitadelData.get(level).setDirty();
        return true;
    }

    /** Adds provisions, up to the Gateway's capacity; any meal ends its hunger. */
    public static void feed(MinecraftServer server, CitadelData.Citadel citadel, double points) {
        citadel.provisions = Math.min(capacity(server, citadel), citadel.provisions + points);
        if (citadel.provisions > 0) {
            citadel.hungryTicks = 0;
        }
        announce(server, citadel);
    }

    static String describeDays(double days) {
        if (days >= 1.95) {
            return String.format(Locale.ROOT, "%.0f days", Math.floor(days + 0.05));
        }
        if (days >= 0.95) {
            return "a day";
        }
        long hours = Math.max(1, Math.round(days * 24));
        return hours + (hours == 1 ? " hour" : " hours");
    }

    /** What feeds a Gateway, in a few words for the screen. */
    static String feedHint() {
        List<String> names = new ArrayList<>();
        for (Cost entry : content().upkeep().feed()) {
            String name = entry.item().map(id -> net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id).getDescription().getString())
                    .orElseGet(() -> entry.tag().map(Cost::tagName).orElse("?"));
            names.add(name + " " + trim(entry.points()));
        }
        return names.isEmpty() ? "Nothing feeds it." : String.join(", ", names);
    }

    private static String trim(double value) {
        return value == Math.floor(value) ? Integer.toString((int) value) : String.format(Locale.ROOT, "%.1f", value);
    }

    // ---- upgrades ------------------------------------------------------------------------------------------------

    /** The tier an upgrade works at: as built, one lower if damaged, nothing if neglect has taken it. */
    public static int workingTier(CitadelData.Citadel citadel, String id) {
        Optional<CitadelUpgrade> upgrade = content().upgrade(id);
        if (upgrade.isEmpty() || citadel.hold != CitadelData.Hold.CLAN || !upgrade.get().worksAt(stage(citadel))) {
            return 0;
        }
        return Math.max(0, citadel.tier(id) - (citadel.damaged(id) ? 1 : 0));
    }

    /** One of an upgrade's values as the Citadel stands now. */
    public static double value(CitadelData.Citadel citadel, String id, String key) {
        return content().upgrade(id).map(upgrade -> upgrade.value(workingTier(citadel, id), key)).orElse(0.0);
    }

    /** Builds an upgrade's next tier for the player's clan, if they carry its cost and the Gateway is fed. */
    public static String upgrade(ServerPlayer player, CitadelData.Citadel citadel, String id) {
        Optional<CitadelUpgrade> found = content().upgrade(id);
        if (found.isEmpty()) {
            return "There is no such upgrade.";
        }
        CitadelUpgrade upgrade = found.get();
        int tier = citadel.tier(id);
        if (tier >= upgrade.maxTier()) {
            return upgrade.name() + " is built as high as it goes.";
        }
        if (citadel.damaged(id)) {
            return "Repair " + upgrade.name() + " before you build on it.";
        }
        if (stage(citadel) != Neglect.FED) {
            return "The Gateway hungers. Feed it before you build.";
        }
        if (!pay(player, upgrade.tiers().get(tier).cost(), 1.0)) {
            return "You do not carry what " + upgrade.name() + " " + CitadelUpgrade.numeral(tier + 1) + " costs.";
        }
        citadel.tiers.put(id, tier + 1);
        ServerLevel level = player.serverLevel();
        CitadelData.get(level).setDirty();
        built(level, citadel, id);
        level.playSound(null, citadel.gateway, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.1F);
        level.sendParticles(ParticleTypes.END_ROD, citadel.gateway.getX() + 0.5, citadel.gateway.getY() + 1.2,
                citadel.gateway.getZ() + 0.5, 40, 0.6, 0.8, 0.6, 0.05);
        String name = upgrade.name() + " " + CitadelUpgrade.numeral(tier + 1);
        Optional.ofNullable(citadel.clan).flatMap(clan -> ClanService.byId(player.server, clan)).ifPresent(clan ->
                ClanService.tell(player.server, clan, player.getGameProfile().getName() + " built " + name + " at " + citadel.name + "."));
        return name + " stands.";
    }

    /** Repairs an upgrade a lost raid damaged, for a share of its tier's cost. */
    public static String repair(ServerPlayer player, CitadelData.Citadel citadel, String id) {
        Optional<CitadelUpgrade> upgrade = content().upgrade(id);
        if (upgrade.isEmpty() || !citadel.damaged(id)) {
            return "Nothing here needs repair.";
        }
        if (!pay(player, repairCost(upgrade.get(), citadel.tier(id)), 1.0)) {
            return "You do not carry what repairing " + upgrade.get().name() + " costs.";
        }
        citadel.damaged.remove(id);
        ServerLevel level = player.serverLevel();
        CitadelData.get(level).setDirty();
        built(level, citadel, id);
        level.playSound(null, citadel.gateway, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.7F, 1.0F);
        return upgrade.get().name() + " is whole again.";
    }

    /** A damaged tier's repair: its cost, each count scaled by the repair share and rounded up. */
    static List<Cost> repairCost(CitadelUpgrade upgrade, int tier) {
        if (tier <= 0 || tier > upgrade.maxTier()) {
            return List.of();
        }
        double share = content().raids().repairShare();
        return upgrade.tiers().get(tier - 1).cost().stream()
                .map(cost -> new Cost(cost.item(), cost.tag(), Math.max(1, (int) Math.ceil(cost.count() * share)), cost.points()))
                .toList();
    }

    /** Takes a cost from the player's inventory, all or nothing. Creative players pay nothing. */
    static boolean pay(ServerPlayer player, List<Cost> cost, double share) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        for (Cost each : cost) {
            if (carried(player, each::matches) < Math.ceil(each.count() * share)) {
                return false;
            }
        }
        for (Cost each : cost) {
            int left = (int) Math.ceil(each.count() * share);
            for (ItemStack stack : slots(player)) {
                if (left <= 0) {
                    break;
                }
                if (each.matches(stack)) {
                    int taken = Math.min(left, stack.getCount());
                    stack.shrink(taken);
                    left -= taken;
                }
            }
        }
        player.getInventory().setChanged();
        return true;
    }

    static int carried(ServerPlayer player, Predicate<ItemStack> what) {
        int count = 0;
        for (ItemStack stack : slots(player)) {
            if (what.test(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static List<ItemStack> slots(ServerPlayer player) {
        List<ItemStack> slots = new ArrayList<>(player.getInventory().items);
        slots.addAll(player.getInventory().offhand);
        return slots;
    }

    /** What building or repairing an upgrade raises in the world. */
    static void built(ServerLevel level, CitadelData.Citadel citadel, String id) {
        if (!level.isLoaded(citadel.gateway)) {
            return;
        }
        switch (id) {
            case WORKSHOPS -> raiseWorkshops(level, citadel);
            case LANTERNS -> raiseLanterns(level, citadel);
            case LINKS -> CitadelWars.raiseAnchor(level, citadel);
            default -> {
            }
        }
    }

    /** A Forge Hearth, a Smithing Anvil and a Quench Trough west of the dais, a Weaving Table east of it. */
    static void raiseWorkshops(ServerLevel level, CitadelData.Citadel citadel) {
        BlockPos floor = citadel.gateway.below();
        place(level, floor.offset(-6, 0, -2), SmithingRegistry.FORGE_HEARTH.get().defaultBlockState());
        place(level, floor.offset(-6, 0, 0), SmithingRegistry.SMITHING_ANVIL.get().defaultBlockState());
        place(level, floor.offset(-6, 0, 2), SmithingRegistry.QUENCH_TROUGH.get().defaultBlockState());
        place(level, floor.offset(6, 0, 0), WeavingRegistry.WEAVING_TABLE.get().defaultBlockState());
    }

    private static void place(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.getBlockState(pos).isAir() && !level.getBlockState(pos).is(state.getBlock())) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
            level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.02);
        }
    }

    /** Lantern posts every few blocks on a ring around the walls, out to the built tier's ring. */
    static void raiseLanterns(ServerLevel level, CitadelData.Citadel citadel) {
        int ring = (int) value(citadel, LANTERNS, "ring");
        if (ring <= 0) {
            return;
        }
        CitadelPalette palette = CitadelPalette.DEFAULT;
        int posts = Math.max(8, (int) Math.round(2 * Math.PI * ring / 8));
        for (int i = 0; i < posts; i++) {
            double angle = 2 * Math.PI * i / posts;
            int x = citadel.gateway.getX() + (int) Math.round(Math.cos(angle) * ring);
            int z = citadel.gateway.getZ() + (int) Math.round(Math.sin(angle) * ring);
            if (!level.hasChunk(x >> 4, z >> 4)) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos base = new BlockPos(x, y, z);
            BlockPos ground = base.below();
            if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP) || !level.getFluidState(ground).isEmpty()
                    || Math.abs(y - citadel.gateway.getY()) > 12) {
                continue;
            }
            if (!level.getBlockState(base).canBeReplaced() || !level.getBlockState(base.above()).isAir() || !level.getBlockState(base.above(2)).isAir()) {
                continue;
            }
            level.setBlock(base, palette.pillar(), Block.UPDATE_ALL);
            level.setBlock(base.above(), palette.pillar(), Block.UPDATE_ALL);
            BlockState lantern = palette.light();
            level.setBlock(base.above(2), lantern.hasProperty(LanternBlock.HANGING) ? lantern.setValue(LanternBlock.HANGING, false) : lantern,
                    Block.UPDATE_ALL);
        }
    }

    // ---- the upgrades at work ------------------------------------------------------------------------------------

    /** Wards: no hostile mob takes shape naturally within the radius of a warded Gateway. */
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        MobSpawnType type = event.getSpawnType();
        if (type != MobSpawnType.NATURAL && type != MobSpawnType.CHUNK_GENERATION && type != MobSpawnType.PATROL) {
            return;
        }
        if (!(event.getEntity() instanceof Enemy) || !(event.getLevel().getLevel() instanceof ServerLevel level) || !DreamRealm.is(level)) {
            return;
        }
        BlockPos at = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        if (warded(level, at)) {
            event.setSpawnCancelled(true);
        }
    }

    /** Whether a warded Gateway's radius covers this spot. */
    public static boolean warded(ServerLevel level, BlockPos at) {
        for (CitadelData.Citadel citadel : CitadelData.get(level).citadels) {
            double radius = citadel.hold == CitadelData.Hold.CLAN ? value(citadel, WARDS, "radius") : 0;
            if (radius > 0 && citadel.gateway.distSqr(at) <= radius * radius) {
                return true;
            }
        }
        return false;
    }

    /** Wards' alarm, the Archive's reach, once a second for each player. */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0 || player.isSpectator()) {
            return;
        }
        if (DreamRealm.is(player.level())) {
            alarm(player);
        }
        if (player.tickCount % 100 == 0) {
            shareFromAfar(player);
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        REMOTE_SHARES.remove(event.getEntity().getUUID());
    }

    /** A stranger inside a warded Citadel's radius sets off its alarm; at the top tier the wards mark them. */
    static void alarm(ServerPlayer player) {
        if (player.isCreative()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Optional<UUID> own = ClanService.clanOf(player.server, player.getUUID()).map(ClanData.Clan::id);
        for (CitadelData.Citadel citadel : CitadelData.get(level).citadels) {
            if (citadel.hold != CitadelData.Hold.CLAN || citadel.clan == null || own.filter(citadel.clan::equals).isPresent()) {
                continue;
            }
            double radius = value(citadel, WARDS, "radius");
            if (radius <= 0 || value(citadel, WARDS, "alarm") <= 0 || citadel.gateway.distSqr(player.blockPosition()) > radius * radius) {
                continue;
            }
            String key = citadel.id + "/" + player.getUUID();
            long now = level.getGameTime();
            if (now - ALARMS.getOrDefault(key, -ALARM_COOLDOWN) < ALARM_COOLDOWN) {
                continue;
            }
            ALARMS.put(key, now);
            level.playSound(null, citadel.gateway, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 0.7F);
            if (value(citadel, WARDS, "mark") > 0) {
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
            }
            player.displayClientMessage(Component.literal("The wards of " + citadel.name + " have noticed you.")
                    .withStyle(ChatFormatting.DARK_PURPLE), true);
            ClanService.byId(player.server, citadel.clan).ifPresent(clan -> ClanService.tell(player.server, clan,
                    "The wards of " + citadel.name + " ring: " + player.getGameProfile().getName() + " is inside them."));
        }
    }

    /** The Archive: members share with the board from afar, as often as the best Archive of their clan allows. */
    static void shareFromAfar(ServerPlayer player) {
        ServerLevel realm = DreamRealm.level(player.server);
        Optional<ClanData.Clan> clan = ClanService.clanOf(player.server, player.getUUID());
        if (realm == null || clan.isEmpty()) {
            return;
        }
        boolean inRealm = DreamRealm.is(player.level());
        double every = 0;
        for (CitadelData.Citadel citadel : CitadelData.get(realm).citadels) {
            if (citadel.hold != CitadelData.Hold.CLAN || !clan.get().id().equals(citadel.clan)) {
                continue;
            }
            double seconds = value(citadel, ARCHIVE, "remote_seconds");
            if (seconds > 0 && (inRealm || value(citadel, ARCHIVE, "waking") > 0)) {
                every = every <= 0 ? seconds : Math.min(every, seconds);
            }
        }
        if (every <= 0) {
            return;
        }
        long now = player.server.overworld().getGameTime();
        Long last = REMOTE_SHARES.get(player.getUUID());
        if (last == null) {
            REMOTE_SHARES.put(player.getUUID(), now);
            return;
        }
        if (now - last < every * 20) {
            return;
        }
        REMOTE_SHARES.put(player.getUUID(), now);
        FieldGuideData.Learned learned = ClanService.shareBoard(player, clan.get());
        if (learned.any()) {
            player.displayClientMessage(Component.literal("The Archive sends word: your Field Guide learns from the clan's board.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
    }

    /** Lanterns: the clan's board charts the land around the Citadel, as far as is loaded. */
    static void reveal(ServerLevel realm, CitadelData.Citadel citadel) {
        int reach = (int) value(citadel, LANTERNS, "reveal_chunks");
        if (reach <= 0 || citadel.clan == null || !realm.isLoaded(citadel.gateway)) {
            return;
        }
        Optional<ClanData.Clan> clan = ClanService.byId(realm.getServer(), citadel.clan);
        if (clan.isEmpty()) {
            return;
        }
        FieldGuideData board = clan.get().board();
        String dimension = FieldGuideService.dimension(realm);
        ChunkPos centre = new ChunkPos(citadel.gateway);
        boolean any = false;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                int cx = centre.x + dx;
                int cz = centre.z + dz;
                long key = ChunkPos.asLong(cx, cz);
                if (dx * dx + dz * dz > reach * reach || board.explored(dimension, key) || !realm.hasChunk(cx, cz)) {
                    continue;
                }
                LevelChunk chunk = realm.getChunk(cx, cz);
                any |= board.explore(dimension, key, MapSampler.sample(realm, chunk), FieldGuideService.maxChunks());
            }
        }
        if (any) {
            ClanData.get(realm.getServer()).setDirty();
        }
    }

    /** The Citadel of the player's clan whose land they stand on, nearest first. */
    public static Optional<CitadelData.Citadel> home(ServerPlayer player) {
        if (!DreamRealm.is(player.level())) {
            return Optional.empty();
        }
        double reach = CitadelContentLoader.settings().landRadius();
        return CitadelService.clanCitadels(player, player.serverLevel(), player.blockPosition()).stream()
                .filter(citadel -> citadel.gateway.distSqr(player.blockPosition()) <= reach * reach).findFirst();
    }

    /** Workshops: what a piece forged on the clan's land adds to its score. */
    public static int forgeBonus(ServerPlayer player) {
        return home(player).map(citadel -> (int) value(citadel, WORKSHOPS, "forge_score")).orElse(0);
    }

    /** Workshops: the share of Weaving's essence costs paid on the clan's land, in percent. */
    public static int weaveEssencePercent(ServerPlayer player) {
        return home(player).map(citadel -> value(citadel, WORKSHOPS, "weave_essence_percent"))
                .filter(percent -> percent > 0).map(percent -> Mth.clamp((int) Math.round(percent), 0, 100)).orElse(100);
    }

    /** The Hall: how many members may anchor to this Gateway. */
    public static int anchorSlots(CitadelData.Citadel citadel) {
        double slots = value(citadel, HALL, "anchors");
        return slots > 0 ? (int) slots : content().upgrade(HALL).isPresent() ? 0 : ANCHOR_SLOTS_DEFAULT;
    }

    /** Members anchored here, forgetting those who have left the clan or are known to have anchored elsewhere. */
    static int anchoredCount(MinecraftServer server, CitadelData.Citadel citadel) {
        Optional<ClanData.Clan> clan = Optional.ofNullable(citadel.clan).flatMap(id -> ClanService.byId(server, id));
        citadel.anchored.removeIf(member -> {
            if (clan.isEmpty() || !clan.get().has(member)) {
                return true;
            }
            ServerPlayer online = server.getPlayerList().getPlayer(member);
            return online != null && DreamCrossing.data(online).anchor().filter(citadel.gateway::equals).isEmpty();
        });
        return citadel.anchored.size();
    }

    /** Whether one more member may anchor here; the Hall sets the room. */
    public static boolean hasRoom(ServerPlayer player, CitadelData.Citadel citadel) {
        return citadel.anchored.contains(player.getUUID()) || anchoredCount(player.server, citadel) < anchorSlots(citadel);
    }

    // ---- Gateway Links -------------------------------------------------------------------------------------------

    /** Essence a trip through this Gateway's Links costs, or -1 while it has none. */
    public static int travelCost(CitadelData.Citadel citadel) {
        return workingTier(citadel, LINKS) > 0 ? (int) value(citadel, LINKS, "essence") : -1;
    }

    /** Travels from one of the clan's Gateways to another, if its Links are built and the essence is there. */
    public static String travel(ServerPlayer player, CitadelData.Citadel from, UUID to) {
        ServerLevel realm = player.serverLevel();
        int cost = travelCost(from);
        if (cost < 0) {
            return "The Gateway of " + from.name + " has no Links built.";
        }
        Optional<CitadelData.Citadel> target = CitadelData.get(realm).byId(to)
                .filter(found -> found.hold == CitadelData.Hold.CLAN && found.clan != null && found.clan.equals(from.clan) && found != from);
        if (target.isEmpty()) {
            return "That Citadel is not your clan's.";
        }
        if (from.linkCut || target.get().linkCut) {
            return "The supply line of " + (from.linkCut ? from.name : target.get().name) + " is cut. Its Links are dark until it is mended.";
        }
        if (!player.getAbilities().instabuild && !EssenceService.covers(player, cost)) {
            return "A trip costs " + cost + " essence. You do not have it.";
        }
        if (!player.getAbilities().instabuild) {
            EssenceService.spend(player, cost);
        }
        realm.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(), 60, 0.4, 0.8, 0.4, 0.1);
        realm.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.7F);
        realm.getChunk(target.get().gateway);
        Vec3 arrive = DreamCrossing.standBeside(realm, target.get().gateway);
        player.teleportTo(realm, arrive.x, arrive.y, arrive.z, player.getYRot(), player.getXRot());
        realm.sendParticles(ParticleTypes.REVERSE_PORTAL, arrive.x, arrive.y + 1, arrive.z, 60, 0.4, 0.8, 0.4, 0.1);
        realm.playSound(null, BlockPos.containing(arrive), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.9F);
        player.displayClientMessage(Component.literal("The Gateway Links carry you to " + target.get().name + ".")
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return "";
    }

    // ---- the Gateway screen --------------------------------------------------------------------------------------

    /** Opens (or refreshes) the Gateway screen for a member standing at it. */
    public static void open(ServerPlayer player, CitadelData.Citadel citadel, String message) {
        if (player.connection == null || !player.connection.hasChannel(GatewayStatusPayload.TYPE)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, status(player, citadel, message));
    }

    public static GatewayStatusPayload status(ServerPlayer player, CitadelData.Citadel citadel, String message) {
        MinecraftServer server = player.server;
        Optional<ClanData.Clan> clan = Optional.ofNullable(citadel.clan).flatMap(id -> ClanService.byId(server, id));
        double perDay = perDay(server, citadel);
        Neglect stage = stage(citadel);
        String stageLine = switch (stage) {
            case FED -> "Fed for " + describeDays(citadel.provisions / perDay) + ".";
            case HUNGRY -> "Hungry: its wards are down.";
            case STARVING -> "Starving: its wards are down and its workshops cold.";
            case FORSAKEN -> "Forsaken: the Guardian's echo no longer defends it.";
        };
        List<GatewayStatusPayload.Upgrade> upgrades = new ArrayList<>();
        for (Map.Entry<String, CitadelUpgrade> entry : content().upgrades().entrySet()) {
            String id = entry.getKey();
            CitadelUpgrade upgrade = entry.getValue();
            int tier = citadel.tier(id);
            String now = tier <= 0 ? "Not built." : upgrade.tiers().get(tier - 1).summary();
            String next = tier >= upgrade.maxTier() ? "" : upgrade.tiers().get(tier).summary();
            List<GatewayStatusPayload.Need> cost = tier >= upgrade.maxTier() ? List.of() : needs(player, upgrade.tiers().get(tier).cost());
            List<GatewayStatusPayload.Need> repair = citadel.damaged(id) ? needs(player, repairCost(upgrade, tier)) : List.of();
            upgrades.add(new GatewayStatusPayload.Upgrade(id, upgrade.name(), upgrade.description(), tier, upgrade.maxTier(),
                    citadel.damaged(id), tier == 0 || upgrade.worksAt(stage), now, next, cost, repair));
        }
        List<GatewayStatusPayload.Link> links = new ArrayList<>();
        int travel = travelCost(citadel);
        if (travel >= 0) {
            for (CitadelData.Citadel other : CitadelService.clanCitadels(player, player.serverLevel(), citadel.gateway)) {
                if (other != citadel) {
                    links.add(new GatewayStatusPayload.Link(other.id.toString(), other.name, (int) Math.sqrt(other.gateway.distSqr(citadel.gateway))));
                }
            }
        }
        return new GatewayStatusPayload(citadel.id.toString(), citadel.name, clan.map(ClanData.Clan::name).orElse(""),
                clan.map(found -> found.members().size()).orElse(1), citadel.rank.displayName(),
                (float) citadel.provisions, (float) capacity(server, citadel), (float) perDay, stage.ordinal(), stageLine, feedHint(),
                upgrades, anchoredCount(server, citadel), anchorSlots(citadel), CitadelRaids.describe(player.serverLevel(), citadel),
                travel, links, CitadelWars.isSeat(server, citadel), CitadelWars.lines(server, citadel), message);
    }

    private static List<GatewayStatusPayload.Need> needs(ServerPlayer player, List<Cost> cost) {
        return cost.stream().map(each -> new GatewayStatusPayload.Need(each.describe(),
                player.getAbilities().instabuild || carried(player, each::matches) >= each.count())).toList();
    }

    /** A request from the Gateway screen; answered by refreshing it. */
    public static void handle(ServerPlayer player, GatewayActionPayload action) {
        if (!DreamRealm.is(player.level())) {
            return;
        }
        Optional<CitadelData.Citadel> citadel;
        try {
            citadel = CitadelData.get(player.serverLevel()).byId(UUID.fromString(action.citadel()));
        } catch (IllegalArgumentException exception) {
            return;
        }
        if (citadel.isEmpty() || citadel.get().hold != CitadelData.Hold.CLAN
                || citadel.get().gateway.distToCenterSqr(player.position()) > REACH * REACH * 4) {
            return;
        }
        Optional<ClanData.Clan> clan = ClanService.byId(player.server, citadel.get().clan);
        if (clan.isEmpty() || !clan.get().has(player.getUUID())) {
            return;
        }
        String message = switch (action.action()) {
            case GatewayActionPayload.UPGRADE -> upgrade(player, citadel.get(), action.target());
            case GatewayActionPayload.REPAIR -> repair(player, citadel.get(), action.target());
            case GatewayActionPayload.TRAVEL -> {
                try {
                    String result = travel(player, citadel.get(), UUID.fromString(action.target()));
                    if (result.isEmpty()) {
                        yield null;
                    }
                    yield result;
                } catch (IllegalArgumentException exception) {
                    yield "";
                }
            }
            default -> "";
        };
        if (message != null) {
            open(player, citadel.get(), message);
        }
    }
}
