
codes:

```java
public class VecHashingTest {

    private static final BlockPos START = new BlockPos(-100, -20, -100);
    private static final BlockPos END = new BlockPos(100, 50, 100);
    private static final int TOTAL = (END.getX() + 1 - START.getX())
                                     * (END.getY() + 1 - START.getY())
                                     * (END.getZ() + 1 - START.getZ());

    @Test
    public void vanilla() {
        var set = new HashSet<Integer>();
        var collided = 0;
        for (var pos : BlockPos.betweenClosed(START, END)) {
            var hash = (pos.hashCode());
            if (!set.add(hash)) {
                collided++;
            }
        }
        System.out.printf("Vanilla hashing: %s object(s), %s collision(s), %s unique hashcode(s)", TOTAL, collided, set.size());
        System.out.println();
    }

    @Test
    public void mixin() {
        var set = new HashSet<Integer>();
        var collided = 0;
        for (var pos : BlockPos.betweenClosed(START, END)) {
            var hash = (PhiMix.mix(PhiMix.mix(pos.getX()) + pos.getY()) + pos.getZ());
            if (!set.add(hash)) {
                collided++;
            }
        }
        System.out.printf("PhiMix hashing: %s object(s), %s collision(s), %s unique hashcode(s)", TOTAL, collided, set.size());
        System.out.println();
    }
}
```

result:

```
PhiMix hashing: 2868471 object(s), 0 collision(s), 2868471 unique hashcode(s)
Vanilla hashing: 2868471 object(s), 2673900 collision(s), 194571 unique hashcode(s)
```