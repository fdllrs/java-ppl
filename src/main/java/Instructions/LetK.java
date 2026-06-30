package Instructions;

import ast.Expression;
import core.Machine;

import java.util.List;

public class LetK implements Instruction {
	List<Expression> expressions;
	int index;
	
	public LetK(int index, List<Expression> expressions) {
		this.index = index;
		this.expressions = expressions;
	}

	@Override
	public void executedBy(Machine machine) {
		machine.executeLetK(this);
	}
}
