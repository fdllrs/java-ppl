package messaging;

import core.Address;
import core.Machine;
import distributions.Distribution;
import inference.InferenceEngine;

public record Sample(
		Address address, Distribution distribution, Machine machine) implements Message {

	@Override
	public void processMessage(InferenceEngine inferenceEngine) {
		inferenceEngine.processSample(address, distribution, machine);
	}
}
