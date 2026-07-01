package ast;

import core.Address;
import core.Environment;
import core.Machine;

public interface Expression {

	void evaluate(Environment env, Address address, Machine machine);
}
