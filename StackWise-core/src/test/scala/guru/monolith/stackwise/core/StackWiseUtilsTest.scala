package guru.monolith.stackwise.core

import org.junit.Test
import org.junit.Assert

@Test
class StackWiseUtilsTest extends StackWiseTestBase {
  
  @Test
  def testFindThreads {
    val blockedThreads = StackWiseUtils.findThreads(stack4, Array(Thread.State.BLOCKED))
    Assert.assertEquals(6, blockedThreads.length)
    blockedThreads.foreach { stack => Assert.assertEquals(Thread.State.BLOCKED, stack.state) }
  }
  
  @Test
  def testmapThreadsById {
    val idThreadMap = StackWiseUtils.mapThreadsById(stack1)
    Assert.assertEquals(66, idThreadMap.size)
    Assert.assertTrue(idThreadMap.contains("t@172"))
  }
  
  @Test
  def testfindLockOwnership {
    val lockOwnershipMap = StackWiseUtils.findLockOwnership(stack4)
    Assert.assertEquals("0x000000001aa19800", lockOwnershipMap.get("0x0000000682cb47d8").get)
  }
  
  @Test
  def testfindDesiredLock {
    val lockResource = StackWiseUtils.findDesiredLock(stack4.head)
    Assert.assertEquals("0x000000068c17f6a8", lockResource.monitorLockName)
    Assert.assertEquals("java.lang.Object", lockResource.lockedClassName)
  }
  
  @Test
  def testfindBlockingThreads {
    val blockingThreadList = StackWiseUtils.findBlockingThreads(stack4)
    Assert.assertEquals(3, blockingThreadList.size)
  }
  
   @Test
  def findBlockerUnknownThreads {
    val strandedBlockersList = StackWiseUtils.findBlockerUnknownThreads(stack2)
    Assert.assertEquals(23, strandedBlockersList.size)
    
    val strandedBlockersList4 = StackWiseUtils.findBlockerUnknownThreads(stack4)
    Assert.assertEquals(0, strandedBlockersList4.size)
  }
  
}