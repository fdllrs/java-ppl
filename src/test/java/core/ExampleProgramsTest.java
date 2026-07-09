package core;

import ast.Expression;
import inference.LikelihoodWeighting;
import inference.SSMetropolisHastings;
import inference.SequentialMonteCarlo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExampleProgramsTest {

	private static final int LW_ITERATIONS = 50000;
	private static final int SSMH_WARMUP = 1000;
	private static final int SSMH_ITERATIONS = 20000;
	private static final int SMC_PARTICLES = 5000;
	private static final double TOLERANCE = 0.15;

	@Test
	public void testNormalNormalConjugate() {
		verifyExample(TestProgram.normalNormalConjugate(), 1.150);
	}

	private void verifyExample(List<Expression> program, double exactMean) {
		verifyExample(program, exactMean, TOLERANCE);
	}

	private void verifyExample(List<Expression> program, double exactMean, double tolerance) {
		Random random = new Random(42);

		// 1. Likelihood Weighting
		LikelihoodWeighting lw = new LikelihoodWeighting(program,
														 new Random(random.nextLong()),
														 LW_ITERATIONS);
		List<Double> lwSamples = lw.run();
		double lwMean = LikelihoodWeighting.calculateWeightedMean(lwSamples, lw.getWeights());
		assertEquals(exactMean, lwMean, tolerance, "LW failed to converge");

		// 2. Single-Site Metropolis-Hastings
		SSMetropolisHastings ssmh = new SSMetropolisHastings(program,
															 new Random(random.nextLong()),
															 SSMH_WARMUP,
															 SSMH_ITERATIONS);
		List<Double> ssmhSamples = ssmh.run();
		double ssmhMean = ssmhSamples.stream()
									 .mapToDouble(Double::doubleValue)
									 .average()
									 .orElse(0.0);
		assertEquals(exactMean, ssmhMean, tolerance, "SSMH failed to converge");

		// 3. Sequential Monte Carlo
		SequentialMonteCarlo smc = new SequentialMonteCarlo(program,
															new Random(random.nextLong()),
															SMC_PARTICLES);
		List<Double> smcSamples = smc.run();
		double smcMean =
				smcSamples.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		assertEquals(exactMean, smcMean, tolerance, "SMC failed to converge");
	}

	@Test
	public void testNoisyBernoulliSum() {
		verifyExample(TestProgram.noisyBernoulliSum(), 5.014);
	}

	@Test
	public void testMultiObsNormalNormal() {
		verifyExample(TestProgram.multiObsNormalNormal(), 1.333);
	}

	@Test
	public void testHighVarianceNormalPrior() {
		verifyExample(TestProgram.highVarianceNormalPrior(), 2.231);
	}

	@Test
	public void testCoinFlipSelection() {
		verifyExample(TestProgram.coinFlipSelection(), 0.506);
	}

	@Test
	public void testSignalNoiseSum() {
		verifyExample(TestProgram.signalNoiseSum(), 0.599, 0.25);
	}

	@Test
	public void testNoisyBinomial() {
		verifyExample(TestProgram.noisyBinomial(), 5.014);
	}

	@Test
	public void testExponentialExponentialConjugate() {
		verifyExample(TestProgram.exponentialExponentialConjugate(), 0.667);
	}

	@Test
	public void testUniformNormal() {
		verifyExample(TestProgram.uniformNormal(), 4.500);
	}

	@Test
	public void testBetaBernoulliConjugate() {
		verifyExample(TestProgram.betaBernoulliConjugate(), 0.600);
	}

	@Test
	public void testGammaExponentialConjugate() {
		verifyExample(TestProgram.gammaExponentialConjugate(), 1.200);
	}
}
