package core;

import ast.Expression;
import inference.InferenceEngine;
import inference.LikelihoodWeighting;

import java.util.List;
import java.util.Random;

public class Main {

	static void main() {

		List<Expression> program = TestProgram.Ejemplo1();
		Random random = new Random(12345);

		InferenceEngine inferenceEngine = new LikelihoodWeighting(program, random);
		double mean = inferenceEngine.run(100000);

		System.out.println("mean: " + mean);
	}
}