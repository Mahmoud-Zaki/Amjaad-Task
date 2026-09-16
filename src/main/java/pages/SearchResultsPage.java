package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SearchResultsPage extends BasePage {

    private final By productCard = By.xpath("//div[@data-qa='plp-product-box']");
    private final By firstProductLink = By.xpath("//a[contains(@class,'productBoxLink')]");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean hasResults() {
        return isDisplayed(productCard) || isDisplayed(firstProductLink);
    }

    public void openFirstProduct() {
        waitUntilVisible(productCard);
        click(firstProductLink);
        waitUntilVisible(By.cssSelector("[data-qa='pdp-container']"));
    }
}
