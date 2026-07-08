package distributions;

import java.util.Random;

public record Gamma(double shape, double scale) implements Distribution {

	public Gamma {
		Distribution.assertPositive("gamma", shape);
		Distribution.assertPositive("gamma", scale);
	}

	@Override
	public Object sample(Random rng) {
		if (shape < 1.0) {
			double y = sampleGamma(rng, shape + 1.0, scale);
			return y * Math.pow(rng.nextDouble(), 1.0 / shape);
		}
		else {
			return sampleGamma(rng, shape, scale);
		}
	}

	private double sampleGamma(Random rng, double k, double theta) {
		double d = k - 1.0 / 3.0;
		double c = 1.0 / Math.sqrt(9.0 * d);

		while (true) {
			double z = rng.nextGaussian();
			double v = 1.0 + c * z;

			if (v <= 0.0) continue;

			v = v * v * v;
			double u = rng.nextDouble();

			if (u < 1.0 - 0.0331 * z * z * z * z) {
				return d * v * theta;
			}

			if (Math.log(u) < 0.5 * z * z + d * ( 1.0 - v + Math.log(v) )) {
				return d * v * theta;
			}
		}
	}

	@Override
	public double logProb(Object x) {
		Distribution.assertIsNumber(x);

		double val = ( (Number) x ).doubleValue();

		if (val <= 0) {
			return Double.NEGATIVE_INFINITY;
		}

		// ln f(x; k, theta) = (k - 1) ln x - x / theta - k ln theta - ln Gamma(k)
		double term1 = -shape * Math.log(scale);
		double term2 = -logGamma(shape);
		double term3 = ( shape - 1.0 ) * Math.log(val);
		double term4 = -val / scale;

		return term1 + term2 + term3 + term4;
	}

	/**
	 * Computes log(Gamma(x)) using Lanczos approximation (Numerical Recipes).
	 */
	static double logGamma(double x) {
		double tmp = ( x - 0.5 ) * Math.log(x + 4.5) - ( x + 4.5 );
		double ser =
				1.0 + 76.18009173 / ( x + 0 ) - 86.50532033 / ( x + 1 ) + 24.01409822 / ( x + 2 ) -
				1.231739516 / ( x + 3 ) + 0.00120858003 / ( x + 4 ) - 0.00000536382 / ( x + 5 );
		return tmp + Math.log(ser * Math.sqrt(2.0 * Math.PI));
	}

	@Override
	public String toString() {
		return String.format("(gamma %f %f)", shape, scale);
	}
}
