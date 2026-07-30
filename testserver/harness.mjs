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
async function score(bot, objective) {
  const out = await cmd(
    bot,
    `/scoreboard players get ${USER} ${objective}`,
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
async function dimension(bot) {
  const out = await cmd(bot, `/data get entity ${USER} Dimension`, /"[^\"]+:[^\"]+"/)
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

/** Poll dimension until the requested state is actually observed. Timeout is a test error. */
async function waitDimension(bot, predicate, timeoutMs = 6000) {
  let seen = null
  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline) {
    seen = await dimension(bot)
    if (predicate(seen)) return seen
    await sleep(200)
  }
  throw new Error(`Timed out waiting for dimension transition; last dimension=${seen}`)
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
  await cmd(bot, '/damage @s 12') // -> 8 HP, under the 10 HP entry gate
  await sleep(400)

  const weakEntry = await cmd(bot, '/function shadowslave:nightmare/enter', /too weak/i)
  const dimAfterWeak = await dimension(bot)
  const hurtHealth = await score(bot, 'ss_scratch_a')
  // BOTH halves matter. The old OR would pass if the function printed the refusal and then
  // accidentally fell through into the Nightmare anyway.
  assert(
    'entry refused while too weak',
    /too weak/i.test(weakEntry) && notNightmare(dimAfterWeak),
    `health=${hurtHealth}, dimension=${dimAfterWeak}`
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

  const castOutOnDeath = await cmd(
    bot,
    '/execute if entity @s[advancements={shadowslave:test/cast_out=true}] run say GRANTED',
    /GRANTED|Test failed|passed/i
  )
  assert(
    'death does not grant the Cast Out advancement',
    !/GRANTED/.test(castOutOnDeath),
    'Cast Out records ejections only'
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
  await cmd(bot, '/scoreboard players set @s ss_cooldown 600')
  await cmd(bot, '/function shadowslave:sleep', /nothing reaches/i)
  const cdAfterSleep = await score(bot, 'ss_cooldown')
  assert('sleeping clears the cooldown', cdAfterSleep === null || cdAfterSleep === 0, `ss_cooldown=${cdAfterSleep}`)
  const dimAfterRecovery = await dimension(bot)
  assert('the recovery sleep does not pull you in', notNightmare(dimAfterRecovery), `dimension=${dimAfterRecovery}`)

  // --- ejection must not vacuum unrelated Nightmare loot ------------------
  await cmd(bot, '/scoreboard players reset @s ss_cooldown')
  await cmd(bot, '/function shadowslave:test/nightmare')
  await waitDimension(bot, inNightmare)
  await cmd(bot, '/summon item ~5 ~ ~5 {Item:{id:"minecraft:diamond_block",count:1}}')
  await sleep(300)
  await cmd(bot, '/damage @s 16')
  const ejectedTo = await waitDimension(bot, notNightmare)
  const strayHere = await cmd(bot, '/execute as @e[type=item,distance=..6] run data get entity @s Item.id')
  assert(
    'ejection does not sweep loose items onto you',
    notNightmare(ejectedTo) && !/diamond_block/.test(strayHere),
    `ejected to ${ejectedTo}; items the player never dropped must stay in the nightmare`
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
