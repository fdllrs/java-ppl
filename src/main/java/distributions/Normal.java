package distributions;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public record Normal(double mu, double sigma) implements Distribution {
	private static final double LOG2PI = Math.log(2.0 * Math.PI);

	public Normal {
		Distribution.assertPositive("normal", sigma);
	}

	@Override
	public Object sample(Random rng) {
		return mu + rng.nextGaussian() * sigma;
	}

	@Override
	public double logProb(Object x) {
		Distribution.assertIsNumber(x);

		double val = ( (Number) x ).doubleValue();

		double z = ( val - mu ) / sigma;
		return -0.5 * ( LOG2PI + z * z ) - Math.log(sigma);
	}

	@NotNull
	@Override
	public String toString() {
		return String.format("(normal %f %f)", mu, sigma);
	}
}
