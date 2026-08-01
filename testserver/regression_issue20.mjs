// Regression checks for issues #20 and #26.
//
// The frozen datapack has one global Nightmare dimension, bossbar and creature selector. It cannot
// safely run simultaneous First Nightmares, so the accepted compatibility fix is to refuse a second
// entrant at nightmare/enter, the one eligibility choke point. This test proves:
//   1. test/reset restores enough health for entry;
//   2. the first eligible player enters;
//   3. a second eligible player is refused clearly and receives no active/bypass residue;
//   4. the first player's creature can still be killed and their victory completes normally.
//
// Run directly with `node regression_issue20.mjs`; it is also part of `npm test`.

import mineflayer from 'mineflayer'

const HOST = 'localhost'
const PORT = 25565
const NAMES = ['alice', 'bob', 'tester']
const bots = {}
const logs = Object.fromEntries(NAMES.map((name) => [name, []]))
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))
let commandTail = Promise.resolve()

function command(commandText, waitMs = 350) {
  const task = commandTail.then(async () => {
    await sleep(90)
    logs.tester = []
    bots.tester.chat(commandText.startsWith('/') ? commandText : '/' + commandText)
    await sleep(waitMs)
    return logs.tester.join(' | ')
  })
  commandTail = task.catch(() => {})
  return task
}

async function dimension(player) {
  const out = await command(`/data get entity ${player} Dimension`)
  const match = out.match(/"([^\"]+)"/)
  if (!match) throw new Error(`Could not parse ${player} dimension from: ${out}`)
  return match[1]
}

async function health(player) {
  const out = await command(`/data get entity ${player} Health`)
  const match = out.match(/(-?\d+(?:\.\d+)?)f/)
  if (!match) throw new Error(`Could not parse ${player} health from: ${out}`)
  return parseFloat(match[1])
}

async function score(player, objective) {
  const out = await command(`/scoreboard players get ${player} ${objective}`)
  const match = out.match(/has (-?\d+) \[/)
  return match ? parseInt(match[1], 10) : null
}

async function hasTag(player, tag) {
  const out = await command(`/tag ${player} list`)
  if (!/has \d+ tags?|has no tags/i.test(out)) {
    throw new Error(`Could not read ${player} tags from: ${out}`)
  }
  return out.includes(tag)
}

async function waitFor(label, probe, predicate, timeoutMs = 8000) {
  const deadline = Date.now() + timeoutMs
  let seen = null
  while (Date.now() < deadline) {
    seen = await probe()
    if (predicate(seen)) return seen
    await sleep(200)
  }
  throw new Error(`Timed out waiting for ${label}; last value=${seen}`)
}

function require(condition, message) {
  if (!condition) throw new Error(message)
  console.log(`PASS  ${message}`)
}

async function run() {
  console.log('\n=== Concurrent First Nightmare admission regression ===\n')

  await command('/execute in shadowslave:nightmare run kill @e[tag=ss_creature]')
  for (const player of ['alice', 'bob']) {
    await command(`/gamemode survival ${player}`)
    await command(`/execute as ${player} at ${player} run function shadowslave:test/reset`, 650)
  }

  const aliceHealth = await health('alice')
  const bobHealth = await health('bob')
  require(aliceHealth >= 14 && bobHealth >= 14,
    `test/reset restores an enterable health baseline (alice=${aliceHealth}, bob=${bobHealth})`)

  // Keep the admitted player alive while the test forces the creature phase.
  await command('/effect give alice minecraft:resistance 120 4 true')
  await command('/execute as alice at alice run function shadowslave:test/nightmare', 1000)
  await waitFor('alice Nightmare entry', () => dimension('alice'), (value) => value === 'shadowslave:nightmare')
  require(await hasTag('alice', 'ss_in_nightmare'), 'first eligible player owns the active trial')

  logs.bob = []
  await command('/execute as bob at bob run function shadowslave:test/nightmare', 800)
  await sleep(300)

  const bobDimension = await dimension('bob')
  const bobMessages = logs.bob.join(' | ')
  require(bobDimension !== 'shadowslave:nightmare', 'second player remains outside the active Nightmare')
  require(!(await hasTag('bob', 'ss_in_nightmare')), 'second player receives no active-Nightmare tag')
  require(!(await hasTag('bob', 'ss_test_bypass')), 'refused test entry consumes its one-shot bypass')
  require(/Another First Nightmare is already unfolding/i.test(bobMessages),
    'second player receives the explicit compatibility-limit message')

  // Prove the refusal does not disturb the admitted player's ordinary completion path.
  await command('/scoreboard players set alice ss_timer 1')
  await waitFor(
    'alice creature spawn',
    () => hasTag('alice', 'ss_creature_spawned'),
    (value) => value === true,
    10000
  )
  await command('/execute in shadowslave:nightmare run kill @e[tag=ss_creature]', 500)
  await waitFor('alice victory', () => score('alice', 'ss_rank'), (value) => value === 1, 8000)
  await waitFor('alice return', () => dimension('alice'), (value) => value !== 'shadowslave:nightmare', 8000)
  require(!(await hasTag('alice', 'ss_in_nightmare')), 'first player completes and tears down normally')

  for (const player of ['alice', 'bob']) {
    await command(`/execute as ${player} at ${player} run function shadowslave:test/reset`, 500)
  }

  console.log('\nPASS  issues #20 and #26 regression checks completed\n')
  process.exit(0)
}

for (const name of NAMES) {
  const bot = mineflayer.createBot({ host: HOST, port: PORT, username: name, auth: 'offline', version: '1.21.1' })
  bot.on('message', (message) => logs[name].push(message.toString()))
  bot.on('error', (error) => {
    console.error(`${name} error:`, error.message)
    process.exit(1)
  })
  bots[name] = bot
}

Promise.all(Object.values(bots).map((bot) => new Promise((resolve) => bot.once('spawn', resolve))))
  .then(() => sleep(2500))
  .then(run)
  .catch((error) => {
    console.error('FAIL:', error)
    process.exit(1)
  })
