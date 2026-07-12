package core;

import ast.Expression;
import inference.InferenceEngine;
import inference.LikelihoodWeighting;
import inference.Posterior;
import inference.SSMetropolisHastings;
import inference.SequentialMonteCarlo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

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

	// ------------------------------------------------------------------ buildMainEvaluation guards

	@Test
	public void testMultipleTopLevelExpressionsThrows() {
		// Two non-defn expressions: the engine should reject this
		List<Expression> program = Parser.parse("42 43");
		LikelihoodWeighting<Double> lw = new LikelihoodWeighting<>(program, new Random(0), 1);
		assertThrows(RuntimeException.class, lw::run);
	}

	@Test
	public void testAllDefnNoMainThrows() {
		// A program with only defn forms and no main expression
		List<Expression> program = Parser.parse("(defn foo [x] x)");
		LikelihoodWeighting<Double> lw = new LikelihoodWeighting<>(program, new Random(0), 1);
		assertThrows(RuntimeException.class, lw::run);
	}

	@Test
	public void testEmptyProgramThrows() {
		List<Expression> program = Parser.parse("");
		LikelihoodWeighting<Double> lw = new LikelihoodWeighting<>(program, new Random(0), 1);
		assertThrows(RuntimeException.class, lw::run);
	}

	// ------------------------------------------------------------------ softmax guard

	@Test
	public void testSoftmaxThrowsOnAllNegativeInfinity() {
		ArrayList<Double> allNegInf = new ArrayList<>(List.of(
				Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY));
		assertThrows(IllegalStateException.class, () -> InferenceEngine.softmax(allNegInf));
	}

	@Test
	public void testSoftmaxNormalWeights() {
		ArrayList<Double> logWeights = new ArrayList<>(List.of(0.0, 0.0, 0.0));
		ArrayList<Double> probs = InferenceEngine.softmax(logWeights);
		for (double p : probs) {
			assertEquals(1.0 / 3.0, p, 1e-12);
		}
	}

	// ------------------------------------------------------------------ convergence stats

	@Test
	public void testLWStdDevConverges() {
		List<Expression> program = TestProgram.normalNormalConjugate();
		Posterior<Double> p = new LikelihoodWeighting<Double>(program, new Random(42), 50000).run();
		// exact stdDev = sqrt(0.5) ≈ 0.707
		assertEquals(0.707, p.stdDev(), 0.15);
	}

	@Test
	public void testLWEffectiveSampleSizePositive() {
		List<Expression> program = TestProgram.normalNormalConjugate();
		Posterior<Double> p = new LikelihoodWeighting<Double>(program, new Random(42), 10000).run();
		double ess = p.effectiveSampleSize();
		assertTrue(ess > 0, "ESS should be positive, got: " + ess);
		assertTrue(ess <= 10000, "ESS cannot exceed number of samples");
	}

	@Test
	public void testSSMHEssEqualsNSamples() {
		// SSMH uses ofUnweighted → uniform weights → ESS == N
		List<Expression> program = TestProgram.normalNormalConjugate();
		int iterations = 1000;
		Posterior<Double> p = new SSMetropolisHastings<Double>(program, new Random(42), 100, iterations).run();
		assertEquals(iterations, p.effectiveSampleSize(), 1.0);
	}

	@Test
	public void testSMCEssEqualsParticleCount() {
		// SMC uses ofUnweighted → uniform weights → ESS == particleCount
		List<Expression> program = TestProgram.normalNormalConjugate();
		int particles = 500;
		Posterior<Double> p = new SequentialMonteCarlo<Double>(program, new Random(42), particles).run();
		assertEquals(particles, p.effectiveSampleSize(), 1.0);
	}
}

