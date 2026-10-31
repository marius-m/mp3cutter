package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.Gpu
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

internal class JobSpecMp4GpuCreateTest {

    private val ffProbeResult = FFmpegProbeResult().apply {
        format = FFmpegFormat().apply { filename = "/input/input.mp4" }
    }

    private val spec = JobSpec(
        targetOutput = File("/output/Artist - Track.mp4"),
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

    /**
     * Track 0:01:00 - 0:03:30 (2s fades), encoded on [Gpu.AMD]
     * Expected '-vaapi_device /dev/dri/renderD128' before input '-i',
     * video filter same as mp4 with 'format=nv12,hwupload' appended,
     * video encoded with 'h264_vaapi', audio fade and cut range same as mp4
     */
    @Test
    fun amd() {
        // Assemble
        val jobBuilder = JobSpecMp4Gpu(gpu = Gpu.AMD)

        // Act
        val result = jobBuilder.create(ffProbeResult, spec).build()

        // Assert
        assertThat(result).containsSubsequence("-vaapi_device", "/dev/dri/renderD128", "-i", "/input/input.mp4")
        assertThat(result).containsSequence("-af", "afade=t=in:st=60:d=2,afade=t=out:st=208:d=2")
        assertThat(result).containsSequence(
            "-vf",
            "drawtext=text='Artist - Track':x=10:y=10:fontcolor=white:fontsize=24,fade=t=in:st=60:d=2,fade=t=out:st=208:d=2,format=nv12,hwupload",
        )
        assertThat(result).containsSequence("-vcodec", "h264_vaapi")
        assertThat(result).containsSequence("-f", "mp4")
        assertThat(result).containsSequence("-ss", "00:01:00")
        assertThat(result).containsSequence("-t", "00:02:30")
        assertThat(result).endsWith("/output/Artist - Track.mp4")
    }

    /**
     * Track 0:01:00 - 0:03:30 (2s fades), with [Gpu.NONE]
     * Expected same args as [JobSpecMp4]: no GPU device, no filter suffix, no forced codec
     */
    @Test
    fun none() {
        // Assemble
        val jobBuilder = JobSpecMp4Gpu(gpu = Gpu.NONE)

        // Act
        val result = jobBuilder.create(ffProbeResult, spec).build()
        val resultMp4 = JobSpecMp4().create(ffProbeResult, spec).build()

        // Assert
        assertThat(result).isEqualTo(resultMp4)
    }
}
