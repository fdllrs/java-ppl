import math

def phi(x):
    """Probability density function (PDF) of standard normal distribution."""
    return math.exp(-0.5 * x * x) / math.sqrt(2.0 * math.pi)

def Phi(x):
    """Cumulative distribution function (CDF) of standard normal distribution."""
    return 0.5 * (1.0 + math.erf(x / math.sqrt(2.0)))

def run_exact_calculations():
    print("==================================================")
    print("      EXACT ANALYTICAL METRICS FOR JAVA-PPL      ")
    print("==================================================")

    # --- EJEMPLO 1: conj ---
    # Prior: mu ~ N(0, 1) [precision = 1]
    # Obs: y ~ N(mu, 1) -> 2.3 [precision = 1]
    # Posterior: N(1.15, 0.5)
    e1_mean = 1.150
    e1_stddev = math.sqrt(0.5)
    print(f"Ejemplo 1 (conj)             - Mean: {e1_mean:.6f}, StdDev: {e1_stddev:.6f}")

    # --- EJEMPLO 2: bits ---
    # Prior: total ~ Binomial(8, 0.5)
    # Obs: normal(7, 2) at total
    w = {}
    for k in range(9):
        prior = math.comb(8, k)
        likelihood = math.exp(-0.5 * ((k - 7) / 2) ** 2)
        w[k] = prior * likelihood
    total_w = sum(w.values())
    e2_mean = sum(k * w[k] for k in w) / total_w
    e2_var = sum(w[k] * (k - e2_mean)**2 for k in w) / total_w
    e2_stddev = math.sqrt(e2_var)
    print(f"Ejemplo 2 (bits)             - Mean: {e2_mean:.6f}, StdDev: {e2_stddev:.6f}")

    # --- EJEMPLO 3: multi-normal ---
    # Prior: mu ~ N(0, 1)
    # Obs: N(mu, 1) at 2.3 and N(mu, 1) at 1.7
    # Posterior: N(4/3, 1/3)
    e3_mean = 4.0 / 3.0
    e3_stddev = math.sqrt(1.0 / 3.0)
    print(f"Ejemplo 3 (multi-normal)     - Mean: {e3_mean:.6f}, StdDev: {e3_stddev:.6f}")

    # --- EJEMPLO 4: normal-prior ---
    # Prior: mu ~ N(1, 2) [variance = 4]
    # Obs: N(mu, 3) at 5.0 [variance = 9]
    # Posterior: N(29/13, 36/13)
    e4_mean = 29.0 / 13.0
    e4_stddev = math.sqrt(36.0 / 13.0)
    print(f"Ejemplo 4 (normal-prior)     - Mean: {e4_mean:.6f}, StdDev: {e4_stddev:.6f}")

    # --- EJEMPLO 5: coin-flips ---
    # Prior: biased ~ Bernoulli(0.5) -> p = 0.8 if true, else 0.5
    # Obs: Bernoulli(p) at [1, 1, 0]
    # P(biased=1 | obs) = 0.128 / (0.128 + 0.125) = 128/253
    e5_mean = 128.0 / 253.0
    e5_var = (128.0 / 253.0) * (125.0 / 253.0)
    e5_stddev = math.sqrt(e5_var)
    print(f"Ejemplo 5 (coin-flips)       - Mean: {e5_mean:.6f}, StdDev: {e5_stddev:.6f}")

    # --- EJEMPLO 6: signal-noise ---
    # Prior: x ~ N(0, 1), y ~ N(0, 2), independent. z = x + y ~ N(0, 5)
    # Obs: Normal(z, 0.1) at 3.0 -> w = z + e ~ N(0, 5.01)
    # Posterior x | w = 3.0 is Normal(3.0/5.01, 4.01/5.01)
    e6_mean = 300.0 / 501.0
    e6_var = 401.0 / 501.0
    e6_stddev = math.sqrt(e6_var)
    print(f"Ejemplo 6 (signal-noise)     - Mean: {e6_mean:.6f}, StdDev: {e6_stddev:.6f}")

    # --- EJEMPLO 7: binomial-test ---
    # Identical posterior as Ejemplo 2
    print(f"Ejemplo 7 (binomial-test)    - Mean: {e2_mean:.6f}, StdDev: {e2_stddev:.6f}")

    # --- EJEMPLO 8: exponential ---
    # Prior: lambda ~ Exp(1.0)
    # Obs: Exp(lambda) at 2.0
    # Posterior: Gamma(shape=2, rate=3 => scale=1/3)
    e8_mean = 2.0 / 3.0
    e8_stddev = math.sqrt(2.0 / 9.0)
    print(f"Ejemplo 8 (exponential)      - Mean: {e8_mean:.6f}, StdDev: {e8_stddev:.6f}")

    # --- EJEMPLO 9: uniform ---
    # Prior: x ~ Uniform(0, 10)
    # Obs: Normal(x, 1) at 4.5
    # Posterior is a Truncated Normal on [0, 10] with mu = 4.5, sigma = 1.0
    # Formula for Truncated Normal mean & variance:
    a_limit = (0.0 - 4.5) / 1.0
    b_limit = (10.0 - 4.5) / 1.0
    Z_norm = Phi(b_limit) - Phi(a_limit)
    e9_mean = 4.5 + (phi(a_limit) - phi(b_limit)) / Z_norm
    e9_var = 1.0 + (a_limit * phi(a_limit) - b_limit * phi(b_limit)) / Z_norm - ((phi(a_limit) - phi(b_limit)) / Z_norm) ** 2
    e9_stddev = math.sqrt(e9_var)
    print(f"Ejemplo 9 (uniform)          - Mean: {e9_mean:.6f}, StdDev: {e9_stddev:.6f}")

    # --- EJEMPLO 10: beta ---
    # Prior: p ~ Beta(2, 2)
    # Obs: Bernoulli(p) at 1.0
    # Posterior: Beta(3, 2)
    e10_mean = 3.0 / 5.0
    e10_var = (3.0 * 2.0) / (25.0 * 6.0)
    e10_stddev = math.sqrt(e10_var)
    print(f"Ejemplo 10 (beta)            - Mean: {e10_mean:.6f}, StdDev: {e10_stddev:.6f}")

    # --- EJEMPLO 11: gamma ---
    # Prior: lambda ~ Gamma(shape=2, scale=2)
    # Obs: Exp(lambda) at 2.0
    # Posterior: Gamma(shape=3, scale=0.4)
    e11_mean = 3.0 * 0.4
    e11_var = 3.0 * (0.4 ** 2)
    e11_stddev = math.sqrt(e11_var)
    print(f"Ejemplo 11 (gamma)           - Mean: {e11_mean:.6f}, StdDev: {e11_stddev:.6f}")
    print("==================================================")

if __name__ == "__main__":
    run_exact_calculations()
