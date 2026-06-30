package Instructions;

import core.Machine;

public class CallK extends Instruction {
	@Override
	public void executedBy(Machine machine) {
		machine.executeCallK(this);
	}
}
