package lt.markmerkk.mp3cutter

import TestUtils
import lt.markmerkk.mp3cutter.entities.SilenceInterval
import lt.markmerkk.mp3cutter.entities.TrackSegment
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration

class SilenceTracklistGeneratorGenerateTest {

    private val generator = SilenceTracklistGenerator()

    /**
     * No silences detected -> whole input is a single track 0..300s
     */
    @Test
    fun noSilence() {
        // Assemble
        val silences = emptyList<SilenceInterval>()

        // Act
        val result = generator.generate(silences, Duration.ofSeconds(300))

        // Assert
        Assertions.assertThat(result).containsExactly(
            segment(startSeconds = 0, endSeconds = 300),
        )
    }

    /**
     * 4 silences in shuffled order, input is 400s long
     * Tracks are expected between silences, with silences trimmed off:
     * - 0..0 before the first silence -> dropped, 0s long
     * - 1..120, 122..200, 201..300
     * - 303..400 after the last silence, closed by total duration
     */
    @Test
    fun betweenSilences() {
        // Assemble
        val silences = listOf(
            silence(startMillis = 120000, endMillis = 122000),
            silence(startMillis = 0, endMillis = 1000),
            silence(startMillis = 300000, endMillis = 303000),
            silence(startMillis = 200000, endMillis = 201000),
        )

        // Act
        val result = generator.generate(silences, Duration.ofSeconds(400))

        // Assert
        Assertions.assertThat(result).containsExactly(
            segment(startSeconds = 1, endSeconds = 120),
            segment(startSeconds = 122, endSeconds = 200),
            segment(startSeconds = 201, endSeconds = 300),
            segment(startSeconds = 303, endSeconds = 400),
        )
    }

    /**
     * 4 silences in shuffled order, input is 230s long
     * Segments shorter than 30s (default min track length) are not tracks, expected to be dropped:
     * - 0..50 (50s) -> kept
     * - 52..60 (8s), 61..70 (9s) -> dropped
     * - 72..200 (128s) -> kept
     * - 202..230 (28s) -> dropped
     */
    @Test
    fun shortSegmentsDropped() {
        // Assemble
        val silences = listOf(
            silence(startMillis = 70000, endMillis = 72000),
            silence(startMillis = 200000, endMillis = 202000),
            silence(startMillis = 50000, endMillis = 52000),
            silence(startMillis = 60000, endMillis = 61000),
        )

        // Act
        val result = generator.generate(silences, Duration.ofSeconds(230))

        // Assert
        Assertions.assertThat(result).containsExactly(
            segment(startSeconds = 0, endSeconds = 50),
            segment(startSeconds = 72, endSeconds = 200),
        )
    }

    /**
     * 4 silences in shuffled order with sub-second precision, input is 400s long
     * Times are expected to round to the nearest whole second (half rounds up):
     * - 1.499 -> 1, 99.5 -> 100
     * - 100.4 -> 100, 199.6 -> 200
     * - 200.5 -> 201, 299.499 -> 299
     * - 300.6 -> 301, 400 -> 400
     */
    @Test
    fun roundsToSeconds() {
        // Assemble
        val silences = listOf(
            silence(startMillis = 99500, endMillis = 100400),
            silence(startMillis = 0, endMillis = 1499),
            silence(startMillis = 299499, endMillis = 300600),
            silence(startMillis = 199600, endMillis = 200500),
        )

        // Act
        val result = generator.generate(silences, Duration.ofSeconds(400))

        // Assert
        Assertions.assertThat(result).containsExactly(
            segment(startSeconds = 1, endSeconds = 100),
            segment(startSeconds = 100, endSeconds = 200),
            segment(startSeconds = 201, endSeconds = 299),
            segment(startSeconds = 301, endSeconds = 400),
        )
    }

    /**
     * Real ffmpeg output from 'dark-jazz2.webm' (1:37:15.25), after merging 21 silences remain
     * Leftovers before the first silence (0..0.007) and after the last one (5835.24..5835.25)
     * are dropped, so 20 tracks are expected:
     * - first: 1.868..259.042 -> 0:00:02..0:04:19
     * - last: 5539.26..5833.69 -> 1:32:19..1:37:14
     */
    @Test
    fun fullLog() {
        // Assemble
        val totalDuration = Duration.ofMillis(5835250)
        val silences = SilenceIntervalsUtils.merge(
            SilenceLogParser().parse(TestUtils.loadContent("/dark-jazz2-silence.txt"), totalDuration)
        )

        // Act
        val result = generator.generate(silences, totalDuration)

        // Assert
        Assertions.assertThat(result).hasSize(20)
        Assertions.assertThat(result.first()).isEqualTo(segment(startSeconds = 2, endSeconds = 259))
        Assertions.assertThat(result.last()).isEqualTo(segment(startSeconds = 5539, endSeconds = 5834))
    }

    private fun silence(startMillis: Long, endMillis: Long): SilenceInterval {
        return SilenceInterval(
            start = Duration.ofMillis(startMillis),
            end = Duration.ofMillis(endMillis),
        )
    }

    private fun segment(startSeconds: Long, endSeconds: Long): TrackSegment {
        return TrackSegment(
            start = Duration.ofSeconds(startSeconds),
            end = Duration.ofSeconds(endSeconds),
        )
    }
}
