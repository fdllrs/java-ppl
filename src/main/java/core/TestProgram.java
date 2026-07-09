package core;

import ast.Expression;

import java.util.List;

public class TestProgram {

	public static List<Expression> normalNormalConjugate() {
		String program = "(let [mu (sample (normal 0 1))] (observe (normal mu 1) 2.3) mu)";

		return Parser.parse(program);
	}

	public static List<Expression> noisyBernoulliSum() {
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

		return Parser.parse(program);
	}

	public static List<Expression> multiObsNormalNormal() {
		String program = "(let [mu (sample (normal 0 1))] " + "  (observe (normal mu 1) 2.3) " +
						 "  (observe (normal mu 1) 1.7) " + "  mu)";

		return Parser.parse(program);
	}

	public static List<Expression> highVarianceNormalPrior() {
		String program =
				"(let [mu (sample (normal 1 2))] " + "  (observe (normal mu 3) 5.0) " + "  mu)";

		return Parser.parse(program);
	}

	public static List<Expression> coinFlipSelection() {
		String program =
				"(let [biased (sample (bernoulli 0.5)) " + "      p (if biased 0.8 0.5)] " +
				"  (observe (bernoulli p) 1) " + "  (observe (bernoulli p) 1) " +
				"  (observe (bernoulli p) 0) " + "  (if biased 1 0))";

		return Parser.parse(program);
	}

	public static List<Expression> signalNoiseSum() {
		String program = "(let [x (sample (normal 0 1)) " + "      y (sample (normal 0 2)) " +
						 "      z (+ x y)] " + "  (observe (normal z 0.1) 3.0) " + "  x)";

		return Parser.parse(program);
	}

	public static List<Expression> noisyBinomial() {
		// Equivalent to Ejemplo2 (bits sum) but using the Binomial distribution directly
		String program =
				"(let [total (sample (binomial 8 0.5))] " + "  (observe (normal 7 2) total) " +
				"total)";

		return Parser.parse(program);
	}

	public static List<Expression> exponentialExponentialConjugate() {
		// Exponential-Exponential conjugate model
		String program = "(let [lambda (sample (exponential 1.0))] " +
						 "  (observe (exponential lambda) 2.0) lambda)";

		return Parser.parse(program);
	}

	public static List<Expression> uniformNormal() {
		// Uniform-Normal model
		String program =
				"(let [x (sample (uniform 0.0 10.0))] " + "  (observe (normal x 1.0) 4.5) x)";

		return Parser.parse(program);
	}

	public static List<Expression> betaBernoulliConjugate() {
		// Beta-Bernoulli conjugate model
		String program = "(let [p (sample (beta 2.0 2.0))] " + "  (observe (bernoulli p) 1.0) p)";

		return Parser.parse(program);
	}

	public static List<Expression> gammaExponentialConjugate() {
		// Gamma-Exponential conjugate model
		String program = "(let [lambda (sample (gamma 2.0 2.0))] " +
						 "  (observe (exponential lambda) 2.0) lambda)";

		return Parser.parse(program);
	}
}
