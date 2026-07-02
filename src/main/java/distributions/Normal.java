package distributions;

import java.util.Random;

public record Normal(double mu, double sigma) implements Distribution {
	private static final double LOG2PI = Math.log(2.0 * Math.PI);

	public Normal(double mu, double sigma) {
		this.mu = mu;
		this.sigma = sigma;
		if (sigma <= 0) {
			throw new IllegalArgumentException("normal: sigma must be > 0, got: " + sigma);
		}
	}

	@Override
	public Object sample(Random rng) {
		return mu + rng.nextGaussian() * sigma;
	}

	@Override
	public double logProb(Object x) {
		double val;
		if (x instanceof Number) {
			val = ( (Number) x ).doubleValue();
		}
		else {
			throw new IllegalArgumentException("normal logProb expects a number, got: " + x);
		}
		double z = ( val - mu ) / sigma;
		return -0.5 * ( LOG2PI + z * z ) - Math.log(sigma);
	}

	@Override
	public String toString() {
		return String.format("(normal %f %f)", mu, sigma);
	}
}
