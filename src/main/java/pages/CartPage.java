package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CartPage extends BasePage {

    private final By cartLinkFallback = By.cssSelector("[data-qa='btn_cartLink-Header-Desktop']");
    private final By proceedToCheckoutButton = By.xpath("//button[contains(.,'صفحة الدفع') or contains(.,'Checkout')]");
    private final By cartItem = By.cssSelector("[data-qa='item-quantity-input'], [data-qa='cart-remove_item']");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        click(cartLinkFallback);
        wait.until(ExpectedConditions.urlContains("cart"));
        waitUntilVisible(proceedToCheckoutButton);
    }

    public boolean hasItems() {
        return waitUntilPresent(cartItem);
    }

    public void proceedToCheckout() {
        click(proceedToCheckoutButton);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("checkout"),
                ExpectedConditions.urlContains("payment"),
                ExpectedConditions.urlContains("order")
        ));
    }
}
