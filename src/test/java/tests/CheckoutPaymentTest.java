package tests;

import base.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.CheckoutPage;
import pages.SearchResultsPage;
import utils.ConfigReader;

public class CheckoutPaymentTest extends TestBase {

    @Test(priority = 1, description = "Proceed to payment and fill dummy new card details without completing the purchase")
    public void shouldFillNewCardDetailsAndStopBeforePayment() {
        loginWithConfiguredUser();

        homePage.search(ConfigReader.get("search.term"));
        SearchResultsPage resultsPage = new SearchResultsPage(driver);
        Assert.assertTrue(resultsPage.hasResults(), "Search should return products");
        resultsPage.openFirstProduct().addToCart();

        CartPage cartPage = new CartPage(driver).open();
        Assert.assertTrue(cartPage.hasItems(), "Cart should contain the added product");
        cartPage.proceedToCheckout();

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        checkoutPage.openAddNewCardForm();
        checkoutPage.fillNewCardDetails(
                ConfigReader.get("card.number"),
                ConfigReader.get("card.holder.name"),
                ConfigReader.get("card.expiry.month"),
                ConfigReader.get("card.expiry.year"),
                ConfigReader.get("card.cvv")
        );

        Assert.assertTrue(checkoutPage.isCardNumberFilled(), "Dummy card details should be entered");
        // Stop here. Do not save the card, confirm payment, or place an order.
    }
}
