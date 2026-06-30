package Instructions;

import core.Environment;
import core.Machine;

import java.util.Map;

public abstract class Instruction {

	Environment environment = null;
	Map<String, Integer> address;

	public abstract void executedBy(Machine machine);

	public Environment getEnvironment() { return environment; }
}
