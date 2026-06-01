package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads database settings from config.properties in the project working directory.
 * Falls back to defaults so existing local setups keep working.
 */
public final class AppConfig {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/student_result_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root123";

    private static final Properties props = new Properties();
    private static boolean loaded;

    private AppConfig() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        props.clear();
        props.setProperty("db.url", DEFAULT_URL);
        props.setProperty("db.user", DEFAULT_USER);
        props.setProperty("db.password", DEFAULT_PASSWORD);

        try (InputStream in = openConfigStream()) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read config.properties — using defaults.");
        }

        String urlOverride = System.getProperty("db.url");
        if (urlOverride != null && !urlOverride.isBlank()) {
            props.setProperty("db.url", urlOverride.trim());
        }
        String userOverride = System.getProperty("db.user");
        if (userOverride != null && !userOverride.isBlank()) {
            props.setProperty("db.user", userOverride.trim());
        }
        String passOverride = System.getProperty("db.password");
        if (passOverride != null) {
            props.setProperty("db.password", passOverride);
        }

        loaded = true;
    }

    private static InputStream openConfigStream() throws IOException {
        java.io.File local = new java.io.File("config.properties");
        if (local.isFile()) {
            return new FileInputStream(local);
        }
        InputStream classpath = AppConfig.class.getClassLoader()
                .getResourceAsStream("config.properties");
        if (classpath != null) {
            return classpath;
        }
        return null;
    }

    public static String getDbUrl() {
        load();
        return props.getProperty("db.url", DEFAULT_URL).trim();
    }

    public static String getDbUser() {
        load();
        return props.getProperty("db.user", DEFAULT_USER).trim();
    }

    public static String getDbPassword() {
        load();
        return props.getProperty("db.password", DEFAULT_PASSWORD);
    }
}
