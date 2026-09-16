package tests;

import base.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ProfilePage;
import utils.ConfigReader;

public class ProfileTest extends TestBase {

    @Test(priority = 1, description = "Update profile details")
    public void shouldUpdateProfileDetails() {
        loginWithConfiguredUser();
        homePage.openProfileFromAccountMenu();

        ProfilePage profilePage = new ProfilePage(driver);
        String firstName = ConfigReader.get("profile.first.name");
        String lastName = ConfigReader.get("profile.last.name");
        profilePage
                .enterFirstName(firstName)
                .enterLastName(lastName)
                .updateProfile();

        Assert.assertEquals(profilePage.getFirstNameValue(), firstName, "First name should be updated");
    }
}
