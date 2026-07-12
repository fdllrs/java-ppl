package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Address {
	private final Address parent;
	private final Object value;
	private final int hashCode;

	public Address() {
		this.parent = null;
		this.value = null;
		this.hashCode = 1;
	}

	private Address(Address parent, Object value) {
		this.parent = parent;
		this.value = value;
		// apparently standard...
		this.hashCode = 31 * parent.hashCode + ( value == null ? 0 : value.hashCode() );
	}

	public Address append(Object... elements) {
		Address curr = this;
		for (Object el : elements) {
			curr = new Address(curr, el);
		}
		return curr;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!( obj instanceof Address other )) return false;
		if (this.hashCode != other.hashCode) return false;

		Address currThis = this;
		Address currOther = other;
		while (currThis != null && currOther != null) {
			if (!Objects.equals(currThis.value, currOther.value)) return false;
			currThis = currThis.parent;
			currOther = currOther.parent;
		}
		return currThis == null && currOther == null;
	}

	@Override
	public String toString() {
		return path().toString();
	}

	public List<Object> path() {
		List<Object> list = new ArrayList<>();
		Address curr = this;
		while (curr.parent != null) {
			list.add(curr.value);
			curr = curr.parent;
		}
		Collections.reverse(list);
		return Collections.unmodifiableList(list);
	}
}