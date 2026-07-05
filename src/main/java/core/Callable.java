package core;

import java.util.List;

public interface Callable {
	void apply(Machine machine, List<Object> args, Address address);
}