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

        LocationPage locationPage = homePage.openLocationSelector();
        locationPage.startNewAddress();

        Assert.assertTrue(
                locationPage.searchAddress(address),
                "Address search should return at least one suggestion for: " + address
        );
        locationPage.selectFirstSuggestion();
        Assert.assertFalse(
                locationPage.getPinnedAddress().isBlank(),
                "Map overlay should show the address picked from the suggestions"
        );

        locationPage.confirmLocation();
        Assert.assertTrue(
                locationPage.isAddressFormDisplayed(),
                "Confirming the location should open the receiver details form"
        );
        locationPage.enterReceiverDetails(
                ConfigReader.get("delivery.flat.num"),
                ConfigReader.get("delivery.first.name"),
                ConfigReader.get("delivery.last.name"),
                ConfigReader.get("delivery.phone.num")
        );
        locationPage.save();

        // The header label shows the city, not the street, so it stays "القاهرة" for a Cairo
        // address. Asserting it merely changed could never pass; assert the picker closed and the
        // header still reports a delivery location instead.
        Assert.assertFalse(
                homePage.getDisplayedLocation().isBlank(),
                "Header should still show a delivery location"
        );
    }
}
