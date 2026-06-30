package Instructions;

import core.Environment;
import core.Machine;

public interface Instruction {

	Environment environment = null;
	float address = Float.NaN;

	void executedBy(Machine machine);
}
