package core;

import ast.Expression;
import ast.ValueExpression;
import distributions.Normal;
import instructions.EvaluateK;
import instructions.Instruction;
import messaging.Done;
import messaging.Message;
import messaging.Observe;
import messaging.Sample;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MachineTest {

	@Test
	public void testMachineInitializationAndDone() {
		Random rng = new Random(42);
		Environment env = new Environment();
		List<Expression> program = Parser.parse("42");

		Deque<Instruction> controlStack = new ArrayDeque<>();
		controlStack.push(new EvaluateK(program.getFirst(), env, new Address()));

		Machine machine = new Machine(controlStack, env, rng);
		Message msg = machine.resume();

		assertInstanceOf(Done.class, msg);
		assertEquals(42L, ( (Done) msg ).returnValue());
	}

	@Test
	public void testMachineLetExpressionExecution() {
		Random rng = new Random(42);
		Environment env = new Environment();
		List<Expression> program = Parser.parse("(let [x 10] (+ x 5))");

		Deque<Instruction> controlStack = new ArrayDeque<>();
		controlStack.push(new EvaluateK(program.getFirst(), env, new Address()));

		Machine machine = new Machine(controlStack, env, rng);
		Message msg = machine.resume();

		assertInstanceOf(Done.class, msg);
		assertEquals(15L, ( (Done) msg ).returnValue());
	}

	@Test
	public void testMachineFork() {
		Random rng = new Random(42);
		Environment env = new Environment();
		env.add("a", 1);

		Deque<Instruction> controlStack = new ArrayDeque<>();
		controlStack.push(new EvaluateK(new ValueExpression(10), env, new Address()));

		Machine parent = new Machine(controlStack, env, rng);
		parent.addToLogWeight(1.5);
		parent.pushResult(100);

		Machine child = parent.fork(new Random(43));

		// Verify state is copied
		assertEquals(parent.getLogWeight(), child.getLogWeight());
		assertEquals(1, child.lookupInEnvironment("a"));

		// Mutate the child and check that the parent is unaffected
		child.addToLogWeight(2.0);
		child.addToEnvironment("a", 2);
		child.addToEnvironment("b", 3);
		child.executeDiscard(); // pops 100 from value stack

		assertEquals(1.5, parent.getLogWeight());
		assertEquals(3.5, child.getLogWeight());

		assertEquals(1, parent.lookupInEnvironment("a"));
		assertEquals(2, child.lookupInEnvironment("a"));
		assertFalse(parent.environmentContains("b"));
		assertTrue(child.environmentContains("b"));
	}

	@Test
	public void testMachineSampleSuspends() {
		Random rng = new Random(42);
		Environment env = new Environment();
		List<Expression> program = Parser.parse("(sample (normal 0 1))");

		Deque<Instruction> controlStack = new ArrayDeque<>();
		controlStack.push(new EvaluateK(program.getFirst(), env, new Address()));

		Machine machine = new Machine(controlStack, env, rng);
		Message msg = machine.resume();

		assertInstanceOf(Sample.class, msg);
		Sample sampleMsg = (Sample) msg;
		assertInstanceOf(Normal.class, sampleMsg.distribution());
		assertEquals(0.0, ( (Normal) sampleMsg.distribution() ).mu());
	}

	@Test
	public void testMachineObserveSuspends() {
		Random rng = new Random(42);
		Environment env = new Environment();
		List<Expression> program = Parser.parse("(observe (normal 5 1) 5.5)");

		Deque<Instruction> controlStack = new ArrayDeque<>();
		controlStack.push(new EvaluateK(program.getFirst(), env, new Address()));

		Machine machine = new Machine(controlStack, env, rng);
		Message msg = machine.resume();

		assertInstanceOf(Observe.class, msg);
		Observe obsMsg = (Observe) msg;
		assertInstanceOf(Normal.class, obsMsg.distribution());
		assertEquals(5.5, obsMsg.value());
	}

	// ------------------------------------------------------------------ new fix coverage

	@Test
	public void testNonBooleanConditionThrows() {
		List<Expression> program = Parser.parse("(if 42 1 0)");
		Environment env = new Environment();
		Deque<Instruction> stack = new ArrayDeque<>();
		stack.push(new EvaluateK(program.getFirst(), env, new Address()));
		Machine machine = new Machine(stack, env, new Random(0));
		assertThrows(RuntimeException.class, machine::resume);
	}

	@Test
	public void testClosureArityMismatchThrows() {
		// (fn [x] x) called with two arguments
		List<Expression> program = Parser.parse("((fn [x] x) 1 2)");
		Environment env = new Environment();
		Deque<Instruction> stack = new ArrayDeque<>();
		stack.push(new EvaluateK(program.getFirst(), env, new Address()));
		Machine machine = new Machine(stack, env, new Random(0));
		assertThrows(RuntimeException.class, machine::resume);
	}

	@Test
	public void testUnboundVariableThrows() {
		List<Expression> program = Parser.parse("undefined-variable");
		Environment env = new Environment();
		Deque<Instruction> stack = new ArrayDeque<>();
		stack.push(new EvaluateK(program.getFirst(), env, new Address()));
		Machine machine = new Machine(stack, env, new Random(0));
		RuntimeException ex = assertThrows(RuntimeException.class, machine::resume);
		assertTrue(ex.getMessage().contains("undefined-variable"));
	}

	@Test
	public void testNonCallableThrows() {
		// (42 1) — 42 is not callable
		List<Expression> program = Parser.parse("(42 1)");
		Environment env = new Environment();
		Deque<Instruction> stack = new ArrayDeque<>();
		stack.push(new EvaluateK(program.getFirst(), env, new Address()));
		Machine machine = new Machine(stack, env, new Random(0));
		RuntimeException ex = assertThrows(RuntimeException.class, machine::resume);
		assertTrue(ex.getMessage().contains("not callable"));
	}

	// ------------------------------------------------------------------ language features

	@Test
	public void testUserDefinedFunctionCall() {
		List<Expression> program = Parser.parse("((fn [x] (* x 2)) 5)");
		Environment env = new Environment();
		Deque<Instruction> stack = new ArrayDeque<>();
		stack.push(new EvaluateK(program.getFirst(), env, new Address()));
		Machine machine = new Machine(stack, env, new Random(0));
		Message msg = machine.resume();
		assertInstanceOf(Done.class, msg);
		assertEquals(10.0, ((Number) ((Done) msg).returnValue()).doubleValue(), 1e-12);
	}

	@Test
	public void testDefnAndCall() {
		// A defn + invocation via InferenceEngine boilerplate
		List<Expression> program = Parser.parse("""
				(defn double [x] (* x 2))
				(double 7)
				""");
		inference.LikelihoodWeighting<Double> lw =
				new inference.LikelihoodWeighting<>(program, new Random(0), 1);
		inference.Posterior<Double> p = lw.run();
		assertEquals(14.0, p.mean(), 1e-9);
	}

	@Test
	public void testMultiExpressionBody() {
		// The first expression is discarded; result should be the second
		List<Expression> program = Parser.parse("(let [x 3] (+ x 0) (+ x 5))");
		Environment env = new Environment();
		Deque<Instruction> stack = new ArrayDeque<>();
		stack.push(new EvaluateK(program.getFirst(), env, new Address()));
		Machine machine = new Machine(stack, env, new Random(0));
		Message msg = machine.resume();
		assertInstanceOf(Done.class, msg);
		assertEquals(8.0, ((Number) ((Done) msg).returnValue()).doubleValue(), 1e-12);
	}

	@Test
	public void testNestedLet() {
		List<Expression> program = Parser.parse("(let [x 1] (let [y 2] (+ x y)))");
		Environment env = new Environment();
		Deque<Instruction> stack = new ArrayDeque<>();
		stack.push(new EvaluateK(program.getFirst(), env, new Address()));
		Machine machine = new Machine(stack, env, new Random(0));
		Message msg = machine.resume();
		assertInstanceOf(Done.class, msg);
		assertEquals(3L, ((Done) msg).returnValue());
	}

	@Test
	public void testRecursiveFunction() {
		// factorial via defn + recursion
		List<Expression> program = Parser.parse("""
				(defn fact [n] (if (== n 0) 1 (* n (fact (- n 1)))))
				(fact 5)
				""");
		inference.LikelihoodWeighting<Double> lw =
				new inference.LikelihoodWeighting<>(program, new Random(0), 1);
		inference.Posterior<Double> p = lw.run();
		assertEquals(120.0, p.mean(), 1e-9);
	}
}
