// Build the pack, install it into the test server's world, and /reload.
//
// Exists because the server loads world/datapacks/ss-trace.zip, NOT the working tree. Editing a
// .mcfunction and re-running `npm test` silently tests the previously deployed build — the harness
// connects, the pack loads, and every assertion reports on code you did not write. That cost most of
// a session once; see docs/ENGINEERING-NOTES.md "The server runs a built zip, not your working tree".
//
// Usage: node deploy.mjs   (or `npm run deploy`), then `npm test`.

import { execFileSync } from 'node:child_process'
import { copyFileSync, readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import mineflayer from 'mineflayer'

// Absolute paths throughout: build_release.py lives in shadowslave/tools/ but writes to the repo
// root, and the pack directory shares its name with the repo directory. Relative paths through those
// two facts are their own source of wasted commands.
const TESTSERVER = dirname(fileURLToPath(import.meta.url))
const REPO = dirname(TESTSERVER)
const PACK = join(REPO, 'shadowslave')
const TARGET = join(TESTSERVER, 'world', 'datapacks', 'ss-trace.zip')

function packVersion() {
  const meta = JSON.parse(readFileSync(join(PACK, 'pack.mcmeta'), 'utf8'))
  const match = /v(\d+\.\d+\.\d+)/.exec(meta?.pack?.description ?? '')
  if (!match) throw new Error('pack.mcmeta description carries no vX.Y.Z version')
  return match[1]
}

// The validator runs inside build_release.py, so a structurally broken pack never reaches the server.
// Report its output plainly on failure: the useful line ("FAIL: version mismatch: ...") arrives on the
// child's stdout, and letting execFileSync throw buries it inside a JS exception dump.
// stdio 'pipe' captures stderr too; left inherited it prints once on its own and again below.
try {
  console.log(execFileSync('python3', [join(PACK, 'tools', 'build_release.py')],
    { encoding: 'utf8', stdio: 'pipe' }).trim())
} catch (error) {
  console.error('deploy: build failed, nothing was installed.')
  // The validator writes its verdict to stdout ("FAIL: version mismatch: ..."); the accompanying
  // Python traceback says nothing extra. Fall back to stderr only when there is no verdict to show.
  const verdict = (error.stdout ?? '').trim()
  console.error(verdict || (error.stderr ?? '').trim() || error.message)
  process.exit(1)
}

const version = packVersion()
copyFileSync(join(REPO, `shadowslave-v${version}.zip`), TARGET)
console.log(`Installed v${version} -> ${TARGET}`)

const bot = mineflayer.createBot({
  host: 'localhost', port: 25565, username: 'tester', auth: 'offline', version: '1.21.1'
})

const log = []
bot.on('message', (message) => log.push(message.toString()))
bot.on('error', (error) => { console.error('deploy: bot error:', error.message); process.exit(1) })
bot.on('kicked', (reason) => { console.error('deploy: kicked:', reason); process.exit(1) })

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

bot.once('spawn', async () => {
  await sleep(500)
  log.length = 0
  bot.chat('/reload')
  await sleep(3000)

  const replies = log.join('\n')
  bot.quit()

  // init.mcfunction announces the running version on load. Confirming the announced version matches
  // what we just installed is the whole point: it proves the server is running THIS build. A silent
  // reload that left the old pack in place is the exact failure this script exists to prevent.
  //
  // ponytail: this branch is reasoned, not exercised. The copy above is unconditional, so producing a
  // ponytail: real mismatch needs a server that ignores the installed pack, which is not cheap to fake.
  // ponytail: the build-failure path above HAS been seen to fail correctly. Per ENGINEERING-NOTES
  // ponytail: ("a check you have never seen fail is not a check"), treat this one as unproven until a
  // ponytail: genuine stale-reload happens — at which point confirm it fires rather than assuming.
  if (!replies.includes(`v${version}`)) {
    console.error(`deploy: reloaded, but the server did not announce v${version}.`)
    console.error(replies || '<no reply>')
    process.exit(1)
  }

  console.log(`Reloaded; server announced v${version}. Safe to run npm test.`)
  process.exit(0)
})
