package lt.markmerkk.mp3cutter

import TestUtils
import lt.markmerkk.mp3cutter.entities.ExportFormat
import lt.markmerkk.mp3cutter.entities.Gpu
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.Ignore

//@Ignore
internal class Mp3CutterIntegrationTest {

    private val exportFormat: ExportFormat = ExportFormat.asDefault()
    private val nameParser = NameParser()

    @Test
    fun valid() {
        // Assemble
        val tracksRawAsString = TestUtils.loadContent("/garden-ambience-tracks.txt")
        val tracks = nameParser.parse(rawText = tracksRawAsString)

        val inputFile = TestUtils.fileSource("/Garden-ambience-sound-effect.mp3")
        val outputDir = File("build/")
        val mp3Cutter = Mp3Cutter(
            exportFormat = exportFormat,
            inputFile = inputFile,
            outputDir = outputDir,
        )

        // Act
        val result = mp3Cutter.cut(tracks = tracks)

        // Assert
        // Check results in 'build'
    }

    @Test
    fun valid2() {
        // Assemble
        val tracksRawAsString = TestUtils.loadContent("/sample1-tracks.txt")
        val tracks = nameParser.parse(rawText = tracksRawAsString)

        val inputFile = TestUtils.fileSource("/sample1.mp3")
        val outputDir = File("build/")
        val mp3Cutter = Mp3Cutter(
            exportFormat = exportFormat,
            inputFile = inputFile,
            outputDir = outputDir,
        )

        // Act
        val result = mp3Cutter.cut(tracks = tracks)

        // Assert
        // Check results in 'build'
    }

    @Test
    fun valid3_video() {
        // Assemble
        val tracksRawAsString = TestUtils.loadContent("/big-buck-bunny-tracks.txt")
        val tracks = nameParser.parse(rawText = tracksRawAsString)

        val inputFile = TestUtils.fileSource("/big-buck-bunny.mp4")
        val outputDir = File("build/")
        val mp3Cutter = Mp3Cutter(
            exportFormat = ExportFormat.MP4,
            inputFile = inputFile,
            outputDir = outputDir,
        )

        // Act
        val result = mp3Cutter.cut(tracks = tracks)

        // Assert
        // Check results in 'build'
    }

    /**
     * Same as [valid3_video], but video is encoded on AMD GPU (VAAPI)
     * Expected every cut track to have its video stream encoded as 'h264' (by 'h264_vaapi')
     * Skipped on machines without a GPU render device
     */
    @Test
    fun valid4_video_gpuAmd() {
        // Assemble
        Assumptions.assumeTrue(File("/dev/dri/renderD128").exists(), "No GPU render device")
        val tracksRawAsString = TestUtils.loadContent("/big-buck-bunny-tracks.txt")
        val tracks = nameParser.parse(rawText = tracksRawAsString)

        val inputFile = TestUtils.fileSource("/big-buck-bunny.mp4")
        val outputDir = File("build/gpu-amd/").apply { mkdirs() }
        val mp3Cutter = Mp3Cutter(
            exportFormat = ExportFormat.MP4,
            inputFile = inputFile,
            outputDir = outputDir,
            gpu = Gpu.AMD,
        )

        // Act
        mp3Cutter.cut(tracks = tracks)

        // Assert
        val ffprobe = FFprobe(FfmpegResolver().resolve(null).ffprobe.absolutePath)
        val videoCodecs = tracks.map { track ->
            ffprobe.probe(File(outputDir, "${track.name}.mp4").absolutePath)
                .streams
                .first { it.codec_type == FFmpegStream.CodecType.VIDEO }
                .codec_name
        }
        assertThat(videoCodecs).containsExactly("h264", "h264", "h264")
    }
}