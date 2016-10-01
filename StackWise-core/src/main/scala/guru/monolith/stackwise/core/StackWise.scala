/*******************************************************************************
 * Copyright (c) 2016 Break The Monolith, Derek C. Ashmore and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *    Derek C. Ashmore - initial API and implementation
 *******************************************************************************/
package guru.monolith.stackwise.core

import java.io.OutputStream
import java.io.PrintStream

import scala.collection.JavaConversions._

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.SystemUtils
import scala.collection.mutable.ArrayBuffer

class StackWise(dumpFile: String) {
  require(StringUtils.isNotEmpty(dumpFile), "Null or empty dumpFile not allowed")
  val stackList = DumpParser.parse(dumpFile)
  val blockedThreads = StackWiseUtils.findThreads(stackList, Array(Thread.State.BLOCKED))
  val runnableThreads = StackWiseUtils.findThreads(stackList, Array(Thread.State.RUNNABLE))  
  
  val idThreadMap = StackWiseUtils.mapThreadsById(stackList)
  val lockedOwnershipMap = StackWiseUtils.findLockOwnership(stackList)
  val desiredLockOwnershipMap = StackWiseUtils.findDesiredLockOwnership(stackList)
  val blockingThreads = StackWiseUtils.findBlockingThreads(stackList)
  val blockingResourceMap = StackWiseUtils.findBlockingResourceMap(stackList)
  val blockedThreadsUnknownBlocker = StackWiseUtils.findBlockerUnknownThreads(stackList)
  
  def reportAll(outStream: OutputStream, packageQualifier: String = "") {
    reportThreadSummary(outStream, packageQualifier)
    outStream.write(SystemUtils.LINE_SEPARATOR.getBytes)
    reportBlockedThreads(outStream, packageQualifier)
    outStream.write(SystemUtils.LINE_SEPARATOR.getBytes)
    reportHotSpots(outStream, packageQualifier)
    
    outStream.write(SystemUtils.LINE_SEPARATOR.getBytes)
    outStream.write(String.format("Produced by StackWise (https://github.com/BreakTheMonolith/StackWise)%s", SystemUtils.LINE_SEPARATOR).getBytes)
    outStream.write(SystemUtils.LINE_SEPARATOR.getBytes)
  }
  
  def reportThreadSummary(outStream: OutputStream, packageQualifier: String = "") {
    val applicationThreads = StackWiseUtils.findThreadsReferencingSpecificClasses(runnableThreads, Array(packageQualifier))
    val ioBoundThreads = StackWiseUtils.findThreadsInSpecificClasses(applicationThreads, Array("java.net", "java.io", "java.nio"))
    
    val printStream = new PrintStream(outStream)
    val spacer = "   "
    printStream.println("Thread Summary:")
    printStream.println(String.format(spacer + "There are %s running threads.", runnableThreads.size.toString))
    if (StringUtils.isNotEmpty(packageQualifier)) {
      printStream.println(String.format(spacer + "Out of those, %s appear to be application related.", applicationThreads.size.toString))
    }
    printStream.println(String.format(spacer + "Out of those, %s appear to be IO bound.", ioBoundThreads.size.toString))
    
    if (ioBoundThreads.size == 0)  return
    printStream.println()
    printStream.println("The following threads appear to be IO bound.")
    printStream.println()
    
    ioBoundThreads.foreach { stack => printStream.println(StackWiseUtils.formatStack(stack, packageQualifier))  }
  }

  def reportBlockedThreads(outStream: OutputStream, packageQualifier: String = "") {
    

    val printStream = new PrintStream(outStream)
    if (blockedThreads.length == 0) {
      printStream.println("Thread dump reports no blocked threads.")
      return
    }
    
    if (blockingThreads.length > 0) {
      printStream.println("The following threads are blocking other threads from executing.")
      printStream.println()
      blockingResourceMap.keys.foreach { stack => {
        val messageBuffer = new ArrayBuffer[String]
        blockingResourceMap.get(stack).get.foreach { lock => 
          messageBuffer.+=(String.format("   Other threads waiting to lock resource <%s>, (a %s)", 
              lock.monitorLockName, lock.lockedClassName)) 
        }
        
        printStream.println(StackWiseUtils.formatStack(stack, packageQualifier, messageBuffer.toArray)) 
        }
      }
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