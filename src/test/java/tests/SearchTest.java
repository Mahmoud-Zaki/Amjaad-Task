package tests;

import base.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SearchResultsPage;
import utils.ConfigReader;

public class SearchTest extends TestBase {

    @Test(priority = 1, description = "Search using an Arabic search term")
    public void shouldReturnResultsForArabicSearchTerm() {
        String arabicTerm = ConfigReader.get("search.term");
        homePage.search(arabicTerm);

        SearchResultsPage resultsPage = new SearchResultsPage(driver);
        Assert.assertTrue(resultsPage.hasResults(), "Arabic search should return product results");
        Assert.assertTrue(driver.getCurrentUrl().contains("search"), "User should land on a search results experience");
    }
}
