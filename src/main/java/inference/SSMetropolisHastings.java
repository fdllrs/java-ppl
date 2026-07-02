package inference;

import ast.Expression;
import core.Address;
import core.Machine;
import distributions.Distribution;
import messaging.*;

import java.util.*;

public class SSMetropolisHastings extends InferenceEngine {

	public SSMetropolisHastings(List<Expression> program, Random rng) {
		super(program, rng);
	}

	private static SSMHTraceResult processMessage(Random rng,
			Address addressToReuse,
			Map<Address, Object> cache,
			Message message,
			Map<Address, Object> xMap,
			Map<Address, Object> sMap,
			Machine machine,
			Map<Address, Object> oMap) {

		switch (message) {
			case Sample(Address sampleAddress, Distribution distribution) -> processSample(rng,
																						   addressToReuse,
																						   cache,
																						   xMap,
																						   sMap,
																						   machine,
																						   sampleAddress,
																						   distribution);
			case Observe(
					Address observeAddress, Distribution distribution, Object observation
			) -> processObservation(machine, oMap, observeAddress, distribution, observation);
			case Done(var returnValue) -> {
				return new SSMHTraceResult(returnValue, xMap, sMap, oMap);
			}
			case Fork() -> throw new RuntimeException("Should not fork in SSMH");
		}
		return null;
	}

	private static void processObservation(Machine machine,
			Map<Address, Object> oMap,
			Address observeAddress,
			Distribution distribution,
			Object observation) {
		oMap.put(observeAddress, distribution.logProb(observation));
		machine.send(observation);
	}

	private static void processSample(Random rng,
			Address addressToReuse,
			Map<Address, Object> cache,
			Map<Address, Object> xMap,
			Map<Address, Object> sMap,
			Machine machine,
			Address sampleAddress,
			Distribution distribution) {
		if (sampleAddress.equals(addressToReuse) || !( cache.containsKey(sampleAddress) )) {
			xMap.put(sampleAddress, distribution.sample(rng));
		}
		else {
			xMap.put(sampleAddress, cache.get(sampleAddress));
		}
		processObservation(machine, sMap, sampleAddress, distribution, xMap.get(sampleAddress));
	}

	private static double mhLogAlpha(SSMHTraceResult current,
			SSMHTraceResult proposed,
			Address a0) {

		Map<Address, Object> X = current.xMap();
		Map<Address, Object> X2 = proposed.xMap();
		Map<Address, Object> S = current.sMap();
		Map<Address, Object> S2 = proposed.sMap();
		Map<Address, Object> O = current.oMap();
		Map<Address, Object> O2 = proposed.oMap();

		Set<Address> fwd = new HashSet<>();
		fwd.add(a0);
		for (Address key : X2.keySet()) {
			if (!X.containsKey(key)) {
				fwd.add(key);
			}
		}

		Set<Address> rev = new HashSet<>();
		rev.add(a0);
		for (Address key : X.keySet()) {
			if (!X2.containsKey(key)) {
				rev.add(key);
			}
		}

		double num = 0.0;
		for (Map.Entry<Address, Object> entry : S2.entrySet()) {
			if (!fwd.contains(entry.getKey())) {
				num += ( (Number) entry.getValue() ).doubleValue();
			}
		}
		for (Object val : O2.values()) {
			num += ( (Number) val ).doubleValue();
		}

		double den = 0.0;
		for (Map.Entry<Address, Object> entry : S.entrySet()) {
			if (!rev.contains(entry.getKey())) {
				den += ( (Number) entry.getValue() ).doubleValue();
			}
		}
		for (Object val : O.values()) {
			den += ( (Number) val ).doubleValue();
		}

		return ( Math.log(X.size()) - Math.log(X2.size()) ) + ( num - den );
	}

	@Override
	public Double run(int iterations) {
		int warmup = 3000;

		SSMHTraceResult currentTraceResult;
		currentTraceResult = runWithTrace(rng, null, new HashMap<>());

		ArrayList<Double> results = new ArrayList<>();

		for (int stepNumber = 0; stepNumber < iterations + warmup; stepNumber++) {
			currentTraceResult = performInferenceStep(currentTraceResult);

			if (stepNumber >= warmup) {
				results.add(( (Number) currentTraceResult.value() ).doubleValue());
			}
		}

		return results.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
	}

	private SSMHTraceResult runWithTrace(Random rng,
			Address addressToReuse,
			Map<Address, Object> cache) {

		Machine machine = initializeMachine();

		Map<Address, Object> xMap = new HashMap<>();
		Map<Address, Object> sMap = new HashMap<>();
		Map<Address, Object> oMap = new HashMap<>();

		while (true) {
			Message message = machine.resume();

			SSMHTraceResult returnValue = processMessage(rng,
														 addressToReuse,
														 cache,
														 message,
														 xMap,
														 sMap,
														 machine,
														 oMap);
			if (returnValue != null) return returnValue;
		}
	}

	private SSMHTraceResult performInferenceStep(SSMHTraceResult currentTraceResult) {
		Map<Address, Object> currentXMap = currentTraceResult.xMap();
		Address a0 = getRandomAddress(currentXMap);

		SSMHTraceResult candidateTraceResult = runWithTrace(rng, a0, currentXMap);

		currentTraceResult = updateTraceResult(currentTraceResult, candidateTraceResult, a0);

		return currentTraceResult;
	}

	private Address getRandomAddress(Map<Address, Object> anAddressMap) {
		List<Address> addresses = new ArrayList<>(anAddressMap.keySet());
		int randomIndex = rng.nextInt(addresses.size());
		return addresses.get(randomIndex);
	}

	private SSMHTraceResult updateTraceResult(SSMHTraceResult currentTraceResult,
			SSMHTraceResult candidateTraceResult,
			Address a0) {
		double logAlpha = mhLogAlpha(currentTraceResult, candidateTraceResult, a0);
		if (Math.log(rng.nextDouble()) < logAlpha) {
			currentTraceResult = candidateTraceResult;
		}
		return currentTraceResult;
	}

	private record SSMHTraceResult(
			Object value,
			Map<Address, Object> xMap,
			Map<Address, Object> sMap,
			Map<Address, Object> oMap) { }
}
