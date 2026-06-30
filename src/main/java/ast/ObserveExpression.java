package ast;

import core.Address;
import core.Environment;

public class ObserveExpression implements Expression {

	Address address;
	Object distribution;

	public ObserveExpression(Address address, Object distribution) {
		this.address = address;
		this.distribution = distribution;
	}

	@Override
	public double evaluate(Environment env) {
		return 0;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}
}
