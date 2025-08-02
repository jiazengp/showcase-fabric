package com.showcase.utils;

import net.minecraft.screen.ScreenHandlerType;

public class ScreenHandlerUtils {
    public static ScreenHandlerType<?> handlerTypeForRows(int rows) {
        return switch (Math.max(1, Math.min(rows, 6))) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            case 6 -> ScreenHandlerType.GENERIC_9X6;
            default -> ScreenHandlerType.GENERIC_9X4;
        };
    }
}
