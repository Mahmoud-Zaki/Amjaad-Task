package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SearchResultsPage extends BasePage {

    private final By productCard = By.cssSelector("[data-qa='plp-product-box']");
    private final By firstProductLink = By.cssSelector("a[class*='productBoxLink']");
    private final By productDetailsContainer = By.cssSelector("[data-qa='pdp-container']");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean hasResults() {
        return isDisplayed(productCard) || isDisplayed(firstProductLink);
    }

    public int getResultCount() {
        return driver.findElements(productCard).size();
    }

    /**
     * Opens the first product.
     *
     * <p>The results grid keeps re-rendering as prices, badges and sponsored slots stream in, which
     * detaches the anchor between locating it and clicking it. {@link #clickUntil} re-locates on
     * every attempt, so a stale hit is retried rather than failing the test.</p>
     */
    public void openFirstProduct() {
        waitUntilVisible(productCard);
        boolean opened = clickUntil(firstProductLink, () -> isVisibleNow(productDetailsContainer), 4);
        if (!opened) {
            throw new IllegalStateException("Could not open a product from the search results");
        }
    }
}
