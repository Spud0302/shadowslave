// Regression check for issues #20 and #21 — the frozen datapack supports one active
// First Nightmare at a time, including while its owner is offline.
//
// The datapack has one global Nightmare dimension, bossbar and creature selector. True
// per-player ownership belongs to the Java NightmareService. The safe compatibility fix is
// therefore a persistent global slot lock that survives disconnects and server restarts.
//
// This test proves the supported contract:
//   1. Alice enters and claims the persistent slot.
//   2. Alice disconnects while her persistent creature remains in unloaded chunks.
//   3. Bob is refused without receiving shared trial state.
//   4. Alice reconnects, resumes, completes, and releases the slot.
//   5. Bob can enter after teardown.
//
// Exit 0 means the serialization contract holds. Exit 1 means overlap, persistence or teardown is broken.

import mineflayer from 'mineflayer'

import { waitForDimensionObservation } from './dimension_wait.mjs'

const HOST = 'localhost'
const PORT = 25565
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))
const bots = {}
let commandLog = []
let commandTail = Promise.resolve()

function createBot(name) {
  return new Promise((resolve, reject) => {
    const bot = mineflayer.createBot({
      host: HOST,
      port: PORT,
      username: name,
      auth: 'offline',
      version: '1.21.1'
    })

    const timeout = setTimeout(() => reject(new Error(`${name} did not spawn within 15 seconds`)), 15000)
    bot.once('spawn', () => {
      clearTimeout(timeout)
      bots[name] = bot
      resolve(bot)
    })
    bot.once('error', (error) => {
      clearTimeout(timeout)
      reject(error)
    })
    bot.on('message', (message) => {
      if (name === 'tester') commandLog.push(message.toString())
    })
  })
}

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

async function dimension(player, waitMs = 350) {
  const output = await cmd(`/data get entity ${player} Dimension`, waitMs)
  const match = output.match(/"([a-z_]+:[a-z0-9_/.-]+)"/)
  return match ? match[1] : null
}

const inNightmare = (value) => value === 'shadowslave:nightmare'
const notNightmare = (value) => typeof value === 'string' && value !== 'shadowslave:nightmare'

async function waitDimension(player, predicate, timeoutMs = 60000) {
  return waitForDimensionObservation(
    (remainingMs) => dimension(player, Math.min(1000, remainingMs)),
    predicate,
    { timeoutMs, retryDelayMs: 200 }
  )
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

async function kickAndWait(player) {
  const bot = bots[player]
  const ended = new Promise((resolve) => bot.once('end', resolve))
  await cmd(`/kick ${player} Regression disconnect`, 500)
  await Promise.race([ended, sleep(5000)])
  delete bots[player]
}

function fail(message) {
  console.error(`FAIL: ${message}`)
  process.exitCode = 1
}

function finish(exitCode) {
  for (const bot of Object.values(bots)) {
    try {
      bot.quit('Regression complete')
    } catch {
      // A kicked or already-closed bot needs no further cleanup.
    }
  }
  setTimeout(() => process.exit(exitCode), 250)
}

async function run() {
  console.log('\n=== REGRESSION #20: persist and serialize the frozen-datapack trial slot ===')

  await cmd('/execute in shadowslave:nightmare run kill @e[tag=ss_creature]', 700)
  await cmd('/scoreboard players set $global ss_trial_lock 0')
  await resetPlayer('alice')
  await resetPlayer('bob')

  console.log('1) Alice enters and claims the persistent global slot')
  await cmd('/execute as alice at alice run function shadowslave:test/nightmare', 1200)

  const aliceEntryDimension = await waitDimension('alice', inNightmare)
  const aliceActive = await hasTag('alice', 'ss_in_nightmare')
  const entryLock = await score('ss_trial_lock', '$global')
  if (aliceEntryDimension !== 'shadowslave:nightmare' || !aliceActive || entryLock !== 1) {
    fail(`Alice did not enter cleanly (dimension=${aliceEntryDimension}, active=${aliceActive}, lock=${entryLock})`)
    return
  }

  console.log('2) Alice disconnects; the persistent lock must remain occupied')
  await kickAndWait('alice')
  const offlineLock = await score('ss_trial_lock', '$global')
  if (offlineLock !== 1) {
    fail(`Trial lock was lost when Alice disconnected (lock=${offlineLock})`)
    return
  }

  console.log('3) Bob is refused while the offline owner still holds the slot')
  await cmd('/execute as bob at bob run function shadowslave:test/nightmare', 900)

  const bobBlockedDimension = await dimension('bob')
  const bobBlockedActive = await hasTag('bob', 'ss_in_nightmare')
  const bobBlockedRank = await score('ss_rank', 'bob')
  const blockedLock = await score('ss_trial_lock', '$global')
  if (bobBlockedDimension !== 'minecraft:overworld') {
    fail(`Bob entered the shared dimension while Alice was offline (${bobBlockedDimension})`)
  }
  if (bobBlockedActive) {
    fail('Bob received ss_in_nightmare despite the persistent slot being occupied')
  }
  if (bobBlockedRank !== 0) {
    fail(`Bob progression changed while entry was refused (ss_rank=${bobBlockedRank})`)
  }
  if (blockedLock !== 1) {
    fail(`Bob's refused attempt changed the persistent lock (lock=${blockedLock})`)
  }

  console.log('4) Alice reconnects, resumes, completes, and releases the slot')
  await createBot('alice')
  await sleep(1500)

  const aliceResumeDimension = await waitDimension('alice', inNightmare)
  const aliceResumeActive = await hasTag('alice', 'ss_in_nightmare')
  if (aliceResumeDimension !== 'shadowslave:nightmare' || !aliceResumeActive) {
    fail(`Alice did not resume the owned trial (dimension=${aliceResumeDimension}, active=${aliceResumeActive})`)
    return
  }

  // This regression owns serialization and teardown, not the frozen prototype's global creature
  // selector. Invoke the real completion seam directly so scenario timing cannot turn a lock test
  // into a second copy of the known Issue #20 limitation.
  await cmd('/execute as alice at alice run function shadowslave:nightmare/survive', 1200)
  const aliceReturnDimension = await waitDimension('alice', notNightmare)
  const aliceRank = await score('ss_rank', 'alice')
  const aliceStillActive = await hasTag('alice', 'ss_in_nightmare')
  const releasedLock = await score('ss_trial_lock', '$global')
  if (aliceRank !== 1) {
    fail(`Alice did not receive completion progression (ss_rank=${aliceRank})`)
  }
  if (aliceReturnDimension !== 'minecraft:overworld') {
    fail(`Alice did not return after completion (${aliceReturnDimension})`)
  }
  if (aliceStillActive) {
    fail('Alice retained ss_in_nightmare after normal teardown')
  }
  if (releasedLock !== 0) {
    fail(`Normal teardown did not release the persistent slot (lock=${releasedLock})`)
  }

  console.log('5) Bob enters after Alice releases the slot')
  await cmd('/execute as bob at bob run function shadowslave:test/nightmare', 1200)

  const bobEntryDimension = await waitDimension('bob', inNightmare)
  const bobEntryActive = await hasTag('bob', 'ss_in_nightmare')
  const bobEntryLock = await score('ss_trial_lock', '$global')
  if (bobEntryDimension !== 'shadowslave:nightmare' || !bobEntryActive || bobEntryLock !== 1) {
    fail(`Bob could not enter after teardown (dimension=${bobEntryDimension}, active=${bobEntryActive}, lock=${bobEntryLock})`)
  }

  await cmd('/execute as bob at bob run function shadowslave:test/reset', 700)
  await cmd('/execute as alice at alice run function shadowslave:test/reset', 700)
  await cmd('/execute in shadowslave:nightmare run kill @e[tag=ss_creature]', 700)

  if (process.exitCode === 1) {
    console.error('REGRESSION FAILED')
    return
  }

  console.log('PASS: disconnect keeps the slot locked, reconnect resumes it, and teardown releases it.')
}

Promise.all(['alice', 'bob', 'tester'].map(createBot))
  .then(() => sleep(2500))
  .then(run)
  .then(() => finish(process.exitCode ?? 0))
  .catch((error) => {
    console.error('Regression setup failed:', error)
    finish(1)
  })
