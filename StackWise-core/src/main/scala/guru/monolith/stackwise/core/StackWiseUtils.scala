package guru.monolith.stackwise.core

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

object StackWiseUtils {
  def findThreads (mainStackList:Seq[ThreadStack], states:Array[Thread.State]) : Seq[ThreadStack] = {
    val filteredList = new ListBuffer[ThreadStack]
    for (stack <- mainStackList) {
      if (states.contains(stack.state)) filteredList+=stack
    }
    
    return filteredList
  }
  
  def findBlockerUnknownThreads (mainStackList:Seq[ThreadStack]) : Seq[ThreadStack] = {
    val blockedThreadList = findThreads(mainStackList, Array(Thread.State.BLOCKED))
    val lockOwnershipMap = findLockOwnership(mainStackList)
    val idThreadMap = mapThreadsById(mainStackList)
    
    val blockedList = new ListBuffer[ThreadStack]
    for (stack <- blockedThreadList) {
      val desiredLock = findDesiredLock(stack)
      if (desiredLock == null) {
        blockedList += stack
      } else if ( !lockOwnershipMap.contains(desiredLock.monitorLockName)){
        blockedList += stack
      }
    }
    
    return blockedList
  }
  
  def findBlockingThreads (mainStackList:Seq[ThreadStack]) : Seq[ThreadStack] = {
    val blockedThreadList = findThreads(mainStackList, Array(Thread.State.BLOCKED))
    val lockOwnershipMap = findLockOwnership(mainStackList)
    val idThreadMap = mapThreadsById(mainStackList)
    
    val blockingList = new ListBuffer[ThreadStack]
    for (stack <- blockedThreadList) {
      val desiredLock = findDesiredLock(stack)
      if (desiredLock != null && lockOwnershipMap.contains(desiredLock.monitorLockName)) {
        val blockingThread = idThreadMap.get(lockOwnershipMap.get(desiredLock.monitorLockName).get).get
        if ( !blockingList.contains(blockingThread)) {
          blockingList += blockingThread
        }
      }
    }
    
    return blockingList
  }
  
  def mapThreadsById (mainStackList:Seq[ThreadStack]) : Map[String,ThreadStack] = {
    val stackNameMap = HashMap.empty[String,ThreadStack]
    mainStackList.foreach { stack => stackNameMap.put(stack.id, stack) }
    
    return stackNameMap.toMap
  }
  
  def findLockOwnership (mainStackList:Seq[ThreadStack]) : Map[String,String] = {
    val lockOwnershipMap = HashMap.empty[String,String]
    val executionPointList = executionPointSeq(mainStackList)
    mainStackList.foreach { stack => 
      lockResourceSeq(stack.executionPointList).foreach { lock => lockOwnershipMap.put(lock.monitorLockName, stack.id) }
      }
    
    return lockOwnershipMap.toMap
  }
  
  def findDesiredLock (stack:ThreadStack) : LockedResource = {
    var locked : LockedResource = null
    stack.executionPointList.toSeq.foreach { point => if (point.blockedOn != null) locked=point.blockedOn }
    return locked
  }
  
  def executionPointSeq(mainStackList:Seq[ThreadStack]):Seq[ExecutionPoint] = {
    val pointList = new ListBuffer[ExecutionPoint]
    mainStackList.foreach { stack => pointList++=stack.executionPointList }
    return pointList
  }
  
  def lockResourceSeq(pointList:Seq[ExecutionPoint]):Seq[LockedResource] = {
    val lockList = new ListBuffer[LockedResource]
    pointList.foreach { point => lockList++=point.lockedResourceList }
    return lockList
  }
  
}