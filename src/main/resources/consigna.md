Reescribir la implementación del lenguaje que usamos en la clase del viernes 26/6, pero en otro lenguaje de su preferencia.
El libro de Crista Lopes es una excelente referencia sobre cómo escribir en otro estilo.
También puede ser Python, pero debería ser en un estilo completamente distinto: por ejemplo, más declarativo, más funcional, OOP, etc.
No hace falta escribir un parser. Si necesitan, pueden asumir que tienen el programa ya parseado, como el resultado que les devuelve parse_one en los notebooks:
[let,
[b1,
[if, [<, [sample, [uniform-continuous, 0, 1]], 0.5], 1, 0],
b2,
[if, [<, [sample, [uniform-continuous, 0, 1]], 0.5], 1, 0],
b3,
[if, [<, [sample, [uniform-continuous, 0, 1]], 0.5], 1, 0],
b4,
[if, [<, [sample, [uniform-continuous, 0, 1]], 0.5], 1, 0],
b5,
[if, [<, [sample, [uniform-continuous, 0, 1]], 0.5], 1, 0],
b6,
[if, [<, [sample, [uniform-continuous, 0, 1]], 0.5], 1, 0],
b7,
[if, [<, [sample, [uniform-continuous, 0, 1]], 0.5], 1, 0],
b8,
[if, [<, [sample, [uniform-continuous, 0, 1]], 0.5], 1, 0],
total,
[+, b1, b2, b3, b4, b5, b6, b7, b8]],
[observe, [normal, 7, 1], total],
total]
Mi recomendación es que no usen IA, pero pueden usarla. La condición es que yo tengo que notar que pensaron sobre el problema.
Se pueden agregar cosas que no están en las implementaciones de los notebooks (no es necesario). Por ejemplo, hacer análisis estático para detectar un problema que puede ocurrir al usar SMC: que distintas trazas frenen en distintos lugares.

# Clase 26/6

!git clone https://github.com/jburroni/IntroPPLs26.git

%cd IntroPPLs26/notebooks/Jun-26



# Activity 5 — The message interface and higher-order programs

Two parts, matching the lecture.

**Part 1 — One runtime, many algorithms.** The evaluator emits `sample` and `observe` messages and does no inference itself; a controller answers. Likelihood weighting and SMC are given. You will build a third controller, **single-site Metropolis-Hastings**, over the same runtime.

**Part 2 — Closures and recursion.** First-class functions need closures: a value pairing a function body with the environment where it was defined. You will complete **closure application** in the evaluator's `callk` case.

Run top to bottom. The tests after each part print a number to check against an exact answer.

import os, sys
_p = os.path.abspath(os.getcwd())
while _p != os.path.dirname(_p):
if os.path.isdir(os.path.join(_p, 'interpreters', 'minippl')):
sys.path.insert(0, os.path.join(_p, 'interpreters')); break
_p = os.path.dirname(_p)

import numpy as np
import matplotlib.pyplot as plt
from scipy import stats
from scipy.special import softmax
from minippl import parse, Symbol, PRIMITIVES, is_primitive
from minippl.distributions import Normal, Bernoulli

def log_prob(d, x):
if isinstance(d, Normal):
return float(stats.norm.logpdf(x, d.mu, d.sigma))
if isinstance(d, Bernoulli):
return float(stats.bernoulli.logpmf(int(x), d.p))
raise NotImplementedError(f"no density for {d!r}")

## The stack machine

The control stack `C` and value stack `V` together are the continuation: pausing is returning from `resume`, resuming is calling it again, forking is copying the stacks. `Closure` is a first-class function value; `M` is a suspendable execution.

class Closure:
__slots__ = ('params', 'body', 'env')
def __init__(self, params, body, env):
self.params, self.body, self.env = params, body, env

class M:
def __init__(self, C, V=None, env=None, rng=None, log_w=0.0):
self.C = list(C)
self.V = [] if V is None else list(V)
self.env = {} if env is None else dict(env)
self.rng = rng; self.log_w = log_w
def fork(self, rng=None):
return M(C=list(self.C), V=list(self.V), env=dict(self.env),
rng=self.rng if rng is None else rng, log_w=self.log_w)

## The message-interface runtime (given)

`resume(m)` runs until the next probabilistic effect and returns one of:

```
('sample',  address, distribution, m)
('observe', address, distribution, observed_value, m)
('done',    value, m)
```

At `samplek` and `observek` the evaluator does no inference: it records the address and returns the message. The controller answers with `send(m, value)`, which pushes a value, and resumes.

The runtime is complete except for **one line**: closure application in `callk` (Part 2). Part 1 uses no user-defined functions, so you can run it now and complete `callk` later.

def _push_body(C, body, env, addr):
seq = []
for n, b in enumerate(body[:-1]):
seq.append(('ev', b, env, addr + ('body', n))); seq.append(('discard',))
seq.append(('ev', body[-1], env, addr + ('body', len(body) - 1)))
for item in reversed(seq):
C.append(item)

def resume(m):
C, V = m.C, m.V
while C:
instr = C.pop(); t = instr[0]
if t == 'ev':
_, e, env, addr = instr
if isinstance(e, Symbol):
if e in env: V.append(env[e])
elif is_primitive(e): V.append(PRIMITIVES[e])
else: raise NameError(e)
elif not isinstance(e, list):
V.append(e)
else:
head = e[0]
if head == 'let':
binds, body = e[1], e[2:]
if binds:
C.append(('letk', binds, 0, body, env, addr)); C.append(('ev', binds[1], env, addr + ('let', 0)))
else:
_push_body(C, body, env, addr)
elif head == 'if':
_, test, then, els = e
C.append(('ifk', then, els, env, addr)); C.append(('ev', test, env, addr + ('test',)))
elif head == 'fn':
_, params, *body = e
V.append(Closure(params, body, env))
elif head == 'sample':
C.append(('samplek', addr)); C.append(('ev', e[1], env, addr + ('d',)))
elif head == 'observe':
C.append(('observek', addr)); C.append(('ev', e[2], env, addr + ('v',))); C.append(('ev', e[1], env, addr + ('d',)))
else:
C.append(('callk', len(e) - 1, addr))
for i in range(len(e) - 1, 0, -1): C.append(('ev', e[i], env, addr + (i - 1,)))
C.append(('ev', e[0], env, addr + ('fn',)))
elif t == 'letk':
_, binds, i, body, env, addr = instr
env = dict(env); env[binds[2*i]] = V.pop()
if 2*(i+1) < len(binds):
C.append(('letk', binds, i+1, body, env, addr)); C.append(('ev', binds[2*(i+1)+1], env, addr + ('let', 2*(i+1))))
else:
_push_body(C, body, env, addr)
elif t == 'ifk':
_, then, els, env, addr = instr
branch, tag = (then, 'then') if V.pop() else (els, 'else')
C.append(('ev', branch, env, addr + (tag,)))
elif t == 'discard':
V.pop()
elif t == 'callk':
_, n, addr = instr
args = [V.pop() for _ in range(n)][::-1]; f = V.pop()
if isinstance(f, Closure):
# Do something with f.env and f.params
raise NotImplementedError('complete closure application in callk')
_push_body(C, f.body, new_env, addr)
else:
V.append(f(*args))
elif t == 'samplek':
_, addr = instr
d = V.pop()
return ('sample', addr, d, m)
elif t == 'observek':
_, addr = instr
y = V.pop(); d = V.pop()
return ('observe', addr, d, y, m)
return ('done', V[-1], m)

def send(m, value):
m.V.append(value)

def initial_machine(program, rng):
genv = {}; main = None
for form in parse(program):
if isinstance(form, list) and form and form[0] == 'defn':
_, name, params, *body = form
genv[name] = Closure(params, body, genv)
else:
main = form
return M([('ev', main, genv, ())], env=genv, rng=rng)

## Likelihood weighting and SMC controllers (given)

Two controllers over the message stream. At `sample` LW draws from the prior; at `observe` it accumulates the log-weight. SMC keeps many machines, advancing each to its next breakpoint, then scoring, resampling, and forking. You will add a third controller below.

def run_lw(program, rng):
m = initial_machine(program, rng)
while True:
msg = resume(m); tag = msg[0]
if tag == 'done':
_, value, m = msg; return value, m.log_w
if tag == 'sample':
_, a, d, m = msg; send(m, d.sample(m.rng))
elif tag == 'observe':
_, a, d, y, m = msg; m.log_w += log_prob(d, y); send(m, y)

def likelihood_weighting(program, rng, N):
values, log_w = zip(*(run_lw(program, rng) for _ in range(N)))
return np.array(values, dtype=float), softmax(np.array(log_w))

def advance(m):
msg = resume(m)
while msg[0] == 'sample':
_, a, d, m = msg; send(m, d.sample(m.rng)); msg = resume(m)
return msg

def run_smc(program, rngs, N):
particles = [initial_machine(program, rngs[i]) for i in range(N)]
while True:
messages = [advance(p) for p in particles]
tags = {msg[0] for msg in messages}
if tags == {'done'}:
return np.array([float(msg[1]) for msg in messages])
if tags != {'observe'}:
raise ValueError("particles reached different breakpoints: SMC needs a shared observe sequence")
# It might happen that the observe are not the same. We could improve that
log_inc, paused = [], []
for msg in messages:
_, a, d, y, m = msg
lp = log_prob(d, y); m.log_w += lp
log_inc.append(lp); send(m, y); paused.append(m)
anc = rngs[0].choice(N, size=N, p=softmax(np.array(log_inc)))
particles = [paused[i].fork(rng=rngs[j]) for j, i in enumerate(anc)]

---
# Part 1: Build the single-site MH controller

Single-site MH keeps an **address-keyed trace**. Each step picks one address, **resamples** that site, **reuses** the rest by address, re-runs, and accepts on the Metropolis-Hastings ratio.

The dimension-corrected ratio `mh_log_alpha` is given (it is the single-site ratio with prior proposals: the proposal densities at the resampled and newly reached sites cancel against the prior, leaving the reused sites, the observes, and a `1/n` term for the change in trace length).

**Your task:** complete two lines.
1. In `run`, the `sample` reply policy: resample the selected or newly reached site, otherwise reuse by address.
2. In `single_site_mh`, the propose-and-accept step.

The point of this activity is not to rederive the ratio, but to see that the controller has enough information to compute it from the two address-keyed traces.

def mh_log_alpha(X, X2, S, S2, O, O2, a0):
"""Single-site MH log acceptance ratio (given).
X, X2  : address -> value, current and proposed traces
S, S2  : address -> log_prob at sample sites
O, O2  : address -> log_prob at observe sites
a0     : the resampled address
"""
fwd = {a0} | (set(X2) - set(X))     # resampled + newly required sites
rev = {a0} | (set(X)  - set(X2))    # resampled + dropped sites
num = sum(p for k, p in S2.items() if k not in fwd) + sum(O2.values())
den = sum(p for k, p in S.items()  if k not in rev) + sum(O.values())
return (np.log(len(X)) - np.log(len(X2))) + (num - den)

def run(program, rng, x0, cache):
"""One execution over the message interface.
x0       : the address to redraw (or None)
cache    : {address: value} from the current trace, to reuse
Returns (value, X, S, O): trace values, sample log-probs, observe log-probs.
"""
m = initial_machine(program, rng); X, S, O = {}, {}, {}
while True:
msg = resume(m); tag = msg[0]
if tag == 'sample':
_, a, d, m = msg
# --- YOUR CODE HERE ---
# What is missing form Algorithm 14?
raise NotImplementedError("write the sample reply policy")
X[a] = x; S[a] = log_prob(d, x); send(m, x)
elif tag == 'observe':
_, a, d, y, m = msg
O[a] = log_prob(d, y); send(m, y)
elif tag == 'done':
_, value, m = msg
return value, X, S, O

def single_site_mh(program, rng, steps, warmup=2000):
value, X, S, O = run(program, rng, None, {})    # initial trace: nothing to resample or reuse
chain = []
for i in range(steps + warmup):
a0 = list(X)[int(rng.integers(len(X)))]                       # pick one site to change
# --- YOUR CODE HERE ---
# 1. propose: re-run with x0=a0, reusing the current trace X as cache
# 2. accept with probability exp(mh_log_alpha(...)): if accepted, replace value, X, S, O
raise NotImplementedError("write the propose-and-accept step")
if i >= warmup:
chain.append(float(value))
return np.array(chain, dtype=float)

### Test: conjugate and a discrete-sum model

Conjugate: prior $\mu \sim \mathcal{N}(0,1)$, likelihood $y\mid\mu \sim \mathcal{N}(\mu,1)$, $y=2.3$. Exact posterior $\mathcal{N}(1.15, 0.5)$.

Bit-sum: eight fair coins, `(observe (normal 7 2) total)`. Exact mean below.

conj = "(let [mu (sample (normal 0 1))] (observe (normal mu 1) 2.3) mu)"
ch = single_site_mh(conj, np.random.default_rng(0), 60000, warmup=3000)
print(f"conj   SSMH mean = {ch.mean():.3f}  std = {ch.std():.3f}   (exact 1.150, {0.5**0.5:.3f})")

import math
from math import comb
bits = "(let [" + " ".join(f"b{i} (if (sample (bernoulli 0.5)) 1 0)" for i in range(1, 9)) \
+ " total (+ " + " ".join(f"b{i}" for i in range(1, 9)) + ")]" \
+ " (observe (normal 7 2) total) total)"
w = {k: comb(8, k) * math.exp(-0.5 * ((k - 7) / 2) ** 2) for k in range(9)}
exact = sum(k * w[k] for k in w) / sum(w.values())
ch2 = single_site_mh(bits, np.random.default_rng(1), 40000, warmup=3000)
print(f"bits   SSMH mean = {ch2.mean():.3f}   (exact {exact:.3f})")

### One model, three controllers

The same conjugate posterior, recovered by LW, SMC, and your SSMH over the one runtime.

vals, w_lw = likelihood_weighting(conj, np.random.default_rng(2), 100000)
lw_mean = float((w_lw * vals).sum())
smc = run_smc(conj, [np.random.default_rng(1000 + i) for i in range(20000)], 20000)
print(f"LW   mean = {lw_mean:.3f}")
print(f"SMC  mean = {smc.mean():.3f}")
print(f"SSMH mean = {ch.mean():.3f}    (all exact 1.150, one runtime)")

---
# Part 2: Complete closure application

`geom` and the closure example need **user-defined functions**, which the runtime applies in the `callk` case. That case is the one gap left in `resume`: it starts a new environment from the closure's captured `env`, but does not yet bind the parameters.

**Your task:** in the runtime cell above, complete the `callk` Closure branch:


Then re-run that cell and the checks below. The bindings extend the **closure's** environment, not the caller's: that is lexical scope.


### Check: closures and recursion

With `callk` completed, a returned closure remembers `mu` (lexical scope), and the geometric program recurses a random number of times. These should print `13` and a mean near `2.33`.


shift = "(let [make-shift (fn [mu] (fn [x] (+ x mu)))  f (make-shift 10)] (f 3))"
print("closure: (f 3) =", run_lw(shift, np.random.default_rng(0))[0], " (expect 13)")

geom = "(defn geom [] (if (sample (bernoulli 0.3)) 0 (+ 1 (geom)))) (geom)"
rng = np.random.default_rng(1)
ks = np.array([run_lw(geom, rng)[0] for _ in range(200000)])
print(f"geom mean = {ks.mean():.3f}   exact (1-p)/p = {0.7/0.3:.3f}")