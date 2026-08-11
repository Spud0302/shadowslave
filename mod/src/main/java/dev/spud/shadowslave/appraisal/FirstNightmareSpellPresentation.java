package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.appraisal.generation.AttributeContentCatalog;
import dev.spud.shadowslave.content.spell.SpellPresentationCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Runtime presentation of already-resolved First-Nightmare appraisal state. */
public final class FirstNightmareSpellPresentation {
    private FirstNightmareSpellPresentation() {}

    public record ResolvedView(
            String scenarioName,
            String historicalRoleName,
            String terminalResolutionName,
            Optional<String> divergenceSummary,
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
            divergenceSummary = Objects.requireNonNull(divergenceSummary, "divergenceSummary")
                    .map(summary -> requireText(summary, "divergenceSummary"));
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
        Optional<String> divergenceSummary = award.divergence().map(result ->
                "Deviation from the original course: " + result.score() + "/" + result.maximumScore()
                        + " weighted fate; changed " + result.changedAxes().size()
                        + " of " + (result.changedAxes().size() + result.unchangedAxes().size() + result.unknownAxes().size())
                        + " tracked outcomes.");
        return new ResolvedView(
                scenarioName,
                historicalRoleName,
                terminalResolutionName,
                divergenceSummary,
                award.identity().aspect().formalName(),
                award.identity().flaw().formalName(),
                attributeName,
                committed.memory().formalName(),
                committed.echo().formalName()
        );
    }

    /** Ordering is presentation DESIGN; all facts come from resolved Java-owned state. */
    public static List<SpellPresentationCatalog.PresentationLine> render(ResolvedView view) {
        Objects.requireNonNull(view, "view");
        SpellPresentationCatalog catalog = SpellPresentationCatalog.waveOne();
        ArrayList<String> appraisalEvidence = new ArrayList<>();
        appraisalEvidence.add("Terminal resolution: " + view.terminalResolutionName() + ".");
        view.divergenceSummary().ifPresent(appraisalEvidence::add);
        ArrayList<SpellPresentationCatalog.PresentationLine> lines = new ArrayList<>(catalog.appraisal(
                "You endured " + view.scenarioName() + " in the historical role of " + view.historicalRoleName() + ".",
                List.copyOf(appraisalEvidence),
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
