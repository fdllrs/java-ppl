package ast;

import core.Environment;

public class SymbolExpression implements Expression{
	public String name;

	@Override
	public double evaluate(Environment env) {
		return 0;
	}

	public static class Constant { }
}
