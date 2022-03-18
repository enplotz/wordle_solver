# Jordle

Java wordle solver implementation following along with the "roget" implementation stream (in Rust),
which implements the 3blue1brown algorithm (sorta, I guess) and optimized it along the way.

## Algorithms

- `Naive.java`: first algorithm enumerating patterns and calculating entropy. avg score: 1.960383
- `NaiveParallel.java`: simple parallel stream version of `Naive.java`. avg score: 1.954961
- `MostFreq.java`: guess most-frequent remaining word. avg score: 2.102621

## Benchmarking

Use [*hyperfine*](https://github.com/sharkdp/hyperfine) to compare different implementations.


```bash
⌘  hyperfine -n mostfreq 'bin/jordle -a mostfreq 1' -n pnaive 'bin/jordle -a pnaive 1' -n naive 'bin/jordle -a naive 1'
Benchmark 1: mostfreq
  Time (mean ± σ):     235.6 ms ±   1.7 ms    [User: 316.8 ms, System: 95.3 ms]
  Range (min … max):   232.9 ms … 238.5 ms    12 runs

Benchmark 2: pnaive
  Time (mean ± σ):     526.0 ms ±  13.1 ms    [User: 1524.4 ms, System: 675.6 ms]
  Range (min … max):   509.9 ms … 553.5 ms    10 runs

Benchmark 3: naive
  Time (mean ± σ):     806.1 ms ±  16.0 ms    [User: 724.2 ms, System: 236.1 ms]
  Range (min … max):   790.8 ms … 836.7 ms    10 runs

Summary
  'mostfreq' ran
    2.23 ± 0.06 times faster than 'pnaive'
    3.42 ± 0.07 times faster than 'naive'
```


## TODOs

- [ ] Other algorithms/implementations from the stream.
- [ ] Use test cases from [wordle-tests](https://github.com/yukosgiti/wordle-tests)
- [ ] Parallelization of candidate evaluation (not games, which is rather trivial).


## GraalVM native-image (Rosetta)

- Version: CE 22.0.0.2 (build 17.0.2+8-jvmci-22.0-b05)
- Rosetta: yes, since darwin+aarch64 (aka. native M1) support has not yet been released 

Using GraalVM native-image under Rosetta to compile to a "native" executable leads
to the following observations.
When comparing the 'mostfreq' algorithm on different platforms:

- 'native' (Rosetta) is **9.08 ± 1.34 ✕** faster than running under Rosetta GraalVM JVM
- 'native' (Rosetta) is **1.51 ± 0.03 ✕** faster than running under aarch64 JVM build 17.0.1+12-39

```bash
hyperfine --warmup 2 -n mostfreq 'bin/jordle -a mostfreq 1' -n mostfreq-native './jordle -a mostfreq 1'
```

Steps to reproduce:

1. Install [GraalVM under homebrew](https://github.com/graalvm/homebrew-tap#homebrew-tap-for-graalvm) using a [Rosetta-enabled Terminal.app](https://apple.stackexchange.com/a/428769) (`arch` should print `i386`).
   It seems that this terminal is only used for installing via homebrew, such that it does not complain about a missing build for the arm64 architecture.
   Apparently, GraalVM 22.1 (or 22.2 latest) will support M1 natively.
2. Remove quarantine flag: `sudo xattr -r -d com.apple.quarantine /path/to/graal`.
3. Set `PATH` to include the GraalVM binaries in `bin` and `JAVA_HOME` to point to GraalVM `Home`.
   I recommend doing this temporarily, so you can more easily experiment and do not hose your system.
4. Make sure to check the current Java version `java -version`. It should include "GraalVM".
5. Install native-image: `gu install native-image`.
6. Package the project (`mvn clean package`) and compile to a native image:
   ```bash
   native-image -jar target/release/lib/jordle.jar --install-exit-handlers -H:IncludeResources='.*\.txt$' -H:+ReportUnsupportedElementsAtRuntime
   ```

The native-image build takes around 1m 10s on the M1 MacBook Air.

```bash
⌘  file jordle
jordle: Mach-O 64-bit executable x86_64
⌘  du -h jordle
 15M	jordle
```

NB: "DARWIN does not support building static executable images."

### [Flamegraphs](https://github.com/brendangregg/FlameGraph)

Using [async-profiler](https://github.com/jvm-profiling-tools/async-profiler/), we can generate flamegraphs pretty easily. 
[Unfortunately](https://github.com/jvm-profiling-tools/async-profiler/#restrictionslimitations), on macOS it seems we're limited to user-space code only :(.
Nonetheless, they provide interesting introspection into the running code.

For example, to evaluate a currently running `jordle` (from the `bin` script, which sets some necessary JVM args), execute:
```bash
../async-profiler-2.7-macos/profiler.sh -d 10 -t -f perf-out.html (jps | awk -F ' ' '/jordle/ { print $1}')
```

After the 10 second sample time, you can then open `perf-out.html` in the browser to inspect your flamegraph.
