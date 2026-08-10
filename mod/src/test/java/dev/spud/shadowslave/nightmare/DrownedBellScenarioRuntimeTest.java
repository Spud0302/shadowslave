package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.nightmare.content.DrownedBellScenarioDefinition;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrownedBellScenarioRuntimeTest {
    private static NightmareInstance fresh() {
        NightmareInstance base = new NightmareInstance(
                UUID.fromString("10cb31a3-6cb5-45a2-86b8-428ad2e8280d"),
                UUID.fromString("8f7b0da4-4a83-4a79-8f25-9dbe26304927"),
                2,
                DrownedBellScenarioDefinition.SCENARIO_ID,
                "watch_apprentice",
                net.minecraft.resources.ResourceLocation.parse("minecraft:overworld"),
                0.0, 64.0, 0.0, 0.0F, 0.0F,
                DrownedBellScenario.originForSlot(2),
                DrownedBellScenario.bellStation(DrownedBellScenario.originForSlot(2)),
                Optional.empty(),
                100L
        );
        var initial = DrownedBellScenarioDefinition.resolutionGraph().initial();
        return base.withResolutionState(initial.stateId(), initial.terminalResolutionId());
    }

    @Test
    void bellPathReachesAuthoredTowerHeldTerminal() {
        NightmareInstance instance = fresh();
        assertEquals(Optional.of("repair_bell"),
                DrownedBellScenario.eventForInteraction(instance, DrownedBellScenario.bellStation(instance.origin())));

        var repaired = DrownedBellScenario.applyEvent(instance, "repair_bell");
        assertTrue(repaired.accepted());
        assertEquals(Optional.of("warning_ready"), repaired.instance().resolutionStateId());

        var terminal = DrownedBellScenario.applyEvent(repaired.instance(), "ring_bell");
        assertTrue(terminal.accepted());
        assertEquals(Optional.of("tower_held"), terminal.instance().terminalResolutionId());
        assertEquals("Last Bell Standing", DrownedBellScenario.resolutionContent(terminal.instance()).orElseThrow().name());
    }

    @Test
    void quarryPathReachesEvacuationTerminalWithoutCombat() {
        NightmareInstance instance = fresh();
        var opened = DrownedBellScenario.applyEvent(instance, "open_quarry_route");
        assertTrue(opened.accepted());

        var terminal = DrownedBellScenario.applyEvent(opened.instance(), "guide_evacuation");
        assertTrue(terminal.accepted());
        assertEquals(Optional.of("villagers_evacuated"), terminal.instance().terminalResolutionId());
    }

    @Test
    void floodgatePathReachesFloodDiversionTerminal() {
        NightmareInstance instance = fresh();
        var reached = DrownedBellScenario.applyEvent(instance, "reach_floodgate");
        assertTrue(reached.accepted());

        var terminal = DrownedBellScenario.applyEvent(reached.instance(), "divert_flood");
        assertTrue(terminal.accepted());
        assertEquals(Optional.of("flood_diverted"), terminal.instance().terminalResolutionId());
    }

    @Test
    void lurePathRequiresBellPreparationAndCanBuryCreature() {
        NightmareInstance instance = fresh();
        var illegal = DrownedBellScenario.applyEvent(instance, "lure_creature");
        assertFalse(illegal.accepted());
        assertEquals(Optional.of("event_not_accepted_in_current_state"), illegal.rejectionReason());

        var repaired = DrownedBellScenario.applyEvent(instance, "repair_bell");
        var lured = DrownedBellScenario.applyEvent(repaired.instance(), "lure_creature");
        assertTrue(lured.accepted());
        assertEquals(Optional.of("creature_lured"), lured.instance().resolutionStateId());

        var terminal = DrownedBellScenario.applyEvent(lured.instance(), "collapse_quarry");
        assertTrue(terminal.accepted());
        assertEquals(Optional.of("creature_buried"), terminal.instance().terminalResolutionId());
    }

    @Test
    void terminalStateRejectsFurtherEventsAndSurvivesNbtRoundTrip() {
        NightmareInstance instance = DrownedBellScenario.applyEvent(
                DrownedBellScenario.applyEvent(fresh(), "repair_bell").instance(),
                "ring_bell"
        ).instance();

        NightmareInstance restored = NightmareInstance.load(instance.save());
        assertEquals(instance.resolutionStateId(), restored.resolutionStateId());
        assertEquals(instance.terminalResolutionId(), restored.terminalResolutionId());
        assertTrue(DrownedBellScenario.eventForInteraction(restored, DrownedBellScenario.bellStation(restored.origin())).isEmpty());

        var rejected = DrownedBellScenario.applyEvent(restored, "open_quarry_route");
        assertFalse(rejected.accepted());
        assertEquals(Optional.of("scenario_already_terminal"), rejected.rejectionReason());
    }
}
