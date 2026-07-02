package messaging;

import core.Address;
import core.Machine;

public record Observe(
		Address address, Object distribution, Object value, Machine machine) implements Message {

}
