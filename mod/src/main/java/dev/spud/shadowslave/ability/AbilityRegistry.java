package dev.spud.shadowslave.ability;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Activatable abilities by id. Ability ids without an entry here are passive or not built yet. */
public final class AbilityRegistry {
    private static final Map<ResourceLocation, AspectAbility> ABILITIES = new LinkedHashMap<>();

    static {
        register(new KindleAbility());
        register(new MistPassageAbility());
        register(new HoldfastAbility());
        register(new ReturningTideAbility());
        register(new CarryTheFlameAbility());
        register(new EmberWardAbility());
        register(new ThroughTheHazeAbility());
        register(new VeilAbility());
        register(new RootInStoneAbility());
        register(new QuakeAbility());
        register(new LowTideAbility());
        register(new UndertowAbility());
        register(new MarkPreyAbility());
        register(new SnareSigilAbility());
        register(new RelentlessHuntAbility());
        register(new ForesightAbility());
        register(new FateThreadAbility());
        register(new SharedSightAbility());
        register(new SoulSiphonAbility());
        register(new HarvestAbility());
        register(new ReapingArcAbility());
        register(new EssenceGiftAbility());
        register(new SoulWardAbility());
        register(new OverflowAbility());
        register(new QuenchAbility());
        register(new MasterworkAbility());
        register(new MoltenEdgeAbility());
        register(new TunnelStrikeAbility());
        register(new VeinPullAbility());
        register(new StonewalkAbility());
        register(new RaiseWallAbility());
        register(new BlueprintAbility());
        register(new ScaffoldAbility());
        register(new BloomAbility());
        register(new LivingWallAbility());
        register(new WildCallAbility());
        register(new WaymarkAbility());
        register(new AnchorRecallAbility());
        register(new TrailblazeAbility());
        register(new LullabyAbility());
        register(new DreamfallAbility());
        register(new SleepwalkerAbility());
        register(new GloamEdgeAbility());
        register(new ShadeStepAbility());
        register(new SnuffAbility());
        register(new IceLanceAbility());
        register(new EncaseAbility());
        register(new RimepathAbility());
        register(new ProvokeAbility());
        register(new GroundSlamAbility());
        register(new SecondWindAbility());
        register(new PocketAbility());
        register(new DoorwayAbility());
        register(new BanishAbility());
        register(new ShiftWeightAbility());
        register(new CrushAbility());
        register(new GravityWellAbility());
        register(new StasisAbility());
        register(new RewindAbility());
        register(new StillMomentAbility());
        register(new WarBannerAbility());
        register(new RallyCallAbility());
        register(new OathOfArmsAbility());
        register(new SanctuaryAbility());
        register(new BulwarkAbility());
        register(new GatewayStepAbility());
        register(new CoinOfFateAbility());
        register(new TwistFateAbility());
        register(new BorrowedLuckAbility());
        register(new CommandWordAbility());
        register(new SilencingWordAbility());
        register(new RallyingNameAbility());
        register(new RallyAbility());
        register(new PackTacticsAbility());
        register(new EchoMergeAbility());
        register(new SwapPlacesAbility());
        register(new SharedWoundsAbility());
        register(new BorrowedFaceAbility());
        register(new MirrorSwapAbility());
        register(new StolenVoiceAbility());
        register(new LastStandAbility());
        register(new GritAbility());
        register(new DefianceAbility());
        register(new FirestormAbility());
        register(new PhoenixRiteAbility());
        register(new PhantomStepAbility());
        register(new SmotherAbility());
        register(new StoneSpikesAbility());
        register(new StoneskinAbility());
        register(new MaelstromAbility());
        register(new HighTideAbility());
        register(new LivingFlameForm());
        register(new MistformForm());
        register(new ColossusForm());
        register(new TidebodyForm());
    }

    private AbilityRegistry() {
    }

    private static void register(AspectAbility ability) {
        if (ABILITIES.putIfAbsent(ability.id(), ability) != null) {
            throw new IllegalStateException("Duplicate ability " + ability.id());
        }
    }

    public static Optional<AspectAbility> get(ResourceLocation id) {
        return Optional.ofNullable(ABILITIES.get(id));
    }

    public static Collection<AspectAbility> all() {
        return Collections.unmodifiableCollection(ABILITIES.values());
    }

    public static boolean isActivatable(ResourceLocation id) {
        return ABILITIES.containsKey(id);
    }
}
