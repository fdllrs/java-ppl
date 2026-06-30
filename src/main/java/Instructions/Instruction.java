package Instructions;

import core.AddressTag;
import core.Environment;
import core.Machine;

import java.util.Map;

public abstract class Instruction {

	Environment environment = null;
	Map<AddressTag, Integer> addresses;

	public abstract void executedBy(Machine machine);

	public Environment getEnvironment() { return environment; }

	public Map<AddressTag, Integer> getAddresses() { return addresses; }
}
