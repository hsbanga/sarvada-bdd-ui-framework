@login
Feature: Login
  As a shopper
  I want to sign in to the store
  So that I can browse and buy products

  Background:
    Given I am on the login page

  @smoke @regression
  Scenario: Successful login with valid credentials
    When I log in with username "standard_user" and password "secret_sauce"
    Then I should see the products page

  @regression @negative
  Scenario Outline: Login is rejected for <case>
    When I log in with username "<username>" and password "<password>"
    Then I should see the login error "<error>"

    Examples:
      | case             | username        | password     | error                                                                     |
      | a locked account | locked_out_user | secret_sauce | Sorry, this user has been locked out.                                     |
      | a wrong password | standard_user   | wrong        | Username and password do not match any user in this service               |
      | a blank username |                 | secret_sauce | Username is required                                                      |
