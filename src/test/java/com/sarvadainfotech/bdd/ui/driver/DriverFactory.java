package com.sarvadainfotech.bdd.ui.driver;

import com.sarvadainfotech.bdd.ui.config.Config;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Creates WebDriver instances. Selenium Manager (bundled with Selenium 4.6+) resolves and downloads
 * the matching browser driver automatically, so no WebDriverManager or manual driver files are needed.
 *
 * <p>Every local Chromium session gets its own throw-away profile directory so parallel sessions never
 * share state. Do not add {@code --no-sandbox} or {@code --disable-dev-shm-usage} on Windows: with several
 * concurrent Chrome instances those flags make input events silently stop reaching the page.
 */
public final class DriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    /** A created driver together with the temporary profile directory it owns (null when not applicable). */
    public record Session(WebDriver driver, Path profileDir) {
    }

    public static Session create() {
        Config cfg = Config.get();
        String browser = cfg.browser();
        boolean headless = cfg.headless();
        boolean local = cfg.gridUrl().isEmpty();
        Path profileDir = local ? newProfileDir(browser) : null;

        MutableCapabilities options = optionsFor(browser, headless, profileDir);
        WebDriver driver = cfg.gridUrl()
                .map(url -> remote(url, options))
                .orElseGet(() -> local(browser, options));

        LOG.info("Created {} session {} on {}", browser, ((RemoteWebDriver) driver).getSessionId(),
                Thread.currentThread().getName());
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(cfg.pageLoadTimeoutSeconds()));
        driver.manage().timeouts().implicitlyWait(Duration.ZERO); // explicit waits only
        if (!headless) {
            driver.manage().window().maximize();
        }
        return new Session(driver, profileDir);
    }

    private static Path newProfileDir(String browser) {
        if (!browser.equals("chrome") && !browser.equals("edge")) {
            return null;
        }
        try {
            return Files.createTempDirectory("sarvada-" + browser + "-profile-");
        } catch (IOException e) {
            throw new IllegalStateException("Could not create a temporary browser profile directory", e);
        }
    }

    private static MutableCapabilities optionsFor(String browser, boolean headless, Path profileDir) {
        return switch (browser) {
            case "chrome" -> {
                ChromeOptions o = new ChromeOptions();
                o.addArguments("--window-size=1920,1080", "--disable-notifications", "--disable-infobars",
                        "--no-first-run", "--no-default-browser-check");
                if (profileDir != null) {
                    o.addArguments("--user-data-dir=" + profileDir.toAbsolutePath());
                }
                if (headless) {
                    o.addArguments("--headless=new");
                }
                yield o;
            }
            case "edge" -> {
                EdgeOptions o = new EdgeOptions();
                o.addArguments("--window-size=1920,1080", "--disable-notifications", "--no-first-run");
                if (profileDir != null) {
                    o.addArguments("--user-data-dir=" + profileDir.toAbsolutePath());
                }
                if (headless) {
                    o.addArguments("--headless=new");
                }
                yield o;
            }
            case "firefox" -> {
                FirefoxOptions o = new FirefoxOptions();
                o.addArguments("--width=1920", "--height=1080");
                if (headless) {
                    o.addArguments("-headless");
                }
                yield o;
            }
            case "safari" -> new SafariOptions();
            default -> throw new IllegalArgumentException(
                    "Unsupported browser '" + browser + "'. Use chrome, edge, firefox or safari.");
        };
    }

    private static WebDriver local(String browser, MutableCapabilities options) {
        return switch (browser) {
            case "chrome" -> new ChromeDriver((ChromeOptions) options);
            case "edge" -> new EdgeDriver((EdgeOptions) options);
            case "firefox" -> new FirefoxDriver((FirefoxOptions) options);
            case "safari" -> new SafariDriver((SafariOptions) options);
            default -> throw new IllegalStateException("unreachable");
        };
    }

    private static WebDriver remote(String gridUrl, MutableCapabilities options) {
        try {
            return new RemoteWebDriver(new URL(gridUrl), options);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("grid.url is not a valid URL: " + gridUrl, e);
        }
    }
}
