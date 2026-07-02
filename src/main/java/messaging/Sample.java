package messaging;

import core.Address;
import core.Machine;

public record Sample(Address address, Object distribution, Machine machine) implements Message {

}
