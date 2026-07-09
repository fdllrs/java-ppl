package core;

import Instructions.EvaluateK;
import Instructions.Instruction;
import ast.Expression;
import ast.ValueExpression;
import distributions.Normal;
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
}
