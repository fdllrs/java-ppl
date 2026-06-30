package Instructions;

import core.Machine;

public class ObserveK extends Instruction {
	@Override
	public void executedBy(Machine machine) {
		machine.executeObserveK(this);
	}
}
