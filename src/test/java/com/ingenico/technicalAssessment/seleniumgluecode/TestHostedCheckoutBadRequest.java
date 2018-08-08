package com.ingenico.technicalAssessment.seleniumgluecode;

import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;

import com.ingenico.connect.gateway.sdk.java.Client;
import com.ingenico.connect.gateway.sdk.java.CommunicatorConfiguration;
import com.ingenico.connect.gateway.sdk.java.Factory;
import com.ingenico.connect.gateway.sdk.java.ValidationException;
import com.ingenico.connect.gateway.sdk.java.domain.definitions.AmountOfMoney;
import com.ingenico.connect.gateway.sdk.java.domain.hostedcheckout.CreateHostedCheckoutRequest;
import com.ingenico.connect.gateway.sdk.java.domain.hostedcheckout.definitions.HostedCheckoutSpecificInput;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.Customer;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.Order;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class TestHostedCheckoutBadRequest {

	static ValidationException badRequestException;

	@When("^merchant send a new request to the hostedcheckout missing a required property$")
	public void merchant_send_a_new_request_to_the_hostedcheckout_missing_a_required_property() throws Throwable {
		
		// Prepare a hosted checkout request without the billing address parameter
		TestHostedCheckoutSuccess.client = getClient(
				TestHostedCheckoutSuccess.prop.getProperty("connect.qa.selenium.sandbox.api_key_id"),
				TestHostedCheckoutSuccess.prop.getProperty("connect.qa.selenium.sandbox.secret_api_key"));
		HostedCheckoutSpecificInput hostedCheckoutSpecificInput = new HostedCheckoutSpecificInput();
		hostedCheckoutSpecificInput.setLocale("en_GB");
		hostedCheckoutSpecificInput.setVariant("100");
		AmountOfMoney amountOfMoney = new AmountOfMoney();
		amountOfMoney.setAmount(100L);
		amountOfMoney.setCurrencyCode("EUR");
		Customer customer = new Customer();
		customer.setMerchantCustomerId("2312");
		Order order = new Order();
		order.setAmountOfMoney(amountOfMoney);
		order.setCustomer(customer);
		CreateHostedCheckoutRequest body = new CreateHostedCheckoutRequest();
		body.setHostedCheckoutSpecificInput(hostedCheckoutSpecificInput);
		body.setOrder(order);
		
		try {
			// Send the bad request (missing parameters) to the Ingenico Hosted Checkout API
			TestHostedCheckoutSuccess.client.merchant("2312").hostedcheckouts().create(body);
		} catch (ValidationException e) {
			// Catch the validation exception, thrown when a parameter is missing
			badRequestException = e;
		}
	}

	@Then("^an HTTP (\\d+) Bad Request response is returned$")
	public void an_HTTP_Bad_Request_response_is_returned(int arg1) throws Throwable {
		
		// Check if the request sent to the Hosted Checkout API, has raised a ValidationException
		String actualMessageError = badRequestException.getMessage();
		String expectedMessageError = "the Ingenico ePayments platform returned an incorrect request error response";
		Assert.assertEquals(expectedMessageError, actualMessageError);
		
		// Close Chrome window
		TestHostedCheckoutSuccess.driver.close();
	}

	private Client getClient(String apiKeyId, String secretApiKey) throws URISyntaxException {
		URL propertiesUrl = getClass().getResource("/api-configuration.properties");
		CommunicatorConfiguration configuration = Factory.createConfiguration(propertiesUrl.toURI(), apiKeyId,
				secretApiKey);
		return Factory.createClient(configuration);
	}
}
