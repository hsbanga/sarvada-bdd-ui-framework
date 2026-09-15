package com.sarvadainfotech.bdd.ui.pages;

import com.sarvadainfotech.bdd.ui.core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/** Shopping cart page. */
public class CartPage extends BasePage {

    private final By pageTitle = By.cssSelector(".title");
    private final By itemNames = By.cssSelector(".cart_item .inventory_item_name");
    private final By checkoutButton = By.id("checkout");
    private final By checkoutFirstName = By.id("first-name");

    public boolean isLoaded() {
        return becomesVisible(pageTitle) && "Your Cart".equals(text(pageTitle));
    }

    public List<String> itemNames() {
        return isDisplayed(itemNames) ? texts(itemNames) : List.of();
    }

    private By rowFor(String productName) {
        return By.xpath("//div[@class='cart_item'][.//div[normalize-space()=" + xpathLiteral(productName) + "]]");
    }

    /** Clicks the row's Remove button and waits for the row to disappear. */
    public void remove(String productName) {
        By row = rowFor(productName);
        By button = By.xpath("//div[@class='cart_item'][.//div[normalize-space()=" + xpathLiteral(productName) + "]]//button");
        clickAndWaitFor(button, ExpectedConditions.invisibilityOfElementLocated(row));
    }

    public CheckoutPage checkout() {
        clickAndWaitFor(checkoutButton, ExpectedConditions.visibilityOfElementLocated(checkoutFirstName));
        return new CheckoutPage();
    }
}
