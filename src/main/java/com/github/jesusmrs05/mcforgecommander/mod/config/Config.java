package com.github.jesusmrs05.mcforgecommander.mod.config;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class Config {
    public static Configuration config;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        if (config.hasChanged()) {
            config.save();
        }
    }
}