package com.github.jesusmrs05.mcforgecommander.mod.config;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import java.io.File;
import java.util.Iterator;
import java.util.regex.Pattern;

public class Config {
    public static final String DEFAULT_PORT = "50000";
/*    public static final int MIN_PORT = 1;
    public static final int MAX_PORT = 65535;*/
    public static Configuration config;
    public static boolean enableServer;
    public static String port;
    public static String key;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        load(configFile);
    }

    public static void load(File configFile){
        enableServer = config.getBoolean(
                "Enable Server",
                Configuration.CATEGORY_GENERAL,
                false,
                "Turn on/off the server"
        );
        port = config.getString(
                "Port",
                Configuration.CATEGORY_GENERAL,
                "50000",
                "The server port number. Requires turning off and on the server.",
                Pattern.compile("(?:[1-9]|[1-9][0-9]{1,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])")
        );
        key = config.getString(
                "Key",
                "security",
                "",
                "Key to connect to the server"
        );
    }

    public static void save(){
        config.get(Configuration.CATEGORY_GENERAL, "Port", DEFAULT_PORT).set(port);
        config.get("security", "Key", "").set(key);
        config.save();
    }
}