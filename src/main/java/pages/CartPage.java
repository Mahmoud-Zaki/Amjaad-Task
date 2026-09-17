package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ConfigReader;

public class CartPage extends BasePage {

    private final By cartHeaderLink = By.cssSelector("[data-qa='btn_cartLink-Header-Desktop']");
    /** The checkout button carries no {@code data-qa}, so it has to be matched on its label. */
    private final By proceedToCheckoutButton =
            By.xpath("//button[contains(.,'صفحة الدفع') or contains(.,'Checkout')]");
    private final By cartItem = By.cssSelector("[data-qa='item-quantity-input']");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    /** Opens the cart by URL, which avoids depending on the header rendering first. */
    public CartPage open() {
        navigateTo(ConfigReader.get("base.url") + "cart/");
        waitUntilVisible(proceedToCheckoutButton);
        return this;
    }

    public void openFromHeader() {
        click(cartHeaderLink);
        wait.until(ExpectedConditions.urlContains("cart"));
        waitUntilVisible(proceedToCheckoutButton);
    }

    public boolean hasItems() {
        return isDisplayed(cartItem);
    }

    public int getItemCount() {
        return driver.findElements(cartItem).size();
    }

    public void proceedToCheckout() {
        if (!clickUntil(proceedToCheckoutButton, this::isOnCheckout, 4)) {
            throw new IllegalStateException("Checkout button did not leave the cart; still at " + getCurrentUrl());
        }
    }

    private boolean isOnCheckout() {
        String url = getCurrentUrl();
        return url.contains("checkout") || url.contains("payment") || url.contains("order");
    }
}
