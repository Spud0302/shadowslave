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
  // Drain first. A reply that arrives after the previous cmd() gave up still lands in the next
  // command's window, where a loose pattern happily matches it — that is how `/tag list`'s
  // "tester has 2 tags:" was read as ss_timer=2, failing entry assertions on a pack that was
  // working correctly. Settle, then clear, then send.
  await sleep(120)
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
  const out = await cmd(bot, `/scoreboard players get ${USER} ${objective}`, /has -?\d+ \[|Can't get|no score/i)
  const m = out.match(/has (-?\d+) \[/)
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
  // Call the real entry function, not test/nightmare — as of 1.4.4 the wrapper carries
  // ss_test_bypass and is meant to walk straight through this gate. Asserting on the wrapper
  // would test the bypass while claiming to test the gate.
  await cmd(bot, '/function shadowslave:test/infect')
  await cmd(bot, '/damage @s 12')  // -> 8 HP, under the 1.4.5 entry gate of 10
  await sleep(400)
  const hurtHealth = await score(bot, 'ss_scratch_a')
  const weakEntry = await cmd(bot, '/function shadowslave:nightmare/enter')
  const dimAfterWeak = await dimension(bot)
  assert(
    'entry refused while too weak',
    /too weak/i.test(weakEntry) || dimAfterWeak !== 'shadowslave:nightmare',
    `dimension=${dimAfterWeak}`
  )

  // ...and the bypass gets the tester in at the same health that just refused everyone else.
  await cmd(bot, '/function shadowslave:test/nightmare')
  const dimAfterBypass = await dimension(bot)
  assert(
    'test/nightmare bypasses the weakness gate',
    dimAfterBypass === 'shadowslave:nightmare',
    `hurt to ${hurtHealth}, dimension=${dimAfterBypass}`
  )
  assert('the bypass tag is consumed by entry', !(await hasTag(bot, 'ss_test_bypass')))
  await cmd(bot, '/function shadowslave:nightmare/leave')
  await cmd(bot, '/scoreboard players reset @s ss_cooldown')

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

  // Item recovery on death is NOT asserted here, deliberately.
  //
  // Three different assertions "failed" this feature while it worked, and the probes finally
  // showed why: after the player leaves, the nightmare chunks unload, so `execute in
  // shadowslave:nightmare run data get entity @e[type=item]` reports nothing whether or not
  // items are there. Force-loading the area makes them appear — and then the overworld query
  // returns the SAME coordinates as the nightmare query, so the check cannot even tell the two
  // dimensions apart. A test that returns the same answer for pass and fail is worse than none.
  //
  // Confirmed in-game instead, twice, by the playtester: after dying in the trial the drops
  // land around the bed, most within pickup range, a few a couple of blocks out.
  needsHuman(
    'item recovery on death',
    'not machine-checkable — nightmare chunks unload before the query runs; re-confirm by hand'
  )

  // --- T5: an untouched player is Mundane, not a Sleeper ------------------
  await cmd(bot, '/function shadowslave:test/reset')
  // Wait for `Rank:` specifically. /Soul/ also matches the "Triggered [Soul]" receipt,
  // which arrives a tick before the readout itself.
  const soulUntouched = await cmd(bot, '/trigger soul', /Rank:/)
  assert('untouched reads as Mundane', /Mundane/i.test(soulUntouched), soulUntouched.match(/Rank:[^\n]*/)?.[0] || '')

  // --- T9: cure refuses on an Awakened ------------------------------------
  await cmd(bot, '/function shadowslave:test/awaken', /Awakened/i)
  const cureAwakened = await cmd(bot, '/function shadowslave:test/cure', /Awakened|lost interest/i)
  assert('cure refuses on an Awakened', /cannot lose interest|test\/reset/i.test(cureAwakened), cureAwakened.slice(0, 80))

  // --- T7: re-rolling clears the previous Aspect's modifiers --------------
  // Force Bone so there is definitely an armour modifier to leave behind.
  await cmd(bot, '/tag @s remove ss_aspect_shadow')
  await cmd(bot, '/tag @s remove ss_aspect_flame')
  await cmd(bot, '/tag @s remove ss_aspect_wind')
  await cmd(bot, '/tag @s add ss_aspect_bone')
  await sleep(1400)  // upkeep runs once a second
  const armourWithBone = await cmd(bot, '/attribute @s minecraft:generic.armor get', /Value of|attribute/i)
  await cmd(bot, '/function shadowslave:test/reset')
  await cmd(bot, '/function shadowslave:test/awaken', /Awakened/i)
  await sleep(1400)
  const armourAfterReroll = await cmd(bot, '/attribute @s minecraft:generic.armor get', /Value of|attribute/i)
  const boneVal = parseFloat(armourWithBone.match(/is ([\d.]+)/)?.[1] ?? '0')
  const afterVal = parseFloat(armourAfterReroll.match(/is ([\d.]+)/)?.[1] ?? '-1')
  const rerolledBone = /ss_aspect_bone/.test(await cmd(bot, `/tag ${USER} list`, /has \d+ tags?|has no tags/i))
  assert(
    'a re-roll does not leave the old Aspect modifier behind',
    rerolledBone ? afterVal === boneVal : afterVal < boneVal,
    `bone=${boneVal} after=${afterVal}${rerolledBone ? ' (rerolled Bone again)' : ''}`
  )

  // --- T2/T3: a real ejection sets the cooldown and locks you out ---------
  await cmd(bot, '/function shadowslave:test/reset')
  await cmd(bot, '/effect give @s minecraft:instant_health 1 10 true')
  await cmd(bot, '/function shadowslave:test/infect')
  await cmd(bot, '/function shadowslave:test/nightmare')
  await sleep(600)
  // drop below the ejection threshold without dying
  await cmd(bot, '/damage @s 16')  // -> 4 HP, at the 1.4.5 ejection threshold
  await sleep(2500)
  const dimAfterEject = await dimension(bot)
  assert('low health ejects you from the trial', dimAfterEject !== 'shadowslave:nightmare', `dimension=${dimAfterEject}`)

  const cooldown = await score(bot, 'ss_cooldown')
  assert('ejection starts the cooldown', cooldown !== null && cooldown > 0, `ss_cooldown=${cooldown}`)

  await cmd(bot, '/effect give @s minecraft:instant_health 1 10 true')
  await sleep(400)
  // Call the real entry function, not the test wrapper — the wrapper clears the cooldown on
  // purpose, so it would prove nothing.
  const entryDuringCooldown = await cmd(bot, '/execute as @s at @s run function shadowslave:nightmare/enter')
  const dimDuringCooldown = await dimension(bot)
  assert(
    'you cannot re-enter during the cooldown',
    dimDuringCooldown !== 'shadowslave:nightmare',
    `dimension=${dimDuringCooldown}`
  )

  // --- 1.4.5: sleeping ends the cooldown, rather than 600s of wall clock ---
  await cmd(bot, '/scoreboard players set @s ss_cooldown 600')
  await cmd(bot, '/function shadowslave:sleep')
  const cdAfterSleep = await score(bot, 'ss_cooldown')
  assert('sleeping clears the cooldown', cdAfterSleep === null || cdAfterSleep === 0, `ss_cooldown=${cdAfterSleep}`)
  // The recovery sleep must NOT also pull you in — clearing the score and testing it for the
  // return on the next line would read the value just erased and fall through into the trial.
  const dimAfterRecovery = await dimension(bot)
  assert(
    'the recovery sleep does not pull you in',
    dimAfterRecovery !== 'shadowslave:nightmare',
    `dimension=${dimAfterRecovery}`
  )

  // --- 1.4.5: ejection must not vacuum the nightmare onto the player -------
  await cmd(bot, '/scoreboard players reset @s ss_cooldown')
  await cmd(bot, '/function shadowslave:test/nightmare')
  await cmd(bot, '/summon item ~5 ~ ~5 {Item:{id:"minecraft:diamond_block",count:1}}')
  await sleep(300)
  await cmd(bot, '/damage @s 16')
  await sleep(600)
  const strayHere = await cmd(bot, '/execute as @e[type=item,distance=..6] run data get entity @s Item.id')
  assert(
    'ejection does not sweep loose items onto you',
    !/diamond_block/.test(strayHere),
    'items the player never dropped must stay in the nightmare'
  )
  await cmd(bot, '/scoreboard players reset @s ss_cooldown')

  // --- cooldown (1.4.0) ---------------------------------------------------

  // --- things only a person can judge -------------------------------------
  // Keep this list honest. Everything below is genuinely unsettled; anything the playtester has
  // already ruled on gets deleted, not left here. A list that re-asks answered questions wastes
  // the one resource this harness cannot replace.
  //
  // Settled, do not re-add: the fight (too hard at wood/no-armour -> drove the cooldown and the
  // 1.4.5 threshold drop), the dark (ambient_light 0.1 fine), the bossbar (switches to the
  // creature and tracks its health), spawn rates ("could go either way"), sneak-to-enter feel
  // (tap does nothing, hold takes you, telegraph is immediate).
  needsHuman('the fight at 2 hearts', '1.4.5 dropped ejection to 4 HP — does it now kill you outright too often?')
  needsHuman('the recovery sleep', '1.4.5: sleeping ends the cooldown. Does one night feel like the right price for losing?')

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
