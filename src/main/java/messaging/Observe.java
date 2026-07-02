package messaging;

import core.Address;
import core.Machine;
import inference.InferenceEngine;

public record Observe(
		Address address,
		distributions.Distribution distribution,
		Object value,
		Machine machine) implements Message {

	@Override
	public void processMessage(InferenceEngine inferenceEngine) {
		inferenceEngine.processObserve(address, distribution, value, machine);
	}
}
