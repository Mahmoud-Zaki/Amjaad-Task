package base;

import org.openqa.selenium.By;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Shared Selenium plumbing for every Page Object.
 *
 * <p>noon.com is a hydrated React app that renders several variants of the same control
 * (desktop header, store header, collapsed modal) into the DOM at once. Only one of them is ever
 * visible, and it is frequently not the first match in document order. Every wait here therefore
 * resolves the first <b>visible</b> match, unlike
 * {@code ExpectedConditions.visibilityOfElementLocated}, which only ever looks at match zero.</p>
 */
public class BasePage {

    /** Short wait for optional elements, so an absent element costs seconds and not minutes. */
    protected static final Duration SHORT_WAIT = Duration.ofSeconds(8);

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds")));
    }

    // ---------------------------------------------------------------- waits

    /** First visible element matching the locator. */
    protected WebElement waitUntilVisible(By locator) {
        return wait.until(webDriver -> firstVisible(webDriver, locator));
    }

    /** First visible and enabled element matching the locator. */
    protected WebElement waitUntilClickable(By locator) {
        return wait.until(webDriver -> {
            WebElement element = firstVisible(webDriver, locator);
            return element != null && element.isEnabled() ? element : null;
        });
    }

    protected List<WebElement> waitUntilAllVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected void waitUntilInvisible(By locator) {
        wait.until(webDriver -> firstVisible(webDriver, locator) == null);
    }

    /**
     * Returns the first displayed element for the locator, or null when nothing displayed matches.
     * A stale element counts as "not displayed" so a re-render mid-poll retries instead of failing.
     */
    private WebElement firstVisible(WebDriver webDriver, By locator) {
        for (WebElement element : webDriver.findElements(locator)) {
            try {
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (RuntimeException staleOrDetached) {
                // Element vanished between findElements and isDisplayed; keep scanning.
            }
        }
        return null;
    }

    // --------------------------------------------------------------- actions

    /**
     * Re-locates and retries an interaction that failed because the element went stale.
     *
     * <p>noon streams prices, badges and sponsored slots into pages that already look ready, so
     * elements are routinely detached between being located and being used. Holding a
     * {@link WebElement} across that boundary is what produces
     * {@code StaleElementReferenceException}; re-locating on each attempt is the fix.</p>
     */
    private void withFreshElement(By locator, java.util.function.Function<By, WebElement> resolver,
                                  java.util.function.Consumer<WebElement> action) {
        RuntimeException lastFailure = null;
        for (int attempt = 0; attempt < 3; attempt++) {
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
        withFreshElement(locator, this::waitUntilClickable, element -> {
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
     * Clicks the locator and waits for {@code settled}, retrying the click up to {@code attempts}.
     *
     * <p>React attaches its handlers after the markup is painted, so the first click on a freshly
     * loaded noon page is routinely swallowed. Retrying is the only reliable way to tell a
     * pre-hydration click apart from a genuinely broken locator.</p>
     *
     * @return true once {@code settled} is observed, false when every attempt was swallowed.
     */
    protected boolean clickUntil(By locator, Supplier<Boolean> settled, int attempts) {
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                click(locator);
            } catch (RuntimeException notRenderedYet) {
                // The control has not rendered yet; fall through to the settle check and retry.
            }
            try {
                new WebDriverWait(driver, SHORT_WAIT).until(webDriver -> settled.get());
                return true;
            } catch (TimeoutException swallowed) {
                // Click had no effect. Give hydration another beat, then retry.
            }
        }
        return Boolean.TRUE.equals(settled.get());
    }

    protected void type(By locator, String text) {
        withFreshElement(locator, this::waitUntilClickable, element -> {
            element.click();
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            element.sendKeys(text);
        });
    }

    protected void clear(By locator) {
        waitUntilVisible(locator).clear();
    }

    /** Reads a value from the first visible match, re-locating if the node is replaced mid-read. */
    private <T> T read(By locator, java.util.function.Function<WebElement, T> reader) {
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
     * Reads a live DOM <b>property</b>. Use this for {@code value}: the {@code value} attribute only
     * holds the server-rendered default, so it stays empty after Selenium types into the field.
     */
    protected String getDomProperty(By locator, String propertyName) {
        // Coalesce to "" so an absent property does not make the wait spin until it times out.
        return read(locator, element -> orEmpty(element.getDomProperty(propertyName)));
    }

    /** Reads a static HTML attribute, e.g. {@code title} or {@code data-qa}. */
    protected String getDomAttribute(By locator, String attributeName) {
        return read(locator, element -> orEmpty(element.getDomAttribute(attributeName)));
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    /** True when the locator becomes visible within {@link #SHORT_WAIT}. */
    protected boolean isDisplayed(By locator) {
        try {
            new WebDriverWait(driver, SHORT_WAIT).until(webDriver -> firstVisible(webDriver, locator) != null);
            return true;
        } catch (TimeoutException absent) {
            return false;
        }
    }

    /**
     * Text of the first match that actually has some, or null when none do.
     *
     * <p>Useful for error banners: the same class is often used for an empty placeholder node that
     * renders before the message arrives, so "first match" and "the match with the message" differ.</p>
     */
    protected String getFirstNonBlankText(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            try {
                if (element.isDisplayed() && !element.getText().isBlank()) {
                    return element.getText().trim();
                }
            } catch (RuntimeException staleOrDetached) {
                // Keep scanning.
            }
        }
        return null;
    }

    /** True right now, without waiting, for polling inside a {@link #clickUntil} condition. */
    protected boolean isVisibleNow(By locator) {
        return firstVisible(driver, locator) != null;
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    protected boolean waitUntilPresent(By locator) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException absent) {
            return false;
        }
    }

    /** Scrolls the first match into view. Works on lazily rendered nodes that are not yet visible. */
    protected void scrollIntoView(By locator) {
        withFreshElement(locator, this::firstPresent,
                element -> ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].scrollIntoView({block:'center'});", element));
    }

    /** First match whether or not it is visible, for scrolling lazily rendered content into view. */
    private WebElement firstPresent(By locator) {
        return wait.until(webDriver -> {
            List<WebElement> elements = webDriver.findElements(locator);
            return elements.isEmpty() ? null : elements.get(0);
        });
    }

    protected void pressEnter(By locator) {
        pressKeys(locator, Keys.ENTER);
    }

    protected void pressKeys(By locator, CharSequence... keys) {
        withFreshElement(locator, this::waitUntilVisible, element -> element.sendKeys(keys));
    }

    protected void navigateTo(String url) {
        driver.get(url);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    // --------------------------------------------------------------- windows

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

    protected List<String> getWindowHandlesList() {
        return new ArrayList<>(driver.getWindowHandles());
    }

    public String getOriginalWindowHandle() {
        return driver.getWindowHandle();
    }

    protected void sendGlobalKeys(CharSequence... keys) {
        new Actions(driver).sendKeys(keys).perform();
    }
}
