package distributions;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public record Poisson(double lambda) implements Distribution {

	public Poisson {
		Distribution.assertPositive("poisson", lambda);
	}

	@Override
	public Object sample(Random rng) {
		if (lambda < 30.0) {
			// Knuth's algorithm
			double L = Math.exp(-lambda);
			int k = 0;
			double p = 1.0;
			do {
				k++;
				p *= rng.nextDouble();
			} while (p > L);
			return k - 1;
		}
		else {
			// Atkinson's rejection algorithm for large lambda
			double c = 0.767 - 3.36 / lambda;
			double beta = Math.PI / Math.sqrt(3.0 * lambda);
			double alpha = beta * lambda;
			double kConst = Math.log(c) - lambda - Math.log(beta);

			while (true) {
				double u = rng.nextDouble();
				double x = ( alpha - Math.log(( 1.0 - u ) / u) ) / beta;
				int n = (int) Math.floor(x + 0.5);
				if (n < 0) {
					continue;
				}

				double v = rng.nextDouble();
				double y = alpha - beta * x;
				double lhs = y + Math.log(v) - 2.0 * log1PlusExp(y);
				double rhs = kConst + n * Math.log(lambda) - Gamma.logGamma(n + 1);

				if (lhs <= rhs) {
					return n;
				}
			}
		}
	}

	private static double log1PlusExp(double y) {
		if (y > 0) {
			return y + Math.log1p(Math.exp(-y));
		}
		else {
			return Math.log1p(Math.exp(y));
		}
	}

	@Override
	public double logProb(Object x) {
		Distribution.assertIsNumber(x);
		double val = ( (Number) x ).doubleValue();

		if (val != Math.round(val)) {
			return Double.NEGATIVE_INFINITY;
		}

		int k = (int) Math.round(val);
		if (k < 0) {
			return Double.NEGATIVE_INFINITY;
		}

		// log P(X = k) = k * log(lambda) - lambda - logGamma(k + 1)
		return k * Math.log(lambda) - lambda - Gamma.logGamma(k + 1);
	}

	@NotNull
	@Override
	public String toString() {
		return String.format("(poisson %f)", lambda);
	}
}
