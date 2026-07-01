package Instructions;

import core.Machine;

public class DiscardK extends Instruction {
	@Override
	public void executedBy(Machine machine) {
		machine.executeDiscard();
	}
}
