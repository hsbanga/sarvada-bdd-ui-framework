package com.sarvadainfotech.bdd.ui.pages;

import com.sarvadainfotech.bdd.ui.core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/** Product listing page. */
public class InventoryPage extends BasePage {

    private final By pageTitle = By.cssSelector(".title");
    private final By productNames = By.cssSelector(".inventory_item_name");
    private final By productPrices = By.cssSelector(".inventory_item_price");
    private final By sortSelect = By.cssSelector("[data-test='product-sort-container']");
    private final By cartBadge = By.cssSelector(".shopping_cart_badge");
    private final By cartLink = By.cssSelector(".shopping_cart_link");

    public boolean isLoaded() {
        return becomesVisible(pageTitle) && "Products".equals(text(pageTitle));
    }

    public List<String> productNames() {
        return texts(productNames);
    }

    public List<Double> productPrices() {
        return texts(productPrices).stream().map(p -> Double.parseDouble(p.replace("$", ""))).toList();
    }

    public void sortBy(String visibleOption) {
        selectByVisibleText(sortSelect, visibleOption);
    }

    private By cartButtonFor(String productName) {
        return By.xpath("//div[contains(@class,'inventory_item_name') and normalize-space()=" + xpathLiteral(productName) + "]"
                + "/ancestor::div[@class='inventory_item']//button");
    }

    /** Clicks "Add to cart" and waits for the button to flip to "Remove", proving the item was added. */
    public void addToCart(String productName) {
        By button = cartButtonFor(productName);
        clickAndWaitFor(button, ExpectedConditions.textToBe(button, "Remove"));
    }

    public int cartCount() {
        return isDisplayed(cartBadge) ? Integer.parseInt(text(cartBadge)) : 0;
    }

    public CartPage openCart() {
        clickAndWaitFor(cartLink, ExpectedConditions.textToBe(pageTitle, "Your Cart"));
        return new CartPage();
    }
}
