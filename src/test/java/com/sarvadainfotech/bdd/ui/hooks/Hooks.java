package com.sarvadainfotech.bdd.ui.hooks;

import com.sarvadainfotech.bdd.ui.config.Config;
import com.sarvadainfotech.bdd.ui.driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scenario lifecycle: a fresh browser per scenario, screenshot on failure (embedded in the report and
 * saved under target/screenshots), and guaranteed browser shutdown even when a step blows up.
 */
public class Hooks {

    private static final Logger LOG = LoggerFactory.getLogger(Hooks.class);
    private static final Path SCREENSHOT_DIR = Path.of("target", "screenshots");

    @Before(order = 0)
    public void startScenario(Scenario scenario) {
        LOG.info("==== START  {}  [{}]", scenario.getName(), String.join(" ", scenario.getSourceTagNames()));
        DriverManager.getDriver();
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        if (Config.get().screenshotEveryStep() && !scenario.isFailed()) {
            attachScreenshot(scenario, "step");
        }
    }

    @After(order = 0)
    public void endScenario(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                attachScreenshot(scenario, "FAILED");
            }
        } finally {
            DriverManager.quitDriver();
            LOG.info("==== END    {}  status={}", scenario.getName(), scenario.getStatus());
        }
    }

    private void attachScreenshot(Scenario scenario, String label) {
        if (!DriverManager.hasDriver()) {
            return;
        }
        try {
            WebDriver driver = DriverManager.getDriver();
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(png, "image/png", label + " - " + scenario.getName());
            Files.createDirectories(SCREENSHOT_DIR);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
            String safeName = scenario.getName().replaceAll("[^A-Za-z0-9._-]+", "_");
            Files.write(SCREENSHOT_DIR.resolve(label + "_" + safeName + "_" + stamp + ".png"), png);
        } catch (IOException | RuntimeException e) {
            LOG.warn("Could not capture screenshot: {}", e.toString());
        }
    }
}
