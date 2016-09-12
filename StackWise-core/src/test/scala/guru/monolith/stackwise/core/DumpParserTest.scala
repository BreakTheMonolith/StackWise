package guru.monolith.stackwise.core

import org.junit.Assert
import org.junit.Test

@Test
class DumpParserTest extends StackWiseTestBase {
  
  @Test 
  def testThreadParse {
    Assert.assertEquals(66, stack1.length)
    Assert.assertEquals(76, stack2.length)
    Assert.assertEquals(45, stack3.length)
    
    Assert.assertEquals("t@172", stack1(0).id)
    Assert.assertEquals("66", stack2(0).id)
    Assert.assertEquals("0x000000005955c800", stack3(0).id)
    
    Assert.assertEquals("RMI TCP Connection(207)-10.13.1.18", stack1(0).name)
    Assert.assertEquals("RMI Scheduler(0)", stack2(0).name)
    Assert.assertEquals("RMI TCP Connection(3)-192.168.56.1", stack3(0).name)
    
    Assert.assertEquals("0x000000005d4cc000", stack3(0).monitorId)
    Assert.assertNull(stack1(0).monitorId)
    Assert.assertNull(stack2(0).monitorId)
    
    Assert.assertEquals(Thread.State.RUNNABLE, stack1(0).state)
    Assert.assertEquals(Thread.State.WAITING, stack2(0).state)
    Assert.assertEquals(Thread.State.TIMED_WAITING, stack3(0).state)
    
    Assert.assertEquals(1, stack1(0).lockedSunchronizerList.size())
    Assert.assertEquals("6756a33c", stack1(0).lockedSunchronizerList.get(0).monitorLockName)
    Assert.assertEquals("java.util.concurrent.ThreadPoolExecutor$Worker", stack1(0).lockedSunchronizerList.get(0).lockedClassName)
  }
  
  @Test 
  def testExecutionPointParse {
    Assert.assertEquals(12, stack1(0).executionPointList.size())
    Assert.assertEquals(9, stack2(0).executionPointList.size())
    Assert.assertEquals(25, stack3(0).executionPointList.size())
    
    Assert.assertEquals("java.net.SocketInputStream", stack1(0).executionPointList.get(0).className)
    Assert.assertEquals("socketRead0", stack1(0).executionPointList.get(0).methodName)
    Assert.assertEquals("Native Method", stack1(0).executionPointList.get(0).sourceFileName)
    
    Assert.assertEquals("java.net.SocketInputStream", stack1(0).executionPointList.get(1).className)
    Assert.assertEquals("read", stack1(0).executionPointList.get(1).methodName)
    Assert.assertEquals("SocketInputStream.java", stack1(0).executionPointList.get(1).sourceFileName)
    Assert.assertEquals(152, stack1(0).executionPointList.get(1).lineNbr)
    
    Assert.assertEquals(1, stack1(0).executionPointList.get(4).lockedResourceList.size())
    Assert.assertEquals("584982c0", stack1(0).executionPointList.get(4).lockedResourceList.get(0).monitorLockName)
    Assert.assertEquals("java.io.BufferedInputStream", stack1(0).executionPointList.get(4).lockedResourceList.get(0).lockedClassName)
    
    Assert.assertEquals("6eea934e", stack1(64).executionPointList.get(0).blockedOn.monitorLockName)
    Assert.assertEquals("java.lang.ref.Reference$Lock", stack1(64).executionPointList.get(0).blockedOn.lockedClassName)
  }
  
}