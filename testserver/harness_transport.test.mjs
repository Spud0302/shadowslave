import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

import {
  HARNESS_CHECK_TIMEOUT_INTERVAL_MS,
  HARNESS_MAX_OBSERVATION_MS,
  withHarnessTransportBudget
} from './harness_transport.mjs'

const HERE = dirname(fileURLToPath(import.meta.url))
const source = (name) => readFileSync(join(HERE, name), 'utf8')

test('transport watchdog exceeds the longest harness observation budget', () => {
  assert.equal(HARNESS_MAX_OBSERVATION_MS, 60_000)
  assert.equal(HARNESS_CHECK_TIMEOUT_INTERVAL_MS, 90_000)
  assert.ok(HARNESS_CHECK_TIMEOUT_INTERVAL_MS > HARNESS_MAX_OBSERVATION_MS)
})

test('transport budget raises shorter watchdogs without shortening longer explicit ones', () => {
  assert.equal(withHarnessTransportBudget({}).checkTimeoutInterval, 90_000)
  assert.equal(withHarnessTransportBudget({ checkTimeoutInterval: 30_000 }).checkTimeoutInterval, 90_000)
  assert.equal(withHarnessTransportBudget({ checkTimeoutInterval: 120_000 }).checkTimeoutInterval, 120_000)
})

test('transport wrapper preserves unrelated Mineflayer options', () => {
  const options = withHarnessTransportBudget({ host: 'localhost', username: 'tester', auth: 'offline' })
  assert.equal(options.host, 'localhost')
  assert.equal(options.username, 'tester')
  assert.equal(options.auth, 'offline')
})

test('direct lifecycle and concurrency entrypoints load the transport budget transitively', () => {
  assert.match(source('harness.mjs'), /from '\.\/dimension_wait\.mjs'/)
  assert.match(source('regression_issue20.mjs'), /from '\.\/dimension_wait\.mjs'/)
  assert.match(source('dimension_wait.mjs'), /import '\.\/harness_transport\.mjs'/)
})

test('direct deploy entrypoint loads the transport budget explicitly', () => {
  assert.match(source('deploy.mjs'), /import '\.\/harness_transport\.mjs'/)
})
