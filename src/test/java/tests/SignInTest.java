package tests;

import base.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.ConfigReader;

public class SignInTest extends TestBase {

    @Test(priority = 1, description = "Sign in with email and password")
    public void shouldSignInWithEmailAndPassword() {
            homePage.openSignIn();

            LoginPage loginPage = new LoginPage(driver);
            Assert.assertTrue(loginPage.isLoginFormDisplayed(), "Login form should be visible");

            loginPage
                    .enterEmailOrPhone(ConfigReader.get("user.email"))
                    .clickContinue()
                    .clickLoginWithPassword()
                    .enterPassword(ConfigReader.get("user.password"))
                    .submitSignIn();

            Assert.assertFalse(
                    driver.getCurrentUrl().contains("login"),
                    "User should leave the login step after a successful sign-in"
            );

    }
}
