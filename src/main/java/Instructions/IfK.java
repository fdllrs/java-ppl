package Instructions;

import core.Machine;

public class IfK implements Instruction {
	@Override
	public void executedBy(Machine machine) {
		machine.executeIfK(this);
	}
}
