@inventory
Feature: Product catalogue
  As a shopper
  I want to sort products and add them to my cart
  So that I can find what I want quickly

  Background:
    Given I am logged in as a standard user

  @regression
  Scenario: Sort products by price, low to high
    When I sort products by "Price (low to high)"
    Then products should be sorted by price ascending

  @regression
  Scenario: Sort products by name, Z to A
    When I sort products by "Name (Z to A)"
    Then products should be sorted by name descending

  @smoke @regression
  Scenario: Add products to the cart
    When I add "Sauce Labs Backpack" to the cart
    And I add "Sauce Labs Bike Light" to the cart
    Then the cart badge should show 2
    When I open the cart
    Then the cart should contain:
      | Sauce Labs Backpack   |
      | Sauce Labs Bike Light |

  @regression
  Scenario: Remove a product from the cart
    When I add "Sauce Labs Onesie" to the cart
    And I open the cart
    And I remove "Sauce Labs Onesie" from the cart
    Then the cart should be empty
