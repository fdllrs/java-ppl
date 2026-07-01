package ast;

import core.Address;
import core.Environment;
import core.Machine;

public interface Expression {

	void evaluate(Environment environment, Address address, Machine machine);
}
