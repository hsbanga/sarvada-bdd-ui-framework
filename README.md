# Sarvada BDD UI Automation Framework

Behaviour-driven web UI test automation by **Sarvada Infotech**, built on
**Java 21 · Cucumber 7 · Selenium 4 · TestNG · Maven**.

Write scenarios in plain-English Gherkin, keep all browser knowledge in page objects, run the
same suite on any browser, any environment, locally or on a Selenium Grid, sequentially or in
parallel, and get HTML, JSON, JUnit and timeline reports out of the box.

The repository ships with a runnable demo suite against the public
[Sauce Demo](https://www.saucedemo.com) store: login, catalogue sorting, cart and checkout.

## Quick start

Requirements: JDK 21 and Chrome, Edge or Firefox. Maven is bundled through the wrapper and
Selenium Manager downloads the matching browser driver automatically.

```bash
./mvnw verify                                             # whole suite, headed Chrome, dev env
./mvnw verify -Dcucumber.filter.tags="@smoke"             # only smoke scenarios
./mvnw verify -Dheadless=true -Dbrowser=edge              # headless Edge
./mvnw verify -Dthreads=4                                 # 4 scenarios in parallel, one browser each
./mvnw verify -Denv=stag                                  # use config/stag.properties
./mvnw verify -Dgrid.url=http://localhost:4444            # run on a Selenium Grid / cloud provider
./mvnw test -Dcucumber.features=@target/cucumber-reports/rerun.txt   # re-run only the failures
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

Reports after a run:

| Artifact | Location |
|---|---|
| Rich HTML report (Masterthought) | `target/cucumber-html-report/cucumber-html-reports/overview-features.html` |
| Cucumber HTML / JSON / JUnit | `target/cucumber-reports/` |
| Parallel timeline | `target/cucumber-reports/timeline/index.html` |
| Failure screenshots | `target/screenshots/` (also embedded in the reports) |
| Execution log | `target/logs/test-run.log` |

## Runtime switches

| Switch | Default | Purpose |
|---|---|---|
| `-Dbrowser` | `chrome` | `chrome`, `edge`, `firefox`, `safari` |
| `-Dheadless` | `false` | run without a visible browser window |
| `-Denv` | `dev` | picks `config/<env>.properties` |
| `-Dthreads` | `1` | parallel scenarios (TestNG data-provider threads) |
| `-Dcucumber.filter.tags` | all | any Cucumber tag expression, e.g. `"@regression and not @negative"` |
| `-Dgrid.url` | local | Selenium Grid or cloud hub URL |
| `-Dscreenshot.every.step` | `false` | attach a screenshot after every step |
| `-Dwait.explicit.seconds` | `15` | explicit wait timeout used by page objects |

Every switch can also be supplied as an environment variable in upper snake case
(`HEADLESS=true`, `BASE_URL=...`), which is how CI pipelines and secrets should feed the suite.

## Project layout

```
src/test
├── java/com/sarvadainfotech/bdd/ui
│   ├── runner/TestRunner.java        TestNG + Cucumber entry point, parallel data provider
│   ├── hooks/Hooks.java              browser per scenario, screenshot on failure, clean shutdown
│   ├── steps/*Steps.java             Gherkin glue: thin, delegate to page objects, assert with AssertJ
│   ├── pages/*Page.java              page objects: locators + business actions, one class per screen
│   ├── core/BasePage.java            explicit waits, stale-safe click/type, JS helpers
│   ├── driver/DriverFactory.java     Chrome / Edge / Firefox / Safari, headless, local or Grid
│   ├── driver/DriverManager.java     ThreadLocal driver so scenarios can run in parallel
│   ├── context/ScenarioContext.java  per-scenario state shared between step classes (PicoContainer)
│   └── config/Config.java            layered configuration, see below
└── resources
    ├── features/*.feature            executable specifications
    ├── config/global.properties      framework defaults
    ├── config/{dev,stag,prod}.properties   per-environment URLs and non-secret data
    ├── testng.xml                    suite definition
    └── log4j2.xml                    console + rolling file logging
```

## Design principles

- **Thin steps, fat pages.** Step definitions translate Gherkin into page-object calls and
  assertions. All waiting, locators and DOM knowledge live in page objects, so a UI change is fixed
  in exactly one place.
- **Explicit waits only.** Implicit wait is zero. `BasePage` waits for visibility or clickability
  and retries on stale elements, which removes flaky `Thread.sleep` calls entirely.
- **Parallel-safe by construction.** One `WebDriver` per thread, one `ScenarioContext` per scenario,
  no static mutable state. `-Dthreads=N` just works.
- **Layered configuration.** `-D` flag > environment variable > `config/local.properties`
  (git-ignored) > `config/<env>.properties` > `config/global.properties`. Secrets are never
  committed; they arrive through environment variables or CI secrets.
- **Zero driver management.** Selenium Manager resolves browser drivers; no binaries in the repo,
  no WebDriverManager dependency.
- **Reports for every audience.** Pretty console output for developers, JUnit XML for CI, JSON for
  the Masterthought dashboard, timeline for parallel-run analysis, and a `rerun.txt` for retries.

## Adding a new page and scenario

1. Create `pages/OrdersPage.java` extending `BasePage`; declare `By` locators and public actions.
2. Add a `features/orders.feature` describing the behaviour in Given / When / Then.
3. Run `./mvnw test`. Cucumber prints ready-to-paste snippets for any undefined step.
4. Implement the snippets in `steps/OrdersSteps.java`, calling the page object and asserting with
   AssertJ. Share objects between step classes through `ScenarioContext`.

## Continuous integration

`.github/workflows/ui-tests.yml` runs the smoke suite headless on every push and pull request,
can be triggered manually with a tag expression and browser of choice, and uploads all reports and
screenshots as build artifacts. Credentials are injected from repository secrets.

## Troubleshooting

- **`Unable to establish loopback connection` on Windows.** The JDK creates a local socket in the
  temp folder; a long or restricted `TEMP` path breaks it. Point `TMP` and `TEMP` at a short folder
  such as `C:\Temp` for the run.
- **Browser starts but the page never loads on CI.** Add `-Dheadless=true`; CI runners have no
  display.
- **Clicks or typed text are silently ignored when running several browsers in parallel on
  Windows.** Do not add `--no-sandbox` or `--disable-dev-shm-usage` to the Chrome options. Those
  flags are meant for Linux containers; on Windows they make concurrent Chrome instances drop input
  events. The framework deliberately leaves them out.
- **Element not found after a UI change.** Only the locator in the page object needs updating; the
  feature file and steps stay untouched.

## License

Copyright Sarvada Infotech. All rights reserved.
