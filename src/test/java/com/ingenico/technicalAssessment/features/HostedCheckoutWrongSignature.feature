Feature: Hosted Checkout Wrong Signature
  I want to test a bad signature for submitting a request to the Hosted Checkout service

  Scenario: Send a request to the Hosted Checkout service with a wrong signature
    Given merchant have a correct api keys
    And merchant have a wrong signature
    When merchant send a new request to the hostedcheckout
    Then hostedcheckout API reject the request of the merchant