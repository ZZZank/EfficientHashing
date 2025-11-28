package zank.mods.efficient_hashing;

/**
 * @author ZZZank
 */
public class PhiMix {

    /** 2<sup>32</sup> &middot; &phi;, where &phi; = (&#x221A;5 &minus; 1)/2. */
    public static final int PHI = 0x9E3779B9;

    public static int mix(int x) {
        final int y = x * PHI;
        return y ^ (y >>> 16);
    }
}
