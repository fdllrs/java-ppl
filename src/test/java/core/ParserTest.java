package core;

import ast.*;
import instructions.LetK.Binding;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

	@Test
	public void testParseSimpleValue() {
		List<Expression> anExpression = Parser.parse("42");
		assertEquals(1, anExpression.size());
		assertInstanceOf(ValueExpression.class, anExpression.getFirst());

		ValueExpression expected = new ValueExpression(42L);
		assertEquals(expected, anExpression.getFirst());
	}

	@Test
	public void testParseDoubleValue() {
		List<Expression> anExpression = Parser.parse("3.14159");
		assertEquals(1, anExpression.size());
		assertInstanceOf(ValueExpression.class, anExpression.getFirst());

		ValueExpression expected = new ValueExpression(3.14159);
		assertEquals(expected, anExpression.getFirst());
	}

	@Test
	public void testParseBooleansAndNil() {
		assertEquals(new ValueExpression(Boolean.TRUE), Parser.parse("true").getFirst());
		assertEquals(new ValueExpression(Boolean.FALSE), Parser.parse("false").getFirst());
		assertEquals(new ValueExpression(null), Parser.parse("nil").getFirst());
	}

	@Test
	public void testParseStringLiteral() {
		List<Expression> anExpression = Parser.parse("\"hello \\\"world\\\"\"");
		assertEquals(1, anExpression.size());
		assertEquals(new ValueExpression("hello \"world\""), anExpression.getFirst());
	}

	@Test
	public void testParseSymbolExpression() {
		List<Expression> anExpression = Parser.parse("my-variable");
		assertEquals(1, anExpression.size());
		assertEquals(new SymbolExpression("my-variable"), anExpression.getFirst());
	}

	@Test
	public void testParseLetExpression() {
		List<Expression> anExpression = Parser.parse("(let [x 10 y 20] (+ x y))");
		assertEquals(1, anExpression.size());
		assertInstanceOf(LetExpression.class, anExpression.getFirst());

		LetExpression expected = new LetExpression(List.of(new Binding("x",
																	   new ValueExpression(10L)),
														   new Binding("y",
																	   new ValueExpression(20L))),
												   List.of(new CallExpression(new SymbolExpression(
														   "+"),
																			  List.of(new SymbolExpression(
																							  "x"),
																					  new SymbolExpression(
																							  "y")))));
		assertEquals(expected, anExpression.getFirst());
	}

	@Test
	public void testParseIfExpression() {
		List<Expression> anExpression = Parser.parse("(if true 1 2)");
		assertEquals(1, anExpression.size());
		assertInstanceOf(IfExpression.class, anExpression.getFirst());

		IfExpression expected = new IfExpression(new ValueExpression(Boolean.TRUE),
												 new ValueExpression(1L),
												 new ValueExpression(2L));
		assertEquals(expected, anExpression.getFirst());
	}

	@Test
	public void testParseFnExpression() {
		List<Expression> anExpression = Parser.parse("(fn [a b] (+ a b))");
		assertEquals(1, anExpression.size());
		assertInstanceOf(FnExpression.class, anExpression.getFirst());

		FnExpression expected = new FnExpression(List.of("a", "b"),
												 List.of(new CallExpression(new SymbolExpression(
														 "+"),
																			List.of(new SymbolExpression(
																							"a"),
																					new SymbolExpression(
																							"b")))));
		assertEquals(expected, anExpression.getFirst());
	}

	@Test
	public void testParseDefnExpression() {
		List<Expression> anExpression = Parser.parse("(defn my-func [x] (* x 2))");
		assertEquals(1, anExpression.size());
		assertInstanceOf(DefnExpression.class, anExpression.getFirst());

		DefnExpression expected = new DefnExpression("my-func",
													 List.of("x"),
													 List.of(new CallExpression(new SymbolExpression(
															 "*"),
																				List.of(new SymbolExpression(
																								"x"),
																						new ValueExpression(
																								2L)))));
		assertEquals(expected, anExpression.getFirst());
	}

	@Test
	public void testParseSampleExpression() {
		List<Expression> anExpression = Parser.parse("(sample (normal 0 1))");
		assertEquals(1, anExpression.size());
		assertInstanceOf(SampleExpression.class, anExpression.getFirst());

		SampleExpression expected = new SampleExpression(new CallExpression(new SymbolExpression(
				"normal"), List.of(new ValueExpression(0L), new ValueExpression(1L))));
		assertEquals(expected, anExpression.getFirst());
	}

	@Test
	public void testParseObserveExpression() {
		List<Expression> anExpression = Parser.parse("(observe (bernoulli 0.5) 1)");
		assertEquals(1, anExpression.size());
		assertInstanceOf(ObserveExpression.class, anExpression.getFirst());

		ObserveExpression expected = new ObserveExpression(new CallExpression(new SymbolExpression(
				"bernoulli"), List.of(new ValueExpression(0.5))), new ValueExpression(1L));
		assertEquals(expected, anExpression.getFirst());
	}

	@Test
	public void testParseCallExpression() {
		List<Expression> anExpression = Parser.parse("(+ 1 2 3)");
		assertEquals(1, anExpression.size());
		assertInstanceOf(CallExpression.class, anExpression.getFirst());

		CallExpression expected = new CallExpression(new SymbolExpression("+"),
													 List.of(new ValueExpression(1L),
															 new ValueExpression(2L),
															 new ValueExpression(3L)));
		assertEquals(expected, anExpression.getFirst());
	}

	@Test
	public void testCommentsAndWhitespace() {
		List<Expression> anExpression = Parser.parse("""
													   ; this is a comment
													  42\s
													  ; another comment\s
													  43\
													 """);
		assertEquals(2, anExpression.size());
		assertEquals(new ValueExpression(42L), anExpression.get(0));
		assertEquals(new ValueExpression(43L), anExpression.get(1));
	}

	@Test
	public void testUnterminatedStringThrows() {
		assertThrows(RuntimeException.class, () -> Parser.parse("\"unterminated"));
	}

	@Test
	public void testMissingClosingParenthesisThrows() {
		assertThrows(RuntimeException.class, () -> Parser.parse("(let [x 10]"));
	}

	@Test
	public void testUnexpectedClosingParenthesisThrows() {
		assertThrows(RuntimeException.class, () -> Parser.parse("(+ 1 2))"));
	}
}
