package distributions;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public record Beta(double alpha, double beta) implements Distribution {

	public Beta {
		Distribution.assertPositive("beta", alpha);
		Distribution.assertPositive("beta", beta);
	}

	@Override
	public Object sample(Random rng) {
		double xSample = (double) new Gamma(alpha, 1.0).sample(rng);
		double ySample = (double) new Gamma(beta, 1.0).sample(rng);
		return xSample / ( xSample + ySample );
	}

	@Override
	public double logProb(Object x) {
		Distribution.assertIsNumber(x);
		double val = ( (Number) x ).doubleValue();
		if (val < 0.0 || val > 1.0) {
			return Double.NEGATIVE_INFINITY;
		}
		double term1 = ( alpha - 1.0 ) * Math.log(val);
		double term2 = ( beta - 1.0 ) * Math.log(1.0 - val);
		double term3 = logBeta(alpha, beta);
		return term1 + term2 - term3;
	}

	private static double logBeta(double alpha, double beta) {
		return Gamma.logGamma(alpha) + Gamma.logGamma(beta) - Gamma.logGamma(alpha + beta);
	}

	@NotNull
	@Override
	public String toString() {
		return String.format("(beta %f %f)", alpha, beta);
	}
}
