package guru.monolith.stackwise.core

import java.util.List;

case class ThreadStack(name:String, state:Thread.State, monitorId: String, executionPointList:List[ExecutionPoint], lockedSunchronizerList:List[LockedResource])
case class ExecutionPoint(className:String, methodName:String, sourceFileName:String, lineNbr:Integer, lockedResourceList:List[LockedResource])
case class LockedResource(monitorLockName:String, lockedClassName:String)