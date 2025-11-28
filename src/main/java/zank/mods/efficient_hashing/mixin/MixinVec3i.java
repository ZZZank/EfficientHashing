package zank.mods.efficient_hashing.mixin;

import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import zank.mods.efficient_hashing.PhiMix;

/**
 * Only applying this hashing change to its subclass {@link net.minecraft.util.math.BlockPos} is a bad idea,
 * because some mods (Yes, I'm talking about you, Immersive Engineering) might use BlockPos as
 * {@link net.minecraft.util.math.Vec3i}, then two same {@code Vec3i} with the same value will have different hashcode
 *
 * @author ZZZank
 */
@Mixin(Vec3i.class)
public abstract class MixinVec3i {

    @Shadow
    private int x;

    @Shadow
    private int y;

    @Shadow
    private int z;

    /**
     * @author ZZZank
     * @reason use another algorithm with much less hash collision in general
     */
    @Override
    @Overwrite
    public int hashCode() {
        return PhiMix.mix(PhiMix.mix(this.x) + this.y) + this.z;
    }
}
