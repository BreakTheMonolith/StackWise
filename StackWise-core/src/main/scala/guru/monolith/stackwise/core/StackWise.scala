package guru.monolith.stackwise.core

import java.io.OutputStream
import java.io.PrintStream

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.SystemUtils

class StackWise(dumpFile: String) {
  require(StringUtils.isNotEmpty(dumpFile), "Null or empty dumpFile not allowed")
  val stackList = DumpParser.parse(dumpFile)
  val idThreadMap = StackWiseUtils.mapThreadsById(stackList)
  val lockedOwnershipMap = StackWiseUtils.findLockOwnership(stackList)
  val blockingThreads = StackWiseUtils.findBlockingThreads(stackList)
  val blockedThreadsUnknownBlocker = StackWiseUtils.findBlockerUnknownThreads(stackList)
  
  def reportAll(outStream: OutputStream, packageQualifier: String = "") {
    reportBlockedThreads(outStream, packageQualifier)
    outStream.write(SystemUtils.LINE_SEPARATOR.getBytes)
    reportHotSpots(outStream, packageQualifier)
  }

  def reportBlockedThreads(outStream: OutputStream, packageQualifier: String = "") {
    val blockedThreads = StackWiseUtils.findThreads(stackList, Array(Thread.State.BLOCKED))

    val printStream = new PrintStream(outStream)
    if (blockedThreads.length == 0) {
      printStream.println("Thread dump reports no blocked threads.")
      return
    }
    
    if (blockingThreads.length > 0) {
      printStream.println("The following threads are blocking other threads from executing.")
      printStream.println()
      blockingThreads.foreach { stack => printStream.println(StackWiseUtils.formatStack(stack, packageQualifier)) }
    }
    
    if (blockedThreadsUnknownBlocker.size > 0) {
      printStream.println("The following threads are blocked, but blockers weren't reported.")
      printStream.println()
      blockedThreadsUnknownBlocker.foreach { stack => printStream.println(StackWiseUtils.formatStack(stack, packageQualifier)) }
    }

  }

  def reportHotSpots(outStream: OutputStream, packageQualifier: String = "") {
    val hotSpots = StackWiseUtils.findClassMethodUsage(stackList, packageQualifier)
    val hotSpotsSorted = hotSpots.sortBy { spot => spot.nbrMentions }.reverse
    val printStream = new PrintStream(outStream)
    
    printStream.println("Hot Spot Listing.")
    printStream.println()
    hotSpotsSorted.take(20).foreach { spot => printStream.println(StackWiseUtils.formatHotSpot(spot)) }
  }

}