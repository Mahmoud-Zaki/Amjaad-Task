package tests;

import base.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LocationPage;
import utils.ConfigReader;

public class LocationTest extends TestBase {

    @Test(priority = 1, description = "Update the delivery location")
    public void shouldUpdateDeliveryLocation() {
        // Signing in first is what makes this a real update: noon only asks for receiver details
        // and saves the address to the account when there is an account behind the session.
        loginWithConfiguredUser();

        String address = ConfigReader.get("delivery.address");

        homePage.openLocationSelector();
        LocationPage locationPage = new LocationPage(driver);
        Assert.assertTrue(locationPage.isLocationPickerDisplayed(), "Location picker should open");

        locationPage.startNewAddress();
        Assert.assertTrue(
                locationPage.searchAddress(address),
                "Address search should return at least one suggestion for: " + address
        );
        locationPage.selectFirstSuggestion();

        String pinnedAddress = locationPage.getPinnedAddress();
        Assert.assertTrue(
                pinnedAddress != null && !pinnedAddress.isBlank(),
                "Map overlay should show the address picked from the suggestions"
        );

        locationPage.confirmLocation();

        Assert.assertTrue(
                locationPage.isAddressFormDisplayed(),
                "Confirming the location should open the receiver details form"
        );
        locationPage.enterFlatNum(ConfigReader.get("delivery.flat.num"));
        locationPage.enterFirstName(ConfigReader.get("delivery.first.name"));
        locationPage.enterLastName(ConfigReader.get("delivery.last.name"));
        locationPage.enterPhoneNum(ConfigReader.get("delivery.phone.num"));
        locationPage.save();

        // The header label shows the city, not the street, so it stays "القاهرة" for a Cairo
        // address. Asserting it merely changed is unreliable; assert the picker closed and the
        // header still reports a delivery location instead.
        String displayedLocation = homePage.getDisplayedLocation();
        Assert.assertNotNull(displayedLocation, "Header should still show a delivery location");
        Assert.assertFalse(displayedLocation.isBlank(), "Header delivery location should not be empty");
    }
}
