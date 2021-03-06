# Jordle

Java wordle solver implementation following along with the ["roget" implementation stream](https://www.youtube.com/watch?v=doFowk4xj7Q) (in Rust),
which implements the 3blue1brown algorithm ([video1](https://www.youtube.com/watch?v=v68zYyaEmEA), 
[video2](https://www.youtube.com/watch?v=fRed0Xmc2Wg&t=556s)). 
The implementation was optimized a bit along the way.

## Variants

Currently, there are two algorithmic variants of the wordle solver: 1) Entropy (3b1b) and 2) MostFreq.

### Entropy

Follows the 3blue1brown algorithm to choose guesses based on the expected gained information of each word.

### Mostfreq

Simple guesser that chooses the word with the highest frequency in the dictionary.

## Scores & Performance

The scores as computed against all answers with a max. of 6 guesses each (which is configurable in the source):

- Entropy: avg score: 3.892023, solved: 98.27%
- Mostfreq: avg score: 4.277778, solved: 96.67%

```bash
'mostfreq' ran 3.19 ± 0.10 times faster than 'entropy'
```

So, the Entropy strategy implementation is better but takes nearly 400% as long!


## Build & Run

```bash
mvn --quiet clean package
bin/jordle
```

## Benchmarking

Use [*hyperfine*](https://github.com/sharkdp/hyperfine) to compare different implementations.


```bash
⌘  hyperfine -w 0 -n entropy 'bin/jordle -a entropy 64' -n mostfreq 'bin/jordle -a mostfreq 64'
Benchmark 1: entropy
  Time (mean ± σ):      1.408 s ±  0.044 s    [User: 1.439 s, System: 0.265 s]
  Range (min … max):    1.357 s …  1.507 s    10 runs

Benchmark 2: mostfreq
  Time (mean ± σ):     441.3 ms ±   4.1 ms    [User: 615.8 ms, System: 106.6 ms]
  Range (min … max):   437.2 ms … 449.8 ms    10 runs

Summary
  'mostfreq' ran
    3.19 ± 0.10 times faster than 'entropy'
```


## TODOs

- [ ] Other algorithms/implementations from the stream.
- [ ] Use test cases from [wordle-tests](https://github.com/yukosgiti/wordle-tests)
- [ ] Parallelization of candidate evaluation (not games, which is rather trivial).


## GraalVM native-image

I tested the implementation under various different GraalVM versions/JDKs: the "old" JDK under Rosetta and a 
pre-release for the M1 ARM architecture.
Benchmark results comparing the ARM version compiled to an executable and "standard JAR" can be found in the [`plot.ipynb`](bench/plot.ipynb).

### Rosetta (Intel-JDK)

- Version: CE 22.0.0.2 (build 17.0.2+8-jvmci-22.0-b05)

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

The Rosetta `native-image` build takes around 1m 10s on the M1 MacBook Air.

```fish
⌘  file jordle
jordle: Mach-O 64-bit executable x86_64
⌘  du -h jordle
 15M	jordle
```

NB: "DARWIN does not support building static executable images."

### Darwin+aarch64 (M1 Native JDK)

Steps to reproduce below.

Note: I'm using `fish` instead of `bash`!
Also, I'm setting it up as a custom asdf Java version.

1. Download current preview and extract:
   ```fish
   $ pushd $HOME/.asdf/installs/java
   $ curl -sLO https://github.com/graalvm/graalvm-ce-dev-builds/releases/download/22.1.0-dev-20220321_2332/graalvm-ce-java17-darwin-aarch64-dev.tar.gz
   $ tar xzvf graalvm-ce-java17-darwin-aarch64-dev.tar.gz
   ```
2. Set up for asdf:
   ```fish
   $ cd graalvm-ce-java17-22.1.0-dev
   $ for d in (ls Contents/Home); ln -s "Contents/Home/$d" $d; end
   $ asdf reshim
   ```
3. Make sure it's listed and go back to the code directory:
   ```fish
   $ asdf list java
   $ popd
   ```
4. Configure as local version and test: 
   ```fish
   $ asdf local java graalvm-ce-java17-22.1.0-dev
   $  java -version
   openjdk version "17.0.3" 2022-04-19
   OpenJDK Runtime Environment GraalVM CE 22.1.0-dev (build 17.0.3+4-jvmci-22.1-b03)
   OpenJDK 64-Bit Server VM GraalVM CE 22.1.0-dev (build 17.0.3+4-jvmci-22.1-b03, mixed mode, sharing)
   ```
5. Install native-image: 
   ```fish
   $ gu install native-image
   $ asdf reshim
   ```
7. Package the project (`mvn clean package`) and compile to a native image:
   ```fish
   $ native-image -jar target/release/lib/jordle.jar --install-exit-handlers -H:IncludeResources='.*\.txt$' -H:+ReportUnsupportedElementsAtRuntime
   ```

The native `native-image` build takes around 21s on the M1 MacBook Air.

```fish
⌘  file jordle
jordle: Mach-O 64-bit executable x86_64
⌘  du -h jordle
 15M	jordle
```

NB: "DARWIN does not support building static executable images."


## [Flamegraphs](https://github.com/brendangregg/FlameGraph)

Using [async-profiler](https://github.com/jvm-profiling-tools/async-profiler/), we can generate flamegraphs pretty easily. 
[Unfortunately](https://github.com/jvm-profiling-tools/async-profiler/#restrictionslimitations), on macOS it seems we're limited to user-space code only :(.
Nonetheless, they provide interesting introspection into the running code.

For example, to evaluate a currently running `jordle` (from the `bin` script, which sets some necessary JVM args), execute:
```fish
../async-profiler-2.7-macos/profiler.sh -d 10 -t -f perf-out.html (jps | awk -F ' ' '/jordle/ { print $1}')
```

After the 10 second sample time, you can then open `perf-out.html` in the browser to inspect your flamegraph.

