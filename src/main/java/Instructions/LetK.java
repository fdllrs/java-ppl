package Instructions;

import ast.Expression;
import core.Address;
import core.Environment;
import core.Machine;

import java.util.List;

public class LetK extends Instruction {
	int index;
	List<Binding> bindings;
	List<Expression> body;

	public LetK(List<Binding> binds,
			int index,
			List<Expression> body,
			Environment env,
			Address address) {
		this.bindings = binds;
		this.index = index;
		this.body = body;
		this.address = address;
		this.environment = env;
	}

	@Override
	public void executedBy(Machine machine) {
		machine.executeLetK(this);
	}

	public List<Binding> getBindings() { return bindings; }

	public int getIndex() { return index; }

	public List<Expression> getBody() { return body; }

	public record Binding(String variableName, Expression valueExpression) { }
}
