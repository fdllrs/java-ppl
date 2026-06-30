package Instructions;

import core.Machine;

public class ObserveK implements Instruction {
	@Override
	public void executedBy(Machine machine) {
		machine.executeObserveK(this);
	}
}
