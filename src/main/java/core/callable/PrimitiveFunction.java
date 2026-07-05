package core.callable;

import core.Address;
import core.Machine;
import distributions.Bernoulli;
import distributions.Normal;

import java.util.List;
import java.util.Objects;

public enum PrimitiveFunction implements Callable {
	ADD("+") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			double sum = 0.0;
			for (Object arg : args) {
				sum += ( (Number) arg ).doubleValue();
			}
			if (sum == (long) sum) {
				machine.pushResult((long) sum);
			}
			else {
				machine.pushResult(sum);
			}
		}
	}, SUB("-") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			if (args.isEmpty()) {
				machine.pushResult(0.0);
				return;
			}
			if (args.size() == 1) {
				machine.pushResult(-( (Number) args.getFirst() ).doubleValue());
				return;
			}
			double diff = ( (Number) args.getFirst() ).doubleValue();
			for (int i = 1; i < args.size(); i++) {
				diff -= ( (Number) args.get(i) ).doubleValue();
			}
			machine.pushResult(diff);
		}
	}, PROD("*") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			double prod = 1.0;
			for (Object arg : args) {
				prod *= ( (Number) arg ).doubleValue();
			}
			machine.pushResult(prod);
		}
	}, DIV("/") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			if (args.isEmpty()) {
				machine.pushResult(1.0);
				return;
			}
			if (args.size() == 1) {
				machine.pushResult(1.0 / ( (Number) args.getFirst() ).doubleValue());
				return;
			}
			double quotient = ( (Number) args.getFirst() ).doubleValue();
			for (int i = 1; i < args.size(); i++) {
				quotient /= ( (Number) args.get(i) ).doubleValue();
			}
			machine.pushResult(quotient);
		}
	}, GREATERTHAN(">") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			if (args.size() != 2) throw new IllegalArgumentException("> expects 2 arguments");
			machine.pushResult(( (Number) args.getFirst() ).doubleValue() >
							   ( (Number) args.get(1) ).doubleValue());
		}
	}, LESSTHAN("<") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			if (args.size() != 2) throw new IllegalArgumentException("< expects 2 arguments");
			machine.pushResult(( (Number) args.getFirst() ).doubleValue() <
							   ( (Number) args.get(1) ).doubleValue());
		}
	}, EQUALS("==") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			if (args.size() != 2) throw new IllegalArgumentException("= expects 2 arguments");
			Object a = args.get(0);
			Object b = args.get(1);
			if (a instanceof Number && b instanceof Number) {
				machine.pushResult(( (Number) a ).doubleValue() == ( (Number) b ).doubleValue());
				return;
			}
			machine.pushResult(Objects.equals(a, b));
		}
	}, NORMAL("normal") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			if (args.size() != 2) throw new IllegalArgumentException(
					"normal distribution expects 2 arguments (mu, sigma)");
			double mu = ( (Number) args.get(0) ).doubleValue();
			double sigma = ( (Number) args.get(1) ).doubleValue();
			machine.pushResult(new Normal(mu, sigma));
		}
	}, BERNOULLI("bernoulli") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			if (args.size() != 1) throw new IllegalArgumentException(
					"bernoulli distribution expects 1 argument (p)");
			double p = ( (Number) args.getFirst() ).doubleValue();
			machine.pushResult(new Bernoulli(p));
		}
	};

	private final String symbol;

	PrimitiveFunction(String symbol) {
		this.symbol = symbol;
	}

	public static PrimitiveFunction fromSymbol(String symbol) {
		for (PrimitiveFunction pf : values()) {
			if (pf.symbol.equals(symbol)) {
				return pf;
			}
		}
		if (symbol.equals("=")) {
			return EQUALS;
		}
		return null;
	}
}
