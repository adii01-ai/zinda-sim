# TESTING.md — SIM Keeper

## Commands
- Unit tests: `./gradlew testDebugUnitTest`
- Instrumented/UI tests (phone or emulator connected): `./gradlew connectedDebugAndroidTest`
- Lint: `./gradlew lintDebug` (fix all errors; no new warnings)

## Unit tests (required)
- `DeadlineTest`: every row of the worked-examples table in `DATA_MODEL.md`, plus boundaries: 7 → SOON, 8 → SAFE, 2 → URGENT, 3 → SOON, 0 → URGENT, -1 → EXPIRED; leap year (2028-02-29); ring progress clamps at 0 and 1.
- `MarkRechargedUseCaseTest`: recharge updates dates and history; Undo restores the exact previous state.
- `ReminderDecisionTest` (pure function used by the worker): fires on enabled offsets only, not twice on the same day, respects quiet hours, repeats every 2 days during grace.
- `CarrierMappingTest`: "Jio 4G", "JIO", "Airtel", "Vi India", "Vodafone IN", "IDEA", "BSNL MOBILE", "CellOne", "" map correctly.
- DAO tests with in-memory Room: insert/observe/cascade delete.

Use a fixed `Clock` at 2026-10-19 in all tests.

## UI tests (Compose)
- Onboarding → Permission denied → manual add → Home shows the SIM.
- Home: urgent SIM first; "I recharged" turns it safe; Undo restores.
- SetValidity preview updates when chips change (28 days + Today = 16 Nov 2026, 28 days left).
- Settings theme switch changes the theme immediately.

## Manual checklist (2 real phones, one dual-SIM; ideally one Xiaomi/Realme)
- [ ] Fresh install: setup under 60 seconds.
- [ ] Permission allowed: both SIMs detected with correct carrier and slot.
- [ ] Permission denied: manual flow works, never re-prompts.
- [ ] Change device date forward (Developer options / auto time off) → notifications at 7, 3, 1, 0 days, correct text and actions.
- [ ] "Remind me tomorrow" snoozes 24 h. Quiet hours defer to 08:00.
- [ ] Reboot phone → reminders still arrive.
- [ ] Battery saver on → reminders still arrive within the day.
- [ ] Widgets: add both, values match the app, update after "I recharged".
- [ ] Dark mode, font size largest, display size largest: nothing clipped.
- [ ] TalkBack: every screen navigable, cards read as one sentence.
- [ ] Hindi and Marathi: no truncated text.
- [ ] Airplane mode the whole time: everything works (the app has no internet).
- [ ] Uninstall/reinstall: starts clean (backup is v1.1).
