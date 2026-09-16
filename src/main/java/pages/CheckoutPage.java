package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Checkout / payment page.
 * Intentionally does not expose Save card, Pay, or Place order actions.
 */
public class CheckoutPage extends BasePage {

    private final By confirmLocationButton = By.cssSelector("[data-qa='address-confirm-location']");
    private final By addNewCardButton = By.xpath("//button[contains(@class,'isCardSection')]");
    private final By cardNumberField = By.id("ccNumber");
    private final By cardHolderNameField = By.xpath("//input[@name='cardNickname']");
    private final By cardExpiryMonthField = By.xpath("//input[@name='cardExpiryMonth']");
    private final By cardExpiryYearField = By.xpath("//input[@name='cardExpiryYear']");
    private final By cardCvvField = By.xpath("//input[@name='cvv']");

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
            click(addNewCardButton);
        }
    }

    public void fillNewCardDetails(String cardNumber, String holderName, String expiryMonth, String expiryYear, String cvv) {
        if (isDisplayed(cardHolderNameField)) {
            type(cardHolderNameField, holderName);
        }
        if (isDisplayed(cardExpiryMonthField)) {
            type(cardExpiryMonthField, expiryMonth);
        }
        if (isDisplayed(cardExpiryYearField)) {
            type(cardExpiryYearField, expiryYear);
        }
        if (isDisplayed(cardNumberField)) {
            type(cardNumberField, cardNumber);
        }
        if (isDisplayed(cardCvvField)) {
            type(cardCvvField, cvv);
        }
    }

    public boolean isCardNumberFilled() {
        if (!driver.findElements(cardNumberField).isEmpty()) {
            String value = getAttribute(cardNumberField, "value");
            return value != null && !value.isBlank();
        }
        String value = getAttribute(cardNumberField, "value");
        return value != null && !value.isBlank();
    }
}
