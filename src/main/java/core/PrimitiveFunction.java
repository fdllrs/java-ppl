package core;

import java.util.List;

public record PrimitiveFunction(String name) implements Callable {

	@Override
	public void apply(Machine machine, List<Object> args, Address address) {

		machine.applyPrimitive(args, this.name);
	}
}
