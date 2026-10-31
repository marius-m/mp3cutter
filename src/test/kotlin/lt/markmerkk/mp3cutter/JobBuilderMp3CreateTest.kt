package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.JobSpec
import lt.markmerkk.mp3cutter.entities.TrackFilterEnd
import lt.markmerkk.mp3cutter.entities.TrackFilterStart
import net.bramp.ffmpeg.probe.FFmpegFormat
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Duration
import java.time.LocalTime

internal class JobBuilderMp3CreateTest {


    private val jobBuilder = JobBuilderMp3()

    private val ffProbeResult = FFmpegProbeResult().apply {
        format = FFmpegFormat().apply { filename = "/input/input.mp3" }
    }

    /**
     * Track 0:01:00 - 0:03:30 (2s fades)
     * Expected input from probe, audio fade in at 60s and out at 208s,
     * cut range '-ss 00:01:00 -t 00:02:30', mp3 format into target file
     * No video filter for audio only export
     */
    @Test
    fun valid() {
        // Assemble
        val spec = JobSpec(
            targetOutput = File("/output/Artist - Track.mp3"),
            offsetStart = Duration.ofMinutes(1),
            duration = Duration.ofSeconds(150),
            filterStart = TrackFilterStart(
                startOffset = LocalTime.of(0, 1, 0),
                duration = Duration.ofSeconds(2),
            ),
            filterEnd = TrackFilterEnd(
                startOffset = LocalTime.of(0, 3, 28),
                duration = Duration.ofSeconds(2),
            ),
            trackName = "Artist - Track",
        )

        // Act
        val result = jobBuilder.create(ffProbeResult, spec).build()

        // Assert
        assertThat(result).containsSequence("-i", "/input/input.mp3")
        assertThat(result).containsSequence("-af", "afade=t=in:st=60:d=2,afade=t=out:st=208:d=2")
        assertThat(result).containsSequence("-f", "mp3")
        assertThat(result).containsSequence("-ss", "00:01:00")
        assertThat(result).containsSequence("-t", "00:02:30")
        assertThat(result).doesNotContain("-vf")
        assertThat(result).endsWith("/output/Artist - Track.mp3")
    }
}
