package instructions;

import core.Address;
import core.Machine;

public class ObserveK extends Instruction {

	public ObserveK(Address address) {
		this.address = address;
	}

	@Override
	public void executedBy(Machine machine) {
		machine.executeObserveK(this);
	}
}
