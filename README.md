# Noon Egypt Selenium Automation

Java 17 + Selenium 4 + TestNG Page Object Model suite for [noon.com/egypt-ar](https://www.noon.com/egypt-ar/).

## Scenarios covered

| Test class | Scenario | Account needed |
| --- | --- | --- |
| `SearchTest` | Search with an Arabic term | no |
| `FooterSocialMediaTest` | Open a social media link from the footer | no |
| `SignInTest` | Sign in with email and password | yes |
| `ProfileTest` | Update profile first / last name | yes |
| `LocationTest` | Add and save a delivery location | yes |
| `WishlistTest` | Add a product to the wishlist, then remove it | yes |
| `CheckoutPaymentTest` | Reach payment and fill a **dummy** card, then stop | yes |

**A purchase is never completed.** `CheckoutPage` deliberately exposes no save-card, pay, or
place-order action, so no test can place an order even by mistake. Do not add one.

## Prerequisites

- JDK 17+
- Maven 3.9+
- Google Chrome (default), or Firefox / Edge

Selenium Manager (bundled with Selenium 4) resolves the browser driver, so there is no
`chromedriver` to download or path to configure.

## Setup

Edit `src/test/resources/config.properties`:

- `user.email` / `user.password` — a **test** noon account. These ship as `dummy email` /
  `dummy password`; the signed-in tests fail until you set real ones. Pass them at run time to
  keep them out of the working tree:
  `mvn test -Duser.email=you@example.com -Duser.password=secret`
- `search.term`, `profile.*`, `delivery.*` — data the scenarios type in.
- `card.*` — leave as dummy data.

Any key can be overridden on the command line, e.g. `-Dbrowser=firefox`, without editing the file.

## Run

```bash
mvn test
```

One class:

```bash
mvn test -Dtest=SearchTest
```

Another browser (`chrome`, `firefox`, `edge`) or headless:

```bash
mvn test -Dbrowser=firefox -Dheadless=true
```

A different suite file:

```bash
mvn test -Dsuite.file=src/test/resources/my-suite.xml
```

From IntelliJ IDEA: run `src/test/resources/testng.xml`, or any class under `src/test/java/tests`.

A failing test writes a screenshot to `target/screenshots/`, named after the test method. Passing
tests capture nothing, so the folder only ever holds what is worth looking at.

> Prefer `mvn test` over `mvn clean test` for repeat runs. `clean` wipes the shared browser
> profile, which forces a fresh sign-in — see below.

## How the pieces fit together

- `TestBase` creates one WebDriver per test method via `DriverFactory`, opens the base URL, and
  quits the browser afterwards, capturing a screenshot if the test failed.
- Tests extend `TestBase` and drive Page Objects. They hold no locators.
- Page Objects extend `BasePage`, which owns every wait, click, keystroke and window switch.
- `ConfigReader` loads `config.properties` and lets Maven `-Dkey=value` override any key.

## Things about noon that the framework has to work around

These are the reasons `BasePage` does not just call `ExpectedConditions` directly. Each one caused
a real, reproducible failure.

**Duplicate DOM variants.** noon renders several copies of the same control at once — desktop
header, store header, and collapsed modal copies — and only one is visible. It is frequently not
the first match: `By.id("view-cart-btn")` matches two nodes and the first is 0×0. Selenium's
`visibilityOfElementLocated` only ever inspects match zero, so it waits out the full timeout on an
element that is present and visible on screen. Every wait in `BasePage` resolves the first
**visible** match instead.

**Clicks before hydration.** The markup is painted before React binds its handlers, so the first
click on a freshly loaded page is silently swallowed. This alone broke every signed-in test: the
sign-in modal never opened and each one failed waiting for `#emailInput`. `BasePage.clickUntil`
clicks, waits for a caller-supplied settle condition, retries, and throws naming the expectation
that never arrived. Settle conditions must be wait-free (`isVisibleNow`, not `isDisplayed`):
`clickUntil` bounds each attempt itself, and a condition that waits internally would nest one
timeout inside another.

**Elements going stale.** Prices, badges and sponsored slots stream into pages that already look
ready, detaching elements between being located and being used. `BasePage` re-locates on every
interaction rather than holding a `WebElement` across that boundary.

**Login throttling.** noon locks the account after a few logins in quick succession ("too many
invalid attempts — check your email"), and recovery needs a link from the account's inbox. A suite
that signs in once per test method trips this and the last tests fail on throttling rather than on
anything real. With `reuse.session=true` (the default) every test shares one browser profile under
`target/browser-profile`, so the session cookie carries over and the suite signs in once;
`loginWithConfiguredUser()` returns immediately when a session already exists. `mvn clean` resets
the profile. A parallel run would need one profile per thread, as Chrome will not share a profile
directory.

**Controls that carry no stable hook.** Several have no `data-qa` and hash-suffixed CSS-module
class names (`_loginBtn_1dxqd_165`, `SiteHeader-module-scss-module__QOvl8a__searchInput`). These
are matched on the stable fragment via `[class*='...']`, or on their visible label where there is
nothing else — the cart's checkout button is matched on its text.

**Multi-step flows behind one button.** The delivery-location picker is three surfaces, not one:
saved addresses (signed-in only) → map overlay → a *separate* search overlay that owns the real
input and the suggestion list → back to the map to confirm → receiver details form. Guests never
reach the last step, because noon only collects receiver details when there is an account to save
them against. `LocationPage` documents and drives all of it.

**Pages that are not where the URL suggests.** The profile form is on a different host,
`account.noon.com/egypt-ar/profile/` (`profile.url` in the config).
`noon.com/egypt-ar/profile/` is a catalogue page for the search term "profile" and renders no form
at all — as does `noon.com/egypt-ar/login/`.

**Assertions that cannot fail.** A few original assertions passed regardless of what the site did
and were replaced:

- The header account button has an empty `innerText` (its label lives in a nested `title`
  attribute), so `getText().contains("Sign in")` reported "signed in" for every visitor. The
  button is also removed from the DOM entirely once signed in, so it cannot serve as the "is there
  an account button" probe either. `isSignedIn()` now checks for the absence of the signed-out
  trigger.
- `SignInTest` asserted the URL no longer contained `login`. Sign-in is a modal on the home page
  and the URL never mentions login, so the assertion passed whatever happened. It now asserts on
  the header.
- `CheckoutPage.isCardNumberFilled()` read the `value` *attribute*, which holds only the
  server-rendered default and stays empty no matter what Selenium types. It now reads the DOM
  property, which is why `BasePage` exposes `getDomProperty` rather than a general
  `getAttribute`.
- `LocationTest` asserted the header location label changed. That label shows the city, so it
  stays "القاهرة" for any Cairo address and the assertion could not pass. It now asserts the
  address was pinned and the receiver form saved.
- `ProfileTest` submitted the configured names every run. From the second run on they already
  matched the account, so noon left the save button disabled, nothing was submitted, and the
  assertions — which compare the fields to those same configured values — still passed. The test
  now alternates the value so every run has a real edit to save, and asserts the save button
  actually enabled before submitting.
