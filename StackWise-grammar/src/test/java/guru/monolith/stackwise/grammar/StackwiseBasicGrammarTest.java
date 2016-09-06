package guru.monolith.stackwise.grammar;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StackwiseBasicGrammarTest extends GrammarTestBase {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testLexerDump1() throws Exception {
		testLexer("src/test/dumps/Thread-dump-1.txt");
	}

	@Test
	public void testLexerDump2() throws Exception {
		testLexer("src/test/dumps/Thread-dump-2.txt");
	}

	@Test
	public void testLexerDump3() throws Exception {
		testLexer("src/test/dumps/Thread-dump-3.txt");
	}

	@Test
	public void testParserDump3() throws Exception {
		testParser("src/test/dumps/Thread-dump-3.txt");
	}

	public void testLexer(String fileName) throws Exception {
		this.setupDump(fileName);
		for (int i = 0; i < dumpTokens.getNumberOfOnChannelTokens(); i++) {
			Token currentToken = dumpTokens.get(i);
			if (currentToken.getType() <= 0 && currentToken.getText() == null) {
				System.out.println("Token: " + currentToken + " type=Not Recognized!");
				Assert.fail();
			} else if (currentToken.getType() > 0) {
				System.out.println(
						"Token: " + currentToken + " type=" + StackwiseLexer.ruleNames[currentToken.getType() - 1]);
			} else {
				System.out.println("Token: " + currentToken + " type=Not Recognized!");
			}
		}
	}

	public void testParser(String fileName) throws Exception {
		this.setupDump(fileName);
		StackwiseParser.ThreadsContext threadTree = dumpParser.threads();
		ParseTreeWalker.DEFAULT.walk(antlrListener, threadTree);
		// if (errorListener.errorDetected) {
		// Assert.fail("Parse errors detected");
		// }
	}

}
