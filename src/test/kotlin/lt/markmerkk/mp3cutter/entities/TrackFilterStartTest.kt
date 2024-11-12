package lt.markmerkk.mp3cutter.entities

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime

internal class TrackFilterStartTest {

    @Test
    fun valid() {
        // Assemble
        val trackItem = TrackItemRegular(
            index = 0,
            startOffset = LocalTime.of(0, 10, 0),
            duration = Duration.ofSeconds(60),
            artist = "Moo",
            track = "Research1",
        )

        // Act
        val result = TrackFilterStart.fromTrack(trackItem)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackFilterStart(
                startOffset = LocalTime.of(0, 10, 0),
                duration = TrackFilter.defaultDuration,
            )
        )
    }

    @Test
    fun shortDuration_lessThanDefault() {
        // Assemble
        val trackItem = TrackItemRegular(
            index = 0,
            startOffset = LocalTime.of(0, 10, 0),
            duration = Duration.ofSeconds(1),
            artist = "Moo",
            track = "Research1",
        )

        // Act
        val result = TrackFilterStart.fromTrack(trackItem)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackFilterStart.asEmpty()
        )
    }

    @Test
    fun exactAsDuration() {
        // Assemble
        val trackItem = TrackItemRegular(
            index = 0,
            startOffset = LocalTime.of(0, 10, 0),
            duration = TrackFilter.defaultDuration,
            artist = "Moo",
            track = "Research1",
        )

        // Act
        val result = TrackFilterStart.fromTrack(trackItem)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackFilterStart.asEmpty()
        )
    }

    @Test
    fun exactAs2xDuration() {
        // Assemble
        val trackItem = TrackItemRegular(
            index = 0,
            startOffset = LocalTime.of(0, 10, 0),
            duration = Duration.ofSeconds(TrackFilter.DEFAULT_FILTER_SEC.toLong() * 2),
            artist = "Moo",
            track = "Research1",
        )

        // Act
        val result = TrackFilterStart.fromTrack(trackItem)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackFilterStart(
                startOffset = LocalTime.of(0, 10, 0),
                duration = TrackFilter.defaultDuration,
            )
        )
    }

    @Test
    fun shortDuration_noDuration() {
        // Assemble
        val trackItem = TrackItemRegular(
            index = 0,
            startOffset = LocalTime.of(0, 10, 0),
            duration = Duration.ofSeconds(0),
            artist = "Moo",
            track = "Research1",
        )

        // Act
        val result = TrackFilterStart.fromTrack(trackItem)

        // Assert
        Assertions.assertThat(result).isEqualTo(
            TrackFilterStart.asEmpty()
        )
    }
}