package messaging;

import core.Address;
import distributions.Distribution;

public record Sample(Address address, Distribution distribution) implements Message { }
