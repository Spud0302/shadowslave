import mineflayer from 'mineflayer'

export const HARNESS_CHECK_TIMEOUT_INTERVAL_MS = 90_000
export const HARNESS_MAX_OBSERVATION_MS = 60_000

export function withHarnessTransportBudget(options = {}) {
  const requested = options.checkTimeoutInterval ?? 0
  return { ...options, checkTimeoutInterval: Math.max(requested, HARNESS_CHECK_TIMEOUT_INTERVAL_MS) }
}

const marker = Symbol.for('shadowslave.harnessTransportWrapped')
if (!mineflayer[marker]) {
  const createBot = mineflayer.createBot.bind(mineflayer)
  mineflayer.createBot = (options) => createBot(withHarnessTransportBudget(options))
  Object.defineProperty(mineflayer, marker, { value: true })
}
