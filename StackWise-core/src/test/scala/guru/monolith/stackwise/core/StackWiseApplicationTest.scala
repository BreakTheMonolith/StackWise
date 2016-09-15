package guru.monolith.stackwise.core

import java.io.OutputStream

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import org.apache.commons.io.FileUtils
import org.junit.Assert

@Test
class StackWiseApplicationTest extends StackWiseTestBase {
  
  var outStream:OutputStream = null
  var tempOutFile:File = null
  
  @Before
  override def initialize() {
    super.initialize
    outStream = new ByteArrayOutputStream
    tempOutFile = File.createTempFile("StackWiseApplicationTest-", ".out")
  }
  
  @After
  def tearDown() {
    tempOutFile.delete()
  }
  
  @Test
  def testBasic() {
    StackWiseApplication.main(Array("-o", tempOutFile.getAbsolutePath, "-p", "com.jmu", "-f", dumpFileName4))
    val outputContent = FileUtils.readFileToString(tempOutFile, "UTF-8")
    
    //System.out.println(outputContent)
    Assert.assertTrue(outputContent.contains("com.jmu"))
    Assert.assertTrue(outputContent.contains("http-bio-10.13.0.222-30105-exec-289"))
    Assert.assertTrue(outputContent.contains("state=RUNNABLE tid=0x000000001bf06000"))
    Assert.assertTrue(outputContent.contains("Other threads waiting to lock resource <0x0000000680440048>, (a com.sun.beans.WeakCache)"))
    Assert.assertTrue(outputContent.contains("locked <0x0000000680440048> (a com.sun.beans.WeakCache)"))
    Assert.assertTrue(outputContent.contains("Hot Spot Listing."))
    Assert.assertTrue(outputContent.contains("21 - com.jmu.common.ui.common.filter.CharsetFilter.doFilter() [CharsetFilter.java]"))
  }
  
}