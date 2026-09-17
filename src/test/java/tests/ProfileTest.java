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

        // The profile form lives on account.noon.com, reached by URL rather than through the
        // account dropdown, which only renders on hover and exposes no stable profile link.
        ProfilePage profilePage = new ProfilePage(driver).open();
        Assert.assertTrue(profilePage.isProfileFormDisplayed(), "Profile form should be displayed");

        String firstName = ConfigReader.get("profile.first.name");
        String lastName = ConfigReader.get("profile.last.name");
        profilePage
                .enterFirstName(firstName)
                .enterLastName(lastName)
                .updateProfile();

        Assert.assertEquals(profilePage.getFirstNameValue(), firstName, "First name should be updated");
        Assert.assertEquals(profilePage.getLastNameValue(), lastName, "Last name should be updated");
    }
}
