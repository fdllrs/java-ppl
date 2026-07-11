package core;

import ast.Expression;
import inference.LikelihoodWeighting;
import inference.Posterior;
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

		LikelihoodWeighting<Double> lw = new LikelihoodWeighting<>(program, random, 50000);
		Posterior<Double> posterior = lw.run();

		// The exact mean is 1.15
		assertEquals(1.15, posterior.mean(), 0.15);
	}

	@Test
	public void testSSMetropolisHastingsConjugateModel() {
		List<Expression> program = TestProgram.normalNormalConjugate();
		Random random = new Random(42);

		SSMetropolisHastings<Double> ssmh = new SSMetropolisHastings<>(program,
																	   random,
																	   1000,
																	   10000);
		Posterior<Double> posterior = ssmh.run();

		// The exact mean is 1.15
		assertEquals(1.15, posterior.mean(), 0.15);
	}

	@Test
	public void testSequentialMonteCarloConjugateModel() {
		List<Expression> program = TestProgram.normalNormalConjugate();
		Random random = new Random(42);

		SequentialMonteCarlo<Double> smc = new SequentialMonteCarlo<>(program, random, 5000);
		Posterior<Double> posterior = smc.run();

		// The exact mean is 1.15
		assertEquals(1.15, posterior.mean(), 0.15);
	}
}
