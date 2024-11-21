package net.runelite.launcher;

import com.google.gson.Gson;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Data
public class KrakenData {
    public static final boolean DEV = false;
    public static final String KRAKEN_BOOTSTRAP = "https://osrsplugins.com/static/launcher/bootstrap.json";
    public static final File KRAKEN_DIR;
    public static final String OUTDATED_MESSAGE_FALLBACK = "The Kraken client is currently offline. \n\nThis is likely due to RuneLite pushing a new client update that needs to be checked by the Kraken team before we can re-open the client. \n\nKeep an eye out on announcement channels in the discord for updates, and please do not message staff members asking why it does not load. \n\nIf you would like to run vanilla RuneLite from this launcher, set runelite mode in the runelite (configure) window or use the --rl arg.";
    private static KrakenBootstrap krakenBootstrap;
    String currentLauncherVersion = "3.0.0";
    String proxy = "";
    String hydraProfile = "";
    String maxMemory = "";
    boolean rlMode = false;
    boolean startDebugger = false;

    static {
        KRAKEN_DIR = new File(Launcher.RUNELITE_DIR, "kraken");
        krakenBootstrap = null;
    }

    public static KrakenBootstrap getKrakenBootstrap(HttpClient httpClient) throws IOException {
        if (krakenBootstrap != null) {
            return krakenBootstrap;
        } else {
            HttpRequest hydraBootstrapReq = HttpRequest.newBuilder().uri(URI.create("https://osrsplugins.com/static/launcher/bootstrap.json")).GET().build();

            HttpResponse<String> resp;
            try {
                resp = httpClient.send(hydraBootstrapReq, HttpResponse.BodyHandlers.ofString());
            } catch (InterruptedException e) {
                throw new IOException(e);
            }

            if (resp.statusCode() != 200) {
                int var10002 = resp.statusCode();
                throw new IOException("Unable to download bootstrap (status code " + var10002 + "): " + resp.body());
            } else {
                krakenBootstrap = (new Gson()).fromJson(resp.body(), KrakenBootstrap.class);
                return krakenBootstrap;
            }
        }
    }
}
