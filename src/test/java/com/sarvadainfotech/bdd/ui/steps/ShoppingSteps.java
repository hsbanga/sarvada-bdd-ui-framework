package com.sarvadainfotech.bdd.ui.steps;

import com.sarvadainfotech.bdd.ui.context.ScenarioContext;
import com.sarvadainfotech.bdd.ui.pages.CartPage;
import com.sarvadainfotech.bdd.ui.pages.CheckoutPage;
import com.sarvadainfotech.bdd.ui.pages.InventoryPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ShoppingSteps {

    private final ScenarioContext ctx;

    public ShoppingSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    private InventoryPage inventory() {
        return ctx.get("inventoryPage", InventoryPage.class);
    }

    private CartPage cart() {
        return ctx.get("cartPage", CartPage.class);
    }

    private CheckoutPage checkout() {
        return ctx.get("checkoutPage", CheckoutPage.class);
    }

    @When("I sort products by {string}")
    public void iSortProductsBy(String option) {
        inventory().sortBy(option);
    }

    @Then("products should be sorted by price ascending")
    public void productsShouldBeSortedByPriceAscending() {
        List<Double> prices = inventory().productPrices();
        assertThat(prices).isNotEmpty().isSortedAccordingTo(Comparator.naturalOrder());
    }

    @Then("products should be sorted by name descending")
    public void productsShouldBeSortedByNameDescending() {
        List<String> names = inventory().productNames();
        assertThat(names).isNotEmpty().isSortedAccordingTo(Comparator.reverseOrder());
    }

    @When("I add {string} to the cart")
    public void iAddToTheCart(String product) {
        inventory().addToCart(product);
    }

    @Then("the cart badge should show {int}")
    public void theCartBadgeShouldShow(int expected) {
        assertThat(inventory().cartCount()).isEqualTo(expected);
    }

    @When("I open the cart")
    public void iOpenTheCart() {
        CartPage cart = inventory().openCart();
        assertThat(cart.isLoaded()).as("cart page loaded").isTrue();
        ctx.put("cartPage", cart);
    }

    @Then("the cart should contain:")
    public void theCartShouldContain(List<String> expectedItems) {
        assertThat(cart().itemNames()).containsExactlyInAnyOrderElementsOf(expectedItems);
    }

    @When("I remove {string} from the cart")
    public void iRemoveFromTheCart(String product) {
        cart().remove(product);
    }

    @Then("the cart should be empty")
    public void theCartShouldBeEmpty() {
        assertThat(cart().itemNames()).isEmpty();
    }

    @When("I proceed to checkout")
    public void iProceedToCheckout() {
        ctx.put("checkoutPage", cart().checkout());
    }

    @And("I enter customer details {string} {string} {string}")
    public void iEnterCustomerDetails(String first, String last, String zip) {
        checkout().fillCustomerDetails(first, last, zip);
    }

    @Then("the order total should be shown")
    public void theOrderTotalShouldBeShown() {
        assertThat(checkout().orderTotalLine()).startsWith("Total: $");
    }

    @When("I finish the order")
    public void iFinishTheOrder() {
        checkout().finish();
    }

    @Then("I should see the confirmation {string}")
    public void iShouldSeeTheConfirmation(String expected) {
        assertThat(checkout().confirmationMessage()).isEqualToIgnoringCase(expected);
    }

    @Then("I should see the checkout error {string}")
    public void iShouldSeeTheCheckoutError(String expected) {
        assertThat(checkout().errorMessage()).contains(expected);
    }
}
