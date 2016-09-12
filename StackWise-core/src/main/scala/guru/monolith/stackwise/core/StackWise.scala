package guru.monolith.stackwise.core

import java.io.OutputStream
import java.io.PrintStream

import org.apache.commons.lang3.StringUtils

class StackWise(dumpFile: String) {
  require(StringUtils.isNotEmpty(dumpFile), "Null or empty dumpFile not allowed")
  val stackList = DumpParser.parse(dumpFile)
  val idThreadMap = StackWiseUtils.mapThreadsById(stackList)
  val lockedOwnershipMap = StackWiseUtils.findLockOwnership(stackList)
  val blockingThreads = StackWiseUtils.findBlockingThreads(stackList)
  val blockedThreadsUnknownBlocker = StackWiseUtils.findBlockerUnknownThreads(stackList)

  def reportBlockedThreads(outStream: OutputStream) {
    val blockedThreads = StackWiseUtils.findThreads(stackList, Array(Thread.State.BLOCKED))

    val printStream = new PrintStream(outStream)
    if (blockedThreads.length == 0) {
      printStream.println("Thread dump reports no blocked threads.")
      return
    }
    
    if (blockingThreads.length > 0) {
      printStream.println("The following threads are blocking other threads from executing.")
      blockingThreads.foreach { stack => printStream.println(stack) }
    }
    
    if (blockedThreadsUnknownBlocker.size > 0) {
      printStream.println("The following threads are blocked, but blockers weren't reported.")
      blockedThreadsUnknownBlocker.foreach { stack => printStream.println(stack) }
    }

  }

  def reportHotSpots(outStream: OutputStream) {

  }

}