package de.tomalbrc.avatarrenderer.impl;

public class Color {
    float r, g, b, a;

    Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    static Color fromARGB(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        return new Color(r, g, b, a);
    }

    int toARGB() {
        int a = (int) (this.a * 255) & 0xFF;
        int r = (int) (this.r * 255) & 0xFF;
        int g = (int) (this.g * 255) & 0xFF;
        int b = (int) (this.b * 255) & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    static Color mix(Color c1, Color c2, float factor) {
        float r = c1.r * (1.0f - factor) + c2.r * factor;
        float g = c1.g * (1.0f - factor) + c2.g * factor;
        float b = c1.b * (1.0f - factor) + c2.b * factor;
        return new Color(r, g, b, 1.0f);
    }
}
