package com.showcase.utils;

import net.minecraft.world.inventory.MenuType;

public class ScreenHandlerUtils {
    public static MenuType<?> handlerTypeForRows(int rows) {
        return switch (Math.max(1, Math.min(rows, 6))) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 5 -> MenuType.GENERIC_9x5;
            case 6 -> MenuType.GENERIC_9x6;
            default -> MenuType.GENERIC_9x4;
        };
    }
}
