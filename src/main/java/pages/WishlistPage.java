package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class WishlistPage extends BasePage {

    private final By wishlistItem = By.xpath("//div[contains(@class,'productCtr')]");
    private final By wishlistItemName = By.xpath("//h2[@data-qa='product-box-name']");
    private final By moreOptionsButton = By.xpath("//button[contains(@class,'moreOptionsButton')]");
    private final By optionsDDL = By.xpath("//*[@id='overlay-portal']//ul");
    private final By acceptConfirmationBtn = By.xpath("//button[contains(@class,'yesBtn')]");

    public WishlistPage(WebDriver driver) {
        super(driver);
    }

    public boolean hasItems() {
        return waitUntilPresent(wishlistItem);
    }

    public String getFirstItemName() {
        return getText(wishlistItemName);
    }

    public WishlistPage openMoreOptions(){
        click(moreOptionsButton);
        waitUntilVisible(optionsDDL);
        return this;
    }

    public WishlistPage removeItem() {
        click(By.xpath("//*[@id='overlay-portal']//ul//li[contains(.,'حذف') or contains(.,'Remove') or contains(.,'Delete')]"));
        return this;
    }

    public void acceptConfirmation(){
        click(acceptConfirmationBtn);
        waitUntilInvisible(acceptConfirmationBtn);
    }

    public boolean isEmpty() {
        waitUntilInvisible(wishlistItem);
        return !isPresent(wishlistItem);
    }
}
