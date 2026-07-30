# Mineflayer harness audit — assertions that can pass for the wrong reason

**Baseline reviewed:** `main` at `a470b914f3e0710d3dfee63adc29b8e6e50d4599`

**Question answered:** `docs/OPEN-QUESTIONS.md` Q1 — "Which harness assertions could not fail if the behaviour broke?"

**Classification:** test correctness. No pack behavior is changed by this review.

The harness is much stronger than it was before the polling work, but several assertions still prove a weaker condition than their names claim. The recurring smell is an assertion that accepts **any refusal / any absence / a parse fallback** instead of proving the specific transition.

Per `ENGINEERING-NOTES.md`, each strengthened assertion should be seen failing against a deliberately broken implementation before being trusted.

---

## High confidence findings

### H1. `entry refused while too weak` can pass for the wrong refusal

Current shape:

```js
assert(
  'entry refused while too weak',
  /too weak/i.test(weakEntry) || dimAfterWeak !== 'shadowslave:nightmare'
)
```

The `||` means **any** reason the player failed to enter proves the weakness gate. Break or remove the weakness message/gate while another guard refuses entry, and the assertion can remain green.

The setup does reduce the chance of this accidentally happening, but the assertion's contract is still weaker than its name.

**Strengthen to require both:**

- response contains the weakness refusal (or another direct observable unique to that guard), **and**
- player remains outside the Nightmare.

Also keep the existing following test that `test/nightmare` bypasses the weakness gate; together they prove both enforcement and bypass.

### H2. `cure on a Carrier does not refuse` is a negative-string assertion

Current shape:

```js
assert(
  'cure on a Carrier does not refuse',
  !/Sleeper|cannot lose interest/i.test(cureAsCarrier)
)
```

An empty response, unrelated response, or a newly reworded broken response all satisfy this. This is the same class of bug the harness already documented when the old `Awakened` negative check became vacuous after the Sleeper reword.

The preceding `cure removes the mark` assertion proves the state change, so this is mostly a **message-contract** assertion. Make that explicit and positive:

- require the success copy (`lost interest`, or another intentionally stable phrase), rather than requiring the absence of one refusal phrase.

If the message text is not worth making part of the contract, delete this assertion rather than keeping a negative one.

### H3. the re-roll modifier assertion can pass when numeric parsing fails

Current code defaults failed parses:

```js
const boneVal = parseFloat(match?.[1] ?? '0')
const afterVal = parseFloat(match?.[1] ?? '-1')
```

Then the non-Bone branch accepts `afterVal < boneVal`.

If **both command-response regexes break**, the fallback values are `boneVal = 0`, `afterVal = -1`, and the assertion passes when the new roll is not Bone. If the new roll is Bone, two failed parses can also produce equal fallback values in other fallback choices and manufacture a pass.

This is a direct example of "a parser failure became a successful game assertion."

**Strengthen:**

1. assert that both attribute outputs parsed successfully;
2. assert the forced-Bone precondition actually produced a meaningful armor increase;
3. only then compare post-reroll armor against the parsed baseline.

Do not give parse failure a numeric value that can participate in the gameplay assertion.

### H4. `you cannot re-enter during the cooldown` proves only that entry did not happen

Current shape:

```js
assert(
  'you cannot re-enter during the cooldown',
  dimDuringCooldown !== 'shadowslave:nightmare'
)
```

Any other broken eligibility guard can keep the player outside and make this green. The setup heals the player and should retain Carrier state, which is good, but the assertion does not verify those preconditions or the cooldown-specific refusal.

**Strengthen:**

- prove `ss_carrier` is still present before the attempt;
- prove health is above the weakness threshold;
- require the cooldown-specific response (`Spell is spent`), **and**
- require the player remains outside.

This matters because entry eligibility has already had three caller-vs-choke-point bugs.

### H5. `low health ejects you from the trial` does not distinguish ejection from another exit

Current shape:

```js
assert(
  'low health ejects you from the trial',
  dimAfterEject !== 'shadowslave:nightmare'
)
```

Leaving the dimension proves an exit, not specifically **Cast Out**. A bug that kills the player, invokes generic teardown, or otherwise sends them home can satisfy it.

The test is deliberately damaging to exactly 4 HP rather than 0, which makes the intended path likely, but the result should still prove the path it names.

**Strengthen:** after the exit, verify at least one ejection-specific observable, preferably the `shadowslave:test/cast_out` advancement because it is granted only by that path. Also verify the player survived if the harness can query health reliably after the transition.

This pairs nicely with the existing `death does not grant the Cast Out advancement` assertion: together they prove the two outcomes stay distinct.

---

## Medium-confidence / worthwhile hardening

### H6. `infect refuses twice` proves the copy, not state preservation

The test checks the `already a Carrier` message but not that the second invocation left the Carrier state intact. A function that printed the refusal and then accidentally removed the tag would still pass this assertion.

Cheap hardening: require `hasTag('ss_carrier')` after the second call as well.

### H7. `re-entry refused while inside` proves the copy, not that the existing trial survived

The assertion matches `already in a nightmare`, but does not verify that the player remained in the Nightmare or that the active trial state/timer was not reset.

Cheap hardening:

- still in `shadowslave:nightmare` after the refused call;
- `ss_in_nightmare` still present;
- optionally ensure timer did not jump back toward the initial 1800 value.

### H8. negative advancement checks should prove the query itself succeeded

`death does not grant the Cast Out advancement` is conceptually good, but the final assertion is `!/GRANTED/.test(...)`. The command helper waits for `GRANTED|Test failed|passed`, which reduces the risk, but an empty timeout result would also satisfy the final negative check.

Make the result parser return an explicit boolean / tri-state and fail on "could not determine". In general, **absence of evidence is not evidence of an ungranted advancement**.

---

## Assertions that are already shaped well

These are good examples to copy:

- infection actually adds `ss_carrier`;
- test Nightmare actually changes dimension;
- entry actually adds `ss_in_nightmare`;
- countdown is pinned near the configured 1800 ticks instead of accepting a huge range;
- Soul readout safety checks the state consequence (still in Nightmare), not just message text;
- death checks both teardown tag state and dimension outcome;
- cooldown checks that a positive score exists after ejection;
- recovery sleep checks both cooldown clearing and not entering the Nightmare;
- the loose-item ejection test waits for the actual dimension transition before observing nearby items.

The move from fixed sleeps to `waitDimension()` is especially strong. Keep extending that pattern to any new asynchronous state assertion.

---

## Recommended test-review process for the release candidate

For each modified/new assertion:

1. intentionally break the behavior it claims to cover;
2. run only enough setup to reach that assertion;
3. confirm it turns red for the **expected reason**;
4. restore the implementation;
5. confirm green;
6. run the full harness at least three times to catch timing races.

For the final datapack release, I would prioritize H1–H5. They cover gates/outcomes that can otherwise make the pack look correct while exercising the wrong path.

**No claim is made here that the current pack behavior is broken.** These are weaknesses in what the tests prove.
