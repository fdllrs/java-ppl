package Instructions;

import core.Machine;

public class SampleK implements Instruction {
	@Override
	public void executedBy(Machine machine) {
		machine.executeSampleK(this);
	}
}
