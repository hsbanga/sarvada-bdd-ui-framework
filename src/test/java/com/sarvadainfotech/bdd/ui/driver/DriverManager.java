package com.sarvadainfotech.bdd.ui.driver;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Holds one WebDriver per thread so scenarios can run in parallel safely, and removes the session's
 * temporary browser profile when the browser is closed.
 */
public final class DriverManager {

    private static final Logger LOG = LoggerFactory.getLogger(DriverManager.class);
    private static final ThreadLocal<DriverFactory.Session> SESSION = new ThreadLocal<>();

    private DriverManager() {
    }

    public static WebDriver getDriver() {
        DriverFactory.Session session = SESSION.get();
        if (session == null) {
            session = DriverFactory.create();
            SESSION.set(session);
        }
        return session.driver();
    }

    public static boolean hasDriver() {
        return SESSION.get() != null;
    }

    public static void quitDriver() {
        DriverFactory.Session session = SESSION.get();
        if (session == null) {
            return;
        }
        try {
            session.driver().quit();
        } finally {
            SESSION.remove();
            deleteQuietly(session.profileDir());
        }
    }

    private static void deleteQuietly(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // Chrome may still hold a lock for a moment; the OS temp cleaner will collect leftovers.
                }
            });
        } catch (IOException e) {
            LOG.debug("Could not fully remove profile dir {}: {}", dir, e.toString());
        }
    }
}
