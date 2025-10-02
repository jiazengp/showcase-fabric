package de.tomalbrc.avatarrenderer.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.util.UndashedUuid;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkinLoader {
    private static final Map<String, BufferedImage> SKINS = new ConcurrentHashMap<>();

    private static final Gson GSON = new Gson();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static Optional<BufferedImage> load(String input) {
        return Optional.ofNullable(SKINS.computeIfAbsent(input, key -> {
            try {
                UUID uuid = getUUIDFromUsername(input);
                if (uuid == null) {
                    return null;
                }

                return load(uuid);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }));
    }

    public static BufferedImage load(UUID uuid) {
        try {
            String skinUrl = getSkinUrlFromUUID(uuid);
            if (skinUrl == null) {
                return null;
            }

            return downloadImage(skinUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static UUID getUUIDFromUsername(String username) throws IOException, InterruptedException {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        JsonObject obj = GSON.fromJson(response.body(), JsonObject.class);
        return obj.has("id") ? UndashedUuid.fromString(obj.get("id").getAsString()) : null;
    }

    private static String getSkinUrlFromUUID(UUID uuid) throws IOException, InterruptedException {
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + UndashedUuid.toString(uuid);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        JsonObject obj = GSON.fromJson(response.body(), JsonObject.class);
        if (!obj.has("properties")) return null;

        JsonArray props = obj.getAsJsonArray("properties");
        for (int i = 0; i < props.size(); i++) {
            JsonObject prop = props.get(i).getAsJsonObject();
            if ("textures".equals(prop.get("name").getAsString())) {
                String base64 = prop.get("value").getAsString();
                String decoded = new String(Base64.getDecoder().decode(base64));
                JsonObject decodedObj = GSON.fromJson(decoded, JsonObject.class);

                if (decodedObj.has("textures")) {
                    JsonObject textures = decodedObj.getAsJsonObject("textures");
                    if (textures.has("SKIN")) {
                        JsonObject skin = textures.getAsJsonObject("SKIN");
                        return skin.has("url") ? skin.get("url").getAsString() : null;
                    }
                }
            }
        }

        return null;
    }

    private static BufferedImage downloadImage(String urlStr) throws IOException, InterruptedException {
        URI uri = URI.create(urlStr);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<InputStream> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            try (InputStream is = response.body()) {
                return ImageIO.read(is);
            }
        } else {
            return null;
        }
    }
}
