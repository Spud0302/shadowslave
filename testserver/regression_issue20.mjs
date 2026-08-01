// Regression check for issue #20 — concurrent First Nightmares block each other's victory.
//
// STATUS: THIS CURRENTLY FAILS. The bug it measures is open. It is deliberately NOT part of
// `npm test`, because wiring a known-failing check into the release gate would break the gate.
// Run it directly:  node regression_issue20.mjs   (exit 0 = fixed, exit 1 = still broken)
//
// WHAT IT MEASURES
// `nightmare/objective_tick` decides victory with `@e[tag=ss_creature]`. That selector is
// DIMENSION-scoped, not per-player, and every concurrent trial shares the one
// `shadowslave:nightmare` dimension — so any other player's creature keeps resetting your
// `ss_gone` counter and you can neither win nor leave.
//
// Rather than driving two real trials (bots get ejected in seconds), it isolates the variable:
// one player in a trial plus one inert decoy creature standing in for a second player's.
// Control run and decoy run are otherwise identical.
//
// THREE THINGS THAT MADE EARLIER ATTEMPTS FALSELY PASS — keep them if you edit this:
//   1. `@e` is dimension-scoped, so a decoy in the Overworld is invisible to objective_tick.
//      The decoy MUST be inside shadowslave:nightmare.
//   2. Entities in unloaded chunks are absent from `@e`. A decoy 3000 blocks away was never
//      counted; it has to spawn in chunks the player is loading.
//   3. `test/reset` does not restore health (issue #26) and entry refuses below 14 HP, so the
//      subject must be healed or the run silently measures the refusal path instead.
// The subject is also made damage-proof so ejection cannot end the run before the measurement.
//
// EXPECTED WHEN FIXED: the decoy run wins, exactly like the control run.

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
    // Stand-in for a second player's creature: far away but in the SAME nightmare
    // dimension, which is where every concurrent trial actually runs.
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
  return rank
}

async function run() {
  console.log('\n=== PROBE: does a creature elsewhere block victory? ===')
  const control = await trialWithDecoy(false)
  await sleep(1500)
  const withDecoy = await trialWithDecoy(true)

  console.log('\n=== VERDICT ===')
  console.log(`  no decoy   -> rank ${control} (${control === 1 ? 'won' : 'did not win'})`)
  console.log(`  with decoy -> rank ${withDecoy} (${withDecoy === 1 ? 'won' : 'did not win'})`)
  if (control !== 1) {
    console.log('  INVALID: the control run did not win, so the experiment proves nothing.')
    console.log('  (Most likely the subject was refused entry - see issue #26, health is not reset.)')
    process.exit(2)
  }
  if (withDecoy === 1) {
    console.log('  PASS: the decoy no longer blocks victory. Issue #20 looks fixed.')
    process.exit(0)
  }
  console.log('  FAIL: an unrelated creature in the nightmare blocks victory and traps the player.')
  console.log('  Issue #20 is still open. Victory must be scoped to the player\'s own creature.')
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
