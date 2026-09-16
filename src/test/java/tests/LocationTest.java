package tests;

import base.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LocationPage;
import utils.ConfigReader;

public class LocationTest extends TestBase {

    @Test(priority = 1, description = "Update the delivery location")
    public void shouldUpdateDeliveryLocation() {
        String address = ConfigReader.get("delivery.address");
        String originalLocation = homePage.getDisplayedLocation();

        homePage.openLocationSelector();
        LocationPage locationPage = new LocationPage(driver);
        Assert.assertTrue(locationPage.isLocationPickerDisplayed(), "Location picker should open");

        locationPage.searchAddress(address);
        locationPage.selectFirstSuggestion();
        locationPage.confirmLocation();

        locationPage.enterFlatNum(ConfigReader.get("delivery.flat.num"));
        locationPage.enterFirstName(ConfigReader.get("delivery.first.name"));
        locationPage.enterLastName(ConfigReader.get("delivery.last.name"));
        locationPage.enterPhoneNum(ConfigReader.get("delivery.phone.num"));
        locationPage.save();

        homePage.waitUntilLocationDiffersFrom(originalLocation);
        String updatedLocation = homePage.getDisplayedLocation();
        Assert.assertNotNull(updatedLocation, "Header should still show a delivery location");
        Assert.assertFalse(updatedLocation.isBlank(), "Updated location should not be empty");
        Assert.assertNotEquals(
                updatedLocation,
                originalLocation,
                "Displayed location should change after confirmation"
        );
    }
}
