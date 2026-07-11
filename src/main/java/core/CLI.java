package core;

import ast.Expression;
import inference.LikelihoodWeighting;
import inference.Posterior;
import inference.SSMetropolisHastings;
import inference.SequentialMonteCarlo;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

@Command(
		name = "java-ppl", mixinStandardHelpOptions = true, version = "Java-ppl 1.0",
		description = "Probabilistic Programming Language (PPL) CLI",
		subcommands = { CLI.RunCommand.class })
public class CLI implements Callable<Integer> {

	static void main(String... args) {
		int exitCode = new CommandLine(new CLI()).execute(args);
		System.exit(exitCode);
	}

	private static void printStatistics(Posterior<?> posterior) {
		List<?> samples = posterior.samples();
		List<Double> doubleSamples = samples.stream()
											.map(x -> ( (Number) x ).doubleValue())
											.toList();
		double min = doubleSamples.stream().mapToDouble(x -> x).min().orElse(0.0);
		double max = doubleSamples.stream().mapToDouble(x -> x).max().orElse(0.0);

		double mean = posterior.mean();
		double stdDev = posterior.stdDev();
		System.out.println("\n--- Inference Statistics ---");

		System.out.printf("Mean    : %.4f%n", mean);
		System.out.printf("Std Dev : %.4f%n", stdDev);
		System.out.printf("Min     : %.4f%n", min);
		System.out.printf("Max     : %.4f%n", max);

		if (max == min) {
			System.out.println("All samples have the same value.");
			return;
		}

		int binCount = Math.min(samples.size(), 10);
		double[] bins = new double[ binCount ];
		double binWidth = ( max - min ) / binCount;

		for (int i = 0; i < samples.size(); i++) {
			double val = doubleSamples.get(i);
			double weight = posterior.weights().get(i);
			int binIndex = (int) ( ( val - min ) / binWidth );
			if (binIndex >= binCount) binIndex = binCount - 1;
			if (binIndex < 0) binIndex = 0;
			bins[ binIndex ] += weight;
		}

		System.out.println("\nDistribution Histogram:");
		for (int i = 0; i < binCount; i++) {
			double binMin = min + i * binWidth;
			double binMax = binMin + binWidth;
			double pct = bins[ i ];
			int barLen = (int) ( pct * 50 );
			String bar = "*".repeat(barLen);
			System.out.printf("  [%6.2f, %6.2f) : %s (%.1f%%)%n", binMin, binMax, bar, pct * 100);
		}
	}

	@Override
	public Integer call() {
		CommandLine.usage(this, System.out);
		return 0;
	}

	@Command(name = "run", description = "Run a .txt file using an inference algorithm")
	public static class RunCommand implements Callable<Integer> {

		@Option(names = { "-f", "--file" }, required = true, description = "Input file to parse.")
		private File file;

		@Option(
				names = { "-a", "--algorithm" }, defaultValue = "smc",
				description = "Algorithm to run: lw, ssmh, smc.")
		private String algorithm;
		@Option(
				names = { "-p", "--particles" }, defaultValue = "10000",
				description = "Number of particles to use.")
		private int particles;
		@Option(
				names = { "-w", "--warmup" }, defaultValue = "1000",
				description = "Warmup iterations (MH only).")
		private int warmup;
		@Option(names = { "-s", "--seed" }, description = "Seed for the random number generator.")
		private Long seed;

		@Override
		public Integer call() throws Exception {
			if (!file.exists()) {
				System.err.println("File not found: " + file.getAbsolutePath());
				return 1;
			}

			List<Expression> program = Parser.parse(file);

			System.out.println("Loaded program: " + program);
			System.out.println("Algorithm: " + algorithm);
			System.out.println("Particles: " + particles);
			Random rng = initializeRandomGenerator();

			Posterior<Object> results;
			results = runAlgorithm(program, rng);

			if (results == null) return 1;
			if (results.samples().isEmpty()) {
				System.out.println("No samples collected.");
			}
			boolean allNumeric = results.samples().stream().allMatch(x -> x instanceof Number);

			if (allNumeric) {
				printStatistics(results);
			}
			return 0;
		}

		private Random initializeRandomGenerator() {
			Random rng;
			if (seed != null) {
				rng = new Random(seed);
				System.out.println("running with seed: " + seed);
			}
			else {
				rng = new Random();
				System.out.println("running with random seed");
			}
			return rng;
		}

		private Posterior<Object> runAlgorithm(List<Expression> program, Random rng) {
			Posterior<Object> results;
			switch (algorithm.toLowerCase()) {
				case "lw" -> {
					System.out.println("Running Likelihood Weighting...");
					LikelihoodWeighting<Object> lw = new LikelihoodWeighting<>(program,
																			   rng,
																			   particles);
					results = lw.run();
				}
				case "smc" -> {
					System.out.println("Running SMC...");
					SequentialMonteCarlo<Object> smc = new SequentialMonteCarlo<>(program,
																				  rng,
																				  particles);
					results = smc.run();
				}
				case "ssmh", "mh" -> {
					System.out.println("Running SSMH...");
					SSMetropolisHastings<Object> ssmh = new SSMetropolisHastings<>(program,
																				   rng,
																				   warmup,
																				   particles);
					results = ssmh.run();
				}
				default -> {
					System.err.println("Invalid algorithm: " + algorithm);
					return null;
				}
			}
			return results;
		}
	}
}