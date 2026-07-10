package core;

import ast.Expression;
import inference.InferenceEngine;
import inference.LikelihoodWeighting;
import inference.SSMetropolisHastings;
import inference.SequentialMonteCarlo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Benchmark {

	private static final int WARMUP_RUNS = 2;
	private static final int MEASUREMENT_RUNS = 5;

	static void main() {
		runBenchmarks();
	}

	public static void runBenchmarks() {
		System.out.println("==================================================");
		System.out.println("             PPL INFERENCE BENCHMARK              ");
		System.out.println("==================================================");
		System.out.println("Warmup runs per method:      " + WARMUP_RUNS);
		System.out.println("Measurement runs per method: " + MEASUREMENT_RUNS);
		System.out.println();

		benchmarkProblem("Ejemplo 1 (conj) - Exact Mean: 1.150",
						 TestProgram.normalNormalConjugate());
		benchmarkProblem("Ejemplo 2 (bits) - Exact Mean: 5.014", TestProgram.noisyBernoulliSum());
		benchmarkProblem("Ejemplo 3 (multi-normal) - Exact Mean: 1.333",
						 TestProgram.multiObsNormalNormal());
		benchmarkProblem("Ejemplo 4 (normal-prior) - Exact Mean: 2.231",
						 TestProgram.highVarianceNormalPrior());
		benchmarkProblem("Ejemplo 5 (coin-flips) - Exact Mean: 0.506",
						 TestProgram.coinFlipSelection());
		benchmarkProblem("Ejemplo 6 (signal-noise) - Exact Mean: 0.599",
						 TestProgram.signalNoiseSum());
	}

	private static void benchmarkProblem(String name, List<Expression> program) {
		System.out.println("--------------------------------------------------");
		System.out.println("Problem: " + name);
		System.out.println("--------------------------------------------------");

		BenchmarkConfig lwConfig = new BenchmarkConfig("Likelihood Weighting", () -> {
			Random random = new Random(5);
			return new LikelihoodWeighting<>(program, random, 1000000);
		}, 1000000, "iterations");

		BenchmarkConfig ssmhConfig = new BenchmarkConfig("SS Metropolis-Hastings", () -> {
			Random random = new Random(5);
			return new SSMetropolisHastings<>(program, random, 3000, 1000000);
		}, 1000000, "iterations");

		BenchmarkConfig smcConfig = new BenchmarkConfig("Sequential Monte Carlo", () -> {
			Random random = new Random(5);
			return new SequentialMonteCarlo<>(program, random, 20000);
		}, 20000, "particles");

		runConfig(lwConfig);
		runConfig(ssmhConfig);
		runConfig(smcConfig);
		System.out.println();
	}

	private static void runConfig(BenchmarkConfig config) {
		System.out.print("Warming up " + config.name + "...");
		for (int i = 0; i < WARMUP_RUNS; i++) {
			InferenceEngine<?> engine = config.factory.create();
			engine.run();
			System.out.print(".");
		}
		System.out.println(" Done.");

		System.out.print("Measuring " + config.name + "...");
		List<Double> timesMs = new ArrayList<>();
		List<Double> meanEstimates = new ArrayList<>();

		for (int i = 0; i < MEASUREMENT_RUNS; i++) {
			System.gc(); // Suggest GC to minimize timing interference
			try {
				Thread.sleep(100); // Let GC settle
			} catch (InterruptedException ignored) { }

			InferenceEngine<?> engine = config.factory.create();
			long start = System.nanoTime();
			ArrayList<?> results = engine.run();
			long end = System.nanoTime();
			double durationMs = ( end - start ) / 1e6;
			timesMs.add(durationMs);

			double meanEstimate;
			if (engine instanceof LikelihoodWeighting<?> lw) {
				ArrayList<Double> weights = lw.getWeights();
				double weightedSum = 0.0;
				for (int j = 0; j < results.size(); j++) {
					weightedSum += ( (Number) results.get(j) ).doubleValue() * weights.get(j);
				}
				meanEstimate = weightedSum;
			}
			else {
				meanEstimate = results.stream()
									  .mapToDouble(x -> ( (Number) x ).doubleValue())
									  .average()
									  .orElse(0.0);
			}
			meanEstimates.add(meanEstimate);
			System.out.print(".");
		}
		System.out.println(" Done.");

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double sum = 0;
		for (double t : timesMs) {
			if (t < min) min = t;
			if (t > max) max = t;
			sum += t;
		}
		double meanTime = sum / timesMs.size();

		double varianceSum = 0;
		for (double t : timesMs) {
			varianceSum += Math.pow(t - meanTime, 2);
		}
		double stdDev = Math.sqrt(varianceSum / ( timesMs.size() - 1 ));

		double avgEstimate = meanEstimates.stream()
										  .mapToDouble(Double::doubleValue)
										  .average()
										  .orElse(0.0);

		double unitsPerSecond = config.unitCount / ( meanTime / 1000.0 );

		System.out.printf("  Results for %s (%d %s):\n",
						  config.name,
						  config.unitCount,
						  config.unitName);
		System.out.printf("    Avg Execution Time : %.2f ms (+- %.2f ms)\n", meanTime, stdDev);
		System.out.printf("    Min / Max Time     : %.2f ms / %.2f ms\n", min, max);
		System.out.printf("    Throughput         : %,.2f %s/sec\n",
						  unitsPerSecond,
						  config.unitName);
		System.out.printf("    Avg Mean Estimate  : %.4f\n", avgEstimate);
		System.out.println();
	}

	interface EngineFactory {
		InferenceEngine<?> create();
	}

	private static class BenchmarkConfig {
		String name;
		EngineFactory factory;
		long unitCount;
		String unitName;

		BenchmarkConfig(String name, EngineFactory factory, long unitCount, String unitName) {
			this.name = name;
			this.factory = factory;
			this.unitCount = unitCount;
			this.unitName = unitName;
		}
	}
}
