package club.thom.tem.storage;

import club.thom.tem.TEM;
import club.thom.tem.hypixel.request.KeyLookupRequest;
import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class TEMConfig extends Vigilant {

    private static final Logger logger = LogManager.getLogger(TEMConfig.class);

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Exotics",
            description = "Enable Exotic Armour"
    )
    public static boolean enableExotics = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Crystal",
            description = "Enable Crystal Armour"
    )
    public static boolean enableCrystal = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Fairy",
            description = "Enable Fairy Armour"
    )
    public static boolean enableFairy = false;

    @Property(
            type = PropertyType.TEXT,
            category = "Blacklist",
            subcategory = "Blacklist",
            name = "User Blacklist",
            hidden = true
    )
    private static String userBlacklistString = "";

    private static HashSet<String> userBlacklist = null;

    public static HashSet<String> getUserBlacklist() {
        if (userBlacklist == null) {
            if (userBlacklistString.length() != 0) {
                userBlacklist = new HashSet<>(Arrays.asList(userBlacklistString.split(",")));
            } else {
                userBlacklist = new HashSet<>();
            }
        }
        return userBlacklist;
    }

    public static void setUserBlacklist(HashSet<String> newBlacklist) {
        userBlacklist = newBlacklist;
        userBlacklist.removeIf(entry -> entry.length() == 0);
        userBlacklistString = StringUtils.join(userBlacklist,",");
        TEM.forceSaveConfig();
    }

    @Property(
            type = PropertyType.TEXT,
            category = "API",
            subcategory = "Hypixel Api",
            name = "Hypixel Api Key",
            description = "Enter your Hypixel Api Key",
            protectedText = true
    )
    private static String hypixelKey = "";

    public static Thread setHypixelKey(String newKey) {
        Thread thread = new Thread(() -> {
            if (isKeyValid(newKey)) {
                hypixelKey = newKey;
                TEM.forceSaveConfig();
            }
        });
        thread.start();
        return thread;
    }

    public static String getHypixelKey() {
        return hypixelKey;
    }

    public static String saveFolder = "config/tem/";
    public static final String fileName = "preferences.toml";
    public static File CONFIG_FILE = null;

    private void checkFolderExists() {
        Path directory = Paths.get(saveFolder);
        if (!Files.exists(directory)) {
            try {
                Files.createDirectory(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isKeyValid(String key) {
        KeyLookupRequest request = new KeyLookupRequest(key, TEM.api);
        TEM.api.addToQueue(request);
        try {
            boolean result = request.getFuture().get();
            if (result) {
                TEM.api.hasValidApiKey = true;
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("Thread interrupted while waiting to trigger api key set.", e);
                    }
                    TEM.api.signalApiKeySet();
                }).start();
            }
            return result;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error checking key validity", e);
            return false;
        }
    }

    Consumer<String> checkApiKey = key -> new Thread(() -> {
        String oldKey = hypixelKey;
        if (!isKeyValid(key)) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            hypixelKey = oldKey;
            TEM.forceSaveConfig();
            return;
        }
        hypixelKey = key;
    }).start();

    public TEMConfig() {
        super(new File(saveFolder + fileName), "TFM Configuration");
        checkFolderExists();
        CONFIG_FILE = new File(saveFolder + fileName);
        initialize();
        try {
            registerListener(this.getClass().getDeclaredField("hypixelKey"), checkApiKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
