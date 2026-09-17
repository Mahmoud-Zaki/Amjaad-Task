package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CartPage extends BasePage {

    /** The checkout button carries no {@code data-qa}, so it has to be matched on its label. */
    private final By proceedToCheckoutButton =
            By.xpath("//button[contains(.,'صفحة الدفع') or contains(.,'Checkout')]");
    private final By cartItem = By.cssSelector("[data-qa='item-quantity-input']");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    /** Opens the cart by URL, which avoids depending on the header rendering first. */
    public CartPage open() {
        navigateTo(siteUrl("cart/"));
        waitUntilVisible(proceedToCheckoutButton);
        return this;
    }

    public boolean hasItems() {
        return isDisplayed(cartItem);
    }

    public void proceedToCheckout() {
        clickUntil(proceedToCheckoutButton, "checkout to open", this::isOnCheckout);
    }

    private boolean isOnCheckout() {
        String url = getCurrentUrl();
        return url.contains("checkout") || url.contains("payment") || url.contains("order");
    }
}
