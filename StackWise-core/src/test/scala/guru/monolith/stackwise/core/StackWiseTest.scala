package guru.monolith.stackwise.core

import org.junit.Test
import java.io.ByteArrayOutputStream
import org.junit.Assert

@Test
class StackWiseTest extends StackWiseTestBase {
  
  @Test
  def testreportBlockedThreads {
    var sw = new StackWise(dump1)
    var outStream = new ByteArrayOutputStream
    sw.reportBlockedThreads(outStream)
    Assert.assertTrue(outStream.toString().contains("no blocked threads"))
    
    outStream = new ByteArrayOutputStream
    sw = new StackWise(dump2)
    sw.reportBlockedThreads(outStream)
    Assert.assertTrue(outStream.toString().contains("blockers weren't reported"))
    
    outStream = new ByteArrayOutputStream
    sw = new StackWise(dump4)
    sw.reportBlockedThreads(outStream)
    Assert.assertTrue(outStream.toString().contains("blocking other threads from executing"))
    
//    sw = new StackWise(dump4)
//    sw.reportBlockedThreads(System.out)
  }
  
  @Test
  def testreportHotSpots {
    var sw = new StackWise(dump4)
    sw.reportHotSpots(System.out)
    
    sw.reportHotSpots(System.out, "com.jmu")
  }
  @Test
  def testreportAll {
    var sw = new StackWise(dump4)
    sw.reportAll(System.out, "com.jmu")
    
    System.out.println("------------------------------------------")
    
    sw = new StackWise(dump1)
    sw.reportAll(System.out, "com.jmu")
  }
}