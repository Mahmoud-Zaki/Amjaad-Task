package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * The sign-in modal.
 *
 * <p>noon defaults to a one-time-password tab, so reaching the password form means switching tabs
 * after the email step — which also re-renders the email field under a different id
 * ({@code #emailInput} becomes {@code #email}).</p>
 */
public class LoginPage extends BasePage {

    private final By emailOrPhoneField = By.id("emailInput");
    private final By continueButton = By.id("login-submit");
    private final By passwordTab = By.cssSelector("button[data-qa*='log-in-with-password'], "
            + "button[data-qa*='تسجيل-الدخول-بكلمة-المرور']");
    private final By passwordField = By.id("password");
    private final By signInSubmitButton = By.cssSelector("button[class*='loginButton']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage enterEmailOrPhone(String emailOrPhone) {
        type(emailOrPhoneField, emailOrPhone);
        return this;
    }

    public LoginPage clickContinue() {
        click(continueButton);
        return this;
    }

    /**
     * Switches from the default one-time-password tab to the password tab.
     *
     * <p>A missing tab usually means noon rejected the email step rather than that the locator
     * broke — most often account throttling after repeated logins. Surface the on-screen message,
     * because a bare "tab not found" sends you hunting for the wrong bug.</p>
     */
    public LoginPage clickLoginWithPassword() {
        try {
            clickUntil(passwordTab, "the password field to appear", () -> isVisibleNow(passwordField));
        } catch (IllegalStateException noPasswordTab) {
            String reason = getErrorMessage();
            throw reason == null ? noPasswordTab
                    : new IllegalStateException("Sign-in rejected. noon reported: " + reason, noPasswordTab);
        }
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordField, password);
        return this;
    }

    /** Submits the form and waits for the modal to close. */
    public void submitSignIn() {
        click(signInSubmitButton);
        waitUntilInvisible(passwordField);
    }

    public boolean isLoginFormDisplayed() {
        return isDisplayed(emailOrPhoneField);
    }

    /** The on-screen validation or throttling message, or null when the modal shows none. */
    public String getErrorMessage() {
        return getFirstNonBlankText(ERROR_BANNER);
    }
}
