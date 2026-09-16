# Noon Egypt Selenium Automation

Java + Selenium WebDriver + TestNG Page Object Model project for [noon.com/egypt-ar](https://www.noon.com/egypt-ar/).

## What is covered

1. Sign in with email and password
2. Add a product to the wishlist
3. Remove that product from the wishlist
4. Proceed to payment and fill a **dummy** new card — then stop
5. Update profile details
6. Update the delivery location
7. Search using an Arabic term
8. Open a social media link from the footer

**Purchase is never completed.** The checkout test fills dummy card details only. It does not save the card, confirm payment, or place an order.

## Project structure

```
src
├── main
│   └── java
│       ├── base          # BasePage: waits and shared Selenium actions
│       ├── pages         # Page Objects (locators + user actions)
│       └── utils         # ConfigReader, DriverFactory
└── test
    ├── java
    │   ├── base          # TestBase: browser lifecycle
    │   └── tests         # TestNG scenarios
    └── resources
        ├── config.properties
        └── testng.xml
```

## Prerequisites

- JDK 17+
- Maven 3.9+
- Google Chrome (default), or Firefox / Edge

Selenium Manager (bundled with Selenium 4) resolves the browser driver. You do not need to download `chromedriver` or set a local path.

## Setup

1. Clone or open this folder in your IDE.
2. Edit `src/test/resources/config.properties`:
   - Set `user.email` and `user.password` to a **test** Noon account.
   - Replace dummy profile / address values if needed.
   - Leave the card values as dummy data.
3. From the project root, download dependencies:

```bash
mvn clean compile
```

## Run

Run the full TestNG suite:

```bash
mvn clean test
```

Run one class:

```bash
mvn -Dtest=SearchTest test
```

Run with another browser:

```bash
mvn test -Dbrowser=firefox
```

Supported `browser` values: `chrome`, `firefox`, `edge`.

Headless:

```bash
mvn test -Dheadless=true
```

From IntelliJ IDEA: open `src/test/resources/testng.xml` and run the suite, or run any class under `src/test/java/tests`.

## How the classes connect

- `TestBase` creates one WebDriver per test method via `DriverFactory`, opens the base URL, and quits the browser afterwards.
- Tests extend `TestBase` and call Page Objects. They do not contain locators.
- `HomePage`, `LoginPage`, `ProductPage`, and the other page classes extend `BasePage`, which owns clicks, typing, waits, and window switching.
- `ConfigReader` loads `config.properties` and allows Maven `-Dkey=value` overrides.

## Important safety rule

Never click save-card, pay, or place-order controls. `CheckoutPage` does not implement those actions.

## Dummy values to replace

See the Dummy locators / values section in the task handoff, or search the repo for `DUMMY_` and `TODO:`.
