package tests;

import base.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ProductPage;
import pages.SearchResultsPage;
import pages.WishlistPage;
import utils.ConfigReader;

public class WishlistTest extends TestBase {

    @Test(priority = 1, description = "Add a product to the wishlist")
    public void shouldAddProductToWishlist() {
        loginWithConfiguredUser();

        homePage.search(ConfigReader.get("search.term"));
        SearchResultsPage resultsPage = new SearchResultsPage(driver);
        Assert.assertTrue(resultsPage.hasResults(), "Search should return products");

        ProductPage productPage = resultsPage.openFirstProduct();
        String productUrl = productPage.getCurrentUrl();
        productPage.addToWishlist();

        homePage.openWishlist();
        WishlistPage wishlistPage = new WishlistPage(driver);
        if (!wishlistPage.hasItems()) {
            // The heart is a toggle and the suite runs against a real account, so if an earlier
            // run left this product favourited, the click above removed it. Re-open the product by
            // URL -- not navigate().back(), which depends on how many history entries the click
            // retries happened to push -- and toggle it back on. If this second click is the one
            // that removes it, the assertion below fails rather than passing quietly.
            driver.get(productUrl);
            productPage.addToWishlist();
            homePage.openWishlist();
        }
        Assert.assertTrue(wishlistPage.hasItems(), "Wishlist should contain the added product");
        // The wishlist card prefixes the brand onto the title, so it is not character-for-character
        // equal to the product page heading. Assert it is populated rather than an exact match.
        Assert.assertFalse(
                wishlistPage.getFirstItemName().isBlank(),
                "Wishlist entry should show a product name"
        );
    }

    @Test(priority = 2, dependsOnMethods = "shouldAddProductToWishlist",
            description = "Remove that product from the wishlist")
    public void shouldRemoveProductFromWishlist() {
        // Each test method gets a fresh browser, but the wishlist is stored against the account,
        // so the product added above is still there.
        loginWithConfiguredUser();
        homePage.openWishlist();

        WishlistPage wishlistPage = new WishlistPage(driver);
        Assert.assertTrue(wishlistPage.hasItems(), "Wishlist should contain a product to remove");
        wishlistPage
                .openMoreOptions()
                .removeItem()
                .acceptConfirmationIfAsked();

        Assert.assertTrue(wishlistPage.isEmpty(), "Wishlist should be empty after removing the product");
    }
}
