package core;

import ast.Expression;
import inference.InferenceEngine;
import inference.LikelihoodWeighting;
import inference.SSMetropolisHastings;
import inference.SequentialMonteCarlo;

import java.util.List;
import java.util.Random;

public class Main {

	public static void main(String[] args) {
		runExample("EJEMPLO 1: conj", "Exact mean: 1.150, exact stddev: 0.707", TestProgram.Ejemplo1());
		runExample("EJEMPLO 2: bits", "Exact mean: 5.014, exact stddev: 1.146", TestProgram.Ejemplo2());
		runExample("EJEMPLO 3: multi-normal", "Exact mean: 1.333, exact stddev: 0.577", TestProgram.Ejemplo3());
		runExample("EJEMPLO 4: normal-prior", "Exact mean: 2.231, exact stddev: 1.664", TestProgram.Ejemplo4());
		runExample("EJEMPLO 5: coin-flips", "Exact mean: 0.506, exact stddev: 0.500", TestProgram.Ejemplo5());
		runExample("EJEMPLO 6: signal-noise", "Exact mean: 0.599, exact stddev: 0.895", TestProgram.Ejemplo6());
		runExample("EJEMPLO 7: binomial-test", "Exact mean: 5.014, exact stddev: 1.146", TestProgram.Ejemplo7());
		runExample("EJEMPLO 8: exponential", "Exact mean: 0.667, exact stddev: 0.471", TestProgram.Ejemplo8());
		runExample("EJEMPLO 9: uniform", "Exact mean: 4.500, exact stddev: 1.000", TestProgram.Ejemplo9());
		runExample("EJEMPLO 10: beta", "Exact mean: 0.600, exact stddev: 0.200", TestProgram.Ejemplo10());
		runExample("EJEMPLO 11: gamma", "Exact mean: 1.200, exact stddev: 0.693", TestProgram.Ejemplo11());

		System.out.println("\n(Tip: Run './gradlew benchmark' to execute the performance benchmarks)");
	}

	private static void runExample(String name, String exactResults, List<Expression> program) {
		System.out.println("\n====== " + name + " ====== \n");
		System.out.println(exactResults);
		executeSamplingMethods(program);
	}

	private static void executeSamplingMethods(List<Expression> program) {
		Random random = new Random(5);

		// Likelihood Weighting (LW)
		LikelihoodWeighting lw = new LikelihoodWeighting(program, random, 1000000);
		List<Double> lwSamples = lw.run();
		List<Double> lwWeights = lw.getWeights();
		double lwMean = LikelihoodWeighting.calculateWeightedMean(lwSamples, lwWeights);
		double lwStdDev = calculateWeightedStddev(lwMean, lwSamples, lwWeights);

		System.out.println("LW mean: " + lwMean + " (stddev: " + lwStdDev + ")");

		// Single-Site Metropolis-Hastings (SSMH)
		InferenceEngine ssmh = new SSMetropolisHastings(program, random, 3000, 1000000);
		List<Double> ssmhSamples = ssmh.run();
		double ssmhMean = calculateAverage(ssmhSamples);
		double ssmhStdDev = calculateStddev(ssmhMean, ssmhSamples);

		System.out.println("SSMH mean: " + ssmhMean + " (stddev: " + ssmhStdDev + ")");

		// Sequential Monte Carlo (SMC)
		InferenceEngine smc = new SequentialMonteCarlo(program, random, 20000);
		List<Double> smcSamples = smc.run();
		double smcMean = calculateAverage(smcSamples);
		double smcStdDev = calculateStddev(smcMean, smcSamples);

		System.out.println("SMC mean: " + smcMean + " (stddev: " + smcStdDev + ")");
	}

	private static double calculateWeightedStddev(double mean,
			List<Double> samples,
			List<Double> weights) {
		double sum = 0.0;
		for (int i = 0; i < samples.size(); i++) {
			sum += weights.get(i) * Math.pow(samples.get(i) - mean, 2);
		}
		return Math.sqrt(sum);
	}

	private static double calculateAverage(List<Double> samples) {
		return samples.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
	}

	private static double calculateStddev(double mean, List<Double> samples) {
		if (samples.size() <= 1) {
			return 0.0;
		}
		double variance = samples.stream().mapToDouble(x -> Math.pow(x - mean, 2)).sum() /
						  ( samples.size() - 1 );
		return Math.sqrt(variance);
	}
}