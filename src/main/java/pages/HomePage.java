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
     * replaces it with a greeting trigger that carries no {@code data-qa} at all, so this doubles
     * as the signed-out probe.
     */
    private final By signedOutTrigger = By.cssSelector("[data-qa^='btn_header_signInOrUp']");
    private final By loginEmailField = By.id("emailInput");

    private final By wishlistHeaderLink = By.cssSelector("a[data-qa='btn_wishlistLink-Header-Desktop']");
    private final By cartHeaderLink = By.cssSelector("a[data-qa='btn_cartLink-Header-Desktop']");
    private final By cartCountBadge = By.cssSelector("[data-qa='btn_cart_count']");
    private final By locationSelector = By.cssSelector("[data-qa='country-select'] button");
    private final By locationSelectorText = By.cssSelector("[data-qa='country-select'] button span span");

    private final By facebookFooterLink = By.cssSelector("a[href*='facebook.com']");
    private final By twitterFooterLink = By.cssSelector("a[href*='twitter.com']");
    private final By instagramFooterLink = By.cssSelector("a[href*='instagram.com']");

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
     * routinely swallowed and the modal never appears. This was the single cause of every
     * authenticated test failing at {@code #emailInput}. Retry until the email field shows up.</p>
     */
    public void openSignIn() {
        if (!clickUntil(signedOutTrigger, () -> isVisibleNow(loginEmailField), 4)) {
            throw new IllegalStateException("Sign-in modal did not open after clicking the header account button");
        }
    }

    /** Header links are swallowed pre-hydration like every other control, so retry on the URL. */
    public void openWishlist() {
        openHeaderLink(wishlistHeaderLink, "wishlist");
    }

    public void openCart() {
        openHeaderLink(cartHeaderLink, "cart");
    }

    private void openHeaderLink(By headerLink, String expectedUrlFragment) {
        if (!clickUntil(headerLink, () -> getCurrentUrl().contains(expectedUrlFragment), 4)) {
            throw new IllegalStateException(
                    "Header link did not navigate to " + expectedUrlFragment + "; still at " + getCurrentUrl());
        }
    }

    /** Header cart badge count, or 0 when the cart is empty and no badge renders. */
    public int getCartCount() {
        if (!isVisibleNow(cartCountBadge)) {
            return 0;
        }
        try {
            // The badge renders Arabic-Indic digits on the Arabic storefront.
            return Integer.parseInt(toWesternDigits(getText(cartCountBadge)));
        } catch (NumberFormatException unexpectedLabel) {
            return 0;
        }
    }

    private static String toWesternDigits(String value) {
        StringBuilder digits = new StringBuilder();
        for (char character : value.toCharArray()) {
            if (Character.isDigit(character)) {
                digits.append(Character.digit(character, 10));
            }
        }
        return digits.toString();
    }

    /**
     * Opens the delivery-location picker.
     *
     * <p>Signed-in visitors land on the saved-addresses modal; guests go straight to the map
     * overlay. Settle on either so the caller does not care which session it has.</p>
     */
    public void openLocationSelector() {
        boolean opened = clickUntil(
                locationSelector,
                () -> isVisibleNow(LocationPage.ADD_NEW_ADDRESS) || isVisibleNow(LocationPage.MAP_OVERLAY),
                4);
        if (!opened) {
            throw new IllegalStateException("Delivery location picker did not open");
        }
    }

    public String getDisplayedLocation() {
        return getText(locationSelectorText);
    }

    public void openFacebookFromFooter() {
        openFooterLink(facebookFooterLink);
    }

    public void openTwitterFromFooter() {
        openFooterLink(twitterFooterLink);
    }

    public void openInstagramFromFooter() {
        openFooterLink(instagramFooterLink);
    }

    private void openFooterLink(By footerLink) {
        scrollIntoView(footerLink);
        jsClick(footerLink);
    }

    /**
     * True once the header stops rendering the signed-out login trigger.
     *
     * <p>Do not compare the button's text. The desktop header button has an empty
     * {@code innerText} (its label lives in a nested {@code title} attribute), so a text-based
     * check reports "signed in" for every visitor, signed in or not.</p>
     */
    public boolean isSignedIn() {
        return !isPresent(signedOutTrigger);
    }

    public void waitUntilSignedIn() {
        wait.until(webDriver -> isSignedIn());
        waitUntilVisible(searchInput);
    }
}
