package core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnvironmentTest {

	@Test
	public void testEmptyEnvironment() {
		Environment env = new Environment();
		assertFalse(env.contains("x"));
		assertNull(env.lookup("x"));
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

		// Modifying a child shouldn't affect a parent
		child.add("z", 30);
		child.add("x", 15); // shadowing/redefinition in child (simulated)

		assertTrue(child.contains("z"));
		assertFalse(parent.contains("z"));
		assertEquals(15, child.lookup("x"));
		assertEquals(10, parent.lookup("x"));
	}
}
