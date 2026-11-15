package com.example.howtobealoh;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class HowToBeALoh implements ModInitializer {
    public static final String MOD_ID = "how-to-be-a-loh";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        List<String> addonNames = loadAddonNames();

        int createdCount = 0;
        for (int i = 0; i < addonNames.size(); i++) {
            int num = i + 1;
            String modId = "loh_addon_" + num;
            String fileName = String.format("HowToBeALoh-Addon-%04d.jar", num);
            Path jarPath = modsDir.resolve(fileName);

            if (!Files.exists(jarPath)) {
                createDummyJar(jarPath, modId, addonNames.get(i));
                createdCount++;
            }
        }

        if (createdCount > 0) {
            LOGGER.info("Создано {} аддонов в папке mods!", createdCount);
        }
    }

    private List<String> loadAddonNames() {
        List<String> names = new ArrayList<>();
        try (InputStream is = HowToBeALoh.class.getResourceAsStream("/addons.txt")) {
            if (is == null) {
                LOGGER.error("Файл addons.txt не найден!");
                return names;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        names.add(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка чтения addons.txt", e);
        }
        return names;
    }

    private void createDummyJar(Path jarPath, String modId, String displayName) {
        String safeName = displayName.replace("\\", "\\\\").replace("\"", "\\\"");

        String fabricJson = "{\n" +
                "  \"schemaVersion\": 1,\n" +
                "  \"id\": \"" + modId + "\",\n" +
                "  \"version\": \"1.0.0\",\n" +
                "  \"name\": \"" + safeName + "\",\n" +
                "  \"description\": \"Пустой аддон для мода How To Be A Loh.\",\n" +
                "  \"authors\": [\"LohTeam\"],\n" +
                "  \"environment\": \"*\",\n" +
                "  \"depends\": {\n" +
                "    \"fabricloader\": \">=0.16.0\",\n" +
                "    \"minecraft\": \"1.21.11\"\n" +
                "  }\n" +
                "}";

        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            JarEntry entry = new JarEntry("fabric.mod.json");
            jos.putNextEntry(entry);
            jos.write(fabricJson.getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        } catch (Exception e) {
            LOGGER.error("Ошибка создания файла " + jarPath, e);
        }
    }
}
