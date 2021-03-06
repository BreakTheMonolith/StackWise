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

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.EnumUtils
import Array._
import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import java.util.ArrayList

object LineType extends Enumeration {
  val Thread, Lock, ExecutionPoint, WhiteSpace, ThreadState, LockSynchronizer, WaitLock = Value
}

object DumpParser {
  val stateList = Array(Thread.State.NEW.toString()
      , Thread.State.RUNNABLE.toString()
      , Thread.State.BLOCKED.toString()
      , Thread.State.WAITING.toString()
      , Thread.State.TIMED_WAITING.toString()
      , Thread.State.TERMINATED.toString())
  
  def parse(threadDump: String) : Seq[ThreadStack] = {
    val threadList : Buffer[ThreadStack] = new ListBuffer[ThreadStack]
    val cleanedDump = StringUtils.remove(StringUtils.remove(threadDump, "\r"), "\t")
    val lineList:Array[String] = StringUtils.split(cleanedDump, "\n");
    
    var currentThreadStack : ThreadStack = null 
    var lastExecutionPoint : ExecutionPoint = null
    for (line <- lineList) {
      val word : Array[String] = StringUtils.split(line)
      val lineType = findLineType(line)
      lineType match {
        case LineType.Thread => {
          if (currentThreadStack != null) threadList+=currentThreadStack
          currentThreadStack = parseThreadStack(line)
        }
        case LineType.ThreadState => {
          currentThreadStack = currentThreadStack.copy(state=parseThreadStackState(word))
        }
        case LineType.ExecutionPoint => {
          lastExecutionPoint = parseExecutionPoint(line)
          currentThreadStack.executionPointList.add(lastExecutionPoint)
        }
        case LineType.Lock => {
          val lockedResource = parseLockedResource(line)
          if (lastExecutionPoint != null) {
            lastExecutionPoint.lockedResourceList.add(lockedResource)
          } else {
            currentThreadStack.lockedSunchronizerList.add(lockedResource)
          }
        }
        case LineType.LockSynchronizer => {
          lastExecutionPoint = null
        }
        case LineType.WaitLock => {
          val lockedResource = parseLockedResource(line)
           lastExecutionPoint = lastExecutionPoint.copy(blockedOn=lockedResource)
           currentThreadStack.executionPointList.remove(currentThreadStack.executionPointList.size() - 1)
           currentThreadStack.executionPointList.add(lastExecutionPoint)
        }
        case _ => {}
      }
    }
    threadList+=currentThreadStack
  }
  
  private def parseLockedResource(line:String) : LockedResource = {
    try {
      val monitorLockName = line.substring(line.indexOf("<") + 1, line.indexOf(">"))
      val lockedClassName = line.substring(line.lastIndexOf("(") + 3, line.lastIndexOf(")"))
      
      new LockedResource(monitorLockName, lockedClassName)
    } catch {
      case e: Exception => throw new RuntimeException("Barf on line: " + line, e);
    }
  }
  
  private def parseExecutionPoint(line:String) : ExecutionPoint = {
    val pointText = line.substring(line.indexOf("at ") + 3)
    val classMethod = pointText.substring(0, pointText.indexOf("("))
    val sourceText = pointText.substring(pointText.indexOf("(") + 1, pointText.indexOf(")"))
    
    val className = classMethod.substring(0, classMethod.lastIndexOf("."))
    val methodName = classMethod.substring(classMethod.lastIndexOf(".") + 1)
    
    var sourceFile:String = null
    var sourceFileLine:Integer = null
    if (sourceText.contains(":")) {
      sourceFile = sourceText.substring(0, sourceText.indexOf(":"))
      sourceFileLine = Integer.parseInt(sourceText.substring(sourceText.indexOf(":") + 1))
    } else {
      sourceFile = sourceText
    }
    
    new ExecutionPoint(className, methodName, sourceFile, sourceFileLine, null, new ArrayList[LockedResource])
  }
  
  private def parseThreadStack(line:String) : ThreadStack = {
    val word : Array[String] = StringUtils.split(line)
    val threadName = line.substring(1, line.indexOf('"', 2))
    val threadId = parseThreadId(word)
    
    new ThreadStack(threadId, threadName, parseThreadStackState(word), parseMonitorId(word(word.length - 1)), new ArrayList[ExecutionPoint], new ArrayList[LockedResource])
  } 
  
  private def parseThreadId(wordArray:Array[String]) : String = {
    for (word <- wordArray) {if (word.startsWith("tid=")) return word.substring(4)}
    wordArray(wordArray.length - 1)
  }
  
  private def parseThreadStackState(wordArray:Array[String]) : Thread.State = {
    for (word <- wordArray) {if (stateList.contains(word)) return Thread.State.valueOf(word)}
    return null
  }
  
  private def parseMonitorId(word:String) : String = {
    if ( (word.startsWith("[") && word.endsWith("]"))
        || (word.startsWith("<") && word.endsWith(">"))) {
      return word.substring(1, word.length() - 1)
    }
  
    return null
  }
  
  private def findLineType(line:String ) : LineType.Value = {
    val word : Array[String] = StringUtils.split(line)
    
    if (line.startsWith("\"")) return LineType.Thread
    if (word.length == 0) return LineType.WhiteSpace
    if ("at".equals(word(0))) return LineType.ExecutionPoint
    if ("java.lang.Thread.State:".equals(word(0))) return LineType.ThreadState
    if ("Locked".equals(word(0))) return LineType.LockSynchronizer
    if ("-".equals(word(0)) && !"None".equalsIgnoreCase(word(1)) && !"waiting".equalsIgnoreCase(word(1))) return LineType.Lock
    if ("-".equals(word(0)) && "waiting".equalsIgnoreCase(word(1))) return LineType.WaitLock
    
    LineType.WhiteSpace
  }
}