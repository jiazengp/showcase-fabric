package com.showcase.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.Person;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ModMetadataHolder {
    public static String MOD_ID = "";
    public static String MOD_NAME = "";
    public static String VERSION = "";
    public static Collection<Person> AUTHORS = List.of();
    public static String LICENSE = "";
    public static String HOMEPAGE = "";
    public static String SOURCE = "";
    public static String ISSUES = "";

    public static void load() {
        Optional<ModContainer> containerOpt = FabricLoader.getInstance().getModContainer("showcase");
        if (containerOpt.isEmpty()) {
            throw new RuntimeException("Cannot find mod container for 'showcase'");
        }
        ModContainer container = containerOpt.get();

        MOD_ID = container.getMetadata().getId();
        MOD_NAME = container.getMetadata().getName();
        VERSION = container.getMetadata().getVersion().getFriendlyString();
        AUTHORS = container.getMetadata().getAuthors();
        LICENSE = container.getMetadata().getLicense().toString();
        HOMEPAGE = container.getMetadata().getContact().get("homepage").orElse("");
        SOURCE = container.getMetadata().getContact().get("sources").orElse("");
        ISSUES = container.getMetadata().getContact().get("issues").orElse("");
    }
}
