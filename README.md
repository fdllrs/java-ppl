# Java-PPL: Stack Machine-based Probabilistic Programming Language

Java implementation of a continuation-passing style stack machine. It decouples the evaluation of probabilistic programs
from inference algorithms using a structured messaging interface.

This engine was developed following the concepts of probabilistic programming as detailed in the textbook *An
Introduction to Probabilistic Programming* by Jan-Willem van de Meent, Brooks Paige, David Tolpin, and Frank Wood.

---

## Theoretical Principles & Architecture

Following the textbook conventions, this engine decouples model definition from the inference engine. The probabilistic
program is treated as a referentially transparent, inference-agnostic computation that cedes control to an external
controller when stochastic side effects occur.

### 1. Separation of Model and Inference

The model specifies the joint probability distribution of the latent variables and observed data. The evaluator
processes standard deterministic AST nodes automatically but suspends execution when a stochastic checkpoint (`sample`
or `observe`) is reached, returning a message to the active inference controller.

### 2. The Messaging Interface

When the evaluator is suspended, it returns a message payload representing the checkpoint. The possible messages defined
in [messaging](src/main/java/messaging) are:

* **`Sample(Address, Distribution)`**: Requests a sample from a prior distribution.
* **`Observe(Address, Distribution, Value)`**: Conditions the model by evaluating the likelihood of the observed value
  under the distribution.
* **`Done(Value)`**: Signifies that the program has finished execution and returned a final value.
* **`Fork()`**: Instructs the inference engine to duplicate the execution path (used in Sequential Monte Carlo).

### 3. Explicit Stack Machine

[Machine.java](src/main/java/core/Machine.java) maintains the following
control structures:

* **Control Stack (`C`)**: Stores instructions and expressions to be evaluated.
* **Value Stack (`V`)**: Stores intermediate evaluation results.
* **Environment (`Environment`)**: Resolves symbol bindings.

---

## Syntax & Primitives

The parser accepts the Lisp-like syntax defined in the book.

* `let`: Evaluates a series of bindings and executes a body.
    * Example: `(let [x 1.0] (+ x 2.0))`
* `if`: Evaluates a test expression to branch into a `then` or `else` execution.
    * Example: `(if (> x 0) 1 0)`
* `fn`: Defines an anonymous function with parameters and a body.
    * Example: `((fn [x] (* x 2)) 3)`
* `defn`: Defines a named function.
    * Example: `(defn double [x] (* x 2))`

### Operations

`+`, `-`, `*`, `/`, `<`, `>`, `==`

### Probabilistic Operations

* `(sample <distribution>)`: Draws a value from the specified distribution.
* `(observe <distribution> <value>)`: Conditions the model by evaluating the log-probability of the observed data.

### Implemented Distributions

* `(normal mu sigma)`: Normal distribution with mean `mu` and standard deviation `sigma`. Implemented
  in [Normal.java](src/main/java/distributions/Normal.java).
* `(bernoulli p)`: Bernoulli distribution with success probability `p` between 0 and 1. Implemented
  in [Bernoulli.java](src/main/java/distributions/Bernoulli.java).
* `(binomial n p)`: Binomial distribution with trials `n` and success probability `p`. Implemented
  in [Binomial.java](src/main/java/distributions/Binomial.java).
* `(exponential rate)`: Exponential distribution with lambda `rate`. Implemented
  in [Exponential.java](src/main/java/distributions/Exponential.java).
* `(uniform min max)`: Uniform continuous distribution between `min` and `max`. Implemented
  in [UniformContinuous.java](src/main/java/distributions/UniformContinuous.java).
* `(beta alpha beta)`: Beta distribution with shape parameters `alpha` and `beta`. Implemented
  in [Beta.java](src/main/java/distributions/Beta.java).
* `(gamma shape scale)`: Gamma distribution with shape parameter `shape` and scale parameter `scale`. Implemented
  in [Gamma.java](src/main/java/distributions/Gamma.java).
* `(poisson lambda)`: Poisson distribution with rate/mean `lambda`. Implemented
  in [Poisson.java](src/main/java/distributions/Poisson.java).

---

## Inference Engines

The project implements three standard probabilistic inference algorithms:

1. **Likelihood Weighting (LW)**: Samples directly from prior distributions and accumulates weights at each observation
   checkpoint based on the likelihood. Implemented
   in [LikelihoodWeighting.java](src/main/java/inference/LikelihoodWeighting.java).
2. **Single-Site Metropolis-Hastings (SSMH)**: An MCMC algorithm that perturbs a single sample site at each step and
   re-executes the stack machine, accepting/rejecting based on the log-acceptance ratio. Implemented
   in [SSMetropolisHastings.java](src/main/java/inference/SSMetropolisHastings.java).
3. **Sequential Monte Carlo (SMC)**: A particle filtering method that runs multiple stack machines (*particles*) in
   parallel, suspending them at observations, and performing systematic resampling when weights degenerate. Implemented
   in [SequentialMonteCarlo.java](src/main/java/inference/SequentialMonteCarlo.java).

---

## Build & Run Instructions

### Prerequisites

* **Java JDK 21** or higher
* **Gradle** (wrapper included)
* **Python 3.x** (for exact analytical metric comparisons)

### Compilation & Building the JAR

To compile the class files and run the entire build lifecycle (including testing):

```bash
./gradlew build
```

To build the executable "fat" JAR containing all runtime dependencies:

```bash
./gradlew jar
```

This generates the main executable JAR at `build/libs/java-ppl.jar`.

### Running the Engine (CLI Usage)

The project provides a Picocli-based CLI implemented in [CLI.java](src/main/java/core/CLI.java).

You can run the engine directly using the Gradle wrapper:

```bash
./gradlew run --args="run -f <path-to-model.txt> [options]"
```

Or you can run the pre-built executable JAR directly with `java`:

```bash
java -jar build/libs/java-ppl.jar run -f <path-to-model.txt> [options]
```

#### CLI Options

* `-f`, `--file <file>`: **(Required)** Path to the text file containing the HOPPL code.
* `-a`, `--algorithm <name>`: The inference algorithm. Choose between `lw`, `ssmh` (or `mh`), and `smc`. (Default:
  `smc`).
* `-p`, `--particles <number>` or `-i`, `--iterations <number>`: Number of particles (for SMC) or iterations (for LW/MH)
  to run. (Default: `10000`).
* `-w`, `--warmup <number>`: Number of warmup/burn-in iterations (MH only). (Default: `1000`).
* `-s`, `--seed <number>`: Sets a fixed random seed for reproducible runs.

#### Examples

Run a normal-normal conjugate model using **Sequential Monte Carlo** with 5,000 particles:

```bash
./gradlew run --args="run -f src/main/resources/models/normalNormalConjugate.txt -a smc --particles 5000"
```

Run a noisy Bernoulli sum model using **Likelihood Weighting** with 20,000 iterations:

```bash
./gradlew run --args="run -f src/main/resources/models/noisyBernoulliSum.txt -a lw --iterations 20000"
```

Run a coin flip selection model using **Metropolis-Hastings** with a fixed seed:

```bash
./gradlew run --args="run -f src/main/resources/models/coinFlipSelection.txt -a ssmh --iterations 25000 -w 2000 -s 42"
```

### Running Tests and Benchmarks

* **Run Unit Tests**: Execute all compiler, parsing, and convergence tests:
  ```bash
  ./gradlew test
  ```
* **Run Benchmarks**: Run the performance benchmarks to evaluate speed and convergence speed:
  ```bash
  ./gradlew benchmark
  ```
* **Run Analytical Exact Metrics**: Calculate the exact mean and standard deviation of the example models (used for
  testing):
  ```bash
  python calculate_exact.py
  ```

---

## Heuristic Validation Suite

To validate the correctness of the Java inference algorithms, the implementation is evaluated against exact theoretical
means and standard deviations.

### Example Models with Analytical Solutions

#### 1. Normal-Normal Conjugate Model (Continuous Parameters)

* **File**: [normalNormalConjugate.txt](src/main/resources/models/normalNormalConjugate.txt)
* **Syntax**:
  ```clojure
  (let [mu (sample (normal 0 1))]
    (observe (normal mu 1) 2.3)
    mu)
  ```
* A simple continuous parameter estimation where a normal prior is updated with a single normal observation.
* **Exact Mean**: `1.150` | **Exact StdDev**: `0.707`

#### 2. Sum of Bernoullis (Combinatorial Latents)

* **File**: [noisyBernoulliSum.txt](src/main/resources/models/noisyBernoulliSum.txt)
* **Syntax**:
  ```clojure
  (let [b1 (if (sample (bernoulli 0.5)) 1 0)
        b2 (if (sample (bernoulli 0.5)) 1 0)
        b3 (if (sample (bernoulli 0.5)) 1 0)
        b4 (if (sample (bernoulli 0.5)) 1 0)
        b5 (if (sample (bernoulli 0.5)) 1 0)
        b6 (if (sample (bernoulli 0.5)) 1 0)
        b7 (if (sample (bernoulli 0.5)) 1 0)
        b8 (if (sample (bernoulli 0.5)) 1 0)
        total (+ b1 b2 b3 b4 b5 b6 b7 b8)]
    (observe (normal 7 2) total)
    total)
  ```
* 8 independent Bernoulli trials sum to a discrete value, observed under a noisy normal. Requires proper proposals
  across the 256 state space transitions.
* **Exact Mean**: `5.014` | **Exact StdDev**: `1.146`

#### 3. Multi-Observation Normal-Normal Model

* **File**: [multiObsNormalNormal.txt](src/main/resources/models/multiObsNormalNormal.txt)
* **Syntax**:
  ```clojure
  (let [mu (sample (normal 0 1))]
    (observe (normal mu 1) 2.3)
    (observe (normal mu 1) 1.7)
    mu)
  ```
* A continuous parameter estimation where a normal prior is updated with multiple normal observations.
* **Exact Mean**: `1.333` | **Exact StdDev**: `0.577`

#### 4. High Variance Normal Prior Model

* **File**: [highVarianceNormalPrior.txt](src/main/resources/models/highVarianceNormalPrior.txt)
* **Syntax**:
  ```clojure
  (let [mu (sample (normal 1 2))]
    (observe (normal mu 3) 5.0)
    mu)
  ```
* A continuous parameter estimation with a high-variance normal prior updated with a single normal observation.
* **Exact Mean**: `2.231` | **Exact StdDev**: `1.664`

#### 5. Coin Flip Selection (Stochastic Control Flow)

* **File**: [coinFlipSelection.txt](src/main/resources/models/coinFlipSelection.txt)
* **Syntax**:
  ```clojure
  (let [biased (sample (bernoulli 0.5))
        p (if biased 0.8 0.5)]
    (observe (bernoulli p) 1)
    (observe (bernoulli p) 1)
    (observe (bernoulli p) 0)
    (if biased 1 0))
  ```
* Tests stochastic branching. A Bernoulli flip selects a coin type (biased vs fair), changing downstream observation
  likelihoods and execution paths.
* **Exact Mean**: `0.506` | **Exact StdDev**: `0.500`

#### 6. Signal-Noise Sum Model

* **File**: [signalNoiseSum.txt](src/main/resources/models/signalNoiseSum.txt)
* **Syntax**:
  ```clojure
  (let [x (sample (normal 0 1))
        y (sample (normal 0 2))
        z (+ x y)]
    (observe (normal z 0.1) 3.0)
    x)
  ```
* A model representing the sum of two independent continuous normal variables under a noisy observation.
* **Exact Mean**: `0.599` | **Exact StdDev**: `0.895`

#### 7. Noisy Binomial Model

* **File**: [noisyBinomial.txt](src/main/resources/models/noisyBinomial.txt)
* **Syntax**:
  ```clojure
  (let [total (sample (binomial 8 0.5))]
    (observe (normal 7 2) total)
    total)
  ```
* A discrete binomial prior observed under a noisy normal likelihood, equivalent to the combinatorial bit-sum model but
  using the Binomial primitive.
* **Exact Mean**: `5.014` | **Exact StdDev**: `1.146`

#### 8. Exponential-Exponential Conjugate Model

* **File**: [exponentialExponentialConjugate.txt](src/main/resources/models/exponentialExponentialConjugate.txt)
* **Syntax**:
  ```clojure
  (let [lambda (sample (exponential 1.0))]
    (observe (exponential lambda) 2.0)
    lambda)
  ```
* A conjugate model where an Exponential prior on the rate parameter is updated with an Exponential observation.
* **Exact Mean**: `0.667` | **Exact StdDev**: `0.471`

#### 9. Uniform-Normal Model (Bounded Continuous Prior)

* **File**: [uniformNormal.txt](src/main/resources/models/uniformNormal.txt)
* **Syntax**:
  ```clojure
  (let [x (sample (uniform 0 10))]
    (observe (normal x 1) 4.5)
    x)
  ```
* Tests continuous bounds. The posterior is a truncated normal distribution on `[0, 10]`.
* **Exact Mean**: `4.500` | **Exact StdDev**: `1.000`

#### 10. Beta-Bernoulli Conjugate Model

* **File**: [betaBernoulliConjugate.txt](src/main/resources/models/betaBernoulliConjugate.txt)
* **Syntax**:
  ```clojure
  (let [p (sample (beta 2.0 2.0))]
    (observe (bernoulli p) 1.0)
    p)
  ```
* A conjugate model where a Beta prior on a success probability is updated with a Bernoulli observation.
* **Exact Mean**: `0.600` | **Exact StdDev**: `0.200`

#### 11. Gamma-Exponential Conjugate Model

* **File**: [gammaExponentialConjugate.txt](src/main/resources/models/gammaExponentialConjugate.txt)
* **Syntax**:
  ```clojure
  (let [lambda (sample (gamma 2.0 2.0))]
    (observe (exponential lambda) 2.0)
    lambda)
  ```
* A conjugate model where a Gamma prior on the rate parameter is updated with an Exponential observation.
* **Exact Mean**: `1.200` | **Exact StdDev**: `0.693`

#### 12. Gamma-Poisson Model (Discrete Observations with Continuous Rate Prior)

* **File**: [gammaPoissonConjugate.txt](src/main/resources/models/gammaPoissonConjugate.txt)
* **Syntax**:
  ```clojure
  (let [lambda (sample (gamma 2.0 2.0))]
    (observe (poisson lambda) 3.0)
    lambda)
  ```
* A conjugate model where a Gamma prior on the rate parameter is updated with a Poisson observation.
* **Exact Mean**: `3.333` | **Exact StdDev**: `1.491`

### Full Analytical Baselines

All 12 models packaged in `src/main/resources/models` converge to the following analytical metrics (asserted
in [ExampleProgramsTest.java](src/test/java/core/ExampleProgramsTest.java)):

| #  | Model Name                        | Expected Mean | Expected StdDev | Description                            |
|----|-----------------------------------|---------------|-----------------|----------------------------------------|
| 1  | `normalNormalConjugate`           | `1.150`       | `0.707`         | Normal-Normal conjugate                |
| 2  | `noisyBernoulliSum`               | `5.014`       | `1.146`         | Combinatorial 8-bit sum                |
| 3  | `multiObsNormalNormal`            | `1.333`       | `0.577`         | Normal-Normal with 2 observations      |
| 4  | `highVarianceNormalPrior`         | `2.231`       | `1.664`         | Normal-Normal with wide prior variance |
| 5  | `coinFlipSelection`               | `0.506`       | `0.500`         | Selection of coin biased vs fair       |
| 6  | `signalNoiseSum`                  | `0.599`       | `0.895`         | Sum of two continuous normals          |
| 7  | `noisyBinomial`                   | `5.014`       | `1.146`         | Equivalent to bit-sum, using Binomial  |
| 8  | `exponentialExponentialConjugate` | `0.667`       | `0.471`         | Exponential-Exponential conjugate      |
| 9  | `uniformNormal`                   | `4.500`       | `1.000`         | Bounded Uniform continuous prior       |
| 10 | `betaBernoulliConjugate`          | `0.600`       | `0.200`         | Beta-Bernoulli conjugate               |
| 11 | `gammaExponentialConjugate`       | `1.200`       | `0.693`         | Gamma-Exponential conjugate            |
| 12 | `gammaPoissonConjugate`           | `3.333`       | `1.491`         | Gamma-Poisson conjugate                |
