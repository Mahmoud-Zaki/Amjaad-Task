package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductPage extends BasePage {

    /**
     * The product-details root. Shared so {@link SearchResultsPage} can settle on it without
     * declaring a second copy of the selector.
     */
    static final By CONTAINER = By.cssSelector("[data-qa='pdp-container']");

    private final By addToWishlistButton = By.cssSelector("[class*='wishlistCtr'] button[class*='wishlistButton']");
    private final By addToCartButton = By.xpath("//div[contains(@data-qa,'add-to-cart')]/parent::button");

    /** Only populated once the quick-cart confirmation actually opens. */
    private final By quickCartProductName = By.cssSelector("[data-qa='quick-cart-product-name']");

    /**
     * Badge shown when this product is already in the cart. Anchored to the product container so
     * the text match is not evaluated against every node on the page.
     */
    private final By alreadyInCartBadge = By.xpath(
            "//*[@data-qa='pdp-container']//*[contains(text(),'في سلتك')"
            + " or contains(text(),'In your cart')]");

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public boolean isProductDisplayed() {
        return isDisplayed(CONTAINER);
    }

    public void addToWishlist() {
        scrollIntoView(addToWishlistButton);
        click(addToWishlistButton);
        waitUntilClickable(addToWishlistButton);
    }

    public boolean isAlreadyInCart() {
        return isVisibleNow(alreadyInCartBadge);
    }

    /**
     * Adds the product to the cart.
     *
     * <p>Settles on the quick-cart confirmation's product name. The confirmation's buttons exist
     * in the DOM from first paint, so only its populated name is proof of an add. Returns early
     * when the product is already in the cart: the suite runs against a real account, and noon
     * then caps the quantity and makes the click a genuine no-op, which is fine — the caller only
     * needs the cart to contain the product.</p>
     */
    public void addToCart() {
        if (isAlreadyInCart()) {
            return;
        }
        scrollIntoView(addToCartButton);
        clickUntil(addToCartButton, "the product to reach the cart",
                () -> isVisibleNow(quickCartProductName) || isAlreadyInCart());
    }
}
