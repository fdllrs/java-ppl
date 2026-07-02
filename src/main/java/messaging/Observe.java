package messaging;

import core.Address;
import distributions.Distribution;

public record Observe(
		Address address,
		Distribution distribution,
		Object value) implements Message { }