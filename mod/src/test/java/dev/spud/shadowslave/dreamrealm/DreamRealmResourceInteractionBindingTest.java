package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmResourceInteractionBindingTest {
    private static final Path RUNTIME = Path.of(
            "src/main/java/dev/spud/shadowslave/dreamrealm/DreamRealmResourceInteractionRuntime.java");

    @Test
    void bindsEveryAuthoredAshenExpanseResourceWithoutInventingRewards() {
        var interactions = DreamRealmResourceInteractionBinding.ashenExpanseResources();
        var region = DreamRealmVerticalSliceDefinition.ashenExpanse().region();
        Set<String> boundIds = interactions.stream()
                .map(DreamRealmResourceInteractionBinding.Interaction::resourceId)
                .collect(Collectors.toSet());

        assertEquals(region.resourceHooks(), boundIds);
        assertEquals(Set.of("bone_char", "ruin_metal", "dry_fungus"), boundIds);
        assertEquals(3, interactions.size());
        assertTrue(region.opportunities().contains(DreamRealmRegionContentCatalog.Opportunity.SALVAGE));
        assertTrue(interactions.stream().allMatch(interaction -> interaction.regionId().equals(DreamRealmVerticalSliceDefinition.REGION_ID)));
        assertTrue(interactions.stream().allMatch(interaction -> !interaction.inspection().isBlank()));
        assertTrue(interactions.stream().allMatch(interaction -> interaction.boundary().contains("no ")));
    }

    @Test
    void eachInteractionMatchesExactlyItsThreePhysicalClusterBlocks() {
        for (var interaction : DreamRealmResourceInteractionBinding.ashenExpanseResources()) {
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

    @Test
    void physicalResourceClustersDoNotOverlap() {
        var interactions = DreamRealmResourceInteractionBinding.ashenExpanseResources();
        for (int i = 0; i < interactions.size(); i++) {
            var owner = interactions.get(i);
            for (int j = 0; j < interactions.size(); j++) {
                if (i == j) continue;
                var other = interactions.get(j);
                assertFalse(DreamRealmResourceInteractionBinding.isPhysicalClusterOffset(
                        other, owner.x(), owner.y() + 1, owner.z()));
                assertFalse(DreamRealmResourceInteractionBinding.isPhysicalClusterOffset(
                        other, owner.x(), owner.y() + 2, owner.z()));
                assertFalse(DreamRealmResourceInteractionBinding.isPhysicalClusterOffset(
                        other, owner.x() + 1, owner.y() + 1, owner.z()));
            }
        }
    }

    @Test
    void runtimeConsumesMatchingInteractionBeforeServerOnlyPresentation() throws IOException {
        String runtime = Files.readString(RUNTIME);

        assertTrue(runtime.contains("event.getLevel().dimension().equals(DreamRealmPreviewService.DREAM_REALM_LEVEL)"));
        assertFalse(runtime.contains("event.getLevel().isClientSide() ||"));
        assertTrue(runtime.contains("event.setCanceled(true);"));
        assertTrue(runtime.contains("event.getHand() != InteractionHand.MAIN_HAND"));
        assertTrue(runtime.indexOf("event.setCanceled(true);") < runtime.indexOf("instanceof ServerPlayer player"));
        assertTrue(runtime.indexOf("event.getHand() != InteractionHand.MAIN_HAND") < runtime.indexOf("sendSystemMessage"));
    }
}
