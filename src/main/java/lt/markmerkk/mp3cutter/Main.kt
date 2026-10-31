package lt.markmerkk.mp3cutter

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import org.slf4j.bridge.SLF4JBridgeHandler
import java.util.logging.LogManager

fun main(args: Array<String>) {
    LogManager.getLogManager().reset()
    SLF4JBridgeHandler.install()

    Main().main(args)
}

class Main {
    @OptIn(ExperimentalCli::class)
    fun main(args: Array<String>) {
        val parser = ArgParser("mp3cutter")
        parser.subcommands(
            CutCommand(),
            InspectSilenceCommand(),
        )
        parser.parse(args)
    }
}
