package lt.markmerkk.mp3cutter.entities

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime

internal class TrackFilterEndTest {

    private val filterDuration = Duration.ofSeconds(5)

    @Test
    fun valid() {
        // Assemble
        val trackItem = TrackItemRegular(
            startOffset = LocalTime.of(0, 10, 0),
            duration = Duration.ofSeconds(60),
            artist = "Moo",
            track = "Research1",
        )

        // Act
        val result = TrackFilterEnd.fromTrack(trackItem, filterDuration)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackFilterEnd(
                startOffset = LocalTime.of(0, 10, 55),
                duration = filterDuration,
            )
        )
    }

    @Test
    fun valid_startFrom0() {
        // Assemble
        val trackItem = TrackItemRegular(
            startOffset = LocalTime.of(0, 0, 1),
            duration = Duration.ofSeconds(9),
            artist = "Moo",
            track = "Research1",
        )

        // Act
        val result = TrackFilterEnd.fromTrack(trackItem, filterDuration)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackFilterEnd(
                startOffset = LocalTime.of(0, 0, 5),
                duration = filterDuration,
            )
        )
    }

    @Test
    fun shortDuration_shorterThanDefault() {
        // Assemble
        val trackItem = TrackItemRegular(
            startOffset = LocalTime.of(0, 0, 0),
            duration = Duration.ofSeconds(2),
            artist = "Moo",
            track = "Research1",
        )

        // Act
        val result = TrackFilterEnd.fromTrack(trackItem, filterDuration)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackFilterEnd(
                startOffset = LocalTime.MIN,
                duration = Duration.ZERO,
            )
        )
    }
}