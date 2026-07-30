// Deterministic verification for the four generated Flaw families.
//
// The main harness cannot fight the Nightmare Creature, so real classification remains a human test.
// These checks deliberately start *after* classification: test-only functions inject the observation
// signal and then run the real progression/become_sleeper -> roll_aspect_flaw path. This proves the
// selected family, generated identity band, mechanical burden and cleanup without duplicating runtime
// generation logic in JavaScript.
//
// STATUS (GPT after 0.7.1): Q4 remains diagnostic, not a release gate. Claude eliminated four guesses
// and showed that adding chat-based diagnostics changes the timing. The fled case now enables a
// TEST-ONLY server-side trace in flaw/weightless: ss_scratch_a counts executions and ss_scratch_b stores
// safe_fall_distance * 1000 immediately after the modifier command. The trace is read only AFTER the
// normal attribute assertion succeeds or fails, so observation cannot perturb the state being measured.

import mineflayer from 'mineflayer'

const HOST = 'localhost'
const PORT = 25565
const USER = 'tester'
const pass = []
const fail = []
let chatLog = []
let commandTail = Promise.resolve()

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

async function runCommand(bot, command, expect = null, timeoutMs = 4000) {
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
  if (expect) throw new Error(`Timed out waiting for ${expect} after ${command}; saw ${joined || '<no reply>'}`)
  return joined
}

// Every query below consumes the same chatLog. Queue commands so no two callers can own the reply
// window at once. This fixed a real independent harness bug in 0.7.1 even though it was not Q4.
function cmd(bot, command, expect = null, timeoutMs = 4000) {
  const task = commandTail.then(() => runCommand(bot, command, expect, timeoutMs))
  commandTail = task.catch(() => {})
  return task
}

async function score(bot, objective, holder = USER) {
  const out = await cmd(bot, `/scoreboard players get ${holder} ${objective}`, /has -?\d+ \[|Can't get|no score/i)
  const match = out.match(/has (-?\d+) \[/)
  return match ? parseInt(match[1], 10) : null
}

async function tagList(bot) {
  return cmd(bot, `/tag ${USER} list`, /has \d+ tags?|has no tags/i)
}

async function hasTag(bot, tag) {
  const out = await tagList(bot)
  return out.includes(tag)
}

async function attributeValue(bot, attribute) {
  const out = await cmd(bot, `/attribute @s ${attribute} get`, / is -?\d+(?:\.\d+)?/i)
  const match = out.match(/is (-?\d+(?:\.\d+)?)/i)
  if (!match) throw new Error(`Could not parse ${attribute}: ${out}`)
  return parseFloat(match[1])
}

// Claude proved Q4 is not a short timeout, but a generous budget costs nothing on a passing poll.
async function waitAttribute(bot, attribute, predicate, timeoutMs = 10000) {
  const deadline = Date.now() + timeoutMs
  let seen = null
  while (Date.now() < deadline) {
    seen = await attributeValue(bot, attribute)
    if (predicate(seen)) return seen
    await sleep(200)
  }
  throw new Error(`Timed out waiting for ${attribute}; last=${seen}`)
}

function assert(name, condition, detail = '') {
  const line = detail ? `${name} — ${detail}` : name
  ;(condition ? pass : fail).push(line)
  console.log(`${condition ? 'PASS' : 'FAIL'}  ${line}`)
}

async function exactOneTag(bot, tags) {
  const out = await tagList(bot)
  return tags.filter((tag) => out.includes(tag)).length === 1
}

const flawTags = [
  'ss_flaw_shadow_slave',
  'ss_flaw_fragile',
  'ss_flaw_ravenous',
  'ss_flaw_weightless'
]
const aspectTags = ['ss_aspect_shadow', 'ss_aspect_flame', 'ss_aspect_bone', 'ss_aspect_wind']

async function forceFamily(bot, name, bandMin, bandMax, expectedTag) {
  await cmd(bot, '/function shadowslave:test/reset')

  // Q4 discriminator: reset must first restore vanilla safe_fall_distance=3.
  if (name === 'fled') {
    const cleanFall = await waitAttribute(bot, 'minecraft:generic.safe_fall_distance', (value) => value === 3)
    assert('fled starts from a clean safe-fall baseline', cleanFall === 3, `safe_fall_distance=${cleanFall}`)

    // Enable an IN-SERVER observation before generation. No chat query reads these values until after
    // the normal attribute assertion, so the trace cannot make the intermittent timing disappear.
    await cmd(bot, '/tag @s add ss_test_trace_weightless')
    await cmd(bot, '/scoreboard players set @s ss_scratch_a 0')
    await cmd(bot, '/scoreboard players set @s ss_scratch_b -9999')
  }

  const applied = await cmd(bot, `/function shadowslave:test/flaw/${name}`, /Sleeper/i)
  if (/Already a Sleeper/i.test(applied)) {
    throw new Error(`test/flaw/${name} refused: player was still a Sleeper after test/reset`)
  }

  // Drive upkeep once so family mechanics are exercised directly. Claude already established that
  // this does not itself resolve Q4; it simply removes the once-per-second scheduler from the test.
  await cmd(bot, '/execute as @s at @s run function shadowslave:upkeep')
  await sleep(200)

  const flaw = await score(bot, 'ss_flaw')
  const aspect = await score(bot, 'ss_aspect')
  assert(
    `${name} produces its generated Flaw score band`,
    flaw !== null && flaw >= bandMin && flaw <= bandMax,
    `ss_flaw=${flaw}, expected ${bandMin}..${bandMax}`
  )
  assert(`${name} selects its expected mechanics tag`, await hasTag(bot, expectedTag), expectedTag)
  assert(`${name} leaves exactly one Flaw mechanics tag`, await exactOneTag(bot, flawTags))
  assert(`${name} also leaves exactly one Aspect mechanics tag`, await exactOneTag(bot, aspectTags))
  assert(
    `${name} generates an encoded Aspect identity`,
    aspect !== null && [1, 2, 3, 4].includes(Math.floor(aspect / 10)) && [1, 2, 3, 4].includes(aspect % 10),
    `ss_aspect=${aspect}`
  )
  assert(`${name} consumes bloodied observation`, !(await hasTag(bot, 'ss_trial_bloodied')))
  assert(`${name} consumes hungry observation`, !(await hasTag(bot, 'ss_trial_hungry')))
  assert(`${name} consumes fled observation`, !(await hasTag(bot, 'ss_trial_fled')))
}

async function run(bot) {
  console.log('\n=== Shadow Slave deterministic Flaw harness ===\n')
  await cmd(bot, '/gamemode survival')
  await cmd(bot, '/gamerule naturalRegeneration false', /naturalRegeneration/i)

  await forceFamily(bot, 'baseline', 11, 14, 'ss_flaw_shadow_slave')
  await forceFamily(bot, 'bloodied', 21, 24, 'ss_flaw_fragile')

  const reducedHealth = await waitAttribute(bot, 'minecraft:generic.max_health', (value) => value === 14)
  assert('bloodied family applies the reduced-health burden', reducedHealth === 14, `max_health=${reducedHealth}`)
  await cmd(bot, '/function shadowslave:test/reset')
  const restoredHealth = await waitAttribute(bot, 'minecraft:generic.max_health', (value) => value === 20)
  assert('reset removes the reduced-health modifier', restoredHealth === 20, `max_health=${restoredHealth}`)

  await forceFamily(bot, 'hungry', 31, 34, 'ss_flaw_ravenous')
  await cmd(bot, '/function shadowslave:flaw/ravenous')
  await cmd(bot, '/scoreboard players reset $hunger ss_scratch_a')
  await cmd(
    bot,
    '/execute store success score $hunger ss_scratch_a run effect clear @s minecraft:hunger',
    /score|effect|Removed|Nothing changed|Test failed/i
  )
  const hungerWasApplied = await score(bot, 'ss_scratch_a', '$hunger')
  assert('hungry family applies Hunger', hungerWasApplied === 1, `effect-clear success=${hungerWasApplied}`)
  await cmd(bot, '/function shadowslave:flaw/ravenous')
  await cmd(bot, '/function shadowslave:test/reset')
  await cmd(bot, '/scoreboard players reset $hunger ss_scratch_a')
  await cmd(
    bot,
    '/execute store success score $hunger ss_scratch_a run effect clear @s minecraft:hunger',
    /score|effect|Removed|Nothing changed|Test failed/i
  )
  const hungerAfterReset = await score(bot, 'ss_scratch_a', '$hunger')
  assert('reset clears transient Hunger', hungerAfterReset === 0, `effect-clear success=${hungerAfterReset}`)

  await forceFamily(bot, 'fled', 41, 44, 'ss_flaw_weightless')

  let reducedFall = null
  let fallError = null
  try {
    reducedFall = await waitAttribute(bot, 'minecraft:generic.safe_fall_distance', (value) => value === 2)
  } catch (error) {
    fallError = error
  }

  // Read the trace only after the observation window. These commands therefore cannot change whether
  // the ordinary attribute poll saw 2 or got stuck at 3.
  const traceRuns = await score(bot, 'ss_scratch_a')
  const traceFallMilli = await score(bot, 'ss_scratch_b')
  assert(
    'Weightless server-side trace executed',
    traceRuns !== null && traceRuns > 0,
    `executions=${traceRuns}`
  )
  assert(
    'server saw safe-fall distance 2 immediately after Weightless add',
    traceFallMilli === 2000,
    `safe_fall_distance*1000=${traceFallMilli}`
  )

  if (fallError) {
    throw new Error(
      `${fallError instanceof Error ? fallError.message : String(fallError)}; ` +
      `server trace executions=${traceRuns}, immediate safe_fall_distance*1000=${traceFallMilli}`
    )
  }

  assert('fled family applies the unsafe-footing burden', reducedFall === 2, `safe_fall_distance=${reducedFall}`)
  await cmd(bot, '/tag @s remove ss_test_trace_weightless')
  await cmd(bot, '/function shadowslave:test/reset')
  const restoredFall = await waitAttribute(bot, 'minecraft:generic.safe_fall_distance', (value) => value === 3)
  assert('reset removes the fall-distance modifier', restoredFall === 3, `safe_fall_distance=${restoredFall}`)

  // The real classifier gives fled > hungry > bloodied. Prove that precedence in the production
  // generator without adding a special "precedence" implementation path.
  await cmd(bot, '/function shadowslave:test/reset')
  await cmd(bot, '/function shadowslave:test/flaw/_prepare')
  await cmd(bot, '/tag @s add ss_trial_bloodied')
  await cmd(bot, '/tag @s add ss_trial_hungry')
  await cmd(bot, '/tag @s add ss_trial_fled')
  await cmd(bot, '/function shadowslave:progression/become_sleeper', /Sleeper/i)
  const precedence = await score(bot, 'ss_flaw')
  assert('Flaw precedence remains fled > hungry > bloodied', precedence !== null && precedence >= 41 && precedence <= 44, `ss_flaw=${precedence}`)

  console.log('\n=== deterministic Flaw summary ===')
  console.log(`${pass.length} passed, ${fail.length} failed\n`)
}

const bot = mineflayer.createBot({ host: HOST, port: PORT, username: USER, auth: 'offline', version: '1.21.1' })
bot.on('message', (msg) => chatLog.push(msg.toString()))
bot.on('error', (error) => { console.error('bot error:', error.message); process.exit(1) })
bot.on('kicked', (reason) => { console.error('kicked:', reason); process.exit(1) })

bot.once('spawn', async () => {
  try {
    await sleep(1500)
    await run(bot)
  } catch (error) {
    console.error('Flaw harness error:', error)
    fail.push(`harness exception — ${error instanceof Error ? error.message : String(error)}`)
  } finally {
    bot.quit()
    process.exit(fail.length ? 1 : 0)
  }
})
