package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SearchResultsPage extends BasePage {

    private final By productCard = By.cssSelector("[data-qa='plp-product-box']");
    private final By firstProductLink = By.cssSelector("a[class*='productBoxLink']");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean hasResults() {
        return isDisplayed(By.cssSelector("[data-qa='plp-product-box'], a[class*='productBoxLink']"));
    }

    /**
     * Opens the first product.
     *
     * <p>The results grid keeps re-rendering as prices, badges and sponsored slots stream in,
     * detaching the anchor between locating and clicking it. {@code clickUntil} re-locates on each
     * attempt, so a stale hit is retried rather than failing the test.</p>
     */
    public ProductPage openFirstProduct() {
        waitUntilVisible(productCard);
        // Settle wait-free: a settle condition that waits would nest one timeout inside another.
        clickUntil(firstProductLink, "the product details page to open",
                () -> isVisibleNow(ProductPage.CONTAINER));
        return new ProductPage(driver);
    }
}
