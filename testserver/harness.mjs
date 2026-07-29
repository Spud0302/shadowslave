// Shadow Slave — automated assertion harness.
//
// A mineflayer bot joins the local test server as a normal player, runs commands, and reads
// the chat replies back. That is the whole trick: Minecraft answers most `/scoreboard`,
// `/data` and `/execute` queries in chat, so a bot that can read chat can assert on game
// state without a client.
//
// It covers the MECHANICAL half only — state transitions, guards, thresholds, teardown.
// Everything requiring judgement (does the fight feel fair, is the darkness right, does the
// bossbar render) is collected into a "needs a human" list and printed at the end.
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

/** Wait ms. */
const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

/**
 * Run a command and return everything the server said in reply.
 * Commands answer asynchronously, so we clear the log, send, and wait a beat.
 */
async function cmd(bot, command, expect = null, timeoutMs = 4000) {
  chatLog = []
  bot.chat(command.startsWith('/') ? command : '/' + command)
  // Fixed sleeps do not work here. After a dimension change the server's replies lag, and a
  // query's answer lands in the NEXT query's window — which made entry look broken when it
  // was fine. Poll for a reply that matches instead, and only fall back to a timeout.
  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline) {
    await sleep(60)
    const joined = chatLog.join('\n')
    if (expect ? expect.test(joined) : joined.length > 0) return joined
  }
  return chatLog.join('\n')
}

/** Read a scoreboard value for the bot. Returns a number, or null if the score is absent. */
async function score(bot, objective) {
  const out = await cmd(bot, `/scoreboard players get ${USER} ${objective}`, /has -?\d+|Can't get|no score/i)
  const m = out.match(/has (-?\d+)/)
  return m ? parseInt(m[1], 10) : null
}

/** True if the bot currently has the given tag. */
async function hasTag(bot, tag) {
  const out = await cmd(bot, `/tag ${USER} list`, /has \d+ tags?|has no tags/i)
  return out.includes(tag)
}

/** The dimension the bot is in, as reported by the server. */
async function dimension(bot) {
  // mineflayer tracks this from the respawn packet, so it is both instant and authoritative —
  // no chat round-trip to race with.
  if (bot.game && bot.game.dimension) return bot.game.dimension
  const out = await cmd(bot, `/data get entity ${USER} Dimension`, /entity data|failed/i)
  const m = out.match(/"([^"]+)"/)
  return m ? m[1] : null
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

  // --- infection lifecycle -------------------------------------------------
  assert('untouched has no carrier tag', !(await hasTag(bot, 'ss_carrier')))

  await cmd(bot, '/function shadowslave:test/infect')
  assert('infect marks a Carrier', await hasTag(bot, 'ss_carrier'))

  const reInfect = await cmd(bot, '/function shadowslave:test/infect')
  assert('infect refuses twice', /already a Carrier/i.test(reInfect), 'guard on existing state')

  const cureAsCarrier = await cmd(bot, '/function shadowslave:test/cure')
  assert('cure removes the mark', !(await hasTag(bot, 'ss_carrier')))
  assert('cure on a Carrier does not refuse', !/Awakened/i.test(cureAsCarrier))

  // --- the weakness gate (v1.4.1) -----------------------------------------
  await cmd(bot, '/function shadowslave:test/infect')
  await cmd(bot, '/effect give @s minecraft:instant_damage 1 1 true')
  await sleep(400)
  const hurtHealth = await score(bot, 'ss_scratch_a')
  const weakEntry = await cmd(bot, '/function shadowslave:test/nightmare')
  const dimAfterWeak = await dimension(bot)
  assert(
    'entry refused while too weak',
    /too weak/i.test(weakEntry) || dimAfterWeak !== 'shadowslave:nightmare',
    `dimension=${dimAfterWeak}`
  )

  // --- entry ---------------------------------------------------------------
  await cmd(bot, '/effect give @s minecraft:instant_health 1 10 true')
  await sleep(400)
  await cmd(bot, '/function shadowslave:test/nightmare')
  const dimIn = await dimension(bot)
  assert('test/nightmare enters the dimension', dimIn === 'shadowslave:nightmare', `dimension=${dimIn}`)
  assert('entry sets the in-nightmare tag', await hasTag(bot, 'ss_in_nightmare'))
  const timer = await score(bot, 'ss_timer')
  assert('entry starts the countdown', timer !== null && timer > 0 && timer <= 6000, `ss_timer=${timer}`)

  const reEnter = await cmd(bot, '/function shadowslave:test/nightmare')
  assert('re-entry refused while inside', /already in a nightmare/i.test(reEnter))

  // --- the soul-readout collision (1.7) -----------------------------------
  // The readout used to write armour into ss_health, which the ejection check reads.
  await cmd(bot, '/trigger soul')
  await sleep(1200)
  const stillIn = await dimension(bot)
  assert('reading the soul does not eject you', stillIn === 'shadowslave:nightmare', `dimension=${stillIn}`)

  // --- death teardown and item recovery (1.6, 1.8) ------------------------
  await cmd(bot, '/give @s minecraft:diamond 7')
  await sleep(400)
  await cmd(bot, '/kill @s')
  await sleep(2000)
  const dimAfterDeath = await dimension(bot)
  assert('death clears the in-nightmare tag', !(await hasTag(bot, 'ss_in_nightmare')))
  assert('death does not strand you in the nightmare', dimAfterDeath !== 'shadowslave:nightmare', `dimension=${dimAfterDeath}`)

  await sleep(1500)  // the sweep is scheduled 5 ticks after the teardown
  const drops = await cmd(bot, '/execute as @s at @s run data get entity @e[type=item,distance=..16,limit=1] Item.id', /entity data|No entity/i)
  assert(
    'dropped items are recoverable near the return point',
    /diamond/i.test(drops),
    drops.includes('No entity') ? 'no item entities found within 12 blocks' : drops.slice(0, 120)
  )

  // --- cooldown (1.4.0) ---------------------------------------------------
  const cd = await score(bot, 'ss_cooldown')
  needsHuman('cooldown after ejection', 'harness kills rather than being ejected, so cooldown was not exercised')

  // --- things only a person can judge -------------------------------------
  needsHuman('the fight', 'creature damage, pacing, whether 60 health is right at wood/no-armour')
  needsHuman('the dark', 'whether ambient_light 0.1 reads as oppressive or unplayable')
  needsHuman('the bossbar', 'renders, switches to the creature, tracks its health')
  needsHuman('mob spawn rate', 'the nightmare biome spawner weights')
  needsHuman('sneak-to-enter feel', 'whether the hold and its telegraph read as deliberate')

  console.log('\n=== summary ===')
  console.log(`${pass.length} passed, ${fail.length} failed`)
  if (fail.length) {
    console.log('\nFAILED:')
    fail.forEach((f) => console.log('  ' + f))
  }
  console.log('\nNEEDS A HUMAN:')
  human.forEach((h) => console.log('  ' + h))
  console.log()
}

const bot = mineflayer.createBot({ host: HOST, port: PORT, username: USER, auth: 'offline', version: '1.21.1' })

bot.on('message', (msg) => chatLog.push(msg.toString()))
bot.on('error', (e) => { console.error('bot error:', e.message); process.exit(1) })
bot.on('kicked', (r) => { console.error('kicked:', r); process.exit(1) })

bot.once('spawn', async () => {
  try {
    await sleep(1500)
    await run(bot)
  } catch (e) {
    console.error('harness error:', e)
    process.exitCode = 1
  } finally {
    bot.quit()
    process.exit(fail.length ? 1 : 0)
  }
})
