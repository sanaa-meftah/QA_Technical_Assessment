package com.ingenico.technicalAssessment.seleniumgluecode;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import com.ingenico.connect.gateway.sdk.java.Client;
import com.ingenico.connect.gateway.sdk.java.CommunicatorConfiguration;
import com.ingenico.connect.gateway.sdk.java.Factory;
import com.ingenico.connect.gateway.sdk.java.domain.definitions.Address;
import com.ingenico.connect.gateway.sdk.java.domain.definitions.AmountOfMoney;
import com.ingenico.connect.gateway.sdk.java.domain.hostedcheckout.CreateHostedCheckoutRequest;
import com.ingenico.connect.gateway.sdk.java.domain.hostedcheckout.CreateHostedCheckoutResponse;
import com.ingenico.connect.gateway.sdk.java.domain.hostedcheckout.definitions.HostedCheckoutSpecificInput;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.Customer;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.Order;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class TestHostedCheckoutSuccess {
	static WebDriver driver;
	static Properties prop = new Properties();
	static Client client;

	@Given("^merchant have a correct api keys$")
	public void merchant_have_a_correct_api_keys() throws Throwable {
		File resourcesDirectory = new File("src/test/resources");

		// Load the test parameters
		InputStream input = null;
		input = new FileInputStream(resourcesDirectory.getAbsolutePath() + "/test-parameters.properties");
		prop.load(input);

		// Initialize the web driver
		System.setProperty("webdriver.chrome.driver",
				resourcesDirectory.getAbsolutePath() + "/web_drivers/chromedriver.exe");
		driver = new ChromeDriver();

		// Sign in sandbox
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.get(prop.getProperty("connect.qa.selenium.sandbox.url"));
		driver.findElement(By.id("loginName")).sendKeys(prop.getProperty("connect.qa.selenium.sandbox.email"));
		driver.findElement(By.id("loginPassword")).sendKeys(prop.getProperty("connect.qa.selenium.sandbox.password"));

		// Click on login button
		WebElement clickLogin = driver
				.findElement(By.cssSelector("button[ng-class=\"{'button--is-loading': loading.login}\"]"));
		clickLogin.click();

		// Click API Keys menu
		WebElement clickApiKeys = driver.findElement(By.cssSelector("li[ng-class=\"{active: isActive('/apikey')}\"]"));
		clickApiKeys.click();

		// Check API Keys values
		List<WebElement> inputsText = driver.findElements(By.xpath("//div[contains(@class, 'panel panel-key')]"));
		String actualAPIkeyID = inputsText.get(0).getText();
		String actualSecretApiKey = inputsText.get(1).getText();
		String expectedAPIkeyID = new String(prop.getProperty("connect.qa.selenium.sandbox.api_key_id"));
		String expectedSecretApiKey = new String(prop.getProperty("connect.qa.selenium.sandbox.secret_api_key"));
		Assert.assertEquals(expectedAPIkeyID, actualAPIkeyID);
		Assert.assertEquals(expectedSecretApiKey, actualSecretApiKey);
	}

	@When("^merchant send a new request to the hostedcheckout$")
	public void merchant_send_a_new_request_to_the_hostedcheckout() throws Exception {

		// Get a new client
		client = getClient(prop.getProperty("connect.qa.selenium.sandbox.api_key_id"),
				prop.getProperty("connect.qa.selenium.sandbox.secret_api_key"));

		// Prepare the hosted checkout request
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

		// Send the request to the Ingenico Hosted Checkout API
		CreateHostedCheckoutResponse response = client.merchant("2312").hostedcheckouts().create(body);

		// Redirection to the payment page
		driver.navigate().to("https://payment." + response.getPartialRedirectUrl());
	}

	@When("^consumer select the ideal payment method$")
	public void consumer_select_the_ideal_payment_method() throws Throwable {

		// Select iDEAL Payment
		WebElement clickIdealPayment = driver.findElement(By.cssSelector("li[data-sortablelistidentifier=\"809\"]"));
		clickIdealPayment.click();

		// Select the issuer
		Select issuer = new Select(driver.findElement(By.id("issuerId")));
		issuer.selectByVisibleText("ING");

		// Click the pay button
		WebElement pay = driver.findElement(By.id("primaryButton"));
		pay.click();

		// Click the confirm transaction button
		WebElement confirmTransaction = driver.findElement(By.name("button.edit"));
		confirmTransaction.click();
	}

	@Then("^payment should be successful$")
	public void payment_should_be_successful() throws Throwable {

		// Get the actual payment status
		String actualPaymentStatus = driver.findElement(By.cssSelector("div.paymentoptions p")).getText();

		// Expected payment status
		String expectedPaymentStatus = new String("Your payment is successful.");

		// Compare the status
		Assert.assertEquals(expectedPaymentStatus, actualPaymentStatus);

		// Close the client connection
		client.close();

		// Close Chrome window
		driver.close();
	}

	private Client getClient(String apiKeyId, String secretApiKey) throws URISyntaxException {
		URL propertiesUrl = getClass().getResource("/api-configuration.properties");
		CommunicatorConfiguration configuration = Factory.createConfiguration(propertiesUrl.toURI(), apiKeyId,
				secretApiKey);
		return Factory.createClient(configuration);
	}

}
