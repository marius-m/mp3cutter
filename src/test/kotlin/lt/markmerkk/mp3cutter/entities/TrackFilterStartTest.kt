package lt.markmerkk.mp3cutter.entities

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime

internal class TrackFilterStartTest {

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
        val result = TrackFilterStart.fromTrack(trackItem, filterDuration)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackFilterStart(
                startOffset = LocalTime.of(0, 10, 0),
                duration = filterDuration,
            )
        )
    }

    @Test
    fun shortDuration_lessThanDefault() {
        // Assemble
        val trackItem = TrackItemRegular(
            startOffset = LocalTime.of(0, 10, 0),
            duration = Duration.ofSeconds(1),
            artist = "Moo",
            track = "Research1",
        )

        // Act
        val result = TrackFilterStart.fromTrack(trackItem, filterDuration)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackFilterStart.asEmpty()
        )
    }

    @Test
    fun shortDuration_noDuration() {
        // Assemble
        val trackItem = TrackItemRegular(
            startOffset = LocalTime.of(0, 10, 0),
            duration = Duration.ofSeconds(0),
            artist = "Moo",
            track = "Research1",
        )

        // Act
        val result = TrackFilterStart.fromTrack(trackItem, filterDuration)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackFilterStart.asEmpty()
        )
    }
}