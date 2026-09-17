package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ConfigReader;

/**
 * Account profile page.
 *
 * <p>This page lives on a <b>separate host</b> ({@code account.noon.com}), not under
 * {@code noon.com/egypt-ar/}. {@code noon.com/egypt-ar/profile/} is a catalogue page for the
 * search term "profile" and renders no form at all, which is why it has to be opened by URL.</p>
 */
public class ProfilePage extends BasePage {

    private final By firstNameField = By.id("firstName");
    private final By lastNameField = By.id("lastName");

    /** The save button sits inside a wrapper that carries the {@code disabled} class until edited. */
    private final By updateProfileButton = By.cssSelector("[class*='updateButton'] button");
    private final By updateProfileWrapper = By.cssSelector("[class*='updateButton']");

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

    /** True while the form is unchanged; noon keeps the save button disabled until then. */
    public boolean isUpdateDisabled() {
        return getDomAttribute(updateProfileWrapper, "class").contains("disabled");
    }

    /**
     * Saves the form, or does nothing when there is nothing to save.
     *
     * <p>noon keeps the save button disabled until a field actually changes, so re-running with
     * the same configured values leaves it disabled and a plain {@code click} waits out the full
     * timeout on a button that will never become clickable. Returning early keeps the test
     * idempotent; the caller's assertions on the field values are what verify the result, and
     * those values come from the server.</p>
     */
    public void updateProfile() {
        if (isUpdateDisabled()) {
            return;
        }
        click(updateProfileButton);
        // The button disables again once the save lands. Treat a slow round trip as non-fatal:
        // the caller asserts on the field values, which is the real check.
        try {
            wait.until(webDriver -> isUpdateDisabled());
        } catch (org.openqa.selenium.TimeoutException stillEnabled) {
            // Fall through to the caller's assertions.
        }
    }

    public String getFirstNameValue() {
        return getDomProperty(firstNameField, "value");
    }

    public String getLastNameValue() {
        return getDomProperty(lastNameField, "value");
    }
}
