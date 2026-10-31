package lt.markmerkk.mp3cutter

import lt.markmerkk.mp3cutter.entities.TrackFilterEnd
import lt.markmerkk.mp3cutter.entities.TrackFilterStart
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime

internal class JobBuilderFilterFadeTest {

    private val filterStart = TrackFilterStart(
        startOffset = LocalTime.of(0, 1, 0),
        duration = Duration.ofSeconds(2),
    )
    private val filterEnd = TrackFilterEnd(
        startOffset = LocalTime.of(0, 3, 25),
        duration = Duration.ofSeconds(5),
    )

    /**
     * Fade in 2s at 60s, fade out 5s at 205s
     * Expected each fade to use its own duration: in 'd=2', out 'd=5'
     */
    @Test
    fun audio() {
        // Act
        val result = JobBuilder.audioFilterFade(filterStart, filterEnd)

        // Assert
        assertThat(result).isEqualTo("afade=t=in:st=60:d=2,afade=t=out:st=205:d=5")
    }

    /**
     * Fade in 2s at 60s, fade out 5s at 205s
     * Expected track name drawn, each fade to use its own duration: in 'd=2', out 'd=5'
     */
    @Test
    fun video() {
        // Act
        val result = JobBuilder.videoFilterOverlay("Artist - Track", filterStart, filterEnd)

        // Assert
        assertThat(result).isEqualTo(
            "drawtext=text='Artist - Track':x=10:y=10:fontcolor=white:fontsize=24,fade=t=in:st=60:d=2,fade=t=out:st=205:d=5"
        )
    }
}
