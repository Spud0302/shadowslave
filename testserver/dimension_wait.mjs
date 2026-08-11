import './harness_transport.mjs'

const defaultSleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

/**
 * Poll a server-authoritative dimension reader until the requested state is observed.
 *
 * A read can fail transiently while Minecraft is completing a cross-dimension respawn. Those
 * failures are retried inside one overall deadline; the deadline remains fail-closed and reports
 * the last valid dimension and read error for diagnosis.
 */
export async function waitForDimensionObservation(
  readDimension,
  predicate,
  {
    timeoutMs = 6000,
    retryDelayMs = 200,
    now = Date.now,
    sleep = defaultSleep
  } = {}
) {
  const deadline = now() + timeoutMs
  let lastDimension = null
  let lastReadError = null

  while (now() < deadline) {
    const remainingMs = Math.max(1, deadline - now())
    let readSucceeded = false
    try {
      lastDimension = await readDimension(remainingMs)
      readSucceeded = true
    } catch (error) {
      lastReadError = error
    }
    if (readSucceeded && predicate(lastDimension)) return lastDimension

    const remainingAfterRead = deadline - now()
    if (remainingAfterRead <= 0) break
    await sleep(Math.min(retryDelayMs, remainingAfterRead))
  }

  const errorDetail = lastReadError
    ? `; last read error=${lastReadError instanceof Error ? lastReadError.message : String(lastReadError)}`
    : ''
  throw new Error(
    `Timed out waiting for dimension transition; last dimension=${lastDimension}${errorDetail}`
  )
}
