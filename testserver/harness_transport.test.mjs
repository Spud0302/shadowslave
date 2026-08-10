import test from 'node:test'
import assert from 'node:assert/strict'

import {
  HARNESS_CHECK_TIMEOUT_INTERVAL_MS,
  HARNESS_MAX_OBSERVATION_MS,
  withHarnessTransportBudget
} from './harness_transport.mjs'

test('transport watchdog exceeds the longest harness observation budget', () => {
  assert.equal(HARNESS_MAX_OBSERVATION_MS, 60_000)
  assert.ok(HARNESS_CHECK_TIMEOUT_INTERVAL_MS > HARNESS_MAX_OBSERVATION_MS)
  assert.equal(withHarnessTransportBudget({}).checkTimeoutInterval, 90_000)
})

test('transport budget raises shorter client watchdogs without shortening longer explicit ones', () => {
  assert.equal(withHarnessTransportBudget({ checkTimeoutInterval: 30_000 }).checkTimeoutInterval, 90_000)
  assert.equal(withHarnessTransportBudget({ checkTimeoutInterval: 120_000 }).checkTimeoutInterval, 120_000)
})

test('transport wrapper preserves unrelated Mineflayer options', () => {
  const options = withHarnessTransportBudget({ host: 'localhost', username: 'tester', auth: 'offline' })
  assert.equal(options.host, 'localhost')
  assert.equal(options.username, 'tester')
  assert.equal(options.auth, 'offline')
})
