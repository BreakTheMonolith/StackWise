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

import java.util.List;

case class ThreadStack(id:String, name:String, state:Thread.State, monitorId: String, executionPointList:List[ExecutionPoint], lockedSunchronizerList:List[LockedResource])
case class ExecutionPoint(className:String, methodName:String, sourceFileName:String, lineNbr:Integer, blockedOn: LockedResource, lockedResourceList:List[LockedResource])
case class LockedResource(monitorLockName:String, lockedClassName:String)
case class HotSpot(className:String, methodName:String, sourceFileName:String, nbrMentions:Int)
case class BlockingResource(lockedResource:LockedResource, blockingThreadId:String, seekingThreadId:String)