package core;

import java.util.HashMap;
import java.util.Map;

public class Environment {
	private final Map<String, Object> frame = new HashMap<>();
	private final Environment parent;

	public Environment() {
		parent = null;
	}

	public Environment(Environment environment) {
		parent = environment;
	}

	public boolean contains(String name) {
		return frame.containsKey(name) || ( parent != null && parent.contains(name) );
	}

	public void add(String name, Object value) { frame.put(name, value); }

	public Object lookup(String name) {
		if (frame.containsKey(name)) return frame.get(name);
		if (parent != null) return parent.lookup(name);

		throw new RuntimeException("Unbound variable: " + name);
	}
}

