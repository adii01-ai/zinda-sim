# DATA_MODEL.md — SIM Keeper

## Room entities

```kotlin
@Entity(tableName = "sims")
data class SimEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                 // "Bank SIM"
    val carrier: Carrier,             // enum: JIO, AIRTEL, VI, BSNL, OTHER (TypeConverter)
    val carrierLabel: String?,        // free text when OTHER
    val slot: Int?,                   // 1 or 2; null for family SIMs
    val isFamily: Boolean = false,
    val ownerName: String? = null,    // "Mummy"
    val subscriptionId: Int? = null,  // from SubscriptionManager, to match on re-detect
    val planDays: Int,                // current plan length
    val lastRechargeDate: LocalDate,  // TypeConverter <-> epochDay Long
    val exactEndDate: LocalDate? = null, // set when user chose "I know the exact end date"
    val lastUsedDate: LocalDate,      // for the 90-day no-use limit
    val createdAt: Long,
    val sortOrder: Int = 0,
)

@Entity(
    tableName = "recharges",
    foreignKeys = [ForeignKey(entity = SimEntity::class, parentColumns = ["id"], childColumns = ["simId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("simId")]
)
data class RechargeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val simId: Long,
    val date: LocalDate,
    val planDays: Int,
    val amountRupees: Int? = null,
    val note: String? = null,
)

@Entity(tableName = "reminder_prefs", primaryKeys = ["simId", "offsetDays"])
data class ReminderPrefEntity(
    val simId: Long,
    val offsetDays: Int,   // 7, 3, 1, 0
    val enabled: Boolean = true,
)
```
Database version 1, `exportSchema = true` (schemas in `app/schemas/`). Add migrations for every later change; never use destructive migration in release builds.

## DAOs (minimum)
- `SimDao`: `observeAll(): Flow<List<SimEntity>>`, `observe(id): Flow<SimEntity?>`, `insert`, `update`, `delete`.
- `RechargeDao`: `observeForSim(simId): Flow<List<RechargeEntity>>` (newest first), `insert`, `delete(id)` (for Undo).
- `ReminderPrefDao`: `observeForSim`, `upsert`.

## Settings (Preferences DataStore)
| Key | Type | Default |
| --- | --- | --- |
| onboardingDone | Boolean | false |
| themeMode | String (SYSTEM/LIGHT/DARK) | SYSTEM |
| dynamicColor | Boolean | false |
| reminderTime | String "HH:mm" | "10:00" |
| quietHours | Boolean | true |
| lastNotified_<simId>_<offset> | String date | — |

## Deadline maths (domain/Deadline.kt, pure Kotlin)
```kotlin
const val NO_USE_LIMIT_DAYS = 90

fun planEnd(sim: Sim): LocalDate = sim.exactEndDate ?: sim.lastRechargeDate.plusDays(sim.planDays.toLong())
fun noUseEnd(sim: Sim): LocalDate = sim.lastUsedDate.plusDays(NO_USE_LIMIT_DAYS.toLong())

data class Deadlines(val planEnd: LocalDate, val noUseEnd: LocalDate) {
    val effective: LocalDate get() = minOf(planEnd, noUseEnd)
    val reason: Reason get() = if (planEnd <= noUseEnd) Reason.PLAN else Reason.NO_USE
}

fun daysLeft(effective: LocalDate, today: LocalDate): Long = ChronoUnit.DAYS.between(today, effective)

fun status(daysLeft: Long): Status = when {
    daysLeft < 0 -> Status.EXPIRED
    daysLeft <= 2 -> Status.URGENT
    daysLeft <= 7 -> Status.SOON
    else -> Status.SAFE
}

fun ringProgress(daysLeft: Long, planDays: Int): Float = (daysLeft.toFloat() / planDays).coerceIn(0f, 1f)
```

## Mark recharged (use case)
1. Insert `RechargeEntity(simId, date = today, planDays = chosen ?: sim.planDays, amountRupees)`.
2. Update SIM: `lastRechargeDate = today`, `planDays = chosen`, `exactEndDate = null`, `lastUsedDate = today` (a recharge counts as use).
3. Clear `lastNotified_*` keys for that SIM, refresh widgets.
4. Undo: delete that recharge row and restore the previous SIM row (keep a copy in the ViewModel until the snackbar closes).

## Worked examples (must pass as unit tests, today = 2026-10-19)
| Case | Input | planEnd | noUseEnd | daysLeft | Status |
| --- | --- | --- | --- | --- | --- |
| Bank SIM | 28 d from 2026-09-23, used 2026-10-15 | 2026-10-21 | 2027-01-13 | 2 | URGENT |
| Daily SIM | 84 d from 2026-09-26, used today | 2026-12-19 | 2027-01-17 | 61 | SAFE |
| Mummy's phone | 28 d from 2026-10-03, used today | 2026-10-31 | 2027-01-17 | 12 | SAFE |
| Long plan, unused | 365 d from 2026-06-01, used 2026-08-01 | 2027-06-01 | 2026-10-30 | 11 | SAFE, reason NO_USE |
| Expired | 28 d from 2026-09-01 | 2026-09-29 | — | -20 | EXPIRED |
| Just recharged | 28 d from today | 2026-11-16 | 2027-01-17 | 28 | SAFE |
