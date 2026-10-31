package lt.markmerkk.mp3cutter

import TestUtils
import lt.markmerkk.mp3cutter.entities.TrackSegment
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration

/**
 * Runs the real ffmpeg (resolved via [FfmpegResolver]), fails on machines without it
 */
internal class SilenceDetectorIntegrationTest {

    private val detector = SilenceDetector()

    /**
     * 'sample1.mp3' is 45.74s long, ffmpeg finds a single silence at the end (44.27..45.71)
     * Expected a single track 0..44, the leftover after silence (46..46) is dropped
     */
    @Test
    fun silenceAtTheEnd() {
        // Assemble
        val inputFile = TestUtils.fileSource("/sample1.mp3")

        // Act
        val result = detector.detect(inputFile)

        // Assert
        Assertions.assertThat(result).containsExactly(
            segment(startSeconds = 0, endSeconds = 44),
        )
    }

    /**
     * 'Garden-ambience-sound-effect.mp3' is 95.61s long, constant background noise without silence
     * Expected a single track over the whole input 0..96
     */
    @Test
    fun noSilence() {
        // Assemble
        val inputFile = TestUtils.fileSource("/Garden-ambience-sound-effect.mp3")

        // Act
        val result = detector.detect(inputFile)

        // Assert
        Assertions.assertThat(result).containsExactly(
            segment(startSeconds = 0, endSeconds = 96),
        )
    }

    private fun segment(startSeconds: Long, endSeconds: Long): TrackSegment {
        return TrackSegment(
            start = Duration.ofSeconds(startSeconds),
            end = Duration.ofSeconds(endSeconds),
        )
    }
}
