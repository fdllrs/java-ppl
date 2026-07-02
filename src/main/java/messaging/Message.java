package messaging;

import inference.InferenceEngine;

public interface Message {

	void processMessage(InferenceEngine inferenceEngine);
}
