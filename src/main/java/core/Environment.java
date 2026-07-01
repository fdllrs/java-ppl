package core;

import java.util.HashMap;
import java.util.Map;

public class Environment {

	public Map<String, Object> variables = new HashMap<>();

	public Environment() {

	}

	public boolean contains(String name) {
		return variables.containsKey(name);
	}

	public void add(String e, Object value) {
		variables.put(e, value);
	}

	public Object lookup(String name) {
		return variables.get(name);
	}
}
