package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.events.EventFiringDecorator;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        String browser = ConfigReader.get("browser").toLowerCase();
        boolean headless = ConfigReader.getBoolean("headless");

        WebDriver driver = switch (browser) {
            case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
            case "edge" -> new EdgeDriver(edgeOptions(headless));
            case "chrome" -> new ChromeDriver(chromeOptions(headless));
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };

        if (!RunRecorder.isEnabled()) {
            return driver;
        }
        // The recorder screenshots through the raw driver so its captures do not re-enter the
        // decorator; the tests drive the decorated one.
        return new EventFiringDecorator<>(new RunRecorder(driver)).decorate(driver);
    }

    /**
     * A browser profile shared by every test in the run, so the suite signs in once: noon locks
     * the account after repeated logins in quick succession.
     *
     * <p>Under {@code target/} so {@code mvn clean} resets it. A parallel run would need one
     * profile per thread, as Chrome refuses to share a profile directory.</p>
     */
    private static Path sharedProfileDir() {
        return Paths.get("target", "browser-profile").toAbsolutePath();
    }

    private static boolean reuseSession() {
        return ConfigReader.getBoolean("reuse.session");
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-infobars");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-features=PasswordLeakDetection,AutofillServerCommunication");
        if (reuseSession()) {
            options.addArguments("--user-data-dir=" + sharedProfileDir());
        }
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (reuseSession()) {
            options.addArguments("-profile", sharedProfileDir().toString());
        }
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }

    private static EdgeOptions edgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        if (reuseSession()) {
            options.addArguments("--user-data-dir=" + sharedProfileDir());
        }
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }
        return options;
    }
}
