package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.appraisal.generation.AttributeContentCatalog;
import dev.spud.shadowslave.content.spell.SpellPresentationCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Runtime integration from already-committed appraisal/reward state into the
 * existing Spell presentation catalogue. This class owns no progression state.
 */
public final class FirstNightmareSpellPresentation {
    private FirstNightmareSpellPresentation() {}

    public record ResolvedView(
            String scenarioName,
            String historicalRoleName,
            String terminalResolutionName,
            String aspectName,
            String flawName,
            Optional<String> revealedAttributeName,
            String memoryName,
            String echoName
    ) {
        public ResolvedView {
            scenarioName = requireText(scenarioName, "scenarioName");
            historicalRoleName = requireText(historicalRoleName, "historicalRoleName");
            terminalResolutionName = requireText(terminalResolutionName, "terminalResolutionName");
            aspectName = requireText(aspectName, "aspectName");
            flawName = requireText(flawName, "flawName");
            revealedAttributeName = Objects.requireNonNull(revealedAttributeName, "revealedAttributeName")
                    .map(name -> requireText(name, "revealedAttributeName"));
            memoryName = requireText(memoryName, "memoryName");
            echoName = requireText(echoName, "echoName");
        }
    }

    public static ResolvedView fromCommitted(
            String scenarioName,
            String historicalRoleName,
            String terminalResolutionName,
            PreviewAppraisalService.CommittedAppraisal committed
    ) {
        Objects.requireNonNull(committed, "committed");
        FirstNightmareAppraisalResolver.Award award = committed.award();
        Optional<String> attributeName = award.attribute().visibility() == AttributeContentCatalog.Visibility.REVEALED
                ? Optional.of(award.attribute().formalName())
                : Optional.empty();
        return new ResolvedView(
                scenarioName,
                historicalRoleName,
                terminalResolutionName,
                award.identity().aspect().formalName(),
                award.identity().flaw().formalName(),
                attributeName,
                committed.memory().formalName(),
                committed.echo().formalName()
        );
    }

    /**
     * The ordering is presentation DESIGN only. All names and resolution facts
     * are supplied from already-resolved Java-owned state.
     */
    public static List<SpellPresentationCatalog.PresentationLine> render(ResolvedView view) {
        Objects.requireNonNull(view, "view");
        SpellPresentationCatalog catalog = SpellPresentationCatalog.waveOne();
        ArrayList<SpellPresentationCatalog.PresentationLine> lines = new ArrayList<>(catalog.appraisal(
                "You endured " + view.scenarioName() + " in the historical role of " + view.historicalRoleName() + ".",
                List.of("Terminal resolution: " + view.terminalResolutionName() + "."),
                "Nightmare conquered"
        ));
        lines.add(catalog.line(SpellPresentationCatalog.EventKind.ASPECT_REVEALED, Map.of("name", view.aspectName())));
        lines.add(catalog.line(SpellPresentationCatalog.EventKind.FLAW_REVEALED, Map.of("name", view.flawName())));
        view.revealedAttributeName().ifPresent(name -> lines.add(
                catalog.line(SpellPresentationCatalog.EventKind.ATTRIBUTE_REVEALED, Map.of("name", name))
        ));
        lines.add(catalog.line(SpellPresentationCatalog.EventKind.MEMORY_RECEIVED, Map.of("name", view.memoryName())));
        lines.add(catalog.line(SpellPresentationCatalog.EventKind.ECHO_RECEIVED, Map.of("name", view.echoName())));
        return List.copyOf(lines);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
