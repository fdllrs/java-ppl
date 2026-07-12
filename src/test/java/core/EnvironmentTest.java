package core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnvironmentTest {

	@Test
	public void testEmptyEnvironment() {
		final String missingVariableName = "x";
		Environment env = new Environment();

		assertFalse(env.contains(missingVariableName));

		RuntimeException exception = assertThrows(RuntimeException.class,
												  () -> env.lookup(missingVariableName));

		assertEquals("Unbound variable: " + missingVariableName, exception.getMessage());
	}

	@Test
	public void testAddAndLookup() {
		Environment env = new Environment();
		env.add("x", 42);
		assertTrue(env.contains("x"));
		assertEquals(42, env.lookup("x"));
	}

	@Test
	public void testCopyConstructor() {
		Environment parent = new Environment();
		parent.add("x", 10);
		parent.add("y", 20);

		Environment child = new Environment(parent);
		assertTrue(child.contains("x"));
		assertTrue(child.contains("y"));
		assertEquals(10, child.lookup("x"));

		// Modifying the child shouldn't affect the parent
		child.add("z", 30);
		child.add("x", 15); // shadowing/redefinition in the child (simulated)

		assertTrue(child.contains("z"));
		assertFalse(parent.contains("z"));
		assertEquals(15, child.lookup("x"));
		assertEquals(10, parent.lookup("x"));
	}

	@Test
	public void testMultiLevelScoping() {
		Environment grandparent = new Environment();
		grandparent.add("a", 1);

		Environment parent = new Environment(grandparent);
		parent.add("b", 2);

		Environment child = new Environment(parent);
		child.add("c", 3);

		// the child can see all three levels
		assertEquals(1, child.lookup("a"));
		assertEquals(2, child.lookup("b"));
		assertEquals(3, child.lookup("c"));

		// the parent cannot see the child's frame
		assertFalse(parent.contains("c"));
		// grandparent cannot see parent's frame
		assertFalse(grandparent.contains("b"));
	}

	@Test
	public void testContainsTraversesParentChain() {
		Environment parent = new Environment();
		parent.add("x", 99);
		Environment child = new Environment(parent);

		// variable is in the parent, not in the child's own frame
		assertTrue(child.contains("x"));
		assertFalse(new Environment().contains("x"));
	}

	@Test
	public void testUnboundInChildChainThrows() {
		Environment parent = new Environment();
		parent.add("x", 1);
		Environment child = new Environment(parent);

		RuntimeException ex = assertThrows(RuntimeException.class, () -> child.lookup("z"));
		assertTrue(ex.getMessage().contains("z"));
	}
}
