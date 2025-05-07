package com.github.jesusmrs05.mcforgecommander.mod.config;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class Config {
    public static Configuration config;
    public static boolean enableServer;
    public static String key;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        load();
    }

    public static void load(){
        enableServer = config.getBoolean(
                "Enable Server",
                Configuration.CATEGORY_GENERAL,
                false,
                "Turn on/off the server"
        );

        key = config.getString(
                "Key",
                "security",
                "",
                "Key to connect to the server"
        );
    }

    public static void save(){
        config.get(Configuration.CATEGORY_GENERAL, "Enable Server", false).set(false);
        config.get("security", "Key", "").set(key);
        config.save();
    }
}