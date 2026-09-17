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
