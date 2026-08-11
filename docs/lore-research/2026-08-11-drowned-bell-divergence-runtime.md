# Drowned Bell divergence runtime — 2026-08-11

## Question

How should the playable First-Nightmare appraisal distinguish the character of a challenger's deeds from how strongly those deeds changed the reconstructed original history?

## Primary evidence rechecked

Official WebNovel Chapter 743 (`Appraisal`) was rechecked on 2026-08-11. Sunny contrasts his Glorious First-Nightmare appraisal with the Excellent appraisal of his Second Nightmare and reasons that most of his enormous Second-Nightmare achievements reproduced the original historical course, while his First Nightmare diverged much more strongly from the original flow of fate. The text presents this as Sunny's inference, not a disclosed Spell formula.

Official WebNovel Chapter 15 was also rechecked as the early appraisal example: the Spell recounts significant actions/achievements before issuing the final appraisal.

No exact numeric formula or grade threshold is exposed by those chapters.

## Classification

- **CANON:** the Spell appraises a completed Nightmare and recounts significant deeds; Chapter 743 explicitly contrasts reproduction of original history with larger divergence from fate as Sunny's explanation for different appraisal quality.
- **INFERRED:** divergence from original history is a major appraisal-quality input and should be represented separately from the thematic character of the deeds.
- **DESIGN:** The Drowned Bell original-history axes, their weights, terminal-resolution-to-axis mappings, numeric `score/maximumScore`, and the exact presentation sentence.
- **UNKNOWN:** the Spell's exact algorithm, grade thresholds, interaction between difficulty/deeds/divergence, and whether every changed historical detail contributes monotonically in canon.
- **COMPATIBILITY:** Java owns original-history identity, accepted terminal resolution, proven fate outcomes and divergence evidence. Minecraft blocks/interactions only produce accepted scenario events. Generated Aspect/Attribute/Flaw flavour evidence remains separate from divergence quality.

## Runtime rule

The Drowned Bell terminal resolution now resolves only fate axes that the accepted terminal path actually proves:

- `tower_held`: warning bell sounded; lower village warned;
- `villagers_evacuated`: quarry route opened; lower village evacuated;
- `flood_diverted`: sea gate diverted; lower village spared;
- `creature_buried`: warning bell used as lure; Drowned Listener buried.

Unmentioned axes remain UNKNOWN and receive no assumed divergence credit.

The existing deed/evidence weights continue to shape generated identity flavour. The divergence score is attached separately to the appraisal award and presented as an appraisal-quality fact. This slice deliberately does not invent a Glorious/Excellent threshold.
