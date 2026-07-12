package inference;

import ast.Expression;
import core.Address;
import core.Machine;
import distributions.Distribution;
import messaging.*;

import java.util.*;

public class SSMetropolisHastings <T extends Number> extends InferenceEngine<T> {

	private final int warmup;
	private final int iterations;

	public SSMetropolisHastings(List<Expression> program, Random rng, int warmup, int iterations) {
		super(program, rng);
		this.warmup = warmup;
		this.iterations = iterations;
	}

	private static Optional<Trace> processMessage(Random rng,
			Address proposalAddress,
			Map<Address, Object> currentSamples,
			Message message,
			Map<Address, Object> samples,
			Map<Address, Object> logPriors,
			Machine machine,
			Map<Address, Object> logLikelihoods) {

		switch (message) {
			case Sample(Address sampleAddress, Distribution distribution) -> processSample(rng,
																						   proposalAddress,
																						   currentSamples,
																						   samples,
																						   logPriors,
																						   machine,
																						   sampleAddress,
																						   distribution);
			case Observe(
					Address observeAddress, Distribution distribution, Object observation
			) -> storeObservationLogLikelihood(machine,
											   logLikelihoods,
											   observeAddress,
											   distribution,
											   observation);
			case Done(var returnValue) -> {
				return Optional.of(new Trace(returnValue,
											 samples,
											 logPriors,
											 logLikelihoods,
											 new ArrayList<>(samples.keySet())));
			}
			case Fork() -> throw new RuntimeException("Should not fork in SSMH");
		}
		return Optional.empty();
	}

	/**
	 * Stores the log-likelihood of an observation and resumes the machine with the observed value.
	 */
	private static void storeObservationLogLikelihood(Machine machine,
			Map<Address, Object> logLikelihoods,
			Address observeAddress,
			Distribution distribution,
			Object observation) {
		logLikelihoods.put(observeAddress, distribution.logProb(observation));
		machine.send(observation);
	}

	/**
	 * Stores the log-prior of a sampled value and resumes the machine with that value.
	 */
	private static void storeSampleLogPrior(Machine machine,
			Map<Address, Object> logPriors,
			Address sampleAddress,
			Distribution distribution,
			Object sampledValue) {
		logPriors.put(sampleAddress, distribution.logProb(sampledValue));
		machine.send(sampledValue);
	}

	private static void processSample(Random rng,
			Address proposalAddress,
			Map<Address, Object> currentSamples,
			Map<Address, Object> samples,
			Map<Address, Object> logPriors,
			Machine machine,
			Address sampleAddress,
			Distribution distribution) {
		if (sampleAddress.equals(proposalAddress) ||
			!( currentSamples.containsKey(sampleAddress) )) {
			samples.put(sampleAddress, distribution.sample(rng));
		}
		else {
			samples.put(sampleAddress, currentSamples.get(sampleAddress));
		}
		storeSampleLogPrior(machine,
							logPriors,
							sampleAddress,
							distribution,
							samples.get(sampleAddress));
	}

	private static double calculateLogAcceptanceRatio(Trace currentTrace,
			Trace proposedTrace,
			Address proposalAddress) {

		Map<Address, Object> currentSamples = currentTrace.samples();
		Map<Address, Object> proposedSamples = proposedTrace.samples();

		double proposedLogProb = sumLogProbabilities(proposedTrace.logPriors(),
													 proposedTrace.logLikelihoods(),
													 currentSamples,
													 proposalAddress);
		double currentLogProb = sumLogProbabilities(currentTrace.logPriors(),
													currentTrace.logLikelihoods(),
													proposedSamples,
													proposalAddress);

		return ( Math.log(currentSamples.size()) - Math.log(proposedSamples.size()) ) +
			   ( proposedLogProb - currentLogProb );
	}

	private static double sumLogProbabilities(Map<Address, Object> logPriors,
			Map<Address, Object> logLikelihoods,
			Map<Address, Object> otherSamples,
			Address proposalAddress) {
		double sum = 0.0;
		for (Map.Entry<Address, Object> entry : logPriors.entrySet()) {
			Address key = entry.getKey();
			if (!key.equals(proposalAddress) && otherSamples.containsKey(key)) {
				sum += ( (Number) entry.getValue() ).doubleValue();
			}
		}
		for (Object val : logLikelihoods.values()) {
			sum += ( (Number) val ).doubleValue();
		}
		return sum;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Posterior<T> run() {

		Trace currentTrace;
		currentTrace = runTrace(rng, null, new HashMap<>());

		ArrayList<T> results = new ArrayList<>();

		for (int stepNumber = 0; stepNumber < iterations + this.warmup; stepNumber++) {
			currentTrace = performInferenceStep(currentTrace);

			if (stepNumber >= this.warmup) {
				results.add((T) currentTrace.returnValue());
			}
		}

		return Posterior.ofUnweighted(results);
	}

	private Trace runTrace(Random rng,
			Address proposalAddress,
			Map<Address, Object> currentSamples) {

		Machine machine = initializeMachine();

		int expectedSize = currentSamples.isEmpty() ? 16 : ( currentSamples.size() + 2 );
		Map<Address, Object> samples = new HashMap<>(expectedSize);
		Map<Address, Object> logPriors = new HashMap<>(expectedSize);
		Map<Address, Object> logLikelihoods = new HashMap<>(expectedSize);

		while (true) {
			Message message = machine.resume();

			Optional<Trace> result = processMessage(rng,
													proposalAddress,
													currentSamples,
													message,
													samples,
													logPriors,
													machine,
													logLikelihoods);
			if (result.isPresent()) return result.get();
		}
	}

	private Trace performInferenceStep(Trace currentTrace) {
		Map<Address, Object> currentSamples = currentTrace.samples();
		Address proposalAddress = getRandomAddress(currentTrace);
		Trace proposedTrace = runTrace(rng, proposalAddress, currentSamples);
		currentTrace = updateTrace(currentTrace, proposedTrace, proposalAddress);

		return currentTrace;
	}

	private Address getRandomAddress(Trace trace) {
		List<Address> addresses = trace.sampleAddresses();
		int randomIndex = rng.nextInt(addresses.size());
		return addresses.get(randomIndex);
	}

	private Trace updateTrace(Trace currentTrace, Trace proposedTrace, Address proposalAddress) {
		double logAcceptanceRatio = calculateLogAcceptanceRatio(currentTrace,
																proposedTrace,
																proposalAddress);
		if (Math.log(rng.nextDouble()) < logAcceptanceRatio) {
			currentTrace = proposedTrace;
		}
		return currentTrace;
	}

	private record Trace(
			Object returnValue,
			Map<Address, Object> samples,
			Map<Address, Object> logPriors,
			Map<Address, Object> logLikelihoods,
			List<Address> sampleAddresses) { }
}
