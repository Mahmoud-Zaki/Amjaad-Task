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

    /** A driver, plus the recorder attached to it when {@code record.run=true}. */
    public record Session(WebDriver driver, RunRecorder recorder) {
    }

    public static WebDriver createDriver() {
        return createSession().driver();
    }

    public static Session createSession() {
        String browser = ConfigReader.get("browser").toLowerCase();
        boolean headless = ConfigReader.getBoolean("headless");

        WebDriver driver = switch (browser) {
            case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
            case "edge" -> new EdgeDriver(edgeOptions(headless));
            case "chrome" -> new ChromeDriver(chromeOptions(headless));
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };

        if (!RunRecorder.isEnabled()) {
            return new Session(driver, null);
        }
        // The recorder screenshots through the raw driver so its own captures do not re-enter the
        // decorator; the tests drive the decorated one.
        RunRecorder recorder = new RunRecorder(driver);
        return new Session(new EventFiringDecorator<>(recorder).decorate(driver), recorder);
    }

    /**
     * A browser profile shared by every test in the run.
     *
     * <p>TestNG gives each test method a fresh driver, which without this would mean a fresh
     * anonymous profile and a fresh sign-in per test. noon locks the account after a handful of
     * logins in quick succession ("too many invalid attempts, check your email"), so a suite that
     * signs in five times fails the last few tests on throttling rather than on anything real.
     * Persisting the profile lets the session cookie carry over, so the suite signs in once.</p>
     *
     * <p>It lives under {@code target/} so {@code mvn clean} resets it, and it is only used when
     * {@code reuse.session=true}. The suite runs sequentially; a parallel run would need one
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
        // Chrome 136+ refuses to attach to the default profile path, so use a dedicated one.
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
