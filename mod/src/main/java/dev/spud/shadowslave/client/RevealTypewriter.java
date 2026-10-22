package dev.spud.shadowslave.client;

import java.util.List;
import java.util.Objects;

/**
 * Timing for the Spell reveal: rows type out one after another at a steady
 * pace, with a pause after each row. Pure arithmetic over elapsed time so the
 * screen stays a thin renderer.
 */
public final class RevealTypewriter {
    private final int[] lengths;
    private final long[] starts;
    private final long msPerChar;
    private final long endMs;

    /**
     * @param rowLengths characters in each row
     * @param pausesAfterMs pause after each row finishes, one per row
     */
    public RevealTypewriter(List<Integer> rowLengths, List<Integer> pausesAfterMs, long msPerChar) {
        Objects.requireNonNull(rowLengths, "rowLengths");
        Objects.requireNonNull(pausesAfterMs, "pausesAfterMs");
        if (rowLengths.size() != pausesAfterMs.size()) {
            throw new IllegalArgumentException("Need one pause per row");
        }
        if (msPerChar <= 0) {
            throw new IllegalArgumentException("msPerChar must be positive");
        }
        this.msPerChar = msPerChar;
        this.lengths = new int[rowLengths.size()];
        this.starts = new long[rowLengths.size()];
        long cursor = 0;
        for (int i = 0; i < lengths.length; i++) {
            lengths[i] = Math.max(0, rowLengths.get(i));
            starts[i] = cursor;
            cursor += lengths[i] * msPerChar + Math.max(0, pausesAfterMs.get(i));
        }
        this.endMs = lengths.length == 0 ? 0 : starts[lengths.length - 1] + lengths[lengths.length - 1] * msPerChar;
    }

    public int rows() {
        return lengths.length;
    }

    /** Characters of {@code row} visible after {@code elapsedMs}. */
    public int visible(int row, long elapsedMs) {
        long typed = (elapsedMs - starts[row]) / msPerChar;
        return (int) Math.max(0, Math.min(lengths[row], typed));
    }

    /** Total characters visible across every row; used to pace the typing sound. */
    public int totalVisible(long elapsedMs) {
        int total = 0;
        for (int row = 0; row < lengths.length; row++) {
            total += visible(row, elapsedMs);
        }
        return total;
    }

    /** The row currently being typed, or -1 between rows and once finished. */
    public int typingRow(long elapsedMs) {
        for (int row = 0; row < lengths.length; row++) {
            int shown = visible(row, elapsedMs);
            if (elapsedMs >= starts[row] && shown < lengths[row]) {
                return row;
            }
        }
        return -1;
    }

    public boolean finished(long elapsedMs) {
        return elapsedMs >= endMs;
    }

    /** Elapsed time at which every row is fully shown. */
    public long endMs() {
        return endMs;
    }
}
