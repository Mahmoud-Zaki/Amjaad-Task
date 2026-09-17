package base;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pages.HomePage;
import pages.LoginPage;
import utils.ConfigReader;
import utils.DriverFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class TestBase {

    private static final Path SCREENSHOT_DIR = Path.of("target", "screenshots");

    protected WebDriver driver;
    protected HomePage homePage;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.createDriver();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("page.load.timeout.seconds")));
        // Keep the implicit wait at zero: mixing it with explicit waits makes every negative
        // lookup (isVisibleNow, findElements) pay the implicit timeout and slows the suite down.
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().window().maximize();
        homePage = new HomePage(driver).open();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (driver == null) {
            return;
        }
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshot(result.getMethod().getMethodName());
        }
        driver.quit();
    }

    /** Saves a screenshot next to the surefire reports so failures can be diagnosed after a run. */
    private void captureScreenshot(String testName) {
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            byte[] image = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path target = SCREENSHOT_DIR.resolve(testName + "-" + System.currentTimeMillis() + ".png");
            Files.write(target, image);
            System.out.println("Screenshot saved: " + target.toAbsolutePath());
        } catch (IOException | RuntimeException couldNotCapture) {
            System.out.println("Could not capture screenshot: " + couldNotCapture.getMessage());
        }
    }

    /**
     * Drops any session carried over in the shared browser profile. Needed by tests whose subject
     * <em>is</em> signing in: with {@code reuse.session=true} they would otherwise find no
     * sign-in button to click.
     */
    protected void signOut() {
        driver.manage().deleteAllCookies();
        homePage = new HomePage(driver).open();
    }

    /**
     * Signs in, unless the shared browser profile already carries a live session. noon throttles
     * repeated logins, so re-authenticating in every test method is what makes the last tests of a
     * suite fail. See {@code DriverFactory.sharedProfileDir()}.
     */
    protected void loginWithConfiguredUser() {
        if (homePage.isSignedIn()) {
            return;
        }
        homePage.openSignIn();
        new LoginPage(driver)
                .enterEmailOrPhone(ConfigReader.get("user.email"))
                .clickContinue()
                .clickLoginWithPassword()
                .enterPassword(ConfigReader.get("user.password"))
                .submitSignIn();
        homePage.waitUntilSignedIn();
    }
}
