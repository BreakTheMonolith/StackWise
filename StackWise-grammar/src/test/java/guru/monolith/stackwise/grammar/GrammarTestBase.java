package guru.monolith.stackwise.grammar;

import java.io.File;
import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;

public class GrammarTestBase {

	protected static class TestListener extends StackwiseBaseListener {
	
			@Override
			public void enterEveryRule(ParserRuleContext ctx) {
				displayStart("enter " + ctx.getClass().getSimpleName(), ctx);
			}
	
			@Override
			public void exitEveryRule(ParserRuleContext ctx) {
				displayStop("exit " + ctx.getClass().getSimpleName(), ctx);
			}
	
			@Override
			public void visitErrorNode(ErrorNode node) {
				// we've screwed up the parser rules
				Assert.fail("visitErrorNode: " + node);
			}
	
			private void displayStart(String label, ParserRuleContext ctx) {
				System.out.println(label + " line=" + ctx.start.getLine() + " column=" + ctx.start.getCharPositionInLine());
			}
	
			private void displayStop(String label, ParserRuleContext ctx) {
				System.out.println(label + " line=" + ctx.stop.getLine() + " column=" + ctx.stop.getCharPositionInLine());
			}
	
		}

	protected static class ErrorListener implements ANTLRErrorListener {
	
			boolean errorDetected = false;
	
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
					String msg, RecognitionException e) {
				// System.err.println("syntaxError=" + msg);
				errorDetected = true;
	
			}
	
			@Override
			public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
					BitSet ambigAlts, ATNConfigSet configs) {
				// NoOp
	
			}
	
			@Override
			public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
					BitSet conflictingAlts, ATNConfigSet configs) {
				// NoOp
	
			}
	
			@Override
			public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction,
					ATNConfigSet configs) {
				// NoOp
	
			}
	
		}

	protected String dumpText;
	protected CommonTokenStream dumpTokens;
	protected TestListener antlrListener;
	protected ErrorListener errorListener;
	protected StackwiseLexer dumpLexer;
	protected StackwiseParser dumpParser;

	protected void setupDump(String fileName) throws Exception {
		dumpText = FileUtils.readFileToString(new File(fileName), "UTF-8");
		ANTLRInputStream input = new ANTLRInputStream(dumpText);
		dumpLexer = new StackwiseLexer(input);
		dumpTokens = new CommonTokenStream(dumpLexer);
		dumpParser = new StackwiseParser(dumpTokens);
		antlrListener = new TestListener();
	}

}
