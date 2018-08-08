Feature: Hosted Checkout iDEAL Payment
  I want to test an iDEAL payment using the Hosted Checkout service

  Scenario: Make an IDEAL payment
    Given merchant have a correct api keys
    When merchant send a new request to the hostedcheckout
    And consumer select the ideal payment method
    Then payment should be successful
