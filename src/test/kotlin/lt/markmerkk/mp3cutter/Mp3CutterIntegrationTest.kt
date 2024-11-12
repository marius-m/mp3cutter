package lt.markmerkk.mp3cutter

import TestUtils
import lt.markmerkk.mp3cutter.entities.ExportFormat
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
}