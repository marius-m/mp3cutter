import org.apache.commons.io.IOUtils
import java.io.StringWriter

object TestUtils {
    fun loadContent(testSource: String): String {
        val stringWriter = StringWriter()
        IOUtils.copy(this.javaClass.getResourceAsStream(testSource), stringWriter, Charsets.UTF_8)
        return stringWriter.toString()
    }
}