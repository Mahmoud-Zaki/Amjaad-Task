package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ConfigReader;

public class HomePage extends BasePage {

    private final By searchInput = By.id("search-input");
    private final By signInButton = By.xpath("//button[@data-qa='btn_header_signInOrUp-header-desktop']");
    private final By wishlistHeaderLink = By.xpath("//a[@data-qa='btn_wishlistLink-Header-Desktop']");
    private final By cartHeaderLink = By.xpath("//a[@data-qa='btn_cartLink-Header-Desktop']");
    private final By locationSelector = By.xpath("//div[@data-qa='country-select']//button");
    private final By locationSelectorText = By.xpath("//div[@data-qa='country-select']//button//span//span");

    private final By facebookFooterLink = By.xpath("//a[contains(@href,'facebook.com')]");
    private final By twitterFooterLink = By.xpath("//a[contains(@href,'twitter.com')]");
    private final By instagramFooterLink = By.xpath("//a[contains(@href,'instagram.com')]");
    private final By accountMenuButton = By.xpath("//button[@data-qa='btn_header_signInOrUp-header-desktop']");
    private final By profileMenuLink = By.xpath("//button[contains(@class,'profileCard')]");

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
//        wait.until(ExpectedConditions.or(
//                ExpectedConditions.urlContains("search"),
//                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-qa='plp-product-box']"))
//        ));
    }

    public void openSignIn() {
        jsClick(signInButton);
    }

    public void openWishlist() {
        click(wishlistHeaderLink);
        wait.until(ExpectedConditions.urlContains("wishlist"));
    }

    public void openCart() {
        click(cartHeaderLink);
        wait.until(ExpectedConditions.urlContains("cart"));
    }

    public void openLocationSelector() {
        click(locationSelector);
    }

    public String getDisplayedLocation() {
        return getText(locationSelectorText);
    }

    public void waitUntilLocationDiffersFrom(String originalLocation) {
        wait.until(webDriver -> {
            String current = getDisplayedLocation();
            return current != null && !current.isBlank() && !current.equals(originalLocation);
        });
    }

    public void openFacebookFromFooter() {
        scrollIntoView(facebookFooterLink);
        jsClick(facebookFooterLink);
    }

    public void openTwitterFromFooter() {
        scrollIntoView(twitterFooterLink);
        jsClick(twitterFooterLink);
    }

    public void openInstagramFromFooter() {
        scrollIntoView(instagramFooterLink);
        jsClick(instagramFooterLink);
    }

    public void openProfileFromAccountMenu() {
        waitUntilSignedIn();
        jsClick(accountMenuButton);
        jsClick(profileMenuLink);
    }

    public boolean isSignedIn() {
        String label = getText(accountMenuButton);
        return !label.contains("تسجيل الدخول") && !label.equalsIgnoreCase("Log in") && !label.equalsIgnoreCase("Sign in");
    }

    public void waitUntilSignedIn() {
        wait.until(webDriver -> isSignedIn());
        waitUntilVisible(searchInput);
    }
}
