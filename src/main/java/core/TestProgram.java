package core;

import ast.Expression;

import java.util.List;

public class TestProgram {

	public static List<Expression> Ejemplo1() {
		String program = "(let [mu (sample (normal 0 1))] (observe (normal mu 1) 2.3) mu)";
		System.out.println("\nEjemplo 1: " + program);

		List<Expression> parsedProgram = Parser.parse(program);
		System.out.println("AST parsed size: " + parsedProgram.size());
		System.out.println("AST parsed representation: " + parsedProgram.getFirst());

		return parsedProgram;
	}

	public static List<Expression> Ejemplo2() {
		String program = "(let [b1 (if (sample (bernoulli 0.5)) 1 0) " +
						 "b2 (if (sample (bernoulli 0.5)) 1 0) " +
						 "b3 (if (sample (bernoulli 0.5)) 1 0) " +
						 "b4 (if (sample (bernoulli 0.5)) 1 0) " +
						 "b5 (if (sample (bernoulli 0.5)) 1 0) " +
						 "b6 (if (sample (bernoulli 0.5)) 1 0) " +
						 "b7 (if (sample (bernoulli 0.5)) 1 0) " +
						 "b8 (if (sample (bernoulli 0.5)) 1 0) " +
						 "total (+ b1 b2 b3 b4 b5 b6 b7 b8)] " +
						 "(observe (normal 7 2) total) total)";

		System.out.println("\nEjemplo 2: " + program);
		List<Expression> parsedProgram = Parser.parse(program);
		System.out.println("AST parsed size: " + parsedProgram.size());

		return parsedProgram;
	}
}
