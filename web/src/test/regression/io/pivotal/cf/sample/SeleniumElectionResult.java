package io.pivotal.cf.sample;

import static org.testng.AssertJUnit.assertTrue;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test
public class SeleniumElectionResult {
	
	WebDriver driver;
	
  public void testElectionResult() {
	  
	  driver.get("http://electiondashboard_us.cfapps.io"); 
	  
      assertTrue(driver.getTitle().equalsIgnoreCase("PivotalOne Demo"));
  }
  @BeforeTest
  public void beforeTest() {
//	  final FirefoxProfile firefoxProfile = new FirefoxProfile();
//	  firefoxProfile.setPreference("xpinstall.signatures.required", false);
	  System.setProperty("webdriver.gecko.driver", "P:\\Priya\\Software\\geckodriver.exe");
	  driver = new FirefoxDriver();
  }

  @AfterTest
  public void afterTest() {
	  driver.close();
  }

}
