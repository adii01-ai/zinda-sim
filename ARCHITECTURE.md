# ARCHITECTURE.md — SIM Keeper

## Stack
| Concern | Choice |
| --- | --- |
| Language | Kotlin 2.x |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM, unidirectional data flow (`StateFlow<UiState>` per screen), single activity |
| DI | Hilt |
| Navigation | Navigation Compose with type-safe routes (`@Serializable` objects) |
| Storage | Room (SIMs, recharges, reminder prefs), Preferences DataStore (settings, onboarding flag) |
| Background | WorkManager (daily reminder check, widget refresh) |
| Notifications | NotificationCompat + channels |
| Widgets | Jetpack Glance |
| Dates | `java.time` (core library desugaring on for API 26) |
| JSON | kotlinx.serialization (bundled `assets/*.json`) |
| Tests | JUnit4, Truth, Turbine, Room in-memory, Compose UI test |

## Versions (minimums, use these or newer stable via `gradle/libs.versions.toml`)
| Library | Version |
| --- | --- |
| Android Gradle Plugin | 8.7.3 |
| Kotlin + Compose compiler plugin | 2.0.21 |
| KSP | 2.0.21-1.0.28 |
| Compose BOM | 2024.12.01 |
| Activity Compose | 1.9.3 |
| Lifecycle (runtime-compose, viewmodel-compose) | 2.8.7 |
| Navigation Compose | 2.8.5 |
| Hilt / hilt-navigation-compose | 2.52 / 1.2.0 |
| Room | 2.6.1 |
| DataStore Preferences | 1.1.1 |
| WorkManager (+ hilt-work 1.2.0) | 2.10.0 |
| Glance AppWidget + Material 3 | 1.1.1 |
| kotlinx-serialization-json | 1.7.3 |
| desugar_jdk_libs | 2.1.3 |

## SDK levels
`minSdk = 26`, `compileSdk = 35`, `targetSdk = 35`. Java/Kotlin target 17.

## Project layout
Single `:app` module (keep it simple for v1). Package root `com.simkeeper.app` (change before publishing; it is permanent once on the Play Store).

```
app/src/main/java/com/simkeeper/app/
  SimKeeperApp.kt            // @HiltAndroidApp, notification channels, WorkManager config
  MainActivity.kt            // setContent { SimKeeperTheme { AppNavHost() } }
  navigation/AppNavHost.kt, Routes.kt
  ui/theme/Color.kt, Type.kt, Shape.kt, Spacing.kt, StatusColors.kt, Theme.kt
  ui/components/DaysRing.kt, SimCard.kt, CarrierTag.kt, MonogramTile.kt, PlanChip.kt,
                SelectChip.kt, PrimaryButton.kt, StatusBanner.kt, SectionLabel.kt, Confetti.kt
  ui/onboarding/  OnboardingScreen.kt, PermissionScreen.kt
  ui/setup/       SimsDetectedScreen.kt, SetValiditySheet.kt, AddSimScreen.kt, SetupViewModel.kt
  ui/home/        HomeScreen.kt, HomeViewModel.kt, EmptyState.kt
  ui/detail/      SimDetailScreen.kt, SimDetailViewModel.kt, RechargedDialog.kt
  ui/settings/    SettingsScreen.kt, SettingsViewModel.kt
  domain/         Deadline.kt (pure maths), Status.kt, model/Sim.kt, model/Recharge.kt,
                  usecase/MarkRechargedUseCase.kt, usecase/ComputeDeadlinesUseCase.kt
  data/db/        AppDatabase.kt, SimEntity.kt, RechargeEntity.kt, ReminderPrefEntity.kt, Daos.kt
  data/repo/      SimRepository.kt, SettingsRepository.kt, CarrierInfoRepository.kt
  data/telephony/ SimDetector.kt   // SubscriptionManager wrapper
  data/assets/    CarrierRules.kt, CarrierPlans.kt (kotlinx.serialization models)
  reminders/      ReminderWorker.kt, ReminderScheduler.kt, NotificationHelper.kt, ActionReceiver.kt
  widget/         AllSimsWidget.kt, SingleSimWidget.kt, WidgetReceivers.kt, SingleSimConfigActivity.kt
  di/             DatabaseModule.kt, RepositoryModule.kt, ClockModule.kt
app/src/main/assets/carrier_rules.json, carrier_plans.json
app/src/main/res/font/ (Space Grotesk, Plus Jakarta Sans)
app/src/main/res/values/strings.xml, values-hi/strings.xml, values-mr/strings.xml
app/src/main/res/xml/locales_config.xml, all_sims_widget_info.xml, single_sim_widget_info.xml
```

## Key rules
- `domain/` is pure Kotlin: no Android imports, fully unit-tested.
- Inject a `Clock` (java.time) everywhere "today" is needed, so tests and previews can fix the date to 2026-10-19.
- Repositories expose `Flow`s; ViewModels map them into immutable `UiState` data classes.
- Screens take `UiState` + lambdas (stateless), with a thin `Route` composable that wires the ViewModel. This keeps previews easy.

## SIM detection
```kotlin
val sm = context.getSystemService(SubscriptionManager::class.java)
val subs = sm.activeSubscriptionInfoList // requires READ_PHONE_STATE; null/empty if denied
subs?.map { DetectedSim(carrier = it.carrierName.toString(), slot = it.simSlotIndex + 1, subscriptionId = it.subscriptionId) }
```
Map `carrierName` to Jio/Airtel/Vi/BSNL by case-insensitive contains ("jio", "airtel", "vi"/"vodafone"/"idea", "bsnl"/"cellone"), else Other. Never read the number.

## Reminders
- `ReminderScheduler` enqueues a unique `PeriodicWorkRequest` (24 h, flex 1 h) named `daily-reminder-check`, aimed near the user's reminder time, plus a one-time request whenever data changes.
- `ReminderWorker`: for each SIM compute the effective deadline; if `daysLeft` equals one of the SIM's enabled offsets (7, 3, 1, 0) and no notification was sent today for that SIM+offset, post it. During grace (daysLeft < 0) post every 2 days until action. Respect quiet hours (defer to 08:00).
- Store "last notified" per SIM+offset in DataStore to avoid duplicates.
- Notification actions go through `ActionReceiver` (snooze) or a deep link `simkeeper://sim/{id}?action=recharged`.
- On boot nothing extra is needed: WorkManager reschedules itself.

## Manifest permissions
```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```
Nothing else. No `INTERNET`.
