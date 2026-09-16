package base;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
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
import java.util.Set;

public class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds")));
    }

    protected WebElement waitUntilVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitUntilClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected List<WebElement> waitUntilAllVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected void waitUntilInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        try {
            waitUntilClickable(locator).click();
        } catch (Exception exception) {
            jsClick(locator);
        }
    }

    protected void jsClick(By locator) {
        WebElement element = waitUntilVisible(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected void jsType(By locator, String value) {
        WebElement element = waitUntilVisible(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + value + "';", element);
    }

    protected void type(By locator, String text) {
        WebElement element = waitUntilVisible(locator);
        element.click();
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        element.sendKeys(Keys.DELETE);
        element.sendKeys(text);
    }

    protected void clear(By locator) {
        waitUntilVisible(locator).clear();
    }

    protected String getText(By locator) {
        return waitUntilVisible(locator).getText().trim();
    }

    protected String getAttribute(By locator, String attributeName) {
        return waitUntilVisible(locator).getAttribute(attributeName);
    }

    protected boolean isDisplayed(By locator) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
            return shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception exception) {
            return false;
        }
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    protected boolean waitUntilPresent(By locator) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException exception) {
            return false;
        }
    }

    protected void scrollIntoView(By locator) {
        WebElement element = waitUntilVisible(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    protected void pressEnter(By locator) {
        waitUntilVisible(locator).sendKeys(Keys.ENTER);
    }

    protected void pressKeys(By locator, CharSequence... keys) {
        waitUntilVisible(locator).sendKeys(keys);
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

    public void switchToNewestWindow() {
        String originalWindow = driver.getWindowHandle();
        wait.until(webDriver -> webDriver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String window : windows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
            }
        }
    }

    protected void closeCurrentWindowAndSwitchToOriginal(String originalWindow) {
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    protected List<String> getWindowHandlesList() {
        return new ArrayList<>(driver.getWindowHandles());
    }

    public String getOriginalWindowHandle() {
        return driver.getWindowHandle();
    }

    protected void clickFirstVisible(By locator) {
        wait.until(webDriver -> !webDriver.findElements(locator).isEmpty());
        List<WebElement> elements = driver.findElements(locator);
        for (WebElement element : elements) {
            if (element.isDisplayed()) {
                try {
                    element.click();
                } catch (Exception exception) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                }
                return;
            }
        }
        jsClick(locator);
    }
    protected void sendGlobalKeys(CharSequence... keys) {
        new Actions(driver).sendKeys(keys).perform();
    }
}
