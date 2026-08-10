# Live generated-appraisal executable-power boundary

**Date:** 2026-08-10  
**Context:** PR #189 review correction

## Finding

PR #189 integrated the broad generated Aspect/Flaw catalogue into the playable First-Nightmare appraisal path. Review identified a concrete runtime mismatch: the generator could select any authored ability or Flaw primitive, while the current player-power executor only implements Kindle and Cold Ash. A successful Nightmare could therefore replace the previously working preview power pair with persisted but mechanically inert generated state.

This is a correctness/playability defect at the definition-to-runtime boundary, not evidence for a new Shadow Slave rule.

## Correction

The broad `ExpandedIdentityContentCatalog.waveOne()` remains unchanged and available for definition/content work. The live `FirstNightmareAppraisalResolver` derives a narrower runtime catalogue containing only:

- `shadowslave:generation/ability/kindle`;
- `shadowslave:generation/flaw/cold_ash`;
- authored natures compatible with both executable primitives;
- the existing authored archetypes, so generated Aspect naming can still vary;
- the existing Attribute catalogue, which remains selected independently.

The resolver fails closed if either executable primitive disappears from the broad catalogue or no authored nature supports the pair. Tests sweep both currently playable First-Nightmare scenarios and require every live award to retain the executable IDs.

This intentionally prefers a narrower playable generator over awarding definition-only mechanics. Additional generated powers should enter the live catalogue only after their server-authoritative executor and focused tests exist.

## Evidence classification

- **CANON:** unchanged. The novel does not supply this runtime whitelist or a deterministic appraisal formula.
- **INFERRED:** unchanged from the generated-appraisal work: once an appraisal identity is resolved, its exact identity/provenance should remain stable rather than being silently replaced or reinterpreted by later generator/catalogue changes.
- **DESIGN:** the live preview restricts generated powers to primitives with implemented runtime execution. Kindle and Cold Ash are the current executable whitelist; catalogue filtering and fail-closed validation are project implementation choices.
- **UNKNOWN:** the canonical Aspect/Flaw determination process; which future authored ability/Flaw primitives will receive executors; the final breadth and balance of the live generated-power pool.
- **COMPATIBILITY:** no persistence schema changes. The broad definition catalogue remains intact. This correction only prevents the unmerged alpha integration from persisting newly generated live powers that the runtime cannot execute.

## Lore/source-policy note

No lore-sensitive mechanic is added or generalized by this correction. `docs/LORE-SOURCE-POLICY.md` and the existing generated-appraisal research remain the governing source boundary. The correction does not promote Kindle, Cold Ash, their selection, or the whitelist to canon.

## Limitations and next condition

This does not solve Issue #34's crash-atomic successful-completion transaction, and it does not implement the other authored Aspect Ability or Flaw effect definitions. The generated pool is intentionally narrower until those mechanics become executable.

Resume expansion of the live power pool only when a candidate primitive has a concrete server-authoritative executor, reset/recovery behavior where applicable, and tests proving the awarded persistent ID dispatches to that executor.
