package Instructions;

import core.Machine;

public class IfK extends Instruction {
	@Override
	public void executedBy(Machine machine) {
		machine.executeIfK(this);
	}
}
