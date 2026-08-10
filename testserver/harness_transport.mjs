// Hosted vanilla worlds can stall the server thread while generating/loading the Nightmare dimension.
// The lifecycle harness deliberately allows up to 60 seconds for an authoritative dimension transition,
// but minecraft-protocol's default keepalive watchdog is only 30 seconds. That lets the transport kill
// an otherwise-valid observation before the harness's own fail-closed timeout can decide the test.
//
// Keep this strictly test-side. It changes no datapack or gameplay timing; it only gives the local
// Mineflayer clients a transport budget longer than the longest authoritative observation window.

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

const createBot = mineflayer.createBot.bind(mineflayer)
mineflayer.createBot = (options) => createBot(withHarnessTransportBudget(options))
