package com.github.jesusmrs05.mcforgecommander.mod.config;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import java.io.File;
import java.util.Iterator;

public class Config {
    public static Configuration config;
    public static boolean enableServer;
    public static String key;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        load();
    }

    public static void load(){
        config.get(Configuration.CATEGORY_GENERAL, "Enable Server", false).set(false);
        enableServer = config.getBoolean(
                "Enable Server",
                Configuration.CATEGORY_GENERAL,
                false,
                "Turn on/off the server"
        );
        config.getCategory(Configuration.CATEGORY_GENERAL).setRequiresMcRestart(false);
        config.getCategory(Configuration.CATEGORY_GENERAL).setRequiresWorldRestart(false);
        key = config.getString(
                "Key",
                "security",
                "",
                "Key to connect to the server"
        );
        config.getCategory("security").setRequiresMcRestart(false);
        config.getCategory("security").setRequiresWorldRestart(false);
    }

    public static void save(){
        config.get("security", "Key", "").set(key);
        config.save();
    }
}