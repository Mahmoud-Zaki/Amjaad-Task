package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Wishlist page.
 *
 * <p>noon groups wishlist items under named lists. Products land in the default list, which is
 * selected on arrival, so the item locators below resolve without picking a list first.</p>
 */
public class WishlistPage extends BasePage {

    private final By wishlistItem = By.cssSelector("[class*='productCtr']");
    private final By wishlistItemName = By.cssSelector("h2[data-qa='product-box-name']");
    private final By moreOptionsButton = By.cssSelector("button[class*='moreOptionsButton']");
    private final By optionsMenu = By.cssSelector("#overlay-portal ul[class*='options']");
    /** The delete entry is the only destructive option, flagged with a {@code danger} class. */
    private final By removeOption = By.cssSelector("#overlay-portal button[class*='optionButton'][class*='danger']");
    private final By confirmRemoveButton = By.cssSelector("button[class*='yesBtn'], #overlay-portal button[class*='confirm']");

    public WishlistPage(WebDriver driver) {
        super(driver);
    }

    public boolean hasItems() {
        return isDisplayed(wishlistItem);
    }

    public int getItemCount() {
        return driver.findElements(wishlistItem).size();
    }

    public String getFirstItemName() {
        return getText(wishlistItemName);
    }

    public WishlistPage openMoreOptions() {
        scrollIntoView(moreOptionsButton);
        if (!clickUntil(moreOptionsButton, () -> isVisibleNow(optionsMenu), 3)) {
            throw new IllegalStateException("Wishlist item options menu did not open");
        }
        return this;
    }

    public WishlistPage removeItem() {
        click(removeOption);
        return this;
    }

    /** Accepts the removal confirmation, if this variant asks for one. */
    public void acceptConfirmationIfAsked() {
        if (isVisibleNow(confirmRemoveButton)) {
            click(confirmRemoveButton);
            waitUntilInvisible(confirmRemoveButton);
        }
    }

    /** Waits for the item grid to clear and reports whether it did. */
    public boolean isEmpty() {
        try {
            waitUntilInvisible(wishlistItem);
            return true;
        } catch (org.openqa.selenium.TimeoutException stillThere) {
            return false;
        }
    }
}
