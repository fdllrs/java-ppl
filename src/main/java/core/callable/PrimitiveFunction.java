package core.callable;

import core.Address;
import core.Machine;
import distributions.*;

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
	}, BINOMIAL("binomial") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			if (args.size() != 2) throw new IllegalArgumentException(
					"binomial distribution expects 2 arguments (n, p)");
			int n = ( (Number) args.get(0) ).intValue();
			double p = ( (Number) args.get(1) ).doubleValue();
			machine.pushResult(new Binomial(n, p));
		}
	}, BETA("beta") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			if (args.size() != 2) throw new IllegalArgumentException(
					"beta distribution expects 2 arguments (alpha, beta)");
			double alpha = ( (Number) args.get(0) ).doubleValue();
			double beta = ( (Number) args.get(1) ).doubleValue();
			machine.pushResult(new Beta(alpha, beta));
		}
	}, GAMMA("gamma") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			if (args.size() != 2) throw new IllegalArgumentException(
					"gamma distribution expects 2 arguments (shape, scale)");
			double shape = ( (Number) args.get(0) ).doubleValue();
			double scale = ( (Number) args.get(1) ).doubleValue();
			machine.pushResult(new Gamma(shape, scale));
		}
	}, EXPONENTIAL("exponential") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			if (args.size() != 1) throw new IllegalArgumentException(
					"exponential distribution expects 1 argument (rate)");
			double rate = ( (Number) args.getFirst() ).doubleValue();
			machine.pushResult(new Exponential(rate));
		}
	}, UNIFORM("uniform") {
		@Override
		public void apply(Machine machine, List<Object> args, Address address) {
			if (args.size() != 2) throw new IllegalArgumentException(
					"uniform distribution expects 2 arguments (min, max)");
			double min = ( (Number) args.get(0) ).doubleValue();
			double max = ( (Number) args.get(1) ).doubleValue();
			machine.pushResult(new UniformContinuous(min, max));
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

		return null;
	}
}
