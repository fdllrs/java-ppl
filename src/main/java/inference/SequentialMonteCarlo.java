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

public class SequentialMonteCarlo extends InferenceEngine {

	private final int particleCount;

	public SequentialMonteCarlo(List<Expression> program, Random rng, int particleCount) {
		super(program, rng);

		this.particleCount = particleCount;
	}

	@Override
	public ArrayList<Double> run() {

		ArrayList<Machine> particles = initializeParticles();

		while (true) {

			ArrayList<Message> messages = advanceAllParticles(particles);

			if (allParticlesDone(messages)) {

				return getMessageResults(messages);
			}
			assertAllMessagesAreObserve(messages);

			ArrayList<Double> log_inc = new ArrayList<>();
			ArrayList<Machine> paused = new ArrayList<>();

			for (int i = 0; i < particleCount; i++) {
				Machine particle = particles.get(i);
				Observe observe = (Observe) messages.get(i);

				double logProb = observe.distribution().logProb(observe.value());
				particle.addToLogWeight(logProb);
				log_inc.add(logProb);
				particle.send(observe.value());
				paused.add(particle);
			}

			ArrayList<Double> softMaxLogInc = softmax(log_inc);
			ArrayList<Double> cumulativeDistribution = new ArrayList<>();

			cumulativeDistribution.add(softMaxLogInc.getFirst());

			for (int i = 1; i < particleCount; i++) {
				cumulativeDistribution.add(
						cumulativeDistribution.get(i - 1) + softMaxLogInc.get(i));
			}

			ArrayList<Integer> ancestors = new ArrayList<>();

			for (int i = 0; i < particleCount; i++) {
				Double u = rng.nextDouble();

				for (int j = 0; j < cumulativeDistribution.size(); j++) {
					if (cumulativeDistribution.get(j) >= u) {
						ancestors.add(j);
						break;
					}
				}
			}

			ArrayList<Machine> newParticles = new ArrayList<>();
			for (int ancestorIndex : ancestors) {
				Machine ancestor = paused.get(ancestorIndex);

				newParticles.add(ancestor.fork(new Random(ancestor.getRng().nextLong())));
			}

			particles = newParticles;
		}
	}

	private ArrayList<Machine> initializeParticles() {
		ArrayList<Machine> particles = new ArrayList<>();

		for (int i = 0; i < particleCount; i++) {
			Random aRNG = new Random(rng.nextLong());
			particles.add(initializeMachineWithRNG(aRNG));
		}
		return particles;
	}

	private ArrayList<Message> advanceAllParticles(ArrayList<Machine> particles) {
		ArrayList<Message> messages = new ArrayList<>();
		for (int i = 0; i < particleCount; i++) {
			Machine particle = particles.get(i);
			messages.add(i, this.advanceParticle(particle));
		}
		return messages;
	}

	private static boolean allParticlesDone(ArrayList<Message> messages) {
		return messages.stream().allMatch((message) -> message instanceof Done);
	}

	private static ArrayList<Double> getMessageResults(ArrayList<Message> messages) {
		ArrayList<Double> results = new ArrayList<>();
		for (Message message : messages) {
			Done done = (Done) message;
			results.add(( (Number) done.returnValue() ).doubleValue());
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

	private Message advanceParticle(Machine particle) {
		Message message = particle.resume();
		while (( message instanceof Sample sample )) {

			particle.send(sample.distribution().sample(particle.getRng()));
			message = particle.resume();
		}
		return message;
	}
}
