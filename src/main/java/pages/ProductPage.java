package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductPage extends BasePage {

    private final By productContainer = By.cssSelector("[data-qa='pdp-container']");
    private final By productTitle = By.cssSelector("[data-qa='pdp-container'] h1");
    private final By addToWishlistButton = By.cssSelector("[class*='wishlistCtr'] button[class*='wishlistButton']");
    private final By addToCartButton = By.xpath("//div[contains(@data-qa,'add-to-cart')]/parent::button");

    /**
     * Quick-cart confirmation button. noon pre-renders two nodes with this id before anything is
     * added (the sticky-bar copy is 0x0), so its presence is <b>not</b> proof the add succeeded —
     * see {@link #addToCart(HomePage)}.
     */
    private final By viewCartButton = By.id("view-cart-btn");

    /** Only rendered with content once the quick-cart confirmation actually opens. */
    private final By quickCartProductName = By.cssSelector("[data-qa='quick-cart-product-name']");

    /** Badge shown when this product is already in the cart. */
    private final By alreadyInCartBadge = By.xpath(
            "//*[contains(text(),'في سلتك') or contains(text(),'In your cart')]");

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public boolean isProductDisplayed() {
        return isDisplayed(productContainer);
    }

    public String getProductTitle() {
        return getText(productTitle);
    }

    public void addToWishlist() {
        scrollIntoView(addToWishlistButton);
        click(addToWishlistButton);
        waitUntilClickable(addToWishlistButton);
    }

    /**
     * Adds the product to the cart.
     *
     * <p>Accepts either signal, because neither is sufficient alone. The header badge counts
     * distinct items, so re-adding a product already in the cart only bumps its quantity and
     * leaves the badge unchanged. The quick-cart confirmation covers that case, but its buttons
     * exist in the DOM from first paint, so only its populated product name is proof of an add.</p>
     */
    public boolean isAlreadyInCart() {
        return isVisibleNow(alreadyInCartBadge);
    }

    public void addToCart(HomePage homePage) {
        // The suite runs against a real account, so an earlier run may already have put this
        // product in the cart. noon then caps the quantity and the add is a genuine no-op, which
        // is fine: the caller only needs the cart to contain the product.
        if (isAlreadyInCart()) {
            return;
        }
        int countBefore = homePage.getCartCount();
        scrollIntoView(addToCartButton);
        boolean added = clickUntil(
                addToCartButton,
                () -> homePage.getCartCount() > countBefore || isVisibleNow(quickCartProductName),
                3);
        if (!added) {
            throw new IllegalStateException("Add to cart produced neither a quick-cart confirmation "
                    + "nor a change in the header cart count (was " + countBefore + ")");
        }
    }

    public void viewCart() {
        click(viewCartButton);
    }
}
