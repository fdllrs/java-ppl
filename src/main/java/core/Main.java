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