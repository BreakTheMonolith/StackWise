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

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.SystemUtils

object StackWiseUtils {
  def findThreads(mainStackList: Seq[ThreadStack], states: Array[Thread.State]): Seq[ThreadStack] = {
    val filteredList = new ListBuffer[ThreadStack]
    for (stack <- mainStackList) {
      if (states.contains(stack.state)) filteredList += stack
    }

    return filteredList
  }
  
  def findThreadsInSpecificClasses(mainStackList: Seq[ThreadStack], partialClassNames: Array[String]): Seq[ThreadStack] = {
    val filteredList = new ListBuffer[ThreadStack]
    for (stack <- mainStackList) {
      if (stack.executionPointList.size > 0 
          && startswithPhraseInArray(stack.executionPointList.get(0).className, true, partialClassNames) ) filteredList += stack
    }
    return filteredList
  }
  
  def findThreadsReferencingSpecificClasses(mainStackList: Seq[ThreadStack], partialClassNames: Array[String]): Seq[ThreadStack] = {
    val filteredList = new ListBuffer[ThreadStack]
    var addedToStack = false
    
    for (stack <- mainStackList) {
      addedToStack = false
      for (point <- stack.executionPointList) {
        if (!addedToStack && startswithPhraseInArray(point.className, true, partialClassNames)) {
          filteredList += stack
          addedToStack = true
        }
      }
    }
    return filteredList
  }
  
  private def startswithPhraseInArray(label:String, startsWith:Boolean, startsWithOptions: Array[String]):Boolean = {
    for (phrase <- startsWithOptions) {
      if (startsWith && label.startsWith(phrase))  return true
      else if (!startsWith && label.contains(phrase))  return true
    }
    return false
  }

  def findBlockerUnknownThreads(mainStackList: Seq[ThreadStack]): Seq[ThreadStack] = {
    val blockedThreadList = findThreads(mainStackList, Array(Thread.State.BLOCKED))
    val lockOwnershipMap = findLockOwnership(mainStackList)
    val idThreadMap = mapThreadsById(mainStackList)

    val blockedList = new ListBuffer[ThreadStack]
    for (stack <- blockedThreadList) {
      val desiredLock = findDesiredLock(stack)
      if (desiredLock == null) {
        blockedList += stack
      } else if (!lockOwnershipMap.contains(desiredLock.monitorLockName)) {
        blockedList += stack
      }
    }

    return blockedList
  }
  
  def findDesiredLockOwnership(mainStackList: Seq[ThreadStack]): Map[String, String] = {
    val lockOwnershipMap = HashMap.empty[String, String]
    val blockedThreadList = findThreads(mainStackList, Array(Thread.State.BLOCKED))
    blockedThreadList.foreach { stack =>
      waitingResourceSeq(stack.executionPointList).foreach { lock => 
        lockOwnershipMap.put(lock.monitorLockName, stack.id) 
      }
    }
    
    return lockOwnershipMap.toMap
  }

  def findBlockingThreads(mainStackList: Seq[ThreadStack]): Seq[ThreadStack] = {
    val blockedThreadList = findThreads(mainStackList, Array(Thread.State.BLOCKED))
    val lockOwnershipMap = findLockOwnership(mainStackList)
    val idThreadMap = mapThreadsById(mainStackList)

    val blockingList = new ListBuffer[ThreadStack]
    for (stack <- blockedThreadList) {
      val desiredLock = findDesiredLock(stack)
      if (desiredLock != null && lockOwnershipMap.contains(desiredLock.monitorLockName)) {
        val blockingThread = idThreadMap.get(lockOwnershipMap.get(desiredLock.monitorLockName).get).get
        if (!blockingList.contains(blockingThread)) {
          blockingList += blockingThread
        }
      }
    }

    return blockingList
  }
  
  def findBlockingResourceMap(mainStackList: Seq[ThreadStack]): Map[ThreadStack,Seq[LockedResource]] = {
    val blockingThreads = findBlockingThreads(mainStackList)
    val desiredLockOwnershipMap = findDesiredLockOwnership(mainStackList)
    val blockingResourceMap = HashMap.empty[ThreadStack, Seq[LockedResource]]
    
    blockingThreads.foreach { stack => {
        val lockedResourceSet = lockResourceSeq(stack.executionPointList)
        val blockingResourceList = new ListBuffer[LockedResource]
        lockedResourceSet.foreach { lock => 
          if (desiredLockOwnershipMap.contains(lock.monitorLockName)) {
            blockingResourceList.+=:(lock)
          }
        }
        blockingResourceMap.put(stack, blockingResourceList.toSeq)
      }
    }
    
    return blockingResourceMap.toMap
  }
  
  def findBlockingResources(mainStackList: Seq[ThreadStack]): Seq[BlockingResource] = {
    val blockingResourcesList = new ListBuffer[BlockingResource]
    val blockedThreadResourceMap  = findBlockingResourceMap(mainStackList)
    val itThreadMap = mapThreadsById(mainStackList)
    val desiredLockOwnershipMap = findDesiredLockOwnership(mainStackList)
    
    blockedThreadResourceMap.foreach {case (threadStack, blockingResourceSeq) => 
      blockingResourceSeq.foreach { blockingResource => 
        blockingResourcesList += new BlockingResource(blockingResource, threadStack.id, desiredLockOwnershipMap.get(blockingResource.monitorLockName).get) 
        }
      }
    
    blockingResourcesList.toSeq
  }

  def mapThreadsById(mainStackList: Seq[ThreadStack]): Map[String, ThreadStack] = {
    val stackNameMap = HashMap.empty[String, ThreadStack]
    mainStackList.foreach { stack => stackNameMap.put(stack.id, stack) }

    return stackNameMap.toMap
  }

  def findLockOwnership(mainStackList: Seq[ThreadStack]): Map[String, String] = {
    val lockOwnershipMap = HashMap.empty[String, String]
    val executionPointList = executionPointSeq(mainStackList)
    mainStackList.foreach { stack =>
      lockResourceSeq(stack.executionPointList).foreach { lock => lockOwnershipMap.put(lock.monitorLockName, stack.id) }
    }

    return lockOwnershipMap.toMap
  }

  def findDesiredLock(stack: ThreadStack): LockedResource = {
    var locked: LockedResource = null
    stack.executionPointList.toSeq.foreach { point => if (point.blockedOn != null) locked = point.blockedOn }
    return locked
  }

  def executionPointSeq(mainStackList: Seq[ThreadStack]): Seq[ExecutionPoint] = {
    val pointList = new ListBuffer[ExecutionPoint]
    mainStackList.foreach { stack => pointList ++= stack.executionPointList }
    return pointList
  }

  def lockResourceSeq(pointList: Seq[ExecutionPoint]): Seq[LockedResource] = {
    val lockList = new ListBuffer[LockedResource]
    pointList.foreach { point => lockList ++= point.lockedResourceList }
    return lockList
  }
  
  def waitingResourceSeq(pointList: Seq[ExecutionPoint]): Seq[LockedResource] = {
    val lockList = new ListBuffer[LockedResource]
    pointList.foreach { point => 
      if (point.blockedOn != null) {
        lockList += point.blockedOn 
      }
    }
    return lockList
  }

  def findClassMethodUsage(mainStackList: Seq[ThreadStack], packageQualifier: String = ""): Seq[HotSpot] = {
    val hotSpotMap = HashMap.empty[String, HotSpot]
    val executionPoints = executionPointSeq(findThreads(mainStackList, Array(Thread.State.RUNNABLE)))

    executionPoints.foreach { point =>
      val classMethod = point.className + "." + point.methodName
      val hotSpotOption = hotSpotMap.get(classMethod)

      if (point.className.startsWith(packageQualifier)) {
        var hotSpot: HotSpot = null
        if (hotSpotOption.isEmpty) {
          hotSpot = new HotSpot(point.className, point.methodName, point.sourceFileName, 1)

        } else {
          hotSpot = hotSpotOption.get.copy(nbrMentions = hotSpotOption.get.nbrMentions + 1)
        }
        hotSpotMap.put(classMethod, hotSpot)
      }
    }

    return hotSpotMap.values.toSeq

  }

  def formatHotSpot(hotSpot: HotSpot): String = {
    return hotSpot.nbrMentions + " - " + hotSpot.className + "." + hotSpot.methodName + "() [" + hotSpot.sourceFileName + "]"
  }

  def formatStack(stack: ThreadStack, packageQualifier: String = "", messages:Array[String] = Array()): String = {
    val builder = new StringBuilder(String.format("\"%s\" - state=%s tid=%s", stack.name, stack.state, stack.id))
    var isFirst: Boolean = true
    var lastPointOmitted: Boolean = false
    messages.foreach { message => builder.append(SystemUtils.LINE_SEPARATOR + message ) }

    stack.executionPointList.foreach { point =>
      if (isFirst) {
        isFirst = false
        builder.append(SystemUtils.LINE_SEPARATOR)
        builder.append(formatPoint(point))
      } else if (point.className.startsWith(packageQualifier)) {
        builder.append(SystemUtils.LINE_SEPARATOR)
        builder.append(formatPoint(point))
        lastPointOmitted = false
      } else if (point.blockedOn != null || point.lockedResourceList.size > 0) {
        builder.append(SystemUtils.LINE_SEPARATOR)
        builder.append(formatPoint(point))
        lastPointOmitted = false
      } else {
        if (!lastPointOmitted) {
          builder.append(SystemUtils.LINE_SEPARATOR)
          builder.append("   ...")
          lastPointOmitted = true
        }

      }

      if (point.lockedResourceList.size > 0) {
        point.lockedResourceList.foreach { lock =>
          builder.append(SystemUtils.LINE_SEPARATOR)
          builder.append(formatLockedResource(lock))
        }

      }
      if (point.blockedOn != null) {
        builder.append(SystemUtils.LINE_SEPARATOR)
        builder.append(formatBlockingResource(point.blockedOn))
      }

    }

    return builder.toString
  }

  def formatBlockingResource(lock: LockedResource): String = {
    return String.format("	- waiting on <%s> (a %s)", lock.monitorLockName, lock.lockedClassName)
  }

  def formatLockedResource(lock: LockedResource): String = {
    return String.format("	- locked <%s> (a %s)", lock.monitorLockName, lock.lockedClassName)
  }

  def formatPoint(point: ExecutionPoint): String = {
    var sourceName = point.sourceFileName
    if (point.lineNbr != null) {
      sourceName = sourceName + ":" + point.lineNbr
    }
    return "   at " + point.className + "." + point.methodName + "(" + sourceName + ")"
  }

}