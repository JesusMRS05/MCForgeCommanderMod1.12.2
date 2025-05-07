package com.github.jesusmrs05.mcforgecommander.mod.config;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class Config {
    public static Configuration config;
    public static boolean enableServer;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        config.load();
        enableServer = config.getBoolean(
                "Enable Server",
                Configuration.CATEGORY_GENERAL,
                false,
                "Turn on/off the server"
        );
        if (config.hasChanged()) {
            config.save();
        }
    }
}