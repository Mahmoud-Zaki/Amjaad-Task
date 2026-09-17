package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * The delivery-location picker, which noon splits across three surfaces:
 *
 * <ol>
 *   <li><b>Saved addresses</b> — signed-in visitors only; "add a new address" leads to the map.</li>
 *   <li><b>Map overlay</b> — its search box is only a trigger. Focusing it swaps in a separate
 *       search overlay that owns the real input and the suggestion list; picking a suggestion
 *       returns here to confirm.</li>
 *   <li><b>Receiver details</b> — flat, name, phone, then save. Guests never see this form: noon
 *       only collects receiver details when there is an account to save them against.</li>
 * </ol>
 *
 * <p>Neither search input carries an id, so both are located structurally.</p>
 */
public class LocationPage extends BasePage {

    private final By addNewAddress = By.cssSelector("[data-qa='address-add-new']");
    private final By mapOverlay = By.cssSelector("[class*='mapOverlay']");

    /** Stage-one search box, which opens the stage-two overlay that owns the real input. */
    private final By mapSearchTrigger = By.cssSelector("[class*='mapOverlay'] input[class*='searchInput']");
    private final By addressSearchInput = By.cssSelector("#overlay-portal [class*='searchBox'] input");
    private final By addressSuggestion = By.cssSelector("#overlay-portal button[class*='searchItem']");

    private final By confirmLocationButton = By.cssSelector("[data-qa='address-confirm-location']");

    private final By roomNum = By.id("room-no");
    private final By firstName = By.id("receiver-first-name");
    private final By lastName = By.id("receiver-last-name");
    private final By phoneNum = By.id("receiverPhoneInput");
    private final By saveButton = By.cssSelector("button[data-qa='address-save']");

    public LocationPage(WebDriver driver) {
        super(driver);
    }

    /** True the moment either entry surface is up: saved addresses for a session, map for a guest. */
    public boolean isPickerOpen() {
        return isVisibleNow(addNewAddress) || isVisibleNow(mapOverlay);
    }

    /** Moves from the saved-addresses modal to the map overlay. A no-op for guests. */
    public void startNewAddress() {
        if (isVisibleNow(addNewAddress)) {
            clickUntil(addNewAddress, "the map overlay to open", () -> isVisibleNow(mapOverlay));
        }
    }

    /**
     * Types the address into the stage-two search overlay and waits for suggestions.
     *
     * @return true when at least one suggestion appeared.
     */
    public boolean searchAddress(String address) {
        clickUntil(mapSearchTrigger, "the address search overlay to open", () -> isVisibleNow(addressSearchInput));
        type(addressSearchInput, address);
        return isDisplayed(addressSuggestion);
    }

    /** Picks the first suggestion, which returns the flow to the map overlay. */
    public void selectFirstSuggestion() {
        click(addressSuggestion);
        waitUntilVisible(confirmLocationButton);
    }

    /**
     * The picker's text, which includes the address pinned above the confirm button.
     *
     * <p>Read from the modal root, not the map overlay: the address panel is a sibling of the map,
     * and its own lines are hash-suffixed CSS-module classes with nothing stable to anchor on.</p>
     */
    public String getPinnedAddress() {
        return getText(By.cssSelector("#overlay-portal"));
    }

    public void confirmLocation() {
        click(confirmLocationButton);
        waitUntilInvisible(confirmLocationButton);
    }

    /** True when the receiver details form is shown (signed-in visitors only). */
    public boolean isAddressFormDisplayed() {
        return isDisplayed(roomNum);
    }

    public void enterReceiverDetails(String flatNum, String receiverFirstName, String receiverLastName, String phone) {
        type(roomNum, flatNum);
        type(firstName, receiverFirstName);
        type(lastName, receiverLastName);
        type(phoneNum, phone);
    }

    /**
     * Saves the address and waits for the picker to close, reporting the on-screen validation text
     * if it does not — a rejected field otherwise leaves the form open with no clue why.
     */
    public void save() {
        try {
            clickUntil(saveButton, "the address picker to close",
                    () -> !isVisibleNow(saveButton) && !isVisibleNow(mapOverlay));
        } catch (IllegalStateException didNotClose) {
            String validation = getFirstNonBlankText(ERROR_BANNER);
            throw validation == null ? didNotClose
                    : new IllegalStateException(didNotClose.getMessage() + " — noon reported: " + validation,
                            didNotClose);
        }
    }
}
