package distributions;

import java.util.Random;

public interface Distribution {
	Object sample(Random rng);
	double logProb(Object x);
}
