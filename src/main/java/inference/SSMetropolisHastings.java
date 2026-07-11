package inference;

import ast.Expression;
import core.Address;
import core.Machine;
import distributions.Distribution;
import messaging.*;

import java.util.*;

public class SSMetropolisHastings <T> extends InferenceEngine<T> {

	private final int warmup;
	private final int iterations;

	public SSMetropolisHastings(List<Expression> program, Random rng, int warmup, int iterations) {
		super(program, rng);
		this.warmup = warmup;
		this.iterations = iterations;
	}

	private static Trace processMessage(Random rng,
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
			) -> processObservation(machine,
									logLikelihoods,
									observeAddress,
									distribution,
									observation);
			case Done(var returnValue) -> {
				return new Trace(returnValue, samples, logPriors, logLikelihoods);
			}
			case Fork() -> throw new RuntimeException("Should not fork in SSMH");
		}
		return null;
	}

	private static void processObservation(Machine machine,
			Map<Address, Object> logLikelihoods,
			Address observeAddress,
			Distribution distribution,
			Object observation) {
		logLikelihoods.put(observeAddress, distribution.logProb(observation));
		machine.send(observation);
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
		processObservation(machine,
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

		Set<Address> forwardProposalSet = computeProposalSet(proposedSamples.keySet(),
															 currentSamples.keySet(),
															 proposalAddress);
		Set<Address> reverseProposalSet = computeProposalSet(currentSamples.keySet(),
															 proposedSamples.keySet(),
															 proposalAddress);

		double proposedLogProb = sumLogProbabilities(proposedTrace.logPriors(),
													 proposedTrace.logLikelihoods(),
													 forwardProposalSet);
		double currentLogProb = sumLogProbabilities(currentTrace.logPriors(),
													currentTrace.logLikelihoods(),
													reverseProposalSet);

		return ( Math.log(currentSamples.size()) - Math.log(proposedSamples.size()) ) +
			   ( proposedLogProb - currentLogProb );
	}

	private static Set<Address> computeProposalSet(Set<Address> sourceKeys,
			Set<Address> targetKeys,
			Address proposalAddress) {
		Set<Address> proposalSet = new HashSet<>(sourceKeys);
		proposalSet.removeAll(targetKeys);
		proposalSet.add(proposalAddress);
		return proposalSet;
	}

	private static double sumLogProbabilities(Map<Address, Object> logPriors,
			Map<Address, Object> logLikelihoods,
			Set<Address> excludedAddresses) {
		double sum = 0.0;
		for (Map.Entry<Address, Object> entry : logPriors.entrySet()) {
			if (!excludedAddresses.contains(entry.getKey())) {
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

		Map<Address, Object> samples = new HashMap<>();
		Map<Address, Object> logPriors = new HashMap<>();
		Map<Address, Object> logLikelihoods = new HashMap<>();

		while (true) {
			Message message = machine.resume();

			Trace returnValue = processMessage(rng,
											   proposalAddress,
											   currentSamples,
											   message,
											   samples,
											   logPriors,
											   machine,
											   logLikelihoods);
			if (returnValue != null) return returnValue;
		}
	}

	private Trace performInferenceStep(Trace currentTrace) {
		Map<Address, Object> currentSamples = currentTrace.samples();
		Address proposalAddress = getRandomAddress(currentSamples);
		Trace proposedTrace = runTrace(rng, proposalAddress, currentSamples);
		currentTrace = updateTrace(currentTrace, proposedTrace, proposalAddress);

		return currentTrace;
	}

	private Address getRandomAddress(Map<Address, Object> samples) {
		List<Address> addresses = new ArrayList<>(samples.keySet());
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
			Map<Address, Object> logLikelihoods) { }
}
