package base;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;

import java.time.Duration;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Shared Selenium plumbing for every Page Object.
 *
 * <p>noon.com renders several variants of the same control (desktop header, store header,
 * collapsed modal) into the DOM at once. Only one is visible, and it is frequently not the first
 * match in document order, so every wait here resolves the first <b>visible</b> match — unlike
 * {@code ExpectedConditions.visibilityOfElementLocated}, which only looks at match zero.</p>
 */
public class BasePage {

    /** Bound for optional elements and for settling, so an absent element costs seconds not minutes. */
    private static final Duration SHORT_WAIT = Duration.ofSeconds(8);

    /** Clicks to try before {@link #clickUntil} gives up. See that method for why retrying is needed. */
    private static final int CLICK_ATTEMPTS = 3;

    /** Re-locate attempts after a stale element. See {@link #withFreshElement}. */
    private static final int STALE_RETRIES = 3;

    /** Validation banners. noon reuses these classes for an empty placeholder node, hence the
     * non-blank scan in {@link #getFirstNonBlankText}. */
    protected static final By ERROR_BANNER = By.cssSelector(
            "[class*='errorMessage'], [class*='errorText'], [class*='error_'], [class*='helperText'], [role='alert']");

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    private final Duration timeout;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.timeout = Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds"));
        this.wait = new WebDriverWait(driver, timeout);
    }

    // ---------------------------------------------------------------- waits

    protected WebElement waitUntilVisible(By locator) {
        return awaitVisible(locator, timeout);
    }

    protected WebElement waitUntilClickable(By locator) {
        return awaitClickable(locator, timeout);
    }

    protected void waitUntilInvisible(By locator) {
        wait.until(webDriver -> firstVisible(webDriver, locator) == null);
    }

    private WebElement awaitVisible(By locator, Duration within) {
        return new WebDriverWait(driver, within).until(webDriver -> firstVisible(webDriver, locator));
    }

    private WebElement awaitClickable(By locator, Duration within) {
        return new WebDriverWait(driver, within).until(webDriver -> {
            WebElement element = firstVisible(webDriver, locator);
            return element != null && element.isEnabled() ? element : null;
        });
    }

    /** Waits for a condition without throwing. Returns whether it came true. */
    private boolean awaitQuietly(BooleanSupplier condition, Duration within) {
        try {
            new WebDriverWait(driver, within).until(webDriver -> condition.getAsBoolean());
            return true;
        } catch (TimeoutException notYet) {
            return false;
        }
    }

    /**
     * First displayed element for the locator, or null when nothing displayed matches. A stale
     * element counts as "not displayed" so a re-render mid-poll retries instead of failing.
     */
    private WebElement firstVisible(WebDriver webDriver, By locator) {
        for (WebElement element : webDriver.findElements(locator)) {
            try {
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (StaleElementReferenceException detached) {
                // Element vanished between findElements and isDisplayed; keep scanning.
            }
        }
        return null;
    }

    // --------------------------------------------------------------- actions

    /**
     * Re-locates and retries an interaction that failed because the element went stale.
     *
     * <p>noon streams prices and sponsored slots into pages that already look ready, so holding a
     * {@link WebElement} across that boundary is what produces stale-element failures.</p>
     */
    private void withFreshElement(By locator, Function<By, WebElement> resolver, Consumer<WebElement> action) {
        StaleElementReferenceException lastFailure = null;
        for (int attempt = 0; attempt < STALE_RETRIES; attempt++) {
            try {
                action.accept(resolver.apply(locator));
                return;
            } catch (StaleElementReferenceException detached) {
                lastFailure = detached;
            }
        }
        throw lastFailure;
    }

    protected void click(By locator) {
        clickWithin(locator, timeout);
    }

    private void clickWithin(By locator, Duration within) {
        withFreshElement(locator, target -> awaitClickable(target, within), element -> {
            try {
                element.click();
            } catch (ElementNotInteractableException covered) {
                // Sticky headers and toasts intercept native clicks; JS still reaches the element.
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            }
        });
    }

    protected void jsClick(By locator) {
        withFreshElement(locator, this::waitUntilVisible,
                element -> ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element));
    }

    /**
     * Clicks until {@code settled} comes true, then returns; throws naming {@code expectation} if
     * it never does.
     *
     * <p>React attaches its handlers after the markup is painted, so the first click on a freshly
     * loaded noon page is routinely swallowed. Retrying is the only reliable way to tell a
     * pre-hydration click apart from a genuinely broken locator.</p>
     *
     * <p>Both the click and the settle are bounded by {@link #SHORT_WAIT} rather than the full
     * explicit-wait timeout: they are multiplied by {@link #CLICK_ATTEMPTS}, so using the full
     * timeout would let one missing control burn minutes before reporting it.</p>
     */
    protected void clickUntil(By locator, String expectation, BooleanSupplier settled) {
        for (int attempt = 0; attempt < CLICK_ATTEMPTS; attempt++) {
            try {
                clickWithin(locator, SHORT_WAIT);
            } catch (RuntimeException notRenderedYet) {
                // The control has not rendered yet; fall through to the settle check and retry.
            }
            if (awaitQuietly(settled, SHORT_WAIT)) {
                return;
            }
        }
        throw new IllegalStateException("Clicked " + locator + " " + CLICK_ATTEMPTS
                + " times but never observed: " + expectation);
    }

    protected void type(By locator, String text) {
        withFreshElement(locator, this::waitUntilClickable, element -> {
            element.click();
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            element.sendKeys(text);
        });
    }

    protected void pressEnter(By locator) {
        withFreshElement(locator, this::waitUntilVisible, element -> element.sendKeys(Keys.ENTER));
    }

    /** Scrolls the first match into view, visible or not, for lazily rendered content. */
    protected void scrollIntoView(By locator) {
        withFreshElement(locator, this::firstPresent,
                element -> ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].scrollIntoView({block:'center'});", element));
    }

    private WebElement firstPresent(By locator) {
        return wait.until(webDriver -> {
            List<WebElement> elements = webDriver.findElements(locator);
            return elements.isEmpty() ? null : elements.get(0);
        });
    }

    // ----------------------------------------------------------------- reads

    /** Reads from the first visible match, re-locating if the node is replaced mid-read. */
    private <T> T read(By locator, Function<WebElement, T> reader) {
        return wait.until(webDriver -> {
            WebElement element = firstVisible(webDriver, locator);
            if (element == null) {
                return null;
            }
            try {
                return reader.apply(element);
            } catch (StaleElementReferenceException detached) {
                return null;
            }
        });
    }

    protected String getText(By locator) {
        return read(locator, element -> element.getText().trim());
    }

    /**
     * Reads a live DOM <b>property</b>. Use this for {@code value}: the {@code value} attribute
     * holds only the server-rendered default, so it stays empty after Selenium types.
     */
    protected String getDomProperty(By locator, String propertyName) {
        return read(locator, element -> orEmpty(element.getDomProperty(propertyName)));
    }

    /** Coalesced to "" so an absent value does not make {@link #read} spin until it times out. */
    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    /** Text of the first match that has any, or null when none do. */
    protected String getFirstNonBlankText(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            try {
                String text = element.getText();
                if (element.isDisplayed() && !text.isBlank()) {
                    return text.trim();
                }
            } catch (StaleElementReferenceException detached) {
                // Keep scanning.
            }
        }
        return null;
    }

    // ------------------------------------------------------------- presence

    /** True when the locator becomes visible within {@link #SHORT_WAIT}. */
    protected boolean isDisplayed(By locator) {
        return awaitQuietly(() -> isVisibleNow(locator), SHORT_WAIT);
    }

    /** True right now, without waiting — use inside a {@link #clickUntil} settle condition. */
    protected boolean isVisibleNow(By locator) {
        return firstVisible(driver, locator) != null;
    }

    /** True when the locator is in the DOM at all, visible or not. */
    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    // ----------------------------------------------------- navigation, windows

    protected void navigateTo(String url) {
        driver.get(url);
    }

    /** Absolute URL for a path under the configured storefront, e.g. {@code siteUrl("cart/")}. */
    protected String siteUrl(String path) {
        String base = ConfigReader.get("base.url");
        return base.endsWith("/") ? base + path : base + "/" + path;
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /** Waits for a second window, switches to it, and returns the handle we came from. */
    public String switchToNewestWindow() {
        String originalWindow = driver.getWindowHandle();
        wait.until(webDriver -> webDriver.getWindowHandles().size() > 1);
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        return originalWindow;
    }

    public void closeCurrentWindowAndSwitchTo(String targetWindow) {
        driver.close();
        driver.switchTo().window(targetWindow);
    }
}
