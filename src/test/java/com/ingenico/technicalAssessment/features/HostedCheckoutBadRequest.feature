Feature: Hosted Checkout Bad Request
  I want to test a bad request (missing parameters) using the Hosted Checkout service

  Scenario: Send a bad request to the Hosted Checkout service
    Given merchant have a correct api keys
    When merchant send a new request to the hostedcheckout missing a required property
    Then an HTTP 400 Bad Request response is returned

