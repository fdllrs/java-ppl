package Instructions;

import core.Machine;

public class SampleK extends Instruction {
	@Override
	public void executedBy(Machine machine) {
		machine.executeSampleK(this);
	}
}
