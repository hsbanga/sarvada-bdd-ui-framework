package com.sarvadainfotech.bdd.ui.steps;

import com.sarvadainfotech.bdd.ui.config.Config;
import com.sarvadainfotech.bdd.ui.context.ScenarioContext;
import com.sarvadainfotech.bdd.ui.pages.InventoryPage;
import com.sarvadainfotech.bdd.ui.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {

    private final ScenarioContext ctx;
    private final LoginPage loginPage = new LoginPage();

    public LoginSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        loginPage.open();
        assertThat(loginPage.isLoaded()).as("login page loaded").isTrue();
    }

    @When("I log in with username {string} and password {string}")
    public void iLogInWith(String username, String password) {
        ctx.put("inventoryPage", loginPage.loginAs(username, password));
    }

    @Given("I am logged in as a standard user")
    public void iAmLoggedInAsAStandardUser() {
        Config cfg = Config.get();
        loginPage.open();
        InventoryPage inventory = loginPage.loginAs(cfg.getString("user.standard.name"), cfg.getString("user.standard.password"));
        assertThat(inventory.isLoaded()).as("inventory page loaded after login").isTrue();
        ctx.put("inventoryPage", inventory);
    }

    @Then("I should see the products page")
    public void iShouldSeeTheProductsPage() {
        InventoryPage inventory = ctx.get("inventoryPage", InventoryPage.class);
        assertThat(inventory.isLoaded()).as("products page visible").isTrue();
    }

    @Then("I should see the login error {string}")
    public void iShouldSeeTheLoginError(String expected) {
        assertThat(loginPage.errorMessage()).contains(expected);
    }
}
