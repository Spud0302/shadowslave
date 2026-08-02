// DEFECT PROBE for issue #20 — a stray Nightmare Creature blocks victory and traps the player.
//
// STATUS: THIS FAILS ON PURPOSE. The defect is open. It is deliberately NOT in `npm test`.
// Run it directly:  node defect_issue20_stray_creature.mjs   (exit 0 = fixed, 1 = still broken)
//
// WHY THIS EXISTS SEPARATELY FROM regression_issue20.mjs
// They measure different properties and both are worth keeping:
//   * regression_issue20.mjs  — the CONTRACT: the persistent global slot remains occupied while
//                               its owner is offline. Belongs in the gate.
//   * this file               — the DEFECT: `nightmare/objective_tick` decides victory with
//                               `@e[tag=ss_creature]`, which is dimension-scoped rather than
//                               per-player, so ANY manually introduced stray creature stops
//                               `ss_gone` climbing.
// A passing contract test does not imply per-entity ownership. The persistent slot prevents the
// ordinary disconnect path from admitting another player, while Java owns the real long-term fix.
//
// THREE MEASUREMENT MISTAKES THAT MADE THIS LOOK FIXED WHEN IT WAS NOT — keep them if you edit:
//   1. `@e` is dimension-scoped, so a decoy in the Overworld is invisible to objective_tick.
//      The decoy MUST be inside shadowslave:nightmare.
//   2. Entities in unloaded chunks are absent from `@e`. A decoy 3000 blocks away was never
//      counted; it has to spawn in chunks the player is loading. (This is also why a stray
//      creature reads as "gone" after its owner disconnects - it is unloaded, not dead. That
//      mechanism is what made the disconnect gap reachable, so the note is load-bearing.)
//   3. `test/reset` did not restore health (#26) and entry refuses below 14 HP, so an unhealed
//      subject silently measures the entry-refusal path instead.
// The subject is also made damage-proof so ejection cannot end the run before the measurement.
//
// EXPECTED WHEN TRUE PER-ENTITY OWNERSHIP EXISTS: the decoy run wins, exactly like the control run.
// Then move this into the gate and delete this notice.

import mineflayer from 'mineflayer'

const HOST = 'localhost'
const PORT = 25565
const sleep = (ms) => new Promise((r) => setTimeout(r, ms))
const bots = {}
let log = []
let tail = Promise.resolve()

function cmd(command, ms = 350) {
  const task = tail.then(async () => {
    await sleep(90)
    log = []
    bots.tester.chat(command.startsWith('/') ? command : '/' + command)
    await sleep(ms)
    return log.join(' | ')
  })
  tail = task.catch(() => {})
  return task
}

async function score(objective, holder) {
  const out = await cmd(`/scoreboard players get ${holder} ${objective}`)
  const m = out.match(/has (-?\d+) \[/)
  return m ? parseInt(m[1], 10) : null
}

async function count(selector) {
  await cmd('/scoreboard players reset $n ss_scratch_a')
  await cmd(`/execute in shadowslave:nightmare store result score $n ss_scratch_a if entity ${selector}`)
  return score('ss_scratch_a', '$n')
}

async function dim(who) {
  const out = await cmd(`/data get entity ${who} Dimension`)
  const m = out.match(/"([a-z_]+:[a-z_/]+)"/)
  return m ? m[1].replace('minecraft:', 'mc/').replace('shadowslave:', 'SS/') : '??'
}

async function sample(label, n = 6) {
  for (let i = 1; i <= n; i++) {
    console.log(`   ${label} #${i}  creatures=${await count('@e[tag=ss_creature]')}`
      + `  ss_gone=${await score('ss_gone', 'alice')}`
      + `  rank=${await score('ss_rank', 'alice')}`
      + `  dim=${await dim('alice')}`)
    await sleep(500)
  }
}

async function trialWithDecoy(useDecoy) {
  console.log(`\n=== ${useDecoy ? 'B) WITH a decoy creature elsewhere' : 'A) CONTROL: no decoy'} ===`)
  await cmd('/kill @e[tag=ss_creature]', 700)
  await cmd('/execute in shadowslave:nightmare run kill @e[tag=ss_creature]', 700)
  await cmd('/scoreboard players set $global ss_trial_lock 0')
  await cmd('/gamemode survival alice')
  await cmd('/execute as alice at alice run function shadowslave:test/reset', 700)
  await cmd('/effect give alice minecraft:instant_health 1 10 true')
  await cmd('/effect clear alice')
  // Damage-proof so the ravager cannot eject her before the experiment finishes.
  await cmd('/effect give alice minecraft:resistance 120 4 true')
  await cmd('/effect give alice minecraft:regeneration 120 4 true')

  await cmd('/execute as alice at alice run function shadowslave:test/nightmare', 1200)
  await cmd('/execute as alice run scoreboard players set @s ss_timer 1')
  await sleep(2500)
  console.log('  after spawn: creatures=', await count('@e[tag=ss_creature]'),
    ' spawned_tag=', (await cmd('/tag alice list')).includes('ss_creature_spawned'))

  if (useDecoy) {
    // Stand-in for an unrelated entity carrying the global prototype tag.
    await cmd('/execute at alice run summon minecraft:ravager ~30 ~ ~30 '
      + '{Tags:["ss_creature","ss_decoy"],PersistenceRequired:1b,NoAI:1b,Invulnerable:1b,Silent:1b}', 800)
    console.log('  decoy summoned; creatures now =', await count('@e[tag=ss_creature]'))
  }

  console.log('  --- killing ONLY the real trial creature (decoy survives) ---')
  await cmd('/execute in shadowslave:nightmare run kill @e[tag=ss_creature,tag=!ss_decoy]', 900)
  console.log('  creatures remaining =', await count('@e[tag=ss_creature]'))
  await sample(useDecoy ? 'decoy' : 'ctrl')

  const rank = await score('ss_rank', 'alice')
  console.log(`  RESULT: rank=${rank} -> ${rank === 1 ? 'WON' : 'DID NOT WIN'}`)
  await cmd('/execute in shadowslave:nightmare run kill @e[tag=ss_creature]', 600)
  await cmd('/kill @e[tag=ss_creature]', 600)
  await cmd('/execute as alice at alice run function shadowslave:test/reset')
  await cmd('/scoreboard players set $global ss_trial_lock 0')
  return rank
}

async function run() {
  console.log('\n=== PROBE: does an unrelated tagged creature block victory? ===')
  const control = await trialWithDecoy(false)
  await sleep(1500)
  const withDecoy = await trialWithDecoy(true)

  console.log('\n=== VERDICT ===')
  console.log(`  no decoy   -> rank ${control} (${control === 1 ? 'won' : 'did not win'})`)
  console.log(`  with decoy -> rank ${withDecoy} (${withDecoy === 1 ? 'won' : 'did not win'})`)
  if (control !== 1) {
    console.log('  INVALID: the control run did not win, so the experiment proves nothing.')
    process.exit(2)
  }
  if (withDecoy === 1) {
    console.log('  PASS: the objective no longer depends on the global creature selector.')
    process.exit(0)
  }
  console.log('  FAIL: an unrelated creature in the nightmare still blocks victory.')
  console.log('  The supported one-slot contract is safe, but true per-entity ownership remains deferred to Java.')
  process.exit(1)
}

for (const name of ['alice', 'tester']) {
  const b = mineflayer.createBot({ host: HOST, port: PORT, username: name, auth: 'offline', version: '1.21.1' })
  b.on('message', (m) => { if (name === 'tester') log.push(m.toString()) })
  b.on('error', (e) => { console.error(`${name} error:`, e.message); process.exit(1) })
  bots[name] = b
}
Promise.all(Object.values(bots).map((b) => new Promise((r) => b.once('spawn', r))))
  .then(() => sleep(2500)).then(run)
  .catch((e) => { console.error('failed:', e); process.exit(1) })
