package core;

import ast.Expression;
import inference.InferenceEngine;
import inference.LikelihoodWeighting;
import inference.SSMetropolisHastings;
import inference.SequentialMonteCarlo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

	static void main() {
		System.out.println("\n====== EJEMPLO 1: conj ====== \n");
		System.out.println("Exact mean: 1.150, exact stddev: 0.707");
		List<Expression> ejemplo1 = TestProgram.Ejemplo1();
		executeSamplingMethods(ejemplo1);

		System.out.println("\n====== EJEMPLO 2: bits ====== \n");
		System.out.println("Exact mean: 5.014");
		List<Expression> ejemplo2 = TestProgram.Ejemplo2();
		executeSamplingMethods(ejemplo2);

		System.out.println("\n====== EJEMPLO 3: multi-normal ====== \n");
		System.out.println("Exact mean: 1.333");
		List<Expression> ejemplo3 = TestProgram.Ejemplo3();
		executeSamplingMethods(ejemplo3);

		System.out.println("\n====== EJEMPLO 4: normal-prior ====== \n");
		System.out.println("Exact mean: 2.231");
		List<Expression> ejemplo4 = TestProgram.Ejemplo4();
		executeSamplingMethods(ejemplo4);

		System.out.println("\n====== EJEMPLO 5: coin-flips ====== \n");
		System.out.println("Exact mean: 0.506");
		List<Expression> ejemplo5 = TestProgram.Ejemplo5();
		executeSamplingMethods(ejemplo5);

		System.out.println("\n====== EJEMPLO 6: signal-noise ====== \n");
		System.out.println("Exact mean: 0.599");
		List<Expression> ejemplo6 = TestProgram.Ejemplo6();
		executeSamplingMethods(ejemplo6);

		System.out.println(
				"\n(Tip: Run './gradlew benchmark' to execute the performance benchmarks)");
	}

	private static void executeSamplingMethods(List<Expression> program) {
		Random random = new Random(5);

		LikelihoodWeighting lw = new LikelihoodWeighting(program, random, 1000000);

		ArrayList<Double> lwSamples = lw.run();
		ArrayList<Double> lwWeights = lw.getWeights();
		double lwMean = calculateWeightedMean(lwSamples, lwWeights);
		double lwStdDev = calculateWeightedStddev(lwMean, lwSamples, lwWeights);

		System.out.println("LW mean: " + lwMean + " (stddev: " + lwStdDev + ")");

		InferenceEngine ssmh = new SSMetropolisHastings(program, random, 3000, 1000000);
		ArrayList<Double> ssmhSamples = ssmh.run();
		double ssmhMean = calculateAverage(ssmhSamples);
		double ssmhStdDev = calculateStddev(ssmhMean, ssmhSamples);

		System.out.println("SSMH mean: " + ssmhMean + " (stddev: " + ssmhStdDev + ")");

		InferenceEngine smc = new SequentialMonteCarlo(program, random, 20000);
		ArrayList<Double> smcSamples = smc.run();
		double smcMean = calculateAverage(smcSamples);
		double smcStdDev = calculateStddev(smcMean, smcSamples);

		System.out.println("SMC mean: " + smcMean + " (stddev: " + smcStdDev + ")");
	}

	private static double calculateWeightedMean(ArrayList<Double> samples,
			ArrayList<Double> weights) {
		double sum = 0.0;
		for (int i = 0; i < samples.size(); i++) {
			sum += samples.get(i) * weights.get(i);
		}
		return sum;
	}

	private static double calculateWeightedStddev(double aMean,
			ArrayList<Double> samples,
			ArrayList<Double> weights) {
		double sum = 0.0;
		for (int i = 0; i < samples.size(); i++) {
			sum += weights.get(i) * Math.pow(samples.get(i) - aMean, 2);
		}
		return Math.sqrt(sum);
	}

	private static double calculateAverage(ArrayList<Double> ssmhResults) {
		return ssmhResults.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
	}

	private static double calculateStddev(double aMean, ArrayList<Double> samples) {
		return Math.sqrt(samples.stream().mapToDouble(x -> Math.pow(x - aMean, 2)).sum() /
						 ( samples.size() - 1 ));
	}
}