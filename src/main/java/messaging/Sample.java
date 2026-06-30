package messaging;

import core.Address;
import core.Machine;

public class Sample implements Message {
	private final Address address;
	private final Object distribution;
	private final Machine machine;

	public Sample(Address address, Object distribution, Machine machine) {
		this.address = address;
		this.distribution = distribution;
		this.machine = machine;
	}
}
