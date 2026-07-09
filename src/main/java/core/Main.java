package core;

import ast.Expression;
import inference.LikelihoodWeighting;

import java.util.List;
import java.util.Random;

public class Main {

	static void main() {
		System.out.println("==================================================");
		System.out.println("                  JAVA-PPL RUNNER                 ");
		System.out.println("==================================================");
		System.out.println("Usage instructions:");
		System.out.println("  1. Run './gradlew test' to execute all unit and convergence tests.");
		System.out.println(
				"  2. Run './gradlew benchmark' to execute the performance benchmarks" + ".");
		System.out.println();
		System.out.println("Running a quick example (conjugate normal-normal):");

		List<Expression> program = TestProgram.normalNormalConjugate();
		LikelihoodWeighting lw = new LikelihoodWeighting(program, new Random(42), 10000);
		List<Double> samples = lw.run();
		double mean = LikelihoodWeighting.calculateWeightedMean(samples, lw.getWeights());

		System.out.println("  Exact Mean  : 1.150");
		System.out.println("  LW Estimate : " + String.format("%.4f", mean));
		System.out.println("==================================================");
	}
}