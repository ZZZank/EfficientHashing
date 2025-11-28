# EfficientHashing

EfficientHashing replaced hashing algorithm of `Vec3i` (and its subclasses like `BlockPos`) with one that's much much
more collision resistant, providing performance improvement in basically wherever its hashcode is used.

### 1. Collision Resistant

We can prove this by doing a simple test. Test is perform by hashing every combination of `BlockPos` between
`new BlockPos(-100, -20, -100)` and `new BlockPos(100, 50, 100)` (inclusive), with the total object count reaching
2,800,000+.

Vanilla algorithm produced 194,571 unique hashcode in total. This means that at least 93% of all BlockPos hashcode are
colliding with each other.

And PhiMix, the algorithm used by EfficientHashing, produced 2868471 unique hashcode in total. This means that every
unique BlockPos object gets its unique hashcode, and NONE of all BlockPos hashcode are colliding.

|                  | Unique Hashcode | Collision | Collision Rate |
|------------------|-----------------|-----------|----------------|
| Vanilla          | 194,571         | 2,673,900 | 93.2%          |
| EfficientHashing | 2,868,471       | 0         | 0%             |

If you're curious about the actual test code, see: [here](_dev/hashing_java/collisions.md)

### 2. Performant

Benchmark time:

```
Benchmark                     Mode  Cnt   Score   Error  Units
VecHashingBenchmark.mixin    thrpt    5  47.012 ± 1.029  ops/s
VecHashingBenchmark.vanilla  thrpt    5  49.326 ± 1.507  ops/s
```

PhiMix is about 95.3% the performance of original hashing algorithm. Yes, it's slower, but this performance gap is
relatively tiny compared to other hashing solution, and can be easily outweighed by the significant improvement in hash
collision resistance.

If you're curious about the actual benchmark code, see: [here](_dev/hashing_java/benchmark.md)

### 3. Vanilla compatible

In vanilla hashing algorithm, if you hash the "default" instance of `BlockPos`/`Vec3i`, aka `new BlockPos(0, 0, 0)`, the
produced hashcode will be `0`, which happens to be the "default" instance of integer. This special behavior is also
present in PhiMix, providing the best possible vanilla compatibility.