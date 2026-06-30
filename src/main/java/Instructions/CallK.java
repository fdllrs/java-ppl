package Instructions;

import core.Machine;

public class CallK implements Instruction {
	@Override
	public void executedBy(Machine machine) {
		machine.executeCallK(this);
	}
}
