package core;

import ast.Expression;
import inference.LikelihoodWeighting;
import inference.SSMetropolisHastings;
import inference.SequentialMonteCarlo;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

@Command(
		name = "java-ppl", mixinStandardHelpOptions = true, version = "Java-ppl 1.0",
		description = "Probabilistic Programming Language (PPL) CLI",
		subcommands = { CLI.RunCommand.class, CLI.ReplCommand.class })
public class CLI implements Callable<Integer> {

	static void main(String... args) {
		int exitCode = new CommandLine(new CLI()).execute(args);
		System.exit(exitCode);
	}

	@Override
	public Integer call() {
		// If no subcommand is specified, show the help menu
		CommandLine.usage(this, System.out);
		return 0;
	}

	@Command(name = "run", description = "Run a .ppl file using an inference algorithm")
	public static class RunCommand implements Callable<Integer> {

		@Option(names = { "-f", "--file" }, required = true, description = "Input file to parse.")
		private File file;

		@Option(
				names = { "-a", "--algorithm" }, defaultValue = "smc",
				description = "Algorithm to run: lw, ssmh, smc.")
		private Algorithm algorithm;
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

			// Load and parse the PPL file
			List<String> lines = Files.readAllLines(Path.of(file.getAbsolutePath()));
			String code = String.join("\n", lines);
			List<Expression> program = Parser.parse(code);

			System.out.println("Loaded program: " + program);
			System.out.println("Algorithm: " + algorithm);
			System.out.println("Particles: " + particles);
			Random rng = initializeRandomGenerator();

			ArrayList<Double> results;
			LikelihoodWeighting lw = new LikelihoodWeighting(program, rng, particles);
			switch (algorithm) {
				case LW -> {
					System.out.println("Running Likelihood Weighting...");
					lw = new LikelihoodWeighting(program, rng, particles);
					results = lw.run();
				}
				case SMC -> {
					System.out.println("Running SMC...");
					results = new SequentialMonteCarlo(program, rng, particles).run();
				}
				case SSMH -> {
					System.out.println("Running SSMH...");
					results = new SSMetropolisHastings(program, rng, warmup, particles).run();
				}
				default -> {
					System.err.println("Invalid algorithm: " + algorithm);
					return -1;
				}
			}

			System.out.println("Results: " +
							   LikelihoodWeighting.calculateWeightedMean(results,
																		 lw.getWeights()));
			return 0;
		}

		private @NotNull Random initializeRandomGenerator() {
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

		private enum Algorithm {SSMH, SMC, LW}
	}

	@Command(name = "repl", description = "Start the interactive REPL shell")
	public static class ReplCommand implements Callable<Integer> {

		@Option(names = { "-s", "--seed" }, description = "Seed for the random number generator.")
		private Long seed;

		@Override
		public Integer call() {
			// Later we will launch the REPL here.
			System.out.println("Starting REPL...");
			return 0;
		}
	}
}