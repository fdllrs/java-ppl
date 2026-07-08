package distributions;

import java.util.Random;

public record Bernoulli(double p) implements Distribution {
	public Bernoulli {
		Distribution.assertProbability("bernoulli", p);
	}

	@Override
	public Object sample(Random rng) {
		return rng.nextDouble() < p;
	}

	@Override
	public double logProb(Object x) {
		boolean val = convertToBoolean(x);
		if (val) {
			return p > 0 ? Math.log(p) : Double.NEGATIVE_INFINITY;
		}
		else {
			return p < 1 ? Math.log(1.0 - p) : Double.NEGATIVE_INFINITY;
		}
	}

	private static boolean convertToBoolean(Object x) {
		boolean val;
		if (x instanceof Boolean) {
			val = (Boolean) x;
		}
		else if (x instanceof Number) {
			val = ( (Number) x ).doubleValue() != 0.0;
		}
		else {
			throw new IllegalArgumentException(
					"bernoulli logProb expects boolean or number, got: " + x);
		}
		return val;
	}

	@Override
	public String toString() {
		return String.format("(bernoulli %f)", p);
	}
}
