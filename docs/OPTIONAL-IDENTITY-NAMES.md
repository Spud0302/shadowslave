# Optional Aspect and Flaw formal names

**Status:** bounded runtime-schema correction.

## Lore boundary

- **CANON:** Nightmare-Spell appraisal can explicitly reveal named Aspect and Flaw records, as shown during Sunny's First Nightmare appraisal in Chapter 15.
- **CANON:** natural Awakened do not automatically receive Spell-translated Aspect and Flaw records; they may need to discover and unseal them over time. Chapters 1826 and 2029 describe Rain as Awakened while her Aspect and Flaw are not yet established through the Spell interface.
- **CANON:** later interpretation can identify a Flaw before every associated supernatural detail has been fully deciphered, as shown around Chapters 2030–2033.
- **INFERRED:** a persistent identity may exist before the game has an authoritative formal label to display.
- **DESIGN:** Java stores the formal name as `Optional<String>` while keeping stable instance IDs, mechanics IDs and provenance mandatory.
- **UNKNOWN:** the exact universal order in which naturally awakened people discover formal Aspect and Flaw names. The schema does not invent one.
- **COMPATIBILITY:** existing saves with a string `formal_name` decode unchanged.

## Behaviour

- omitted `formal_name` means unrevealed or not yet established;
- a present name must remain non-blank;
- old named preview and migrated identities remain source- and save-compatible;
- client snapshots use an empty name field and fall back to the stable instance ID;
- the operator Soul readout displays `<name unrevealed>` rather than inventing a label.

## Deliberate limits

This slice does not yet separate Aspect and Flaw pair presence. `SoulIdentityData` still requires both records together for the current Nightmare-Spell post-appraisal path. It also does not broaden the one-ability Aspect record. Those are separate schema tasks requiring their own migration and acceptance boundaries.
