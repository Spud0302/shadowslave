// Hosted vanilla worlds can stall the server thread while generating/loading the Nightmare dimension.
// The lifecycle harness deliberately allows up to 60 seconds for an authoritative dimension transition,
// but minecraft-protocol's default keepalive watchdog is only 30 seconds. That lets transport terminate
// an otherwise-observable test before the harness's own fail-closed assertion budget has expired.
//
// Keep this strictly test-side. Gameplay/datapack timing and assertion deadlines remain unchanged.

import mineflayer from 'mineflayer'

export const HARNESS_CHECK_TIMEOUT_INTERVAL_MS = 90_000
export const HARNESS_MAX_OBSERVATION_MS = 60_000

export function withHarnessTransportBudget(options = {}) {
  const requested = options.checkTimeoutInterval ?? 0
  return {
    ...options,
    checkTimeoutInterval: Math.max(requested, HARNESS_CHECK_TIMEOUT_INTERVAL_MS)
  }
}

// Multiple harness entry points can reach this module through shared imports. Patch once so direct
// `node <harness>.mjs` invocations and npm-script invocations have the same transport contract.
const marker = Symbol.for('shadowslave.harnessTransportWrapped')
if (!mineflayer[marker]) {
  const createBot = mineflayer.createBot.bind(mineflayer)
  mineflayer.createBot = (options) => createBot(withHarnessTransportBudget(options))
  Object.defineProperty(mineflayer, marker, { value: true })
}
