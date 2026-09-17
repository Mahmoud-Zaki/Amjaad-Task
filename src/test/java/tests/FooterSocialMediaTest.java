package tests;

import base.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FooterSocialMediaTest extends TestBase {

    @Test(priority = 1, description = "Open a social media link from the site footer")
    public void shouldOpenFacebookFromFooter() {
        homePage.openFacebookFromFooter();
        String noonWindow = homePage.switchToNewestWindow();

        Assert.assertTrue(
                driver.getCurrentUrl().toLowerCase().contains("facebook.com"),
                "The new window should open the noon Facebook page, but was: " + driver.getCurrentUrl()
        );

        homePage.closeCurrentWindowAndSwitchTo(noonWindow);
        Assert.assertTrue(
                driver.getCurrentUrl().contains("noon.com"),
                "Browser should return to the noon site"
        );
    }
}
