import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import java.io.File

fun main(args: Array<String>) {
    val parser = ArgParser("mp3cut")
    val inputFileName by parser.option(
        ArgType.String,
        shortName = "i",
        description = "Input mp3 file to cut",
    ).required()
    parser.parse(args)

    val inputFile = File(inputFileName)
    if (!inputFile.exists()) {
        throw IllegalArgumentException("Invalid input file (%s)".format(inputFile.absolutePath))
    }
}