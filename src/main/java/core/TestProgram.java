package core;

import ast.Expression;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestProgram {

	public static List<Expression> normalNormalConjugate() {
		return loadFromResource("normalNormalConjugate.txt");
	}

	private static List<Expression> loadFromResource(String resourceName) {
		try (InputStream is = TestProgram.class.getClassLoader().getResourceAsStream(
				"models/" + resourceName)) {
			if (is == null) {
				throw new RuntimeException("Resource not found: models/" + resourceName);
			}
			String code = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			return Parser.parse(code);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load program from resource: " + resourceName, e);
		}
	}

	public static List<Expression> noisyBernoulliSum() {
		return loadFromResource("noisyBernoulliSum.txt");
	}

	public static List<Expression> multiObsNormalNormal() {
		return loadFromResource("multiObsNormalNormal.txt");
	}

	public static List<Expression> highVarianceNormalPrior() {
		return loadFromResource("highVarianceNormalPrior.txt");
	}

	public static List<Expression> coinFlipSelection() {
		return loadFromResource("coinFlipSelection.txt");
	}

	public static List<Expression> signalNoiseSum() {
		return loadFromResource("signalNoiseSum.txt");
	}

	public static List<Expression> noisyBinomial() {
		return loadFromResource("noisyBinomial.txt");
	}

	public static List<Expression> exponentialExponentialConjugate() {
		return loadFromResource("exponentialExponentialConjugate.txt");
	}

	public static List<Expression> uniformNormal() {
		return loadFromResource("uniformNormal.txt");
	}

	public static List<Expression> betaBernoulliConjugate() {
		return loadFromResource("betaBernoulliConjugate.txt");
	}

	public static List<Expression> gammaExponentialConjugate() {
		return loadFromResource("gammaExponentialConjugate.txt");
	}

	public static List<Expression> gammaPoissonConjugate() {
		return loadFromResource("gammaPoissonConjugate.txt");
	}
}
