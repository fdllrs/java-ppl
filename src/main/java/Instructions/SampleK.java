package Instructions;

import core.Address;
import core.Machine;

public class SampleK extends Instruction {

	public SampleK(Address address) {
		this.address = address;
	}

	@Override
	public void executedBy(Machine machine) {
		machine.executeSampleK(this);
	}
}
