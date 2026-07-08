package distributions;

import java.util.Random;

public record Binomial(int n, double p) implements Distribution {

	public Binomial {
		Distribution.assertNonNegative(n);
		Distribution.assertProbability("binomial", p);
	}

	@Override
	public Object sample(Random rng) {
		int successes = 0;
		for (int i = 0; i < n; i++) {
			if (rng.nextDouble() < p) {
				successes++;
			}
		}
		return successes;
	}

	@Override
	public double logProb(Object x) {
		Distribution.assertIsNumber(x);
		double val = ( (Number) x ).doubleValue();

		if (val != Math.round(val)) {
			return Double.NEGATIVE_INFINITY;
		}

		int k = (int) Math.round(val);
		if (k < 0 || k > n) {
			return Double.NEGATIVE_INFINITY;
		}

		if (p == 0.0) {
			return k == 0 ? 0.0 : Double.NEGATIVE_INFINITY;
		}
		if (p == 1.0) {
			return k == n ? 0.0 : Double.NEGATIVE_INFINITY;
		}

		double logCoeff = logBinomialCoeff(n, k);
		return logCoeff + k * Math.log(p) + ( n - k ) * Math.log(1.0 - p);
	}

	private static double logBinomialCoeff(int n, int k) {
		return Gamma.logGamma(n + 1) - Gamma.logGamma(k + 1) - Gamma.logGamma(n - k + 1);
	}

	@Override
	public String toString() {
		return String.format("(binomial %d %f)", n, p);
	}
}
