package instructions;

import core.Address;
import core.Environment;
import core.Machine;

public abstract class Instruction {

	Environment environment;
	Address address;

	public abstract void executedBy(Machine machine);

	public Environment getEnvironment() { return environment; }

	public Address getAddress() { return address; }
}
