// Regression check for issues #20 and #21 — the frozen datapack supports one active
// First Nightmare at a time.
//
// The datapack has one global Nightmare dimension, bossbar and creature selector. True
// per-player ownership belongs to the Java NightmareService. The safe compatibility fix is
// therefore to refuse a second entrant before any shared trial state is created.
//
// This test proves the supported contract rather than injecting an impossible orphaned
// creature:
//   1. Alice enters a First Nightmare.
//   2. Bob attempts to enter while Alice is active and is refused without state leakage.
//   3. Alice can still complete and leave normally.
//   4. Bob can enter after Alice's teardown releases the global slot.
//
// Exit 0 means the serialization contract holds. Exit 1 means overlap or teardown is broken.

import mineflayer from 'mineflayer'

const HOST = 'localhost'
const PORT = 25565
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))
const bots = {}
let commandLog = []
let commandTail = Promise.resolve()

function cmd(command, waitMs = 350) {
  const task = commandTail.then(async () => {
    await sleep(90)
    commandLog = []
    bots.tester.chat(command.startsWith('/') ? command : '/' + command)
    await sleep(waitMs)
    return commandLog.join(' | ')
  })
  commandTail = task.catch(() => {})
  return task
}

async function score(objective, holder) {
  const output = await cmd(`/scoreboard players get ${holder} ${objective}`)
  const match = output.match(/has (-?\d+) \[/)
  return match ? Number.parseInt(match[1], 10) : null
}

async function dimension(player) {
  const output = await cmd(`/data get entity ${player} Dimension`)
  const match = output.match(/"([a-z_]+:[a-z0-9_/.-]+)"/)
  return match ? match[1] : null
}

async function hasTag(player, tag) {
  const output = await cmd(`/tag ${player} list`)
  return output.includes(tag)
}

async function resetPlayer(player) {
  await cmd(`/execute as ${player} at ${player} run function shadowslave:test/reset`, 700)
  // Keep the precondition explicit even though issue #26 makes reset restore health.
  await cmd(`/effect give ${player} minecraft:instant_health 1 10 true`)
  await cmd(`/execute in minecraft:overworld run tp ${player} 0 100 0`)
}

function fail(message) {
  console.error(`FAIL: ${message}`)
  process.exitCode = 1
}

async function run() {
  console.log('\n=== REGRESSION #20: serialize frozen-datapack First Nightmares ===')

  await cmd('/execute in shadowslave:nightmare run kill @e[tag=ss_creature]', 700)
  await resetPlayer('alice')
  await resetPlayer('bob')

  console.log('1) Alice enters the global trial slot')
  await cmd('/execute as alice at alice run function shadowslave:test/nightmare', 1200)

  const aliceEntryDimension = await dimension('alice')
  const aliceActive = await hasTag('alice', 'ss_in_nightmare')
  if (aliceEntryDimension !== 'shadowslave:nightmare' || !aliceActive) {
    fail(`Alice did not enter cleanly (dimension=${aliceEntryDimension}, active=${aliceActive})`)
    return
  }

  console.log('2) Bob is refused while Alice owns the slot')
  await cmd('/execute as bob at bob run function shadowslave:test/nightmare', 900)

  const bobBlockedDimension = await dimension('bob')
  const bobBlockedActive = await hasTag('bob', 'ss_in_nightmare')
  const bobBlockedRank = await score('ss_rank', 'bob')
  if (bobBlockedDimension !== 'minecraft:overworld') {
    fail(`Bob entered the shared dimension during Alice's trial (${bobBlockedDimension})`)
  }
  if (bobBlockedActive) {
    fail('Bob received ss_in_nightmare despite the global slot being occupied')
  }
  if (bobBlockedRank !== 0) {
    fail(`Bob progression changed while entry was refused (ss_rank=${bobBlockedRank})`)
  }

  console.log('3) Alice completes without Bob creating shared creature state')
  await cmd('/execute as alice run scoreboard players set @s ss_timer 1')
  await sleep(2500)
  await cmd('/execute in shadowslave:nightmare run kill @e[tag=ss_creature]', 800)
  await sleep(3000)

  const aliceRank = await score('ss_rank', 'alice')
  const aliceReturnDimension = await dimension('alice')
  const aliceStillActive = await hasTag('alice', 'ss_in_nightmare')
  if (aliceRank !== 1) {
    fail(`Alice did not receive completion progression (ss_rank=${aliceRank})`)
  }
  if (aliceReturnDimension !== 'minecraft:overworld') {
    fail(`Alice did not return after completion (${aliceReturnDimension})`)
  }
  if (aliceStillActive) {
    fail('Alice retained ss_in_nightmare after normal teardown')
  }

  console.log('4) Bob enters after Alice releases the slot')
  await cmd('/execute as bob at bob run function shadowslave:test/nightmare', 1200)

  const bobEntryDimension = await dimension('bob')
  const bobEntryActive = await hasTag('bob', 'ss_in_nightmare')
  if (bobEntryDimension !== 'shadowslave:nightmare' || !bobEntryActive) {
    fail(`Bob could not enter after teardown (dimension=${bobEntryDimension}, active=${bobEntryActive})`)
  }

  await cmd('/execute as bob at bob run function shadowslave:test/reset', 700)
  await cmd('/execute as alice at alice run function shadowslave:test/reset', 700)
  await cmd('/execute in shadowslave:nightmare run kill @e[tag=ss_creature]', 700)

  if (process.exitCode === 1) {
    console.error('REGRESSION FAILED')
    return
  }

  console.log('PASS: concurrent entry is refused, the active trial completes, and teardown releases the slot.')
}

for (const name of ['alice', 'bob', 'tester']) {
  const bot = mineflayer.createBot({
    host: HOST,
    port: PORT,
    username: name,
    auth: 'offline',
    version: '1.21.1'
  })
  bot.on('message', (message) => {
    if (name === 'tester') commandLog.push(message.toString())
  })
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
    console.error('Regression setup failed:', error)
    process.exit(1)
  })
