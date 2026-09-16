package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProfilePage extends BasePage {

    private final By firstNameField = By.id("firstName");
    private final By lastNameField = By.id("lastName");
    private final By updateProfileButton = By.xpath("//div[contains(@class,'updateButton')]/button");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    public ProfilePage enterFirstName(String firstName){
        waitUntilVisible(firstNameField);
        type(firstNameField, firstName);
        return this;
    }

    public ProfilePage enterLastName(String lastName){
        type(lastNameField, lastName);
        return this;
    }
    public void updateProfile() {
        click(updateProfileButton);
        wait.until(webDriver -> {
            String value = getAttribute(firstNameField, "value");
            return value != null && !value.isBlank();
        });
    }

    public String getFirstNameValue() {
        return getAttribute(firstNameField, "value");
    }

}
