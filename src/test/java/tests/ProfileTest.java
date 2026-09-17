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

        // Submit something that differs from what the account already holds. Re-submitting the
        // configured values would leave the form clean, the save button disabled and the update a
        // no-op -- and the assertions below would still pass, so the test would stop testing
        // anything from its second run onward. Alternating gives every run a real edit to save.
        String firstName = alternate(ConfigReader.get("profile.first.name"), profilePage.getFirstNameValue());
        String lastName = alternate(ConfigReader.get("profile.last.name"), profilePage.getLastNameValue());

        profilePage
                .enterFirstName(firstName)
                .enterLastName(lastName);
        Assert.assertTrue(profilePage.isSaveEnabled(), "Editing the name should enable the save button");

        profilePage.updateProfile();

        Assert.assertEquals(profilePage.getFirstNameValue(), firstName, "First name should be updated");
        Assert.assertEquals(profilePage.getLastNameValue(), lastName, "Last name should be updated");
    }

    /** Returns whichever of the two spellings the profile is not currently set to. */
    private String alternate(String configured, String current) {
        return configured.equals(current) ? configured + " QA" : configured;
    }
}
