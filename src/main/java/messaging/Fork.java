package messaging;

import inference.InferenceEngine;

public class Fork implements Message {
	@Override
	public void processMessage(InferenceEngine inferenceEngine) {
		inferenceEngine.processFork();
	}
}
