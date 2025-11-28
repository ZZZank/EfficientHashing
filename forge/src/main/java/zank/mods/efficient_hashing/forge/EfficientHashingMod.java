package zank.mods.efficient_hashing.forge;

import zank.mods.efficient_hashing.EfficientHashing;
import net.minecraftforge.fml.common.Mod;

@Mod(EfficientHashing.ID)
public class EfficientHashingMod {
	public EfficientHashingMod() {
		EfficientHashing.init();
	}
}
