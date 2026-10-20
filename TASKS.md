# TASKS.md — build order

Work top to bottom. After each task: `./gradlew assembleDebug` must pass, and run `./gradlew testDebugUnitTest` when tests exist. Tick the box when done.

## Milestone 1 — Project skeleton
- [x] 1. Create the Gradle project: `:app`, version catalog `gradle/libs.versions.toml` with versions from `ARCHITECTURE.md`, Hilt, KSP, kotlinx.serialization, desugaring, minSdk 26 / target 35.
- [x] 2. `SimKeeperApp` (@HiltAndroidApp), `MainActivity` with edge-to-edge and an empty `AppNavHost`.
- [x] 3. Theme: `Color.kt`, `StatusColors.kt` (urgency + carrier, as a CompositionLocal), `Type.kt` with bundled fonts, `Shape.kt`, `Spacing.kt`, `Theme.kt` (light/dark/system, optional dynamic colour). Add a "Theme gallery" preview showing every token.

## Milestone 2 — Domain and data
- [x] 4. `domain/Deadline.kt` + `Status` + models. Write all worked-example tests from `DATA_MODEL.md` first, then make them pass.
- [x] 5. Room: entities, converters (LocalDate, Carrier), DAOs, `AppDatabase`, `DatabaseModule`. DAO tests with in-memory DB.
- [x] 6. DataStore `SettingsRepository`; `CarrierInfoRepository` reading the two JSON assets.
- [x] 7. `SimRepository` combining DAOs into domain models with computed deadlines; `MarkRechargedUseCase` with Undo support + tests.

## Milestone 3 — Components
- [x] 8. `DaysRing` (animated, 3 sizes), `MonogramTile`, `CarrierTag`, `StatusBanner`, `SectionLabel`, buttons, `SelectChip`, `PlanChip`. Light + dark previews for each.
- [x] 9. `SimCard` with normal and urgent-footer variants; merged semantics description.

## Milestone 4 — Screens
- [x] 10. Onboarding pager (3 pages) + Permission screen + `SimDetector`.
- [x] 11. SIMs detected + SetValidity bottom sheet + AddSim manual screen. Save SIMs and recharges.
- [x] 12. Home (with empty state, banner, sections, FAB, snackbar Undo).
- [x] 13. SIM detail (hero, two deadline tiles, cheapest plan sheet, reminder chips, history, edit, delete).
- [x] 14. Recharged confirmation dialog with ring animation, confetti and haptics.
- [x] 15. Settings (theme, dynamic colour, per-app language, reminder time, quiet hours, share, footer).
- [x] 16. Navigation wiring, start destination from `onboardingDone`, deep link `simkeeper://sim/{id}`.

## Milestone 5 — Reminders and widgets
- [x] 17. Notification channels, `NotificationHelper`, `ReminderWorker`, `ReminderScheduler`, actions (I recharged, Remind me tomorrow). Ask `POST_NOTIFICATIONS` after the first SIM is saved.
- [x] 18. Glance widgets: 4×2 all SIMs, 2×2 single SIM with config activity. Refresh on data change and daily.
- [x] 19. Battery-saver guide: a dismissible card on Home for Xiaomi/Oppo/Vivo/Realme/OnePlus (check `Build.MANUFACTURER`) linking to the app's battery settings.

## Milestone 6 — Polish
- [x] 20. Motion pass (ring fill, card stagger, confetti), respecting reduced motion.
- [x] 21. Accessibility pass: TalkBack labels, font scale 2.0, contrast, 48 dp targets.
- [x] 22. `values-hi` and `values-mr` strings (mark as needing native-speaker review).
- [x] 23. App icon (adaptive, monochrome for themed icons) and splash screen API.
- [ ] 24. Fill `carrier_plans.json` with verified prices and `carrier_rules.json` `lastChecked`.

## Milestone 7 — Release
- [ ] 25. Follow `TESTING.md` full checklist on 2 real phones.
- [ ] 26. Follow `PLAYSTORE_RELEASE.md`.
