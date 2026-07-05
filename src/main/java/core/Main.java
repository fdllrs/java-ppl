package core;

import ast.Expression;
import inference.InferenceEngine;
import inference.LikelihoodWeighting;
import inference.SSMetropolisHastings;
import inference.SequentialMonteCarlo;

import java.util.List;
import java.util.Random;

public class Main {

	static void main() {

		System.out.println("\n====== EJEMPLO 1: conj ====== \n");
		System.out.println("Exact mean: 1.150");
		List<Expression> ejemplo1 = TestProgram.Ejemplo1();
		executeSamplingMethods(ejemplo1);

		System.out.println("\n====== EJEMPLO 2: bits ====== \n");
		System.out.println("Exact mean: 5.014");

		List<Expression> ejemplo2 = TestProgram.Ejemplo2();
		executeSamplingMethods(ejemplo2);
	}

	private static void executeSamplingMethods(List<Expression> program) {
		Random random = new Random(5);

		InferenceEngine lw = new LikelihoodWeighting(program, random, 1000000);
		double lwMean = lw.run().stream().mapToDouble(Double::doubleValue).sum();

		System.out.println("LW mean: " + lwMean);

		InferenceEngine ssmh = new SSMetropolisHastings(program, random, 3000, 1000000);
		double ssmhMean = ssmh.run()
							  .stream()
							  .mapToDouble(Double::doubleValue)
							  .average()
							  .orElse(0.0);

		System.out.println("SSMH mean: " + ssmhMean);

		InferenceEngine smc = new SequentialMonteCarlo(program, random, 20000);
		double smcMean = smc.run().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

		System.out.println("SMC mean: " + smcMean);
	}
}