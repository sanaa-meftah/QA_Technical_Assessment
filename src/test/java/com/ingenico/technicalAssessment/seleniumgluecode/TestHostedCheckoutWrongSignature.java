package com.ingenico.technicalAssessment.seleniumgluecode;

import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;

import com.ingenico.connect.gateway.sdk.java.AuthorizationException;
import com.ingenico.connect.gateway.sdk.java.Client;
import com.ingenico.connect.gateway.sdk.java.CommunicatorConfiguration;
import com.ingenico.connect.gateway.sdk.java.Factory;
import com.ingenico.connect.gateway.sdk.java.domain.definitions.Address;
import com.ingenico.connect.gateway.sdk.java.domain.definitions.AmountOfMoney;
import com.ingenico.connect.gateway.sdk.java.domain.hostedcheckout.CreateHostedCheckoutRequest;
import com.ingenico.connect.gateway.sdk.java.domain.hostedcheckout.definitions.HostedCheckoutSpecificInput;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.Customer;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.Order;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class TestHostedCheckoutWrongSignature {

	static AuthorizationException wrongSignatureException;

	@Given("^merchant have a wrong signature$")
	public void merchant_have_a_wrong_signature() throws Throwable {

		// Get client with wrong signature,api_key_id used as secret_api_key and vice
		// versa
		TestHostedCheckoutSuccess.client = getClient(
				TestHostedCheckoutSuccess.prop.getProperty("connect.qa.selenium.sandbox.secret_api_key"),
				TestHostedCheckoutSuccess.prop.getProperty("connect.qa.selenium.sandbox.api_key_id"));

		HostedCheckoutSpecificInput hostedCheckoutSpecificInput = new HostedCheckoutSpecificInput();
		hostedCheckoutSpecificInput.setLocale("en_GB");
		hostedCheckoutSpecificInput.setVariant("100");

		AmountOfMoney amountOfMoney = new AmountOfMoney();
		amountOfMoney.setAmount(100L);
		amountOfMoney.setCurrencyCode("EUR");

		Address billingAddress = new Address();
		billingAddress.setCountryCode("NL");

		Customer customer = new Customer();
		customer.setBillingAddress(billingAddress);
		customer.setMerchantCustomerId("2312");

		Order order = new Order();
		order.setAmountOfMoney(amountOfMoney);
		order.setCustomer(customer);

		CreateHostedCheckoutRequest body = new CreateHostedCheckoutRequest();
		body.setHostedCheckoutSpecificInput(hostedCheckoutSpecificInput);
		body.setOrder(order);

		try {
			// Send a request to the Hosted Checkout API using a wrong signature
			TestHostedCheckoutSuccess.client.merchant("2312").hostedcheckouts().create(body);
		} catch (AuthorizationException e) {
			// Catch the authorization exception, thrown when the signature is wrong
			wrongSignatureException = e;
		}
	}

	@Then("^hostedcheckout API reject the request of the merchant$")
	public void hostedcheckout_API_reject_the_request_of_the_merchant() throws Throwable {

		// Check if the request sent to the Hosted Checkout API, has thrown an AuthorizationException
		String actualMessageError = wrongSignatureException.getMessage();
		String expectedMessageError = "the Ingenico ePayments platform returned an authorization error response";
		Assert.assertEquals(expectedMessageError, actualMessageError);

		// Check if the returned status is 403
		int actualStatusCode = wrongSignatureException.getStatusCode();
		int expectedStatusCode = 403;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);

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
