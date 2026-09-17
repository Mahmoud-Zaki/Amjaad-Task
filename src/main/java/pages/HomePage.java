package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ConfigReader;

public class HomePage extends BasePage {

    private final By searchInput = By.id("search-input");

    /**
     * The header account button. Present <b>only while signed out</b>: once a session exists noon
     * replaces it with a greeting trigger carrying no {@code data-qa}, so this doubles as the
     * signed-out probe.
     */
    private final By signedOutTrigger = By.cssSelector("[data-qa^='btn_header_signInOrUp']");
    private final By loginEmailField = By.id("emailInput");

    private final By wishlistHeaderLink = By.cssSelector("a[data-qa='btn_wishlistLink-Header-Desktop']");
    private final By locationSelector = By.cssSelector("[data-qa='country-select'] button");
    private final By locationSelectorText = By.cssSelector("[data-qa='country-select'] button span span");
    private final By facebookFooterLink = By.cssSelector("a[href*='facebook.com']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open() {
        navigateTo(ConfigReader.get("base.url"));
        waitUntilVisible(searchInput);
        return this;
    }

    public void search(String term) {
        type(searchInput, term);
        pressEnter(searchInput);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/search"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-qa='plp-product-box']"))
        ));
    }

    /**
     * Opens the sign-in modal.
     *
     * <p>The header button is painted before React binds its handler, so the first click is
     * routinely swallowed and the modal never appears. That alone broke every authenticated
     * test.</p>
     */
    public void openSignIn() {
        clickUntil(signedOutTrigger, "the sign-in modal to open", () -> isVisibleNow(loginEmailField));
    }

    /** Header links are swallowed pre-hydration like every other control, so settle on the URL. */
    public void openWishlist() {
        clickUntil(wishlistHeaderLink, "the wishlist page to open", () -> getCurrentUrl().contains("wishlist"));
    }

    /** Opens the delivery-location picker and hands back the page that drives it. */
    public LocationPage openLocationSelector() {
        LocationPage locationPage = new LocationPage(driver);
        clickUntil(locationSelector, "the delivery location picker to open", locationPage::isPickerOpen);
        return locationPage;
    }

    public String getDisplayedLocation() {
        return getText(locationSelectorText);
    }

    public void openFacebookFromFooter() {
        scrollIntoView(facebookFooterLink);
        jsClick(facebookFooterLink);
    }

    /**
     * True once the header stops rendering the signed-out login trigger.
     *
     * <p>Do not compare the button's text: the desktop header button has an empty
     * {@code innerText} (its label lives in a nested {@code title} attribute), so a text-based
     * check reports "signed in" for every visitor.</p>
     */
    public boolean isSignedIn() {
        return !isPresent(signedOutTrigger);
    }

    public void waitUntilSignedIn() {
        wait.until(webDriver -> isSignedIn());
        waitUntilVisible(searchInput);
    }
}
