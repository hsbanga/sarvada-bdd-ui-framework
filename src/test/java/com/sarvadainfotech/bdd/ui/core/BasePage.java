package com.sarvadainfotech.bdd.ui.core;

import com.sarvadainfotech.bdd.ui.config.Config;
import com.sarvadainfotech.bdd.ui.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Common building blocks for page objects: explicit waits, resilient actions and small JS helpers.
 * Page objects hold {@link By} locators and expose business-level actions; steps never touch Selenium.
 */
public abstract class BasePage {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final Duration timeout = Duration.ofSeconds(Config.get().explicitWaitSeconds());

    protected WebDriver driver() {
        return DriverManager.getDriver();
    }

    protected WebDriverWait waitFor() {
        return new WebDriverWait(driver(), timeout);
    }

    // ---- navigation -------------------------------------------------------------------------

    protected void open(String path) {
        String base = Config.get().baseUrl();
        String url = path.startsWith("http") ? path : base.replaceAll("/$", "") + "/" + path.replaceAll("^/", "");
        log.info("Opening {}", url);
        driver().get(url);
        waitForPageLoad();
    }

    public String currentUrl() {
        return driver().getCurrentUrl();
    }

    public String title() {
        return driver().getTitle();
    }

    public void waitForPageLoad() {
        waitFor().until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    // ---- waits ------------------------------------------------------------------------------

    protected WebElement visible(By locator) {
        return waitFor().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return waitFor().until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected List<WebElement> allVisible(By locator) {
        return waitFor().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected boolean invisible(By locator) {
        return waitFor().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /** Short-timeout presence check that never throws; use for conditional logic and assertions. */
    public boolean isDisplayed(By locator) {
        try {
            return new WebDriverWait(driver(), Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Full-timeout visibility check that returns false instead of throwing; use in {@code isLoaded()}. */
    protected boolean becomesVisible(By locator) {
        try {
            return visible(locator).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    // ---- actions ----------------------------------------------------------------------------

    protected void click(By locator) {
        retryOnStale(() -> clickable(locator).click());
    }

    /**
     * Clicks and then waits for the expected effect (a page title, a button label, an element
     * disappearing). If nothing happens the click is repeated. Single-page apps occasionally drop an
     * event that lands while they re-render; verifying the outcome makes the action deterministic.
     */
    protected void clickAndWaitFor(By locator, ExpectedCondition<?> effect) {
        TimeoutException last = null;
        Duration perAttempt = Duration.ofSeconds(Math.max(3, timeout.toSeconds() / 3));
        for (int attempt = 1; attempt <= 3; attempt++) {
            click(locator);
            try {
                new WebDriverWait(driver(), perAttempt).until(effect);
                return;
            } catch (TimeoutException e) {
                last = e;
                log.warn("Click on {} produced no visible effect (attempt {}/3), retrying. session={} url={} title='{}' target={}",
                        locator, attempt, ((org.openqa.selenium.remote.RemoteWebDriver) driver()).getSessionId(),
                        driver().getCurrentUrl(), driver().getTitle(), describe(locator));
            }
        }
        throw last;
    }

    /** Clears the field, types the text and verifies the value stuck; retries if the app reset the input. */
    protected void type(By locator, CharSequence text) {
        String expected = text.toString();
        for (int attempt = 1; attempt <= 3; attempt++) {
            retryOnStale(() -> {
                WebElement el = visible(locator);
                el.clear();
                if (!expected.isEmpty()) {
                    el.sendKeys(expected);
                }
            });
            try {
                new WebDriverWait(driver(), Duration.ofSeconds(2))
                        .until(d -> expected.equals(d.findElement(locator).getDomProperty("value")));
                return;
            } catch (TimeoutException e) {
                log.warn("Value typed into {} did not persist (attempt {}/3), retrying", locator, attempt);
            }
        }
        throw new IllegalStateException("Could not set value of " + locator + " to '" + expected + "'");
    }

    protected String text(By locator) {
        return visible(locator).getText().trim();
    }

    protected List<String> texts(By locator) {
        return allVisible(locator).stream().map(e -> e.getText().trim()).toList();
    }

    protected String attribute(By locator, String name) {
        return visible(locator).getDomAttribute(name);
    }

    protected void selectByVisibleText(By locator, String option) {
        new Select(visible(locator)).selectByVisibleText(option);
    }

    protected void selectByValue(By locator, String value) {
        new Select(visible(locator)).selectByValue(value);
    }

    protected void scrollIntoView(By locator) {
        ((JavascriptExecutor) driver()).executeScript("arguments[0].scrollIntoView({block:'center'})", visible(locator));
    }

    protected void jsClick(By locator) {
        ((JavascriptExecutor) driver()).executeScript("arguments[0].click()", visible(locator));
    }

    private String describe(By locator) {
        try {
            WebElement el = driver().findElement(locator);
            String html = el.getDomProperty("outerHTML");
            Object page = ((JavascriptExecutor) driver()).executeScript(
                    "return {vis: document.visibilityState, focus: document.hasFocus(), w: window.innerWidth,"
                            + " h: window.innerHeight, active: document.activeElement && document.activeElement.tagName,"
                            + " react: !!document.getElementById('root') && Object.keys(document.getElementById('root')).join(','),"
                            + " listeners: Object.keys(arguments[0]).join(',')}", el);
            return (html == null ? "?" : html.replaceAll("\\s+", " ")) + " displayed=" + el.isDisplayed()
                    + " enabled=" + el.isEnabled() + " rect=" + el.getRect() + " page=" + page;
        } catch (RuntimeException e) {
            return "not found: " + e.getClass().getSimpleName();
        }
    }

    /** Builds an XPath string literal that is safe when the value itself contains quotes. */
    protected static String xpathLiteral(String s) {
        if (!s.contains("'")) {
            return "'" + s + "'";
        }
        if (!s.contains("\"")) {
            return "\"" + s + "\"";
        }
        return "concat('" + s.replace("'", "',\"'\",'") + "')";
    }

    private void retryOnStale(Runnable action) {
        new FluentWait<>(driver())
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    action.run();
                    return true;
                });
    }
}
