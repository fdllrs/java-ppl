package distributions;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class UniformContinuousTest {

	private final Random rng = new Random(42);

	@Test
	public void testUniformContinuousSamplingType() {
		UniformContinuous uniform = new UniformContinuous(1.0, 5.0);
		assertInstanceOf(Double.class, uniform.sample(rng));
	}

	@Test
	public void testUniformContinuousSamplingBoundaries() {
		UniformContinuous uniform = new UniformContinuous(1.0, 5.0);
		double sample = (Double) uniform.sample(rng);
		assertTrue(sample >= 1.0 && sample <= 5.0);
	}

	@Test
	public void testUniformContinuousLogProb() {
		UniformContinuous uniform = new UniformContinuous(1.0, 5.0);
		assertEquals(Math.log(0.25), uniform.logProb(3.0), 1e-6);
	}

	@Test
	public void testUniformContinuousLogProbOutOfBounds() {
		UniformContinuous uniform = new UniformContinuous(1.0, 5.0);
		assertEquals(Double.NEGATIVE_INFINITY, uniform.logProb(0.0));
	}

	@Test
	public void testUniformContinuousThrowsOnMinGreaterThanMax() {
		assertThrows(IllegalArgumentException.class, () -> new UniformContinuous(5.0, 1.0));
	}
}
