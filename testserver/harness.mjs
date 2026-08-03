// Shadow Slave — automated assertion harness.
//
// A mineflayer bot joins the local test server as a normal player, runs commands, and reads
// command replies back from chat. It covers the MECHANICAL half only — state transitions,
// guards, thresholds and teardown. Anything requiring judgement stays on the human list.
//
// Release-gate rule: an unreadable state is a TEST ERROR, not evidence that the state is absent.
// This file has previously produced false confidence from stale dimension caches, late replies,
// permissive negative assertions and checks that could not actually fail.
//
// Usage: node harness.mjs

import mineflayer from 'mineflayer'

import { waitForDimensionObservation } from './dimension_wait.mjs'

const HOST = 'localhost'
const PORT = 25565
const USER = 'tester'

const pass = []
const fail = []
const human = []

let chatLog = []

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

/**
 * Run a command and return the server reply window.
 *
 * When `expect` is supplied, timing out is an error. Returning an empty/unmatched string used to
 * let helpers such as hasTag() turn "the query failed" into false, so negative assertions could
 * pass without observing the state they claimed to verify.
 */
async function cmd(bot, command, expect = null, timeoutMs = 4000) {
  // Drain first. A late reply from the previous command once made `/tag list`'s
  // "tester has 2 tags:" get parsed as ss_timer=2.
  await sleep(120)
  chatLog = []
  bot.chat(command.startsWith('/') ? command : '/' + command)

  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline) {
    await sleep(60)
    const joined = chatLog.join('\n')
    if (expect ? expect.test(joined) : joined.length > 0) return joined
  }

  const joined = chatLog.join('\n')
  if (expect) {
    const seen = joined.length ? joined.slice(0, 300) : '<no reply>'
    throw new Error(`Timed out waiting for ${expect} after ${command}; saw ${seen}`)
  }
  return joined
}

/** Read a scoreboard value. null means Minecraft explicitly reported that no score exists. */
async function score(bot, objective, holder = USER) {
  const out = await cmd(
    bot,
    `/scoreboard players get ${holder} ${objective}`,
    /has -?\d+ \[|Can't get|no score/i
  )
  const match = out.match(/has (-?\d+) \[/)
  return match ? parseInt(match[1], 10) : null
}

/** True/false only after a valid tag-list reply has been observed. */
async function hasTag(bot, tag) {
  const out = await cmd(bot, `/tag ${USER} list`, /has \d+ tags?|has no tags/i)
  return out.includes(tag)
}

/**
 * Ask the SERVER for the dimension every time.
 *
 * Never fall back to bot.game.dimension. Mineflayer updates that cache from respawn packets, and
 * cross-dimension command teleports do not reliably emit one. The stale cache has already caused
 * three false failures against a correct pack.
 */
async function dimension(bot, timeoutMs = 4000) {
  const out = await cmd(
    bot,
    `/data get entity ${USER} Dimension`,
    /"[^\"]+:[^\"]+"/,
    timeoutMs
  )
  const match = out.match(/"([^\"]+)"/)
  if (!match) throw new Error(`Could not parse dimension from: ${out}`)
  return match[1]
}

async function attributeValue(bot, attribute) {
  const out = await cmd(bot, `/attribute @s ${attribute} get`, / is -?\d+(?:\.\d+)?/i)
  const match = out.match(/is (-?\d+(?:\.\d+)?)/i)
  if (!match) throw new Error(`Could not parse ${attribute} from: ${out}`)
  return parseFloat(match[1])
}

async function hasAdvancement(bot, advancement) {
  const out = await cmd(
    bot,
    `/execute if entity @s[advancements={${advancement}=true}] run say GRANTED`,
    /GRANTED|Test failed|passed/i
  )
  return /GRANTED/.test(out)
}

const inNightmare = (value) => value === 'shadowslave:nightmare'
const notNightmare = (value) => typeof value === 'string' && value !== 'shadowslave:nightmare'

/**
 * Read the player's current health from the server.
 */
async function health(bot) {
  const out = await cmd(bot, `/data get entity ${USER} Health`, /entity data|-?\d+(?:\.\d+)?f/i)
  const match = out.match(/(-?\d+(?:\.\d+)?)f/)
  if (!match) throw new Error(`Could not parse health from: ${out}`)
  return parseFloat(match[1])
}

/**
 * Drive health to at or below `target`, confirming it before returning.
 *
 * Assertions that set health and then check a threshold were intermittently failing about one run in
 * four, and every investigation blamed the pack for a race the test created. /damage does not always
 * land the full amount when it lands — invulnerability frames, absorption and regeneration all
 * interfere — so "I issued the damage" is not the same claim as "the player is below the threshold".
 * Establish the precondition, verify it, and only then assert on the behaviour that depends on it.
 */
async function driveHealthTo(bot, target, timeoutMs = 6000) {
  const deadline = Date.now() + timeoutMs
  let current = await health(bot)
  while (Date.now() < deadline) {
    if (current <= target) return current
    await cmd(bot, `/damage @s ${Math.max(1, Math.ceil(current - target))}`, /but took no damage|Applied|damage/i)
    await sleep(250)
    current = await health(bot)
  }
  throw new Error(`Could not drive health to <=${target}; last value=${current}`)
}

/**
 * Poll dimension until the requested state is actually observed. Fresh hosted worlds have blocked
 * command processing for more than 20 seconds while generating the destination, so keep the
 * overall transition budget much larger than any individual read. Timeout remains a test error.
 */
async function waitDimension(bot, predicate, timeoutMs = 60000) {
  return waitForDimensionObservation(
    (remainingMs) => dimension(bot, Math.min(4000, remainingMs)),
    predicate,
    { timeoutMs, retryDelayMs: 200 }
  )
}

/** Poll an attribute rather than guessing when the once-per-second upkeep has run. */
async function waitAttribute(bot, attribute, predicate, timeoutMs = 4000) {
  let seen = null
  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline) {
    seen = await attributeValue(bot, attribute)
    if (predicate(seen)) return seen
    await sleep(200)
  }
  throw new Error(`Timed out waiting for ${attribute}; last value=${seen}`)
}

function assert(name, condition, detail = '') {
  const line = detail ? `${name} — ${detail}` : name
  ;(condition ? pass : fail).push(line)
  console.log(`${condition ? 'PASS' : 'FAIL'}  ${line}`)
}

function needsHuman(what, why) {
  human.push(`${what} — ${why}`)
}

async function run(bot) {
  console.log('\n=== Shadow Slave harness ===\n')

  await cmd(bot, '/gamemode survival')

  // Freeze natural regeneration for the whole run.
  //
  // Several assertions set health to an exact value and then check a threshold. Regen ticks ~1 HP/s,
  // so it can lift the player back across the boundary between the /damage and the check — the
  // weakness gate then correctly does NOT refuse, and the test blames the pack for a race it created
  // itself. This is not masking behaviour: nothing in the pack depends on regeneration, and the gates
  // under test are threshold comparisons, so a stable health value is what makes them observable.
  await cmd(bot, '/gamerule naturalRegeneration false', /naturalRegeneration/i)
  await cmd(bot, '/function shadowslave:test/reset')
  await cmd(bot, '/effect give @s minecraft:instant_health 1 10 true')
  await sleep(300)

  // --- clean state + infection lifecycle -----------------------------------
  assert('untouched has no carrier tag', !(await hasTag(bot, 'ss_carrier')))

  // v1.4.9 regression: eligibility belongs at the entry choke point, not only in callers.
  const untouchedEntry = await cmd(bot, '/function shadowslave:nightmare/enter', /not marked/i)
  const dimUntouched = await dimension(bot)
  assert(
    'untouched direct entry is refused at the choke point',
    /not marked/i.test(untouchedEntry) && notNightmare(dimUntouched),
    `dimension=${dimUntouched}`
  )

  // v1.4.9 regression: reset promises fresh state, including hidden cooldown state.
  await cmd(bot, '/scoreboard players set @s ss_cooldown 123')
  await cmd(bot, '/function shadowslave:test/reset')
  const cooldownAfterCleanReset = await score(bot, 'ss_cooldown')
  assert(
    'test/reset clears transient cooldown state',
    cooldownAfterCleanReset === null || cooldownAfterCleanReset === 0,
    `ss_cooldown=${cooldownAfterCleanReset}`
  )

  await cmd(bot, '/function shadowslave:test/infect')
  assert('infect marks a Carrier', await hasTag(bot, 'ss_carrier'))

  const reInfect = await cmd(bot, '/function shadowslave:test/infect', /already a Carrier/i)
  assert('infect refuses twice', /already a Carrier/i.test(reInfect), 'guard on existing state')

  const cureAsCarrier = await cmd(bot, '/function shadowslave:test/cure', /lost interest|cannot lose|not noticed/i)
  assert('cure removes the mark', !(await hasTag(bot, 'ss_carrier')))
  assert('cure on a Carrier does not refuse', !/Sleeper|cannot lose interest/i.test(cureAsCarrier))

  // --- weakness gate + explicit test bypass --------------------------------
  await cmd(bot, '/function shadowslave:test/infect')
  // Confirm we are actually under the 10 HP entry gate before testing that the gate refuses us.
  const weakenedTo = await driveHealthTo(bot, 8)

  const weakEntry = await cmd(bot, '/function shadowslave:nightmare/enter', /too weak/i)
  const dimAfterWeak = await dimension(bot)
  const hurtHealth = await score(bot, 'ss_scratch_a')
  // BOTH halves matter. The old OR would pass if the function printed the refusal and then
  // accidentally fell through into the Nightmare anyway.
  assert(
    'entry refused while too weak',
    /too weak/i.test(weakEntry) && notNightmare(dimAfterWeak),
    `health=${weakenedTo} (scratch ${hurtHealth}), dimension=${dimAfterWeak}`
  )

  await cmd(bot, '/function shadowslave:test/nightmare')
  const dimAfterBypass = await waitDimension(bot, inNightmare)
  assert(
    'test/nightmare bypasses the weakness gate',
    inNightmare(dimAfterBypass),
    `health=${hurtHealth}, dimension=${dimAfterBypass}`
  )
  assert('the bypass tag is consumed by entry', !(await hasTag(bot, 'ss_test_bypass')))

  // Leaving sets cooldown. Heal, then prove the test wrapper bypasses that gate too.
  await cmd(bot, '/function shadowslave:nightmare/leave')
  await waitDimension(bot, notNightmare)
  await cmd(bot, '/effect give @s minecraft:instant_health 1 10 true')
  await sleep(200)
  await cmd(bot, '/function shadowslave:test/nightmare')
  const dimAfterCooldownBypass = await waitDimension(bot, inNightmare)
  assert(
    'test/nightmare bypasses the cooldown gate',
    inNightmare(dimAfterCooldownBypass),
    `dimension=${dimAfterCooldownBypass}`
  )
  await cmd(bot, '/function shadowslave:nightmare/leave')
  await waitDimension(bot, notNightmare)
  await cmd(bot, '/scoreboard players reset @s ss_cooldown')

  // --- entry ---------------------------------------------------------------
  await cmd(bot, '/effect give @s minecraft:instant_health 1 10 true')
  await sleep(200)
  await cmd(bot, '/function shadowslave:test/nightmare')
  const dimIn = await waitDimension(bot, inNightmare)
  assert('test/nightmare enters the dimension', inNightmare(dimIn), `dimension=${dimIn}`)
  assert('entry sets the in-nightmare tag', await hasTag(bot, 'ss_in_nightmare'))

  const timer = await score(bot, 'ss_timer')
  const COUNTDOWN = 1800
  // Pinned near the exact 90-second value. The old `<= 6000` assertion could not catch a bad retune.
  assert(
    'entry starts the countdown',
    timer !== null && timer > COUNTDOWN - 60 && timer <= COUNTDOWN,
    `ss_timer=${timer}, expected ~${COUNTDOWN}`
  )

  const reEnter = await cmd(bot, '/function shadowslave:test/nightmare', /already in a nightmare/i)
  assert('re-entry refused while inside', /already in a nightmare/i.test(reEnter))

  // --- soul-readout collision regression ----------------------------------
  await cmd(bot, '/trigger soul')
  await sleep(1200)
  const stillIn = await dimension(bot)
  assert('reading the soul does not eject you', inNightmare(stillIn), `dimension=${stillIn}`)

  // v1.4.9 regression: reset invoked from INSIDE must tear down first, then clear the cooldown
  // that leave.mcfunction deliberately creates.
  await cmd(bot, '/function shadowslave:test/reset')
  const dimAfterInsideReset = await waitDimension(bot, notNightmare)
  const insideResetTag = await hasTag(bot, 'ss_in_nightmare')
  const cooldownAfterInsideReset = await score(bot, 'ss_cooldown')
  assert(
    'test/reset inside a Nightmare performs teardown',
    notNightmare(dimAfterInsideReset) && !insideResetTag,
    `dimension=${dimAfterInsideReset}, tagged=${insideResetTag}`
  )
  assert(
    'test/reset inside a Nightmare leaves no cooldown behind',
    cooldownAfterInsideReset === null || cooldownAfterInsideReset === 0,
    `ss_cooldown=${cooldownAfterInsideReset}`
  )

  // --- death teardown and item recovery -----------------------------------
  await cmd(bot, '/effect give @s minecraft:instant_health 1 10 true')
  await cmd(bot, '/function shadowslave:test/infect')
  await cmd(bot, '/function shadowslave:test/nightmare')
  await waitDimension(bot, inNightmare)
  await cmd(bot, '/give @s minecraft:diamond 7')
  await sleep(300)
  await cmd(bot, '/kill @s')
  // Mineflayer needs time to respawn before command queries are reliable.
  await sleep(2000)

  const dimAfterDeath = await dimension(bot)
  assert('death clears the in-nightmare tag', !(await hasTag(bot, 'ss_in_nightmare')))
  assert('death does not strand you in the nightmare', notNightmare(dimAfterDeath), `dimension=${dimAfterDeath}`)

  // Read the condition into a score rather than relying on a conditional `say`.
  //
  // This assertion was itself vacuous until GPT's fail-closed change exposed it, which is a fair
  // answer to OPEN-QUESTIONS Q1. It ran `execute if entity @s[advancements={...}] run say GRANTED`,
  // which produces NO reply when the condition is false — so a passing run and an unreadable one
  // were indistinguishable, and `!/GRANTED/.test('')` was true either way. It could not have failed
  // if death had started granting the advancement.
  //
  // `execute store success` always writes a definite 0 or 1, so the read is unambiguous. This is the
  // idiom test/selfcheck.mcfunction already uses for the same reason.
  await cmd(bot, '/scoreboard players reset $castout ss_roll')
  await cmd(
    bot,
    '/execute store success score $castout ss_roll if entity @s[advancements={shadowslave:test/cast_out=true}]',
    /score|set|Nothing changed|Test failed/i
  )
  const castOutOnDeath = await score(bot, 'ss_roll', '$castout')
  assert(
    'death does not grant the Cast Out advancement',
    castOutOnDeath === 0,
    `store success returned ${castOutOnDeath} (1 = granted); Cast Out records ejections only`
  )

  // This cannot be graded reliably by Mineflayer: Nightmare chunks unload after exit, and the
  // earlier automated checks returned the same answer for pass and fail. Keep the honest manual check.
  needsHuman(
    'item recovery on death (CONFIRMED v1.4.5 — re-check only if the sweep changes)',
    'not machine-checkable: nightmare chunks unload before the query runs'
  )

  // The bot cannot conduct a real fight. This path was confirmed by hand on v1.4.6.
  needsHuman(
    'the whole loop, won (CONFIRMED v1.4.6)',
    're-run after any change to survive/ or progression/ — no bot can fight the creature'
  )

  // --- soul labels + Sleeper rank gate ------------------------------------
  await cmd(bot, '/function shadowslave:test/reset')
  const soulUntouched = await cmd(bot, '/trigger soul', /Rank:/)
  assert('untouched reads as Mundane', /Mundane/i.test(soulUntouched), soulUntouched.match(/Rank:[^\n]*/)?.[0] || '')

  await cmd(bot, '/function shadowslave:test/awaken', /Sleeper/i)
  const cureSleeper = await cmd(bot, '/function shadowslave:test/cure', /Sleeper|lost interest/i)
  assert('cure refuses on a Sleeper', /cannot lose interest|test\/reset/i.test(cureSleeper), cureSleeper.slice(0, 80))

  // A Sleeper is terminal for Phase 1: ordinary sleep should stay in the Overworld and grant
  // the historical test/bypass id whose display title is Sleep Undisturbed.
  await cmd(bot, '/function shadowslave:sleep')
  const dimAfterSleeperSleep = await dimension(bot)
  assert(
    'a Sleeper sleeps without re-entering a First Nightmare',
    notNightmare(dimAfterSleeperSleep),
    `dimension=${dimAfterSleeperSleep}`
  )
  assert(
    'Sleeper sleep grants Sleep Undisturbed',
    await hasAdvancement(bot, 'shadowslave:test/bypass')
  )

  // --- re-roll modifier cleanup -------------------------------------------
  // Force Bone so there is definitely a persistent armour modifier to leave behind.
  await cmd(bot, '/tag @s remove ss_aspect_shadow')
  await cmd(bot, '/tag @s remove ss_aspect_flame')
  await cmd(bot, '/tag @s remove ss_aspect_wind')
  await cmd(bot, '/tag @s add ss_aspect_bone')
  const boneVal = await waitAttribute(bot, 'minecraft:generic.armor', (value) => value >= 6)

  await cmd(bot, '/function shadowslave:test/reset')
  await cmd(bot, '/function shadowslave:test/awaken', /Sleeper/i)
  const rerolledBone = await hasTag(bot, 'ss_aspect_bone')
  const afterVal = rerolledBone
    ? await waitAttribute(bot, 'minecraft:generic.armor', (value) => value >= 6)
    : await attributeValue(bot, 'minecraft:generic.armor')
  assert(
    'a re-roll does not leave the old Aspect modifier behind',
    rerolledBone ? afterVal === boneVal : afterVal < boneVal,
    `bone=${boneVal} after=${afterVal}${rerolledBone ? ' (rerolled Bone again)' : ''}`
  )

  // --- real ejection, cooldown and recovery sleep -------------------------
  await cmd(bot, '/function shadowslave:test/reset')
  await cmd(bot, '/effect give @s minecraft:instant_health 1 10 true')
  await cmd(bot, '/function shadowslave:test/infect')
  await cmd(bot, '/function shadowslave:test/nightmare')
  await waitDimension(bot, inNightmare)
  await cmd(bot, '/damage @s 16') // -> 4 HP, the ejection threshold

  const dimAfterEject = await waitDimension(bot, notNightmare)
  assert('low health ejects you from the trial', notNightmare(dimAfterEject), `dimension=${dimAfterEject}`)

  const cooldown = await score(bot, 'ss_cooldown')
  assert('ejection starts the cooldown', cooldown !== null && cooldown > 0, `ss_cooldown=${cooldown}`)

  await cmd(bot, '/effect give @s minecraft:instant_health 1 10 true')
  await sleep(200)
  const entryDuringCooldown = await cmd(
    bot,
    '/execute as @s at @s run function shadowslave:nightmare/enter',
    /Spell is spent/i
  )
  const dimDuringCooldown = await dimension(bot)
  assert(
    'you cannot re-enter during the cooldown',
    /Spell is spent/i.test(entryDuringCooldown) && notNightmare(dimDuringCooldown),
    `dimension=${dimDuringCooldown}`
  )

  // Sleeping through one night IS recovery. It clears the cooldown and must return before entry.
  // Assert on state, not on the copy.
  //
  // This waited on /nothing reaches/i, which the pack emits via `title @s actionbar` — that never
  // arrives as a chat message, so mineflayer cannot see it and the assertion could ONLY fail. The
  // inverse of the vacuous checks: unfalsifiable rather than unfailable, and equally useless.
  //
  // Matching on player-facing copy is fragile here for a second reason: two assertions have already
  // been silently disarmed by rewording the strings they matched. The observable outcomes below —
  // cooldown cleared, and still in the Overworld — are what the feature actually promises.
  await cmd(bot, '/scoreboard players set @s ss_cooldown 600')
  await cmd(bot, '/function shadowslave:sleep', /returned|Running function/i)
  const cdAfterSleep = await score(bot, 'ss_cooldown')
  assert('sleeping clears the cooldown', cdAfterSleep === null || cdAfterSleep === 0, `ss_cooldown=${cdAfterSleep}`)
  const dimAfterRecovery = await dimension(bot)
  assert('the recovery sleep does not pull you in', notNightmare(dimAfterRecovery), `dimension=${dimAfterRecovery}`)

  // --- ejection must not vacuum unrelated Nightmare loot ------------------
  await cmd(bot, '/scoreboard players reset @s ss_cooldown')

  // Clear loose items in BOTH dimensions first. This assertion was intermittent for a reason that
  // had nothing to do with the sweep: the death test above also summons a diamond, and the death
  // sweep legitimately moves items to the return position in the Overworld. Those survive between
  // harness runs, so this check was periodically finding litter from a PREVIOUS run and blaming
  // this ejection for it. The world is long-lived; the test has to isolate itself.
  await cmd(bot, '/execute in minecraft:overworld run kill @e[type=item]', /Killed|No entity/i)
  await cmd(bot, '/execute in shadowslave:nightmare run kill @e[type=item]', /Killed|No entity/i)

  await cmd(bot, '/function shadowslave:test/nightmare')
  await waitDimension(bot, inNightmare)
  await cmd(bot, '/summon item ~5 ~ ~5 {Item:{id:"minecraft:diamond_block",count:1}}', /Summoned/i)
  await sleep(300)
  await cmd(bot, '/damage @s 16')
  const ejectedTo = await waitDimension(bot, notNightmare)
  // Count matches into a score. `execute as @e[...] run <cmd>` emits NO reply when nothing matches
  // — the same trap that made the Cast Out assertion vacuous — so the clean, passing case was
  // indistinguishable from an unreadable one. `store result ... if entity` always writes a definite
  // count, so 0 genuinely means "no stray items on the player".
  await cmd(bot, '/scoreboard players reset $stray ss_roll')
  await cmd(
    bot,
    '/execute store result score $stray ss_roll if entity @e[type=item,distance=..6,nbt={Item:{id:"minecraft:diamond_block"}}]',
    /score|set|Nothing changed|Test failed/i
  )
  const strayCount = await score(bot, 'ss_roll', '$stray')
  assert(
    'ejection does not sweep loose items onto you',
    notNightmare(ejectedTo) && strayCount === 0,
    `ejected to ${ejectedTo}, stray diamond_blocks on player=${strayCount}; items the player never dropped must stay in the nightmare`
  )

  // Coverage gap, found by probing the new generator: test/awaken deliberately clears trial
  // observations, so it can only ever produce the BASELINE Flaw family. The three earned families
  // (near-collapse, hunger, distance) require a real fought trial, which no bot can do. The
  // interesting half of the feature is therefore unreachable from here.
  needsHuman(
    'earned Flaw families',
    'only the baseline family is machine-reachable; near-collapse / hunger / fled need a real trial'
  )

  // --- things only a person can judge -------------------------------------
  // Settled, do not re-add: recovery sleep, creature chase speed, 2-heart ejection threshold,
  // fight at wood/no-armour, ambient_light 0.1, bossbar handover, spawn rate and sneak-hold feel.

  console.log('\n=== summary ===')
  console.log(`${pass.length} passed, ${fail.length} failed`)
  if (fail.length) {
    console.log('\nFAILED:')
    fail.forEach((item) => console.log('  ' + item))
  }
  console.log('\nNEEDS A HUMAN:')
  human.forEach((item) => console.log('  ' + item))
  console.log()
}

const bot = mineflayer.createBot({
  host: HOST,
  port: PORT,
  username: USER,
  auth: 'offline',
  version: '1.21.1'
})

bot.on('message', (msg) => chatLog.push(msg.toString()))
bot.on('error', (error) => {
  console.error('bot error:', error.message)
  process.exit(1)
})
bot.on('kicked', (reason) => {
  console.error('kicked:', reason)
  process.exit(1)
})

bot.once('spawn', async () => {
  try {
    await sleep(1500)
    await run(bot)
  } catch (error) {
    console.error('harness error:', error)
    // An exception must be indistinguishable from an assertion failure to the release gate.
    fail.push(`harness exception — ${error instanceof Error ? error.message : String(error)}`)
  } finally {
    bot.quit()
    process.exit(fail.length ? 1 : 0)
  }
})
