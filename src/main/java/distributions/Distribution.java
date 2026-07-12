package distributions;

import java.util.Random;

public interface Distribution {
	static void assertIsNumber(Object x) {
		if (!( x instanceof Number )) {
			throw new IllegalArgumentException("Expecting a number, got: " + x);
		}
	}

	static void assertIsInRange(String name, double p) {
		if (p < 0.0 || p > 1.0) {
			throw new IllegalArgumentException(name + ": p must be in [0,1], got: " + p);
		}
	}

	static void assertPositive(String name, double val) {
		if (val <= 0.0) {
			throw new IllegalArgumentException(name + ": value must be > 0, got: " + val);
		}
	}

	static void assertNonNegative(double val) {
		if (val < 0.0) {
			throw new IllegalArgumentException("value must be >= 0, got: " + val);
		}
	}

	static void assertLessThan(String name, double val1, double val2) {
		if (val1 >= val2) {
			throw new IllegalArgumentException(
					name + ": first value must be less than second, got: " + val1 + " >= " + val2);
		}
	}

	Object sample(Random rng);
	double logProb(Object x);
}
