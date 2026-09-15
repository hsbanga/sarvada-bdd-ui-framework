package com.sarvadainfotech.bdd.ui.pages;

import com.sarvadainfotech.bdd.ui.core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Two-step checkout: customer details, then order overview and confirmation. */
public class CheckoutPage extends BasePage {

    private final By firstName = By.id("first-name");
    private final By lastName = By.id("last-name");
    private final By postalCode = By.id("postal-code");
    private final By continueButton = By.id("continue");
    private final By finishButton = By.id("finish");
    private final By summaryTotal = By.cssSelector(".summary_total_label");
    private final By completeHeader = By.cssSelector(".complete-header");
    private final By errorBanner = By.cssSelector("[data-test='error']");

    /** Fills the form and continues; waits for either the overview page or a validation error. */
    public void fillCustomerDetails(String first, String last, String zip) {
        type(firstName, first);
        type(lastName, last);
        type(postalCode, zip);
        clickAndWaitFor(continueButton, ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(summaryTotal),
                ExpectedConditions.visibilityOfElementLocated(errorBanner)));
    }

    public String orderTotalLine() {
        return text(summaryTotal);
    }

    public void finish() {
        clickAndWaitFor(finishButton, ExpectedConditions.visibilityOfElementLocated(completeHeader));
    }

    public String confirmationMessage() {
        return text(completeHeader);
    }

    public String errorMessage() {
        return text(errorBanner);
    }
}
