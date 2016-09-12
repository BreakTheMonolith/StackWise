package guru.monolith.stackwise.core

import org.apache.commons.cli.Options
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset
import org.apache.commons.io.IOUtils

object StackWiseApplication {
  val commandLineOptions: Options = new Options
  commandLineOptions.addOption("f", true, "input dump file (stdin works as well)")
  commandLineOptions.addOption("o", true, "output file (stdout is the default")
  commandLineOptions.addOption("b", false, "blocked and blocking threads")

  def main(args: Array[String]) {
    val commandLine: CommandLine = new DefaultParser().parse(commandLineOptions, args)

    var dumpInput: String = null
    if (commandLine.hasOption("f")) {
      dumpInput = FileUtils.readFileToString(new File(commandLine.getOptionValue("f")), Charset.defaultCharset())
    } else if (System.in.available() > 0) {
      dumpInput = IOUtils.toString(System.in, Charset.defaultCharset())
    } else {
      val formatter: HelpFormatter = new HelpFormatter
      formatter.printHelp("StackWise", commandLineOptions)

      return
    }
  }
}