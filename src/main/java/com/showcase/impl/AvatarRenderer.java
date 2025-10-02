package de.tomalbrc.avatarrenderer.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;

/**
 * port of glsl shader by JNNGL
 *
 * @see <a href="https://github.com/JNNGL/vanilla-shaders">...</a>
 */
public class AvatarRenderer {
    private record Vec2(int x, int y) {
    }

    private static final int IMG_SIZE = 34;
    private static final Color OUTLINE_COLOR = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    private static final Vec2[] OUTLINE_OFFSETS = {
            new Vec2(-1, 1), new Vec2(0, 1), new Vec2(1, 1), new Vec2(1, 0),
            new Vec2(1, -1), new Vec2(0, -1), new Vec2(-1, -1), new Vec2(-1, 0)
    };

    public static Component asTextComponent(BufferedImage image, int yOffset) {
        var style = Style.EMPTY.withShadowColor(0).withFont(ResourceLocation.fromNamespaceAndPath("avatar-renderer", "pixel"));

        MutableComponent line = Component.empty();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = Color.fromARGB(image.getRGB(x, y));
                if (color.a > 0.1) {
                    line.append(Component.literal(Character.toString(0xE001 + yOffset + y) + " ").withColor(color.toARGB()));
                } else {
                    line.append(".");
                }
            }
            if (y != image.getHeight() - 1)
                line.append(" ".repeat(image.getWidth()));
        }

        return Component.empty().withStyle(Style.EMPTY).append(line.withStyle(style.withColor(ChatFormatting.WHITE)));
    }

    public static BufferedImage render(BufferedImage skin, boolean flipped) {
        BufferedImage output = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < IMG_SIZE; y++) {
            for (int x = 0; x < IMG_SIZE; x++) {
                Color color = renderOutlined(new Vec2(flipped ? IMG_SIZE - x : x, y), skin);
                if (color.a >= 0.1f) {
                    color.a = 1.0f;
                    output.setRGB(x, y, color.toARGB());
                }
            }
        }
        return output;
    }

    private static Color renderComposite(Vec2 pixel, BufferedImage skin) {
        Color fragColor;

        Vec2 armRightPixel = new Vec2(pixel.x - 1 - 3, pixel.y - 1);
        fragColor = renderArmRightPart(armRightPixel, skin);
        if (fragColor.a < 0.1f) {
            for (Vec2 offset : OUTLINE_OFFSETS) {
                if (renderArmRightPart(new Vec2(armRightPixel.x + offset.x, armRightPixel.y + offset.y), skin).a > 0.1f) {
                    return OUTLINE_COLOR;
                }
            }
        } else {
            return fragColor;
        }

        Vec2 headPixel = new Vec2(pixel.x - 1 - 3, pixel.y - 1);
        fragColor = renderHeadPart(headPixel, skin);
        if (fragColor.a < 0.1f) {
            for (Vec2 offset : OUTLINE_OFFSETS) {
                if (renderHeadPart(new Vec2(headPixel.x + offset.x, headPixel.y + offset.y), skin).a > 0.1f) {
                    return OUTLINE_COLOR;
                }
            }
        } else {
            return fragColor;
        }

        Vec2 bodyPixel = new Vec2(pixel.x - 1 - 3, pixel.y - 1);
        fragColor = renderBodyPart(bodyPixel, skin);
        if (fragColor.a < 0.1f) {
            for (Vec2 offset : OUTLINE_OFFSETS) {
                if (renderBodyPart(new Vec2(bodyPixel.x + offset.x, bodyPixel.y + offset.y), skin).a > 0.1f) {
                    return OUTLINE_COLOR;
                }
            }
        } else {
            return fragColor;
        }

        Vec2 armLeftPixel = new Vec2(pixel.x - 1, pixel.y - 1);
        fragColor = renderArmLeftPart(armLeftPixel, skin);
        if (fragColor.a < 0.1f) {
            for (Vec2 offset : OUTLINE_OFFSETS) {
                if (renderArmLeftPart(new Vec2(armLeftPixel.x + offset.x, armLeftPixel.y + offset.y), skin).a > 0.1f) {
                    return OUTLINE_COLOR;
                }
            }
        } else {
            return fragColor;
        }

        return new Color(0, 0, 0, 0);
    }

    private static Color renderOutlined(Vec2 pixelReset, BufferedImage skin) {
        Vec2 base = new Vec2(pixelReset.x - 1, pixelReset.y - 1);
        Color fragColor = renderComposite(base, skin);
        if (fragColor.a < 0.1f) {
            Vec2[] outlineOffsets = {
                    new Vec2(-1, 0),
                    new Vec2(1, 0),
                    new Vec2(0, 1)
            };
            for (Vec2 off : outlineOffsets) {
                Vec2 sample = new Vec2(base.x + off.x, base.y + off.y);
                if (renderComposite(sample, skin).a >= 0.1f) {
                    return OUTLINE_COLOR;
                }
            }
        }
        return fragColor;
    }

    private static Color renderHeadPart(Vec2 p, BufferedImage skin) {
        Color fragColor = new Color(0, 0, 0, 0);
        if (p.x >= 0 && p.x < 16 && p.y >= 0 && p.y < 18) {
            int pY = p.y;
            if (pY > 1) pY--;
            if (pY > 15) pY--;
            fragColor = getColorFromSkin(skin, p.x / 2 + 40, pY / 2 + 8);
        } else if (p.x >= 16 && p.x < 24 && p.y >= 0 && p.y < 18) {
            int pX = p.x - 16, pY = p.y;
            if (pY > 0) pY--;
            if (pY > 15) pY--;
            fragColor = getColorFromSkin(skin, pX + 48, pY / 2 + 8);
            fragColor.r *= 0.6f;
            fragColor.g *= 0.6f;
            fragColor.b *= 0.6f;
        }
        if (fragColor.a < 1.0f) {
            if (p.x >= 1 && p.x < 16 && p.y >= 1 && p.y < 17) {
                int pX = p.x - 1, pY = p.y - 1;
                if (pX > 6) pX++;
                Color tex = getColorFromSkin(skin, pX / 2 + 8, pY / 2 + 8);
                if (tex.a > 0.1f) {
                    fragColor = Color.mix(tex, fragColor, fragColor.a);
                    fragColor.a = 1.0f;
                }
            } else if (p.x >= 16 && p.x < 24 && p.y >= 1 && p.y < 17) {
                int pX = p.x - 1 - 15, pY = p.y - 1;
                Color tex = getColorFromSkin(skin, pX + 16, pY / 2 + 8);
                if (tex.a > 0.1f) {
                    tex.r *= 0.6f;
                    tex.g *= 0.6f;
                    tex.b *= 0.6f;
                    fragColor = Color.mix(tex, fragColor, fragColor.a);
                    fragColor.a = 1.0f;
                }
            }
        }
        if (p.x >= 8 && p.x < 23 && p.y == 0) {
            int pX = p.x - 8;
            Vec2 coord = new Vec2(pX / 2 + 56, 8);
            Color tex = getColorFromSkin(skin, coord.x, coord.y);
            if (tex.a > 0.0f && getColorFromSkin(skin, coord.x, coord.y + 1).a > 0.1f) fragColor = tex;
        }
        if (fragColor.a > 0.1f && fragColor.a < 1.0f) {
            fragColor.r *= fragColor.a;
            fragColor.g *= fragColor.a;
            fragColor.b *= fragColor.a;
            fragColor.a = 1.0f;
        }
        return fragColor;
    }

    private static Color renderBodyPart(Vec2 p, BufferedImage skin) {
        if (p.x >= 3 && p.x < 18 && p.y >= 17 && p.y < 31) {
            int pX = p.x - 3, pY = p.y - 17;
            if (pX > 6) pX++;
            Color fragColor = getColorFromSkin(skin, pX / 2 + 20, pY / 2 + 36); // overlay
            if (fragColor.a < 0.1f) {
                fragColor = getColorFromSkin(skin, pX / 2 + 20, pY / 2 + 20); // Body
            }
            return fragColor;
        }
        return new Color(0, 0, 0, 0);
    }

    private static Color renderArmRightPart(Vec2 p, BufferedImage skin) {
        boolean isOldSkin = (skin.getHeight() == 32);

        if (p.x >= 19 && p.x < 25 && p.y >= 17 && p.y < 31) {
            int pX = p.x - 19, pY = p.y - 17;
            Color fragColor;

            if (isOldSkin) {
                fragColor = getColorFromSkin(skin, pX / 2 + 44, pY / 2 + 20);
            } else {
                fragColor = getColorFromSkin(skin, pX / 2 + 52, pY / 2 + 52); // overlay
                if (fragColor.a < 0.1f) {
                    fragColor = getColorFromSkin(skin, pX / 2 + 36, pY / 2 + 52); // base
                }
            }
            return fragColor;

        } else if (p.x >= 25 && p.x < 28 && p.y >= 17 && p.y < 31) {
            if (p.x == 27 && p.y == 17) return new Color(0, 0, 0, 0);
            int pX = p.x - 25, pY = p.y - 17;
            Color fragColor;

            if (isOldSkin) {
                fragColor = getColorFromSkin(skin, pX + 44, pY / 2 + 20);
                fragColor.r *= 0.6f;
                fragColor.g *= 0.6f;
                fragColor.b *= 0.6f;
            } else {
                fragColor = getColorFromSkin(skin, pX + 56, pY / 2 + 52); // overlay
                if (fragColor.a < 0.1f) {
                    fragColor = getColorFromSkin(skin, pX + 40, pY / 2 + 52); // base
                }
                fragColor.r *= 0.6f;
                fragColor.g *= 0.6f;
                fragColor.b *= 0.6f;
            }
            return fragColor;
        }

        return new Color(0, 0, 0, 0);
    }

    private static Color renderArmLeftPart(Vec2 p, BufferedImage skin) {
        if (p.x >= 0 && p.x < 6 && p.y >= 17 && p.y < 31) {
            int pX = p.x, pY = p.y - 17;
            Color fragColor = getColorFromSkin(skin, pX / 2 + 44, pY / 2 + 36); // overlay
            if (fragColor.a < 0.1f) {
                fragColor = getColorFromSkin(skin, pX / 2 + 44, pY / 2 + 20); // Arm
            }
            return fragColor;
        }
        return new Color(0, 0, 0, 0);
    }

    private static Color getColorFromSkin(BufferedImage skin, int x, int y) {
        if (x < 0 || x >= skin.getWidth() || y < 0 || y >= skin.getHeight()) {
            return new Color(0, 0, 0, 0);
        }
        return Color.fromARGB(skin.getRGB(x, y));
    }
}
