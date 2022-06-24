import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class Mp3CutterIntegrationTest {

    private val nameParser = NameParser()

    @Test
    fun valid() {
        // Assemble
        val tracksRawAsString = TestUtils.loadContent("/sample_input_test1.txt")
        val tracks = nameParser.parse(rawText = tracksRawAsString)

        val inputFile = TestUtils.fileSource("/Garden-ambience-sound-effect.mp3")
        val outputDir = File("build/")
        val mp3Cutter = Mp3Cutter(
            inputFile = inputFile,
            outputDir = outputDir,
        )

        // Act
        val result = mp3Cutter.cut(tracks = tracks)

        // Assert
        // Check results in 'build'
    }
}