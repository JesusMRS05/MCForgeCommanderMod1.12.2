package com.github.jesusmrs05.mcforgecommander.mod;

import com.github.jesusmrs05.mcforgecommander.Tags;
import com.github.jesusmrs05.mcforgecommander.mod.config.Config;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
		modid = Tags.MOD_ID,
		name = Tags.MOD_NAME,
		version = Tags.VERSION,
		guiFactory = "com.github.jesusmrs05.mcforgecommander.mod.config.ModGuiFactory"
)
public class MCForgeCommander {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.init(event.getSuggestedConfigurationFile());
	}
}