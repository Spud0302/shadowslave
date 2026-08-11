# Borrowed Dawn `first_light` runtime evidence

**Date:** 2026-08-11  
**Scope:** bounded execution of the existing Java-authored `shadowslave:memory/borrowed_dawn` / `shadowslave:memory_enchantment/first_light` content.

## Primary evidence checked

Research follows `docs/LORE-SOURCE-POLICY.md`. Chapter 74, **Midnight Shard**, establishes Memory rank/tier/type distinctions and shows an ordinary Memory being transferred between people. Chapter 694, **Key Piece**, later distinguishes universal Memory traits such as summon/dismiss and soul connection from the individual enchantment/purpose of a particular Memory. Official WebNovel publication was used to cross-check chapter identity/publication; the owner-designated NovelFull access layer supplied the full chapter text used for the mechanic review.

Neither chapter establishes a Memory named Borrowed Dawn, ambient-light storage, restorative warmth, a brightness threshold, healing quantity, or a decay/rest formula.

## Evidence boundary

- **CANON:** Memories are soul-associated supernatural objects; Memories have differentiated ranks/tiers/types and individual enchantments/purposes; ordinary Memories can be transferred; summon/dismiss is a general Memory property.
- **INFERRED:** when an existing authored Memory enchantment is stateful, its payload should remain server-authoritative Java state rather than living only on a disposable Minecraft item or client presentation.
- **DESIGN:** Borrowed Dawn, `first_light`, one binary stored-light charge, Minecraft local raw brightness 12–15 as sufficient ambient light, four health points of restorative healing, capture/release interaction, cooldowns, messages, and the temporary glowstone-dust item presentation.
- **UNKNOWN:** canonical Borrowed Dawn existence; whether any canonical Memory stores ambient light; qualifying light sources, capacity, healing quantity, essence cost, release conditions, death retention, cross-realm semantics, and every `night_debt` decay/repeated-use/rest rule.
- **COMPATIBILITY:** `MemoryOwnershipData` remains Memory ownership authority. `BorrowedDawnChargeData` owns only the existing enchantment's stored-light payload. NeoForge light sampling, physical item manifestation, healing, cooldowns, text, and item model are replaceable execution/presentation and cannot establish ownership, progression, appraisal, Nightmare resolution, or canonical lore.

## Runtime decision

An owning player may manifest Borrowed Dawn. When empty, using it in sufficiently bright ambient Minecraft light stores one Java-owned binary charge. When charged and the owner is injured, using it heals a bounded amount and consumes the charge. Full-health use retains the charge rather than wasting it. Preview reset clears both the physical manifestation and the Java charge.

The existing `night_debt` enchantment remains definition-only. Its authored hook mentions decay under repeated invocation without rest, but the repository currently supplies no authoritative decay clock, rest definition, repetition threshold, or essence model. Those semantics are therefore not invented in this slice.

## Presentation/dependency boundary

No new dependency or standalone presentation primitive is introduced. The physical item uses a bundled vanilla glowstone-dust texture solely as a temporary executor placeholder. A later project-owned model/texture can replace it without migrating Java-owned Memory or charge state.
