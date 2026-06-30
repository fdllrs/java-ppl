package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record Address(List<Object> path) {

	public Address() {
		this(List.of());
	}

	public Address append(Object... elements) {
		List<Object> newPath = new ArrayList<>(this.path);
		Collections.addAll(newPath, elements);
		return new Address(Collections.unmodifiableList(newPath));
	}
}