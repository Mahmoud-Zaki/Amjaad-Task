package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductPage extends BasePage {

    private final By productTitle = By.cssSelector("[data-qa='pdp-container'] h1");
    private final By addToWishlistButton = By.xpath("//div[contains(@class,'wishlistCtr')]//button");
    private final By addToCartButton = By.xpath("//div[contains(@data-qa,'add-to-cart')]/parent::button");
    private final By viewCartBtn = By.id("view-cart-btn");
    private final By productContainer = By.cssSelector("[data-qa='pdp-container']");

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

    public void addToCart() {
        scrollIntoView(addToCartButton);
        click(addToCartButton);
    }

    public void viewCart(){
        click(viewCartBtn);
    }
}
