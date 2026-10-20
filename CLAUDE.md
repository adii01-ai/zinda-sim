# CLAUDE.md — SIM Keeper

You are building **SIM Keeper**, a native Android app (Kotlin + Jetpack Compose) that reminds people in India to recharge their SIMs so they never lose their number. Read these files before writing any code:

| File | What it holds |
| --- | --- |
| `PRD.md` | What the product is, who it is for, what is in and out of v1 |
| `DESIGN_SYSTEM.md` | Colours, type, spacing, components, motion: the exact visual spec |
| `SCREENS.md` | Every screen, its content, states and actions |
| `ARCHITECTURE.md` | Tech stack, module/package layout, libraries and versions |
| `DATA_MODEL.md` | Room entities, DAOs, the deadline maths |
| `CARRIER_RULES.md` | Carrier grace rules and the bundled JSON format |
| `TASKS.md` | Build order. Work through it top to bottom |
| `TESTING.md` | What to test and how |
| `PLAYSTORE_RELEASE.md` | Signing, listing, closed test, launch |

## Non-negotiable product rules
1. **Offline only.** No `INTERNET` permission, no login, no accounts, no analytics, no ads SDK in v1.
2. **The app cannot see balance, data usage or plans.** It only knows what the user entered. Never design or code "data remaining" or "low balance" features.
3. **No payments.** The main action is "I recharged", which restarts a SIM's countdown.
4. **Two deadlines per SIM:** plan expiry and the 90-day no-use limit. Always show the earlier one. See `DATA_MODEL.md`.
5. **No carrier logos.** Use letter monogram tiles (J, A, V, B) in carrier colours.
6. **No AI features.**
7. **Permissions:** only `READ_PHONE_STATE` (optional, for carrier name and slot) and `POST_NOTIFICATIONS` (Android 13+). Never read the phone number, SMS, call log or contacts.

## How to work
- Follow `TASKS.md` in order. Finish and build each task before starting the next.
- After every task, run `./gradlew assembleDebug` and fix all errors and warnings you introduced.
- Run unit tests with `./gradlew testDebugUnitTest` after touching `domain/` or `data/`.
- Keep composables small (under ~150 lines). Put previews (`@Preview`, light and dark) next to every screen and reusable component.
- Use only colours, type styles and spacing from `ui/theme/` (generated from `DESIGN_SYSTEM.md`). No hard-coded hex values or `dp` magic numbers inside screens.
- All user-facing text goes in `res/values/strings.xml` (with `values-hi` and `values-mr` stubs). No hard-coded strings in Kotlin.
- Dates: use `java.time.LocalDate` everywhere. Format as `21 Oct 2026` (`d MMM yyyy`) with the device locale.
- Money: show as `₹149`. Never invent plan prices. Prices come only from `assets/carrier_plans.json`.
- Do not add libraries that are not in `ARCHITECTURE.md` without saying why.
- When unsure about product behaviour, check `PRD.md` and `SCREENS.md` first. If still unclear, ask instead of guessing.

## Build environment (VS Code)
- JDK 17, Android SDK (command-line tools) with platform `android-35` and build-tools installed, `ANDROID_HOME` set.
- Build: `./gradlew assembleDebug`. Install on a phone with USB debugging: `./gradlew installDebug`.
- Recommended VS Code extensions: Kotlin (fwcd), Gradle for Java.
- Android Studio is optional but is the easiest way to run the emulator and previews.
