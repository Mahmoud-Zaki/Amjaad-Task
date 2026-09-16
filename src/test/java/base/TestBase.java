package base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pages.HomePage;
import pages.LoginPage;
import utils.ConfigReader;
import utils.DriverFactory;

import java.time.Duration;

public class TestBase {

    protected WebDriver driver;
    protected HomePage homePage;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.createDriver();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("page.load.timeout.seconds")));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        driver.manage().window().maximize();
        homePage = new HomePage(driver).open();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void loginWithConfiguredUser() {
        homePage.openSignIn();
        new LoginPage(driver)
                .enterEmailOrPhone(ConfigReader.get("user.email"))
                .clickContinue()
                .clickLoginWithPassword()
                .enterPassword(ConfigReader.get("user.password"))
                .submitSignIn();
        //homePage.waitUntilSignedIn();
    }
}
