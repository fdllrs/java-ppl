package Instructions;

import ast.Expression;
import core.Environment;
import core.Machine;

import java.util.List;
import java.util.Map;

public class LetK extends Instruction {
	List<Expression> expressions;
	int index;
	List<Object> binds;
	Object body;

	public LetK(List<Object> binds,
			int index,
			Object body,
			Environment env,
			Map<String, Integer> address) {
		this.binds = binds;
		this.index = index;
		this.body = body;
		this.address = address;
		this.environment = env;
	}

	public LetK(int index, List<Expression> expressions) {
		this.index = index;
		this.expressions = expressions;
	}

	@Override
	public void executedBy(Machine machine) {
		machine.executeLetK(this);
	}

	public List<Object> getBinds() { return binds; }

	public int getIndex() { return index; }

	public List<Expression> getExpressions() { return expressions; }

	public Object getBody() { return body; }

	public Map<String, Integer> getAddress() { return address; }
}
