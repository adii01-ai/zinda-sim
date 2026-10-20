# TASKS.md — build order

Work top to bottom. After each task: `./gradlew assembleDebug` must pass, and run `./gradlew testDebugUnitTest` when tests exist. Tick the box when done.

## Milestone 1 — Project skeleton
- [ ] 1. Create the Gradle project: `:app`, version catalog `gradle/libs.versions.toml` with versions from `ARCHITECTURE.md`, Hilt, KSP, kotlinx.serialization, desugaring, minSdk 26 / target 35.
- [ ] 2. `SimKeeperApp` (@HiltAndroidApp), `MainActivity` with edge-to-edge and an empty `AppNavHost`.
- [ ] 3. Theme: `Color.kt`, `StatusColors.kt` (urgency + carrier, as a CompositionLocal), `Type.kt` with bundled fonts, `Shape.kt`, `Spacing.kt`, `Theme.kt` (light/dark/system, optional dynamic colour). Add a "Theme gallery" preview showing every token.

## Milestone 2 — Domain and data
- [ ] 4. `domain/Deadline.kt` + `Status` + models. Write all worked-example tests from `DATA_MODEL.md` first, then make them pass.
- [ ] 5. Room: entities, converters (LocalDate, Carrier), DAOs, `AppDatabase`, `DatabaseModule`. DAO tests with in-memory DB.
- [ ] 6. DataStore `SettingsRepository`; `CarrierInfoRepository` reading the two JSON assets.
- [ ] 7. `SimRepository` combining DAOs into domain models with computed deadlines; `MarkRechargedUseCase` with Undo support + tests.

## Milestone 3 — Components
- [ ] 8. `DaysRing` (animated, 3 sizes), `MonogramTile`, `CarrierTag`, `StatusBanner`, `SectionLabel`, buttons, `SelectChip`, `PlanChip`. Light + dark previews for each.
- [ ] 9. `SimCard` with normal and urgent-footer variants; merged semantics description.

## Milestone 4 — Screens
- [ ] 10. Onboarding pager (3 pages) + Permission screen + `SimDetector`.
- [ ] 11. SIMs detected + SetValidity bottom sheet + AddSim manual screen. Save SIMs and recharges.
- [ ] 12. Home (with empty state, banner, sections, FAB, snackbar Undo).
- [ ] 13. SIM detail (hero, two deadline tiles, cheapest plan sheet, reminder chips, history, edit, delete).
- [ ] 14. Recharged confirmation dialog with ring animation, confetti and haptics.
- [ ] 15. Settings (theme, dynamic colour, per-app language, reminder time, quiet hours, share, footer).
- [ ] 16. Navigation wiring, start destination from `onboardingDone`, deep link `simkeeper://sim/{id}`.

## Milestone 5 — Reminders and widgets
- [ ] 17. Notification channels, `NotificationHelper`, `ReminderWorker`, `ReminderScheduler`, actions (I recharged, Remind me tomorrow). Ask `POST_NOTIFICATIONS` after the first SIM is saved.
- [ ] 18. Glance widgets: 4×2 all SIMs, 2×2 single SIM with config activity. Refresh on data change and daily.
- [ ] 19. Battery-saver guide: a dismissible card on Home for Xiaomi/Oppo/Vivo/Realme/OnePlus (check `Build.MANUFACTURER`) linking to the app's battery settings.

## Milestone 6 — Polish
- [ ] 20. Motion pass (ring fill, card stagger, confetti), respecting reduced motion.
- [ ] 21. Accessibility pass: TalkBack labels, font scale 2.0, contrast, 48 dp targets.
- [ ] 22. `values-hi` and `values-mr` strings (mark as needing native-speaker review).
- [ ] 23. App icon (adaptive, monochrome for themed icons) and splash screen API.
- [ ] 24. Fill `carrier_plans.json` with verified prices and `carrier_rules.json` `lastChecked`.

## Milestone 7 — Release
- [ ] 25. Follow `TESTING.md` full checklist on 2 real phones.
- [ ] 26. Follow `PLAYSTORE_RELEASE.md`.
