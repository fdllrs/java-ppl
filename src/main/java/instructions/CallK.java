package instructions;

import core.Address;
import core.Machine;

public class CallK extends Instruction {

	private final int paramAmount;

	public CallK(int paramAmount, Address address) {
		this.paramAmount = paramAmount;
		this.address = address;
	}

	@Override
	public void executedBy(Machine machine) {
		machine.executeCallK(this);
	}

	public int getParamAmount() { return paramAmount; }
}
