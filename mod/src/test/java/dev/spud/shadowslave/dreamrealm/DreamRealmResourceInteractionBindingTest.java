package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmResourceInteractionBindingTest {
    @Test
    void bindsOnlyAuthoredRuinMetalInsideAshenExpanse() {
        var interaction = DreamRealmResourceInteractionBinding.ashenExpanseRuinMetal();
        var region = DreamRealmVerticalSliceDefinition.ashenExpanse().region();

        assertEquals(DreamRealmVerticalSliceDefinition.REGION_ID, interaction.regionId());
        assertEquals("ruin_metal", interaction.resourceId());
        assertTrue(region.resourceHooks().contains(interaction.resourceId()));
        assertTrue(region.opportunities().contains(DreamRealmRegionContentCatalog.Opportunity.SALVAGE));
        assertTrue(interaction.inspection().contains("Ruin metal"));
        assertTrue(interaction.boundary().contains("no item"));
    }

    @Test
    void matchesExactlyTheThreePhysicalClusterBlocks() {
        var interaction = DreamRealmResourceInteractionBinding.ashenExpanseRuinMetal();

        assertTrue(DreamRealmResourceInteractionBinding.isPhysicalClusterOffset(
                interaction, interaction.x(), interaction.y() + 1, interaction.z()));
        assertTrue(DreamRealmResourceInteractionBinding.isPhysicalClusterOffset(
                interaction, interaction.x(), interaction.y() + 2, interaction.z()));
        assertTrue(DreamRealmResourceInteractionBinding.isPhysicalClusterOffset(
                interaction, interaction.x() + 1, interaction.y() + 1, interaction.z()));

        assertFalse(DreamRealmResourceInteractionBinding.isPhysicalClusterOffset(
                interaction, interaction.x() + 1, interaction.y() + 2, interaction.z()));
        assertFalse(DreamRealmResourceInteractionBinding.isPhysicalClusterOffset(
                interaction, interaction.x() - 1, interaction.y() + 1, interaction.z()));
    }
}
