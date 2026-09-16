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
        resultsPage.openFirstProduct();

        ProductPage productPage = new ProductPage(driver);
        Assert.assertTrue(productPage.isProductDisplayed(), "Product details page should be displayed");
        productPage.addToWishlist();

        homePage.openWishlist();
        WishlistPage wishlistPage = new WishlistPage(driver);
        Assert.assertTrue(wishlistPage.hasItems(), "Wishlist should contain the added product");
    }

    @Test(priority = 2, dependsOnMethods = "shouldAddProductToWishlist",
            description = "Remove that product from the wishlist")
    public void shouldRemoveProductFromWishlist() {
        loginWithConfiguredUser();
        homePage.openWishlist();

        WishlistPage wishlistPage = new WishlistPage(driver);
        Assert.assertTrue(wishlistPage.hasItems(), "Wishlist should contain a product to remove");
        wishlistPage
                .openMoreOptions()
                .removeItem()
                .acceptConfirmation();
        Assert.assertTrue(wishlistPage.isEmpty(), "Wishlist should be empty after removing the product");
    }
}
