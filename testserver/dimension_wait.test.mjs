import assert from 'node:assert/strict'
import test from 'node:test'

import { waitForDimensionObservation } from './dimension_wait.mjs'

test('retries a dropped dimension reply and accepts the next server observation', async () => {
  let attempts = 0
  let clock = 0

  const observed = await waitForDimensionObservation(
    async () => {
      attempts += 1
      if (attempts === 1) {
        clock += 4000
        throw new Error('Timed out waiting for dimension; saw <no reply>')
      }
      return 'shadowslave:nightmare'
    },
    (dimension) => dimension === 'shadowslave:nightmare',
    {
      timeoutMs: 6000,
      retryDelayMs: 100,
      now: () => clock,
      sleep: async (ms) => {
        clock += ms
      }
    }
  )

  assert.equal(observed, 'shadowslave:nightmare')
  assert.equal(attempts, 2)
})

test('remains fail-closed when every dimension reply is unreadable', async () => {
  let attempts = 0
  let clock = 0

  await assert.rejects(
    waitForDimensionObservation(
      async () => {
        attempts += 1
        throw new Error('Could not parse dimension from: malformed payload')
      },
      () => true,
      {
        timeoutMs: 300,
        retryDelayMs: 100,
        now: () => clock,
        sleep: async (ms) => {
          clock += ms
        }
      }
    ),
    /last dimension=null; last read error=Could not parse dimension from: malformed payload/
  )

  assert.equal(attempts, 3)
})

test('keeps polling after a valid but non-matching dimension', async () => {
  const replies = ['minecraft:overworld', 'shadowslave:nightmare']
  let clock = 0

  const observed = await waitForDimensionObservation(
    async () => replies.shift(),
    (dimension) => dimension === 'shadowslave:nightmare',
    {
      timeoutMs: 1000,
      retryDelayMs: 100,
      now: () => clock,
      sleep: async (ms) => {
        clock += ms
      }
    }
  )

  assert.equal(observed, 'shadowslave:nightmare')
  assert.deepEqual(replies, [])
})
