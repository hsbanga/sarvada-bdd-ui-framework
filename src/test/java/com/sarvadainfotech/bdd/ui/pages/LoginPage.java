package com.sarvadainfotech.bdd.ui.pages;

import com.sarvadainfotech.bdd.ui.core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Login screen of the demo store. */
public class LoginPage extends BasePage {

    private final By username = By.id("user-name");
    private final By password = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By errorBanner = By.cssSelector("[data-test='error']");
    private final By anyPageTitle = By.cssSelector(".title");

    public LoginPage open() {
        open("/");
        return this;
    }

    /** Submits the form and waits until the app either moves on or shows a validation error. */
    public InventoryPage loginAs(String user, String pass) {
        type(username, user);
        type(password, pass);
        clickAndWaitFor(loginButton, ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(anyPageTitle),
                ExpectedConditions.visibilityOfElementLocated(errorBanner)));
        return new InventoryPage();
    }

    public boolean isLoaded() {
        return becomesVisible(loginButton);
    }

    public String errorMessage() {
        return text(errorBanner);
    }
}
