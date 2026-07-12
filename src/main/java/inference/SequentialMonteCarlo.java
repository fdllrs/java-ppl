package inference;

import ast.Expression;
import core.Machine;
import messaging.Done;
import messaging.Message;
import messaging.Observe;
import messaging.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SequentialMonteCarlo <T extends Number> extends InferenceEngine<T> {

	private final int particleCount;

	public SequentialMonteCarlo(List<Expression> program, Random rng, int particleCount) {
		super(program, rng);

		this.particleCount = particleCount;
	}

	@Override
	public Posterior<T> run() {
		ArrayList<Machine> particles = initializeParticles();

		while (true) {
			ArrayList<Message> messages = advanceAllParticles(particles);

			if (allParticlesDone(messages)) {
				return Posterior.ofUnweighted(getMessageResults(messages));
			}
			assertAllMessagesAreObserve(messages);

			ArrayList<Double> logInc = evaluateObservations(particles, messages);
			ArrayList<Double> cumulativeDistribution = computeCumulativeDistribution(logInc);
			ArrayList<Integer> ancestors = sampleAncestors(cumulativeDistribution);

			particles = resampleParticles(particles, ancestors);
		}
	}

	private ArrayList<Machine> initializeParticles() {
		ArrayList<Machine> particles = new ArrayList<>(particleCount);
		for (int i = 0; i < particleCount; i++) {
			particles.add(initializeMachineWithRNG(new Random(rng.nextLong())));
		}
		return particles;
	}

	private ArrayList<Message> advanceAllParticles(ArrayList<Machine> particles) {
		int numCores = Runtime.getRuntime().availableProcessors();
		int chunkSize = ( particles.size() + numCores - 1 ) / numCores;

		List<List<Machine>> chunks = new ArrayList<>();
		for (int i = 0; i < particles.size(); i += chunkSize) {
			chunks.add(particles.subList(i, Math.min(i + chunkSize, particles.size())));
		}

		return chunks.parallelStream().flatMap(chunk -> {
			List<Message> chunkMessages = new ArrayList<>(chunk.size());
			for (Machine m : chunk) {
				chunkMessages.add(this.advanceParticle(m));
			}
			return chunkMessages.stream();
		}).collect(Collectors.toCollection(ArrayList::new));
	}

	private static boolean allParticlesDone(ArrayList<Message> messages) {
		return messages.stream().allMatch((message) -> message instanceof Done);
	}

	@SuppressWarnings("unchecked")
	private ArrayList<T> getMessageResults(ArrayList<Message> messages) {
		ArrayList<T> results = new ArrayList<>();
		for (Message message : messages) {
			Done done = (Done) message;
			results.add((T) done.returnValue());
		}
		return results;
	}

	private static void assertAllMessagesAreObserve(ArrayList<Message> messages) {
		if (!( messages.stream().allMatch((message) -> message instanceof Observe) )) {
			throw new RuntimeException(
					"particles reached different breakpoints: SMC needs a shared observe " +
					"sequence");
		}
	}

	private ArrayList<Double> evaluateObservations(ArrayList<Machine> particles,
			ArrayList<Message> messages) {
		ArrayList<Double> logInc = new ArrayList<>(particleCount);
		for (int i = 0; i < particleCount; i++) {
			Machine particle = particles.get(i);
			Observe observe = (Observe) messages.get(i);

			Object observationValue = observe.value();

			double logProb = observe.distribution().logProb(observationValue);
			particle.addToLogWeight(logProb);
			particle.send(observationValue);
			logInc.add(logProb);
		}
		return logInc;
	}

	private ArrayList<Double> computeCumulativeDistribution(ArrayList<Double> logInc) {
		ArrayList<Double> softMaxLogInc = softmax(logInc);
		ArrayList<Double> cumulativeDistribution = new ArrayList<>(particleCount);

		double sum = 0.0;
		for (double prob : softMaxLogInc) {
			sum += prob;
			cumulativeDistribution.add(sum);
		}
		return cumulativeDistribution;
	}

	private ArrayList<Integer> sampleAncestors(ArrayList<Double> cumulativeDistribution) {
		ArrayList<Integer> ancestors = new ArrayList<>(particleCount);
		double u = rng.nextDouble() / particleCount;
		int j = 0;

		for (int i = 0; i < particleCount; i++) {
			while (j < cumulativeDistribution.size() - 1 && cumulativeDistribution.get(j) < u) {
				j++;
			}
			ancestors.add(j);
			u += 1.0 / particleCount;
		}
		return ancestors;
	}

	private ArrayList<Machine> resampleParticles(ArrayList<Machine> particles,
			ArrayList<Integer> ancestors) {
		ArrayList<Machine> newParticles = new ArrayList<>(particleCount);
		for (int ancestorIndex : ancestors) {
			Machine ancestor = particles.get(ancestorIndex);
			newParticles.add(ancestor.fork(new Random(ancestor.getRng().nextLong())));
		}
		return newParticles;
	}

	private Message advanceParticle(Machine particle) {
		Message message = particle.resume();
		while (( message instanceof Sample sample )) {

			particle.send(sample.distribution().sample(particle.getRng()));
			message = particle.resume();
		}
		return message;
	}
}
