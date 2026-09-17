package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Checkout / payment page.
 *
 * <p>Deliberately exposes no Save card, Pay, or Place order action, so no test can complete a
 * purchase even by mistake. Do not add one.</p>
 */
public class CheckoutPage extends BasePage {

    private final By confirmLocationButton = By.cssSelector("[data-qa='address-confirm-location']");
    private final By addNewCardButton = By.cssSelector("button[class*='isCardSection'], button[class*='addNewCard']");
    private final By cardNumberField = By.id("ccNumber");
    private final By cardHolderNameField = By.cssSelector("input[name='cardNickname']");
    private final By cardExpiryMonthField = By.cssSelector("input[name='cardExpiryMonth']");
    private final By cardExpiryYearField = By.cssSelector("input[name='cardExpiryYear']");
    private final By cardCvvField = By.cssSelector("input[name='cvv']");

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    public void confirmAddressIfAsked() {
        if (isDisplayed(confirmLocationButton)) {
            click(confirmLocationButton);
        }
    }

    public void openAddNewCardForm() {
        confirmAddressIfAsked();
        if (isDisplayed(addNewCardButton)) {
            clickUntil(addNewCardButton, "the new card form to open", () -> isVisibleNow(cardNumberField));
        }
    }

    /** Fills whichever of the dummy card fields this payment variant renders. */
    public void fillNewCardDetails(String cardNumber, String holderName, String expiryMonth, String expiryYear, String cvv) {
        typeIfPresent(cardNumberField, cardNumber);
        typeIfPresent(cardHolderNameField, holderName);
        typeIfPresent(cardExpiryMonthField, expiryMonth);
        typeIfPresent(cardExpiryYearField, expiryYear);
        typeIfPresent(cardCvvField, cvv);
    }

    private void typeIfPresent(By locator, String value) {
        if (isVisibleNow(locator)) {
            type(locator, value);
        }
    }

    /** True when the card number field holds a value. Reads the property, not the attribute. */
    public boolean isCardNumberFilled() {
        return isVisibleNow(cardNumberField) && !getDomProperty(cardNumberField, "value").isBlank();
    }
}
