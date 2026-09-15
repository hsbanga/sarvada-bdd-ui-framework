@checkout
Feature: Checkout
  As a shopper
  I want to complete an order
  So that my purchase is confirmed

  Background:
    Given I am logged in as a standard user
    And I add "Sauce Labs Fleece Jacket" to the cart
    And I open the cart
    And I proceed to checkout

  @smoke @regression @e2e
  Scenario: Complete an order end to end
    When I enter customer details "Asha" "Verma" "302001"
    Then the order total should be shown
    When I finish the order
    Then I should see the confirmation "Thank you for your order!"

  @regression @negative
  Scenario: Checkout requires a postal code
    When I enter customer details "Asha" "Verma" ""
    Then I should see the checkout error "Postal Code is required"
