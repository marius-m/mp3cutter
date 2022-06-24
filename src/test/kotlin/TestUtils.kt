import org.apache.commons.io.IOUtils
import java.io.File
import java.io.StringWriter

/*
 * Reading from resource:
 * Source: https://www.baeldung.com/junit-src-test-resources-directory-path
 */
object TestUtils {
    fun loadContent(resourcePath: String): String {
        val stringWriter = StringWriter()
        IOUtils.copy(this.javaClass.getResourceAsStream(resourcePath), stringWriter, Charsets.UTF_8)
        return stringWriter.toString()
    }

    fun fileSource(resourcePath: String): File {
        return File(this.javaClass.getResource(resourcePath).file)
            .apply {
                if (!exists()) {
                    throw java.lang.IllegalArgumentException("No file @ '$resourcePath' / ${this.absolutePath}")
                }
            }
    }

    fun tmpFile(tmpFileName: String, ext: String): File {
        return File.createTempFile(tmpFileName, ext)
            .apply {
                println("Tmp file in ${this.absolutePath}")
            }
    }
}
