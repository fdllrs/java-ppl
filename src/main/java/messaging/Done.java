package messaging;

import core.Machine;
import inference.InferenceEngine;

public class Done implements Message {
	Object returnValue;
	Machine machine;

	public Done(Object returnValue, Machine machine) {
		this.returnValue = returnValue;
		this.machine = machine;
	}

	@Override
	public void processMessage(InferenceEngine inferenceEngine) {
		inferenceEngine.processDone(returnValue, machine);
	}
}
