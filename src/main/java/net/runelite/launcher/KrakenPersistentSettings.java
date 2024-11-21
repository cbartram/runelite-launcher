package net.runelite.launcher;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import joptsimple.OptionSet;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class KrakenPersistentSettings {
    private static final String KRAKEN_SETTINGS = "krakenprefs.json";
    boolean rlMode = false;
    String proxy = "";
    String maxMem = "1G";

    void apply(OptionSet options, KrakenData data) {
        if (options.has("rl")) {
            this.rlMode = data.rlMode = true;
        } else {
            data.rlMode = this.rlMode;
        }

        if (options.has("proxy")) {
            this.proxy = data.proxy = String.valueOf(options.valueOf("proxy"));
        } else {
            data.proxy = this.proxy;
        }

        if (options.has("maxmemory")) {
            this.maxMem = data.maxMemory = String.valueOf(options.valueOf("maxmemory"));
        } else {
            data.maxMemory = this.maxMem;
        }

        if (options.has("krakenprofile")) {
            data.hydraProfile = String.valueOf(options.valueOf("krakenprofile"));
        }

        if (options.has("remote-debug")) {
            data.startDebugger = true;
        }

    }

    static KrakenPersistentSettings loadSettings() {
        File settingsFile = new File("hydraprefs.json");

        try (InputStreamReader in = new InputStreamReader(new FileInputStream(settingsFile), StandardCharsets.UTF_8)) {
            KrakenPersistentSettings settings = (new Gson()).fromJson(in, KrakenPersistentSettings.class);
            return MoreObjects.firstNonNull(settings, new KrakenPersistentSettings());
        } catch (JsonParseException | IOException var6) {
            KrakenPersistentSettings launcherSettings = new KrakenPersistentSettings();
            saveSettings(launcherSettings);
            return launcherSettings;
        }
    }

    static void saveSettings(KrakenPersistentSettings settings) {
        File settingsFile = (new File("krakenprefs.json")).getAbsoluteFile();

        try {
            File tmpFile = File.createTempFile("krakenprefs.json", "json");
            Gson gson = new Gson();

            try (
                    FileOutputStream fout = new FileOutputStream(tmpFile);
                    FileChannel channel = fout.getChannel();
                    OutputStreamWriter writer = new OutputStreamWriter(fout, StandardCharsets.UTF_8);
            ) {
                channel.lock();
                gson.toJson(settings, writer);
                writer.flush();
                channel.force(true);
            }

            try {
                Files.move(tmpFile.toPath(), settingsFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException var12) {
                Files.move(tmpFile.toPath(), settingsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException var16) {
            settingsFile.delete();
        }
    }
}
