package core;

import ast.Expression;
import inference.LikelihoodWeighting;
import inference.SSMetropolisHastings;
import inference.SequentialMonteCarlo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InferenceTest {

	@Test
	public void testLikelihoodWeightingConjugateModel() {
		List<Expression> program = TestProgram.normalNormalConjugate();
		Random random = new Random(42);

		LikelihoodWeighting lw = new LikelihoodWeighting(program, random, 50000);
		List<Double> samples = lw.run();
		List<Double> weights = lw.getWeights();

		double weightedMean = LikelihoodWeighting.calculateWeightedMean(samples, weights);

		// The exact mean is 1.15
		assertEquals(1.15, weightedMean, 0.15);
	}

	@Test
	public void testSSMetropolisHastingsConjugateModel() {
		List<Expression> program = TestProgram.normalNormalConjugate();
		Random random = new Random(42);

		SSMetropolisHastings ssmh = new SSMetropolisHastings(program, random, 1000, 10000);
		List<Double> samples = ssmh.run();

		double mean = samples.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

		// The exact mean is 1.15
		assertEquals(1.15, mean, 0.15);
	}

	@Test
	public void testSequentialMonteCarloConjugateModel() {
		List<Expression> program = TestProgram.normalNormalConjugate();
		Random random = new Random(42);

		SequentialMonteCarlo smc = new SequentialMonteCarlo(program, random, 5000);
		List<Double> samples = smc.run();

		double mean = samples.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

		// The exact mean is 1.15
		assertEquals(1.15, mean, 0.15);
	}
}
