package com.ingenico.technicalAssessment.runner;

import java.io.File;

import org.junit.AfterClass;
import org.junit.runner.RunWith;

import com.cucumber.listener.Reporter;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/java/com/ingenico/technicalAssessment/features", glue = {
		"com/ingenico/technicalAssessment/seleniumgluecode" }, plugin = {
				"com.cucumber.listener.ExtentCucumberFormatter:target/cucumber-reports/report.html" }, monochrome = true)

public class TestRunner {
	@AfterClass
	public static void writeExtentReport() {
		Reporter.loadXMLConfig(new File("src/test/resources/cucumber-report-config.xml"));
	}

}
