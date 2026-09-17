package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * The delivery-location picker.
 *
 * <p>noon splits this across three surfaces, which is why a single-input page object could never
 * drive it:</p>
 * <ol>
 *   <li><b>Saved addresses</b> — signed-in visitors only. "Add a new address" leads to the map.</li>
 *   <li><b>Map overlay</b> — its search box is only a trigger; focusing it swaps in a separate
 *       search overlay that owns the real input and the Google-backed suggestion list. Picking a
 *       suggestion returns here for confirmation.</li>
 *   <li><b>Receiver details</b> — flat number, name and phone, then save. Guests never see this
 *       form: noon only collects receiver details when it has an account to save them against.</li>
 * </ol>
 *
 * <p>Neither search input carries an id, so both are located structurally.</p>
 */
public class LocationPage extends BasePage {

    /** Saved-addresses modal. Also used by {@link HomePage} to detect that the picker opened. */
    public static final By ADD_NEW_ADDRESS = By.cssSelector("[data-qa='address-add-new']");

    /** Map overlay. Also used by {@link HomePage} for the guest variant of the picker. */
    public static final By MAP_OVERLAY = By.cssSelector("[class*='mapOverlay']");

    /** Stage-two search box, which the map overlay's own search box opens. */
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

    public boolean isLocationPickerDisplayed() {
        return isVisibleNow(ADD_NEW_ADDRESS) || isDisplayed(MAP_OVERLAY);
    }

    /** Moves from the saved-addresses modal to the map overlay. A no-op for guests. */
    public void startNewAddress() {
        if (isVisibleNow(ADD_NEW_ADDRESS)
                && !clickUntil(ADD_NEW_ADDRESS, () -> isVisibleNow(MAP_OVERLAY), 3)) {
            throw new IllegalStateException("'Add a new address' did not open the map overlay");
        }
        waitUntilVisible(MAP_OVERLAY);
    }

    /**
     * Types the address into the search overlay and waits for suggestions.
     *
     * @return true when at least one suggestion appeared.
     */
    public boolean searchAddress(String address) {
        if (!clickUntil(mapSearchTrigger, () -> isVisibleNow(addressSearchInput), 3)) {
            throw new IllegalStateException("Address search overlay did not open from the map overlay");
        }
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
     * so the overlay's own text is empty. The individual address lines are hash-suffixed
     * CSS-module classes with nothing stable to anchor on.</p>
     */
    public String getPinnedAddress() {
        return getText(By.cssSelector("#overlay-portal"));
    }

    public void confirmLocation() {
        click(confirmLocationButton);
        waitUntilInvisible(confirmLocationButton);
    }

    /** True when the post-confirmation receiver details form is shown (signed-in visitors only). */
    public boolean isAddressFormDisplayed() {
        return isDisplayed(roomNum);
    }

    public void enterFlatNum(String num) {
        type(roomNum, num);
    }

    public void enterFirstName(String name) {
        type(firstName, name);
    }

    public void enterLastName(String name) {
        type(lastName, name);
    }

    public void enterPhoneNum(String num) {
        type(phoneNum, num);
    }

    /**
     * Saves the address and waits for the picker to close.
     *
     * <p>Settles on the whole picker closing rather than just the button disappearing, and reports
     * the on-screen validation text if it does not, since a rejected field leaves the form open
     * with no other clue as to why.</p>
     */
    public void save() {
        if (!clickUntil(saveButton, () -> !isVisibleNow(saveButton) && !isVisibleNow(MAP_OVERLAY), 3)) {
            String validation = getFirstNonBlankText(
                    By.cssSelector("#overlay-portal [class*='errorMessage'], #overlay-portal [class*='error_']"));
            throw new IllegalStateException("Saving the address did not close the picker."
                    + (validation == null ? "" : " noon reported: " + validation));
        }
    }
}
