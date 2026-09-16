package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LocationPage extends BasePage {

    private final By addressSearchInput = By.id("address-search-input");
    private final By confirmLocationButton = By.cssSelector("[data-qa='address-confirm-location']");

    private final By roomNum = By.id("room-no");
    private final By firstName = By.id("receiver-first-name");
    private final By lastName = By.id("receiver-last-name");
    private final By phoneNum = By.id("receiverPhoneInput");

    private final By save = By.xpath("//button[@data-qa='address-save']");
    private final By firstAddressSuggestion = By.xpath("//button[contains(@class,'searchItem')]");

    public LocationPage(WebDriver driver) {
        super(driver);
    }

    public void searchAddress(String address) {
        type(addressSearchInput, address);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(6)).until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".pac-item, [class*='suggestion'], [class*='Suggestion']"))
            );
        } catch (TimeoutException ignored) {
            // Suggestions are not always exposed in the DOM; keyboard selection still works.
        }
//        pressKeys(addressSearchInput, Keys.ARROW_DOWN);
//        pressKeys(addressSearchInput, Keys.ENTER);
//        waitUntilClickable(confirmLocationButton);
    }

    public void selectFirstSuggestion() {
        waitUntilClickable(confirmLocationButton);
    }

    public void confirmLocation() {
        click(confirmLocationButton);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmLocationButton));
    }

    public void enterFlatNum(String num) {
        type(roomNum,num);
    }

    public void enterFirstName(String name) {
        type(firstName,name);
    }

    public void enterLastName(String name) {
        type(lastName,name);
    }

    public void enterPhoneNum(String num) {
        type(phoneNum,num);
    }

    public void save() {
        click(save);
    }

    public boolean isLocationPickerDisplayed() {
        return isDisplayed(addressSearchInput);
    }
}
