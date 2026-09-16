package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {

    private final By emailOrPhoneField = By.id("emailInput");
    private final By continueButton = By.id("login-submit");
    private final By loginWithPasswordTab = By.xpath("//button[contains(@data-qa,'تسجيل-الدخول-بكلمة-المرور') or contains(@data-qa,'log-in-with-password')]");
    private final By passwordField = By.id("password");
    private final By signInSubmitButton = By.xpath("//button[contains(@class,'loginButton')]");

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

    public LoginPage clickLoginWithPassword() {
        click(loginWithPasswordTab);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordField, password);
        return this;
    }

    public void submitSignIn() {
        click(signInSubmitButton);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(passwordField));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(emailOrPhoneField));
    }

    public boolean isLoginFormDisplayed() {
        return isDisplayed(emailOrPhoneField);
    }
}
