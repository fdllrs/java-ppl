package ast;

import core.Environment;

public interface Expression {

	double evaluate(Environment env);

	boolean isPrimitive();
}
