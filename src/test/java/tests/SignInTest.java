package tests;

import base.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.ConfigReader;

public class SignInTest extends TestBase {

    @Test(priority = 1, description = "Sign in with email and password")
    public void shouldSignInWithEmailAndPassword() {
        signOut();
        Assert.assertFalse(homePage.isSignedIn(), "Test should start from a signed-out session");

        homePage.openSignIn();

        LoginPage loginPage = new LoginPage(driver);
        Assert.assertTrue(loginPage.isLoginFormDisplayed(), "Login form should be visible");

        loginPage
                .enterEmailOrPhone(ConfigReader.get("user.email"))
                .clickContinue()
                .clickLoginWithPassword()
                .enterPassword(ConfigReader.get("user.password"))
                .submitSignIn();

        // Asserting the URL no longer contains "login" would pass whatever happened: sign-in is a
        // modal on the home page and the URL never mentions login in the first place. Assert on
        // the header instead, which is the only thing that actually reflects the session.
        Assert.assertTrue(
                homePage.isSignedIn(),
                "Header should stop offering sign-in once the user is authenticated"
        );
        Assert.assertNull(loginPage.getErrorMessage(), "Sign-in should not report an error");
    }
}
