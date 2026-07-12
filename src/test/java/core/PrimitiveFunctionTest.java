package core;

import core.callable.PrimitiveFunction;
import distributions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class PrimitiveFunctionTest {

	// ------------------------------------------------------------------ fromSymbol
	@Test
	public void testFromSymbolKnownOperators() {
		assertEquals(PrimitiveFunction.ADD, PrimitiveFunction.fromSymbol("+"));
		assertEquals(PrimitiveFunction.SUB, PrimitiveFunction.fromSymbol("-"));
		assertEquals(PrimitiveFunction.PROD, PrimitiveFunction.fromSymbol("*"));
		assertEquals(PrimitiveFunction.DIV, PrimitiveFunction.fromSymbol("/"));
		assertEquals(PrimitiveFunction.GREATERTHAN, PrimitiveFunction.fromSymbol(">"));
		assertEquals(PrimitiveFunction.LESSTHAN, PrimitiveFunction.fromSymbol("<"));
		assertEquals(PrimitiveFunction.EQUALS, PrimitiveFunction.fromSymbol("=="));
	}

	@Test
	public void testFromSymbolUnknownReturnsNull() {

		assertNull(PrimitiveFunction.fromSymbol("unknown"));
		assertNull(PrimitiveFunction.fromSymbol(""));
	}

	// ------------------------------------------------------------------ ADD
	@Test
	public void testAddIntegers() {
		assertEquals(6L, runProgram("(+ 1 2 3)"));
	}

	private static Object runProgram(String source) {
		List<ast.Expression> program = Parser.parse(source);
		Environment env = new Environment();
		instructions.EvaluateK eval = new instructions.EvaluateK(program.getFirst(),
																 env,
																 new Address());
		Deque<instructions.Instruction> stack = new ArrayDeque<>();
		stack.push(eval);
		Machine machine = new Machine(stack, env, new Random(0));
		messaging.Message msg = machine.resume();
		return ( (messaging.Done) msg ).returnValue();
	}

	@Test
	public void testAddZeroArgs() {
		// + with no args returns 0 (as long)
		assertEquals(0L, runProgram("(+)"));
	}

	@Test
	public void testAddMixedProducesDouble() {
		Object result = runProgram("(+ 1 0.5)");
		assertInstanceOf(Double.class, result);
		assertEquals(1.5, (double) result, 1e-12);
	}

	// ------------------------------------------------------------------ SUB
	@Test
	public void testSubTwoArgs() {
		assertEquals(3.0, (double) runProgram("(- 5 2)"), 1e-12);
	}

	@Test
	public void testSubOneArgNegates() {
		assertEquals(-7.0, (double) runProgram("(- 7)"), 1e-12);
	}

	@Test
	public void testSubZeroArgs() {
		assertEquals(0.0, (double) runProgram("(-)"), 1e-12);
	}

	// ------------------------------------------------------------------ PROD
	@Test
	public void testProdTwoArgs() {
		assertEquals(6.0, (double) runProgram("(* 2 3)"), 1e-12);
	}

	@Test
	public void testProdZeroArgs() {
		assertEquals(1.0, (double) runProgram("(*)"), 1e-12);
	}

	// ------------------------------------------------------------------ DIV
	@Test
	public void testDivTwoArgs() {
		assertEquals(2.5, (double) runProgram("(/ 5 2)"), 1e-12);
	}

	@Test
	public void testDivOneArgInverts() {
		assertEquals(0.25, (double) runProgram("(/ 4)"), 1e-12);
	}

	@Test
	public void testDivZeroArgs() {
		assertEquals(1.0, (double) runProgram("(/)"), 1e-12);
	}

	// ------------------------------------------------------------------ GREATERTHAN / LESSTHAN
	@Test
	public void testGreaterThan() {
		assertEquals(true, runProgram("(> 3 2)"));
		assertEquals(false, runProgram("(> 2 3)"));
		assertEquals(false, runProgram("(> 2 2)"));
	}

	@Test
	public void testLessThan() {
		assertEquals(true, runProgram("(< 1 2)"));
		assertEquals(false, runProgram("(< 2 1)"));
		assertEquals(false, runProgram("(< 2 2)"));
	}

	@Test
	public void testGreaterThanWrongArityThrows() {
		assertThrows(IllegalArgumentException.class, () -> runProgram("(> 1 2 3)"));
	}

	@Test
	public void testEqualsNumeric() {
		assertEquals(true, runProgram("(== 2 2)"));
		assertEquals(false, runProgram("(== 2 3)"));
	}

	// ------------------------------------------------------------------ distribution constructors
	@Test
	public void testNormalConstructor() {
		Object result = runProgram("(normal 0 1)");
		assertInstanceOf(Normal.class, result);
		assertEquals(0.0, ( (Normal) result ).mu(), 1e-12);
		assertEquals(1.0, ( (Normal) result ).sigma(), 1e-12);
	}

	@Test
	public void testBernoulliConstructor() {
		Object result = runProgram("(bernoulli 0.3)");
		assertInstanceOf(Bernoulli.class, result);
		assertEquals(0.3, ( (Bernoulli) result ).p(), 1e-12);
	}

	@Test
	public void testBinomialConstructor() {
		Object result = runProgram("(binomial 10 0.5)");
		assertInstanceOf(Binomial.class, result);
		assertEquals(10, ( (Binomial) result ).n());
		assertEquals(0.5, ( (Binomial) result ).p(), 1e-12);
	}

	@Test
	public void testBetaConstructor() {
		Object result = runProgram("(beta 2 3)");
		assertInstanceOf(Beta.class, result);
	}

	@Test
	public void testGammaConstructor() {
		Object result = runProgram("(gamma 2 1)");
		assertInstanceOf(Gamma.class, result);
	}

	@Test
	public void testExponentialConstructor() {
		Object result = runProgram("(exponential 2)");
		assertInstanceOf(Exponential.class, result);
		assertEquals(2.0, ( (Exponential) result ).rate(), 1e-12);
	}

	@Test
	public void testUniformConstructor() {
		Object result = runProgram("(uniform 0 10)");
		assertInstanceOf(UniformContinuous.class, result);
		assertEquals(0.0, ( (UniformContinuous) result ).min(), 1e-12);
		assertEquals(10.0, ( (UniformContinuous) result ).max(), 1e-12);
	}
}
