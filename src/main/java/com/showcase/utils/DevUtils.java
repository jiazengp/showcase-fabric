package com.showcase.utils;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Development environment utilities
 */
public class DevUtils {

    /**
     * Check if running in development environment
     * @return true if in development environment
     */
    public static boolean isDevelopment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    /**
     * Check if running in production environment
     * @return true if in production environment
     */
    public static boolean isProduction() {
        return !isDevelopment();
    }

    /**
     * Execute code only in development environment
     * @param runnable Code to execute
     */
    public static void ifDevelopment(Runnable runnable) {
        if (isDevelopment()) {
            runnable.run();
        }
    }

    /**
     * Execute code only in production environment
     * @param runnable Code to execute
     */
    public static void ifProduction(Runnable runnable) {
        if (isProduction()) {
            runnable.run();
        }
    }

    /**
     * Get different values for development and production
     * @param devValue Value for development
     * @param prodValue Value for production
     * @param <T> Value type
     * @return Appropriate value based on environment
     */
    public static <T> T devOrProd(T devValue, T prodValue) {
        return isDevelopment() ? devValue : prodValue;
    }
}