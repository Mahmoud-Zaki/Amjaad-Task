package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ConfigReader;

/**
 * Account profile page.
 *
 * <p>Lives on a <b>separate host</b> ({@code account.noon.com}), so it has to be opened by URL:
 * {@code noon.com/egypt-ar/profile/} is a catalogue page for the search term "profile" and renders
 * no form at all.</p>
 */
public class ProfilePage extends BasePage {

    private final By firstNameField = By.id("firstName");
    private final By lastNameField = By.id("lastName");
    private final By updateProfileButton = By.cssSelector("[class*='updateButton'] button");

    /** noon marks the save button's wrapper disabled until a field actually changes. */
    private final By disabledSaveWrapper = By.cssSelector("[class*='updateButton'][class*='disabled']");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    public ProfilePage open() {
        navigateTo(ConfigReader.get("profile.url"));
        waitUntilVisible(firstNameField);
        return this;
    }

    public boolean isProfileFormDisplayed() {
        return isDisplayed(firstNameField);
    }

    public ProfilePage enterFirstName(String firstName) {
        type(firstNameField, firstName);
        return this;
    }

    public ProfilePage enterLastName(String lastName) {
        type(lastNameField, lastName);
        return this;
    }

    /**
     * True once the form is dirty. Worth asserting before saving: if the values submitted already
     * match what the server holds, the button stays disabled, the save is a no-op, and a test that
     * then compares the fields to those same values passes without exercising anything.
     */
    public boolean isSaveEnabled() {
        return !isVisibleNow(disabledSaveWrapper);
    }

    /** Saves the form and waits for the button to return to its disabled resting state. */
    public void updateProfile() {
        click(updateProfileButton);
        wait.until(webDriver -> isVisibleNow(disabledSaveWrapper));
    }

    public String getFirstNameValue() {
        return getDomProperty(firstNameField, "value");
    }

    public String getLastNameValue() {
        return getDomProperty(lastNameField, "value");
    }
}
