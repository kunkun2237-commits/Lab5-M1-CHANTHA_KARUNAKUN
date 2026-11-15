# Lab 5: Lotus Ledger (navigation and forms)

Continues Lab 4. Three screens with Navigation 3: Dashboard, Detail, Form. Add and edit with input checks, category filter, total in KHR that counts up.

## Open and run
(The Gradle wrapper files `gradlew`, `gradlew.bat` and `gradle/wrapper/` are included. Do not delete them.)

1. Android Studio > File > Open > the folder that contains `settings.gradle.kts`.
2. Gradle JDK = 17 or 21 (File > Settings > Build, Execution, Deployment > Build Tools > Gradle).
3. First sync downloads Gradle 8.11.1 and Android platform 36 (Studio offers to install it). Needs internet.
4. Run on a device or emulator with Android 11 (API 30) or newer.

## Files (app/src/main/java/com/example/lotusledger)
| File | What it does |
|------|--------------|
| `LotusLedgerApp.kt` | NavKeys (`Dashboard`, `Detail`, `Form`), `rememberNavBackStack`, `NavDisplay`, slide animations. Owns the list, the filter and the events (save, delete + Undo) |
| `DashboardScreen.kt` | stateless dashboard: total (animated), category bars, D9 filter, list, empty message |
| `DetailScreen.kt` | D6 detail layout, delete dialog, "Expense not found" |
| `FormScreen.kt` | add/edit form: D7 category picker, D8 Save button, date picker, discard dialog |
| `ExpenseFormState.kt` | the ONE state holder for the form (values, "touched" flags, rules, saver for rotation) |
| `ExpenseValidation.kt` | amount / remark rules as plain functions |
| `ExpenseLogic.kt` | `USD_TO_KHR` constant, totals in KHR, filter, sort, money format |
| `Components.kt` | top bar with back arrow, not-found screen, animated error text |
| `ui/theme/*`, `res/values*` | theme and strings from Lab 4 (+ new strings in English and Khmer) |

## My design choices
- **D6 detail layout:** hero amount card on top, then a card of info rows (currency, category, date, remark)
- **D7 category picker:** chip row (tap the chosen chip again to clear it)
- **D8 save action:** big Save button at the bottom, gray until every rule passes
- **D9 dashboard filter:** dropdown menu with "All" and each category

## Requirement check
- Part 1: Navigation 3 (`NavKey` per screen, `rememberNavBackStack`, `NavDisplay`). Row tap -> Detail, add button -> empty Form, Edit -> filled Form. Back and the arrow both go back. After a delete the Detail is removed; after a save the Form (and the old Detail) are removed. Slide in from the right, slide out to the right. Serializable keys keep the screen after rotation
- Part 2: Detail shows every field, delete asks with an `AlertDialog`, missing expense shows "Expense not found"
- Part 3: amount > 0 with a decimal keyboard, KHR/USD next to the amount (starts at KHR), category required, date from `DatePickerDialog` (starts today), remark max 60. Errors show only after the user used the field, with `AnimatedVisibility`. Save disabled until valid. New id from `UUID.randomUUID()`, edit keeps its id. "Expense saved" snackbar. `BackHandler` + "Discard changes?" dialog
- Part 4: plain `val` for the filtered list, filter kept with `rememberSaveable`, total follows the filter
- Part 5: `USD_TO_KHR = 4000.0` written once, total in KHR, `animateFloatAsState` with 500 ms. "Remove animations" in Accessibility makes it change at once (Compose follows the system animation scale)
- Lab 4 still works: row delete + Undo, English/Khmer, theme colours only

## Good to know
- The list lives in `remember`, so a rotation rebuilds it from the sample data. An expense you added then shows "Expense not found" if its Detail is open during a rotation (the lab expects this).
- Category bars show all expenses; the list and the total follow the filter.
- On Android 12+ dynamic color replaces my palette. Use an Android 11 emulator or set `dynamicColor = false` to show D4.
