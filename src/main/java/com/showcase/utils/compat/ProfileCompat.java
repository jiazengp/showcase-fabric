package com.showcase.utils.compat;

import com.mojang.authlib.GameProfile;
import java.util.Optional;
import net.minecraft.world.item.component.ResolvableProfile;

/**
 * Helpers around profile objects on the 26.x mainline.
 */
public class ProfileCompat {

    /**
     * Creates a ResolvableProfile from a GameProfile.
     */
    public static ResolvableProfile createProfileComponent(GameProfile gameProfile) {
        if (gameProfile == null) {
            throw new IllegalArgumentException("GameProfile cannot be null");
        }
        return ResolvableProfile.createResolved(gameProfile);
    }

    /**
     * Gets the name from a GameProfile.
     */
    public static String getName(GameProfile gameProfile) {
        return gameProfile.name();
    }
}
