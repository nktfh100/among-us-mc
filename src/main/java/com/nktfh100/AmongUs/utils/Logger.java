package com.nktfh100.AmongUs.utils;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class Logger {
    public static void log(Level level, String message) {
        Bukkit.getLogger().log(level, "[Among Us] " + message);
    }

    public static void logError(Level level, String message, Throwable error) {
        Bukkit.getLogger().log(level, "[Among Us] " + message, error);
    }
}
