package guru.monolith.stackwise.core

import java.io.File

import org.apache.commons.io.FileUtils
import org.junit.Before

trait StackWiseTestBase {
  
  var dump1:String = _
  var dump2:String = _
  var dump3:String = _
  var dump4:String = _
  var dump5:String = _
  
  var stack1:Seq[ThreadStack] = _
  var stack2:Seq[ThreadStack] = _
  var stack3:Seq[ThreadStack] = _
  var stack4:Seq[ThreadStack] = _
  var stack5:Seq[ThreadStack] = _
  
  @Before 
  def initialize {
    dump1 = FileUtils.readFileToString(new File("src/test/dumps/Thread-dump-1.txt"), "UTF-8");
    dump2 = FileUtils.readFileToString(new File("src/test/dumps/Thread-dump-2.txt"), "UTF-8");
    dump3 = FileUtils.readFileToString(new File("src/test/dumps/Thread-dump-3.txt"), "UTF-8");
    dump4 = FileUtils.readFileToString(new File("src/test/dumps/Thread-dump-4.txt"), "UTF-8");
    dump5 = FileUtils.readFileToString(new File("src/test/dumps/Thread-dump-5.txt"), "UTF-8");
    
    stack1 = DumpParser.parse(dump1)
    stack2 = DumpParser.parse(dump2)
    stack3 = DumpParser.parse(dump3)
    stack4 = DumpParser.parse(dump4)
    stack5 = DumpParser.parse(dump5)
  }
}
