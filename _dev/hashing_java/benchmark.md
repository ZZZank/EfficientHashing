code:

```java
public class VecHashingBenchmark {

    @Benchmark
    public void vanilla(Blackhole bh) {
        for (var pos : BlockPos.betweenClosed(-100, -20, -100, 100, 50, 100)) {
            bh.consume(pos.hashCode());
        }
    }

    @Benchmark
    public void mixin(Blackhole bh) {
        for (var pos : BlockPos.betweenClosed(-100, -20, -100, 100, 50, 100)) {
            bh.consume(PhiMix.mix(PhiMix.mix(pos.getX()) + pos.getY()) + pos.getZ());
        }
    }

    public static void main(String[] args) throws RunnerException {
        var option = new OptionsBuilder()
            .include(VecHashingBenchmark.class.getSimpleName())
            .warmupIterations(2)
            .forks(1)
            .build();
        new Runner(option).run();
    }
}
```

result:

```
# JMH version: 1.37
# VM version: JDK 21.0.8.0.8, OpenJDK 64-Bit Server VM, 21.0.8.0.8
# VM invoker: D:\_programs\Javas\dragonwell21\bin\java.exe
# VM options: -Dfile.encoding=UTF-8 -Duser.country=CN -Duser.language=zh -Duser.variant
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 2 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: benchmark.VecHashingBenchmark.mixin

# Run progress: 0.00% complete, ETA 00:02:20
# Fork: 1 of 1
# Warmup Iteration   1: 42.268 ops/s
# Warmup Iteration   2: 46.928 ops/s
Iteration   1: 46.605 ops/s
Iteration   2: 46.985 ops/s
Iteration   3: 47.147 ops/s
Iteration   4: 47.330 ops/s
Iteration   5: 46.991 ops/s


Result "benchmark.VecHashingBenchmark.mixin":
  47.012 ±(99.9%) 1.029 ops/s [Average]
  (min, avg, max) = (46.605, 47.012, 47.330), stdev = 0.267
  CI (99.9%): [45.982, 48.041] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 21.0.8.0.8, OpenJDK 64-Bit Server VM, 21.0.8.0.8
# VM invoker: D:\_programs\Javas\dragonwell21\bin\java.exe
# VM options: -Dfile.encoding=UTF-8 -Duser.country=CN -Duser.language=zh -Duser.variant
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 2 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: benchmark.VecHashingBenchmark.vanilla

# Run progress: 50.00% complete, ETA 00:01:10
# Fork: 1 of 1
# Warmup Iteration   1: 45.842 ops/s
# Warmup Iteration   2: 48.077 ops/s
Iteration   1: 49.106 ops/s
Iteration   2: 49.828 ops/s
Iteration   3: 49.134 ops/s
Iteration   4: 49.651 ops/s
Iteration   5: 48.913 ops/s


Result "benchmark.VecHashingBenchmark.vanilla":
  49.326 ±(99.9%) 1.507 ops/s [Average]
  (min, avg, max) = (48.913, 49.326, 49.828), stdev = 0.391
  CI (99.9%): [47.819, 50.834] (assumes normal distribution)


# Run complete. Total time: 00:02:21

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

NOTE: Current JVM experimentally supports Compiler Blackholes, and they are in use. Please exercise
extra caution when trusting the results, look into the generated code to check the benchmark still
works, and factor in a small probability of new VM bugs. Additionally, while comparisons between
different JVMs are already problematic, the performance difference caused by different Blackhole
modes can be very significant. Please make sure you use the consistent Blackhole mode for comparisons.

Benchmark                     Mode  Cnt   Score   Error  Units
VecHashingBenchmark.mixin    thrpt    5  47.012 ± 1.029  ops/s
VecHashingBenchmark.vanilla  thrpt    5  49.326 ± 1.507  ops/s
```
