package guru.monolith.stackwise.core

import org.apache.commons.cli.Options
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset
import org.apache.commons.io.IOUtils
import java.io.OutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import org.apache.commons.lang3.SystemUtils

object StackWiseApplication {
  val commandLineOptions: Options = new Options

  def main(args: Array[String]) {
    
    commandLineOptions.addOption("f", true, "input dump file (stdin works as well)")
    commandLineOptions.addOption("o", true, "output file (stdout is the default)")
    commandLineOptions.addOption("p", true, "package qualifier (used to abbreviate verbose output)")
    commandLineOptions.addOption("h", false, "help")
    
    val commandLine: CommandLine = new DefaultParser().parse(commandLineOptions, args)
    
    //
    if (commandLine.hasOption("h")) {
      printHelp()
      return
    }
    
    // output spec
    var outStream : OutputStream = null
    if (commandLine.hasOption("o")) {
      val file = new File(commandLine.getOptionValue("o"))
      if (file.isDirectory() || !file.canWrite() )  {
        printHelp(String.format("input file must be writable and can't be a directory.  file=%s", commandLine.getOptionValue("o")))
        return
      }
      outStream = new FileOutputStream(file)
    }

    // Dump input spec
    var dumpInput: String = null
    if (commandLine.hasOption("f")) {
      dumpInput = FileUtils.readFileToString(new File(commandLine.getOptionValue("f")), Charset.defaultCharset())
    } else if (System.in.available() > 0) {
      dumpInput = IOUtils.toString(System.in, Charset.defaultCharset())
    } else {
      printHelp("No dump input provided")
      return
    }
    
    // package spec
    var packageQualifier = ""
    if (commandLine.hasOption("p")) {
      packageQualifier = commandLine.getOptionValue("p")
      outStream.write(String.format("Package Qualifier=%s  Classes from other packages may be omitted for brevity.%s%s", packageQualifier, SystemUtils.LINE_SEPARATOR, SystemUtils.LINE_SEPARATOR).getBytes)
    }
    
    val stackWise = new StackWise(dumpInput)
    stackWise.reportAll(outStream, packageQualifier)
  }

  def printHelp(message:String = null) = {
    if (message != null)  System.err.println(message)
    val formatter: HelpFormatter = new HelpFormatter
    formatter.printHelp("StackWise", commandLineOptions)
  }
}