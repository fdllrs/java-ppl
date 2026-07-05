package Instructions;

import ast.Expression;
import core.Address;
import core.Environment;
import core.Machine;

import java.util.List;

public class LetK extends Instruction {
	int index;
	List<Object> binds;
	List<Expression> body;

	public LetK(List<Object> binds,
			int index,
			List<Expression> body,
			Environment env,
			Address address) {
		this.binds = binds;
		this.index = index;
		this.body = body;
		this.address = address;
		this.environment = env;
	}

	@Override
	public void executedBy(Machine machine) {
		machine.executeLetK(this);
	}

	public List<Object> getBinds() { return binds; }

	public int getIndex() { return index; }

	public List<Expression> getBody() { return body; }

	public Object getBindAtIndex(int index) {
		return binds.get(index);
	}
}
