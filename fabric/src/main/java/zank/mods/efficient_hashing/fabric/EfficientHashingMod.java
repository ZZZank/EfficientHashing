package zank.mods.efficient_hashing.fabric;

import net.fabricmc.api.ModInitializer;
import zank.mods.efficient_hashing.EfficientHashing;

public class EfficientHashingMod implements ModInitializer {
	@Override
	public void onInitialize() {
		EfficientHashing.init();
	}
}
