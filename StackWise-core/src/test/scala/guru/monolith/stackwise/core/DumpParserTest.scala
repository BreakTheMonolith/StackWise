package guru.monolith.stackwise.core

import java.util.List
import org.junit.Test
import org.junit.Before
import org.apache.commons.io.FileUtils
import java.io.File
import org.junit.Assert

@Test
class DumpParserTest {
  
  var dump1:String = _
  var dump2:String = _
  var dump3:String = _
  
  var stack1:List[ThreadStack] = _
  var stack2:List[ThreadStack] = _
  var stack3:List[ThreadStack] = _
  
  @Before 
  def initialize {
    dump1 = FileUtils.readFileToString(new File("src/test/dumps/Thread-dump-1.txt"), "UTF-8");
    dump2 = FileUtils.readFileToString(new File("src/test/dumps/Thread-dump-2.txt"), "UTF-8");
    dump3 = FileUtils.readFileToString(new File("src/test/dumps/Thread-dump-3.txt"), "UTF-8");
    
    val parser = new DumpParser
    
    stack1 = parser.parse(dump1)
    stack2 = parser.parse(dump2)
    stack3 = parser.parse(dump3)
  }
  
  @Test 
  def testThreadParse {
    Assert.assertEquals(66, stack1.size())
    Assert.assertEquals(76, stack2.size())
    Assert.assertEquals(45, stack3.size())
    
    Assert.assertEquals("RMI TCP Connection(207)-10.13.1.18", stack1.get(0).name)
    Assert.assertEquals("RMI Scheduler(0)", stack2.get(0).name)
    Assert.assertEquals("RMI TCP Connection(3)-192.168.56.1", stack3.get(0).name)
    
    Assert.assertEquals("0x000000005d4cc000", stack3.get(0).monitorId)
    Assert.assertNull(stack1.get(0).monitorId)
    Assert.assertNull(stack2.get(0).monitorId)
    
    Assert.assertEquals(Thread.State.RUNNABLE, stack1.get(0).state)
    Assert.assertEquals(Thread.State.WAITING, stack2.get(0).state)
    Assert.assertEquals(Thread.State.TIMED_WAITING, stack3.get(0).state)
    
    Assert.assertEquals(1, stack1.get(0).lockedSunchronizerList.size())
    Assert.assertEquals("6756a33c", stack1.get(0).lockedSunchronizerList.get(0).monitorLockName)
    Assert.assertEquals("java.util.concurrent.ThreadPoolExecutor$Worker", stack1.get(0).lockedSunchronizerList.get(0).lockedClassName)
  }
  
  @Test 
  def testExecutionPointParse {
    Assert.assertEquals(12, stack1.get(0).executionPointList.size())
    Assert.assertEquals(9, stack2.get(0).executionPointList.size())
    Assert.assertEquals(25, stack3.get(0).executionPointList.size())
    
    Assert.assertEquals("java.net.SocketInputStream", stack1.get(0).executionPointList.get(0).className)
    Assert.assertEquals("socketRead0", stack1.get(0).executionPointList.get(0).methodName)
    Assert.assertEquals("Native Method", stack1.get(0).executionPointList.get(0).sourceFileName)
    
    Assert.assertEquals("java.net.SocketInputStream", stack1.get(0).executionPointList.get(1).className)
    Assert.assertEquals("read", stack1.get(0).executionPointList.get(1).methodName)
    Assert.assertEquals("SocketInputStream.java", stack1.get(0).executionPointList.get(1).sourceFileName)
    Assert.assertEquals(152, stack1.get(0).executionPointList.get(1).lineNbr)
    
    Assert.assertEquals(1, stack1.get(0).executionPointList.get(4).lockedResourceList.size())
    Assert.assertEquals("584982c0", stack1.get(0).executionPointList.get(4).lockedResourceList.get(0).monitorLockName)
    Assert.assertEquals("java.io.BufferedInputStream", stack1.get(0).executionPointList.get(4).lockedResourceList.get(0).lockedClassName)
    
    Assert.assertEquals("6eea934e", stack1.get(64).executionPointList.get(0).blockedOn.monitorLockName)
    Assert.assertEquals("java.lang.ref.Reference$Lock", stack1.get(64).executionPointList.get(0).blockedOn.lockedClassName)
  }
  
}