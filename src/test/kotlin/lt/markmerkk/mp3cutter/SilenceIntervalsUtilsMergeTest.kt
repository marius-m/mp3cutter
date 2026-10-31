package lt.markmerkk.mp3cutter

import TestUtils
import lt.markmerkk.mp3cutter.entities.SilenceInterval
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration

class SilenceIntervalsUtilsMergeTest {

    /**
     * No silences detected -> nothing to merge, result is empty
     */
    @Test
    fun empty() {
        // Assemble
        val intervals = emptyList<SilenceInterval>()

        // Act
        val result = SilenceIntervalsUtils.merge(intervals)

        // Assert
        Assertions.assertThat(result).isEmpty()
    }

    /**
     * Silences are seconds apart from each other (far more than the 500ms max gap)
     * Input is shuffled, so all 4 silences are expected to be kept as is,
     * only reordered by their start time
     */
    @Test
    fun farApart() {
        // Assemble
        val intervals = listOf(
            silence(startMillis = 10000, endMillis = 12000),
            silence(startMillis = 1000, endMillis = 2000),
            silence(startMillis = 30000, endMillis = 31000),
            silence(startMillis = 20000, endMillis = 21000),
        )

        // Act
        val result = SilenceIntervalsUtils.merge(intervals)

        // Assert
        Assertions.assertThat(result).containsExactly(
            silence(startMillis = 1000, endMillis = 2000),
            silence(startMillis = 10000, endMillis = 12000),
            silence(startMillis = 20000, endMillis = 21000),
            silence(startMillis = 30000, endMillis = 31000),
        )
    }

    /**
     * Two pairs of silences, where one ends exactly when the other starts
     * (as ffmpeg reports at 0.532s in the sample log)
     * Each pair is expected to become a single silence spanning both:
     * - 0.007..0.532 + 0.532..1.868 -> 0.007..1.868
     * - 5..6 + 6..7 -> 5..7
     * Pairs are not next to each other in the input, so merging only works after sorting
     */
    @Test
    fun touching() {
        // Assemble
        val intervals = listOf(
            silence(startMillis = 532, endMillis = 1868),
            silence(startMillis = 6000, endMillis = 7000),
            silence(startMillis = 7, endMillis = 532),
            silence(startMillis = 5000, endMillis = 6000),
        )

        // Act
        val result = SilenceIntervalsUtils.merge(intervals)

        // Assert
        Assertions.assertThat(result).containsExactly(
            silence(startMillis = 7, endMillis = 1868),
            silence(startMillis = 5000, endMillis = 7000),
        )
    }

    /**
     * Two pairs of silences with a short sound between them, shorter than the 500ms max gap
     * Short sound (a click, a breath) is not a track, so each pair is expected to merge:
     * - 1..2 + 2.4..3 (400ms gap) -> 1..3
     * - 9..9.5 + 9.9..10 (400ms gap) -> 9..10
     */
    @Test
    fun withinGap() {
        // Assemble
        val intervals = listOf(
            silence(startMillis = 2400, endMillis = 3000),
            silence(startMillis = 9900, endMillis = 10000),
            silence(startMillis = 1000, endMillis = 2000),
            silence(startMillis = 9000, endMillis = 9500),
        )

        // Act
        val result = SilenceIntervalsUtils.merge(intervals)

        // Assert
        Assertions.assertThat(result).containsExactly(
            silence(startMillis = 1000, endMillis = 3000),
            silence(startMillis = 9000, endMillis = 10000),
        )
    }

    /**
     * Same layout as [withinGap], but gaps are 1ms over the 500ms max gap
     * - 2s..2.501s (501ms gap)
     * - 9.5s..10.001s (501ms gap)
     * All 4 silences are expected to stay separate, only reordered by their start time
     */
    @Test
    fun outsideGap() {
        // Assemble
        val intervals = listOf(
            silence(startMillis = 2501, endMillis = 3000),
            silence(startMillis = 9000, endMillis = 9500),
            silence(startMillis = 1000, endMillis = 2000),
            silence(startMillis = 10001, endMillis = 11000),
        )

        // Act
        val result = SilenceIntervalsUtils.merge(intervals)

        // Assert
        Assertions.assertThat(result).containsExactly(
            silence(startMillis = 1000, endMillis = 2000),
            silence(startMillis = 2501, endMillis = 3000),
            silence(startMillis = 9000, endMillis = 9500),
            silence(startMillis = 10001, endMillis = 11000),
        )
    }

    /**
     * Two pairs of overlapping silences
     * - 1..5 fully contains 2..3 -> 1..5 (end must not shrink to the inner silence end)
     * - 8..9 partially overlaps 8.5..12 -> 8..12 (end extends to the later silence end)
     */
    @Test
    fun overlapping() {
        // Assemble
        val intervals = listOf(
            silence(startMillis = 8500, endMillis = 12000),
            silence(startMillis = 2000, endMillis = 3000),
            silence(startMillis = 8000, endMillis = 9000),
            silence(startMillis = 1000, endMillis = 5000),
        )

        // Act
        val result = SilenceIntervalsUtils.merge(intervals)

        // Assert
        Assertions.assertThat(result).containsExactly(
            silence(startMillis = 1000, endMillis = 5000),
            silence(startMillis = 8000, endMillis = 12000),
        )
    }

    /**
     * Real ffmpeg output from 'dark-jazz2.webm' (1:37:15)
     * Parser produces 23 silences, 2 pairs of them touch each other:
     * - 0.007..0.532 + 0.532..1.868 -> 0.007..1.868
     * - 2916.1..2918.13 + 2918.13..2919.46 -> 2916.1..2919.46
     * So 21 silences are expected after merge
     */
    @Test
    fun fullLog() {
        // Assemble
        val totalDuration = Duration.ofMillis(5835250)
        val intervals = SilenceLogParser()
            .parse(TestUtils.loadContent("/dark-jazz2-silence.txt"), totalDuration)

        // Act
        val result = SilenceIntervalsUtils.merge(intervals)

        // Assert
        Assertions.assertThat(result).hasSize(21)
        Assertions.assertThat(result.first()).isEqualTo(silence(startMillis = 7, endMillis = 1868))
        Assertions.assertThat(result).contains(silence(startMillis = 2916100, endMillis = 2919460))
    }

    private fun silence(startMillis: Long, endMillis: Long): SilenceInterval {
        return SilenceInterval(
            start = Duration.ofMillis(startMillis),
            end = Duration.ofMillis(endMillis),
        )
    }
}
