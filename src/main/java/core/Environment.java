package core;

import java.util.HashMap;
import java.util.Map;

public class Environment {
	private final Map<String, Object> variables = new HashMap<>();

	public Environment() {

	}

	public Environment(Environment environment) {
		for (String name : environment.variables.keySet()) {
			variables.put(name, environment.variables.get(name));
			// habría que hacer deep copy si incluyéramos tipos de datos mutables, por ahora no
			// hace falta
		}
	}

	public boolean contains(String name) { return variables.containsKey(name); }

	public void add(String name, Object value) { variables.put(name, value); }

	public Object lookup(String name) { return variables.get(name); }
}

