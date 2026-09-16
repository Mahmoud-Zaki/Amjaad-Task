package tests;

import base.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FooterSocialMediaTest extends TestBase {

    @Test(priority = 1, description = "Open a social media link from the site footer")
    public void shouldOpenFacebookFromFooter() {
        String originalWindow = homePage.getOriginalWindowHandle();
        homePage.openFacebookFromFooter();
        homePage.switchToNewestWindow();

        Assert.assertTrue(
                driver.getCurrentUrl().toLowerCase().contains("facebook.com"),
                "The new window should open Noon Facebook"
        );

        driver.close();
        driver.switchTo().window(originalWindow);
        Assert.assertTrue(
                driver.getCurrentUrl().contains("noon.com"),
                "Browser should return to the Noon site"
        );
    }
}
