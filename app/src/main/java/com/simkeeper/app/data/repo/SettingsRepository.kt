package com.simkeeper.app.data.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.simkeeper.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class AppSettings(
    val onboardingDone: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val reminderTime: LocalTime = DEFAULT_REMINDER_TIME,
    val quietHours: Boolean = true,
    val batteryGuideDismissed: Boolean = false,
    val notificationsAsked: Boolean = false,
    /** Per-app language tag ("en", "hi", "mr"), or null for the device language. */
    val language: String? = null,
) {
    companion object {
        val DEFAULT_REMINDER_TIME: LocalTime = LocalTime.of(10, 0)
    }
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun current(): AppSettings = settings.first()
    suspend fun setOnboardingDone(done: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setReminderTime(time: LocalTime)
    suspend fun setQuietHours(enabled: Boolean)
    suspend fun setBatteryGuideDismissed(dismissed: Boolean)
    suspend fun setNotificationsAsked(asked: Boolean)
    suspend fun setLanguage(tag: String)
}

/** Remembers which reminders were already posted, so none is sent twice. */
interface NotificationLog {
    /** [key] is a reminder offset (7, 3, 1, 0) or [GRACE_KEY]. */
    suspend fun lastNotified(simId: Long, key: Int): LocalDate?
    suspend fun markNotified(simId: Long, key: Int, date: LocalDate)
    suspend fun snoozedUntil(simId: Long): LocalDate?
    suspend fun snooze(simId: Long, until: LocalDate)
    suspend fun clearSnooze(simId: Long)
    /** Called after a recharge or SIM change: reminders for that SIM start over. */
    suspend fun clear(simId: Long)
    /** Everything stored for [simId], so Undo can put it back exactly. */
    suspend fun snapshot(simId: Long): Map<String, String>
    suspend fun restore(simId: Long, snapshot: Map<String, String>)

    companion object {
        const val GRACE_KEY = -1
    }
}

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository, NotificationLog {

    override val settings: Flow<AppSettings> = dataStore.data.map { p ->
        AppSettings(
            onboardingDone = p[Keys.onboardingDone] ?: false,
            themeMode = p[Keys.themeMode]?.let { v -> ThemeMode.entries.firstOrNull { it.name == v } } ?: ThemeMode.SYSTEM,
            dynamicColor = p[Keys.dynamicColor] ?: false,
            reminderTime = p[Keys.reminderTime]?.let(::parseTime) ?: AppSettings.DEFAULT_REMINDER_TIME,
            quietHours = p[Keys.quietHours] ?: true,
            batteryGuideDismissed = p[Keys.batteryGuideDismissed] ?: false,
            notificationsAsked = p[Keys.notificationsAsked] ?: false,
            language = p[Keys.language],
        )
    }

    override suspend fun setOnboardingDone(done: Boolean) = put(Keys.onboardingDone, done)
    override suspend fun setThemeMode(mode: ThemeMode) = put(Keys.themeMode, mode.name)
    override suspend fun setDynamicColor(enabled: Boolean) = put(Keys.dynamicColor, enabled)
    override suspend fun setReminderTime(time: LocalTime) = put(Keys.reminderTime, time.format(timeFormat))
    override suspend fun setQuietHours(enabled: Boolean) = put(Keys.quietHours, enabled)
    override suspend fun setBatteryGuideDismissed(dismissed: Boolean) = put(Keys.batteryGuideDismissed, dismissed)
    override suspend fun setNotificationsAsked(asked: Boolean) = put(Keys.notificationsAsked, asked)
    override suspend fun setLanguage(tag: String) = put(Keys.language, tag)

    override suspend fun lastNotified(simId: Long, key: Int): LocalDate? =
        dataStore.data.first()[Keys.lastNotified(simId, key)]?.let(::parseDate)

    override suspend fun markNotified(simId: Long, key: Int, date: LocalDate) =
        put(Keys.lastNotified(simId, key), date.toString())

    override suspend fun snoozedUntil(simId: Long): LocalDate? =
        dataStore.data.first()[Keys.snoozedUntil(simId)]?.let(::parseDate)

    override suspend fun snooze(simId: Long, until: LocalDate) = put(Keys.snoozedUntil(simId), until.toString())

    override suspend fun clearSnooze(simId: Long) {
        dataStore.edit { it.remove(Keys.snoozedUntil(simId)) }
    }

    override suspend fun clear(simId: Long) {
        dataStore.edit { prefs ->
            prefs.asMap().keys.filter { Keys.belongsTo(it, simId) }.forEach { prefs.remove(it) }
        }
    }

    override suspend fun snapshot(simId: Long): Map<String, String> =
        dataStore.data.first().asMap()
            .filterKeys { Keys.belongsTo(it, simId) }
            .map { (key, value) -> key.name to value.toString() }
            .toMap()

    override suspend fun restore(simId: Long, snapshot: Map<String, String>) {
        dataStore.edit { prefs ->
            prefs.asMap().keys.filter { Keys.belongsTo(it, simId) }.forEach { prefs.remove(it) }
            snapshot.forEach { (name, value) -> prefs[stringPreferencesKey(name)] = value }
        }
    }

    private suspend fun <T> put(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    private object Keys {
        const val LAST_NOTIFIED_PREFIX = "lastNotified_"
        val onboardingDone = booleanPreferencesKey("onboardingDone")
        val themeMode = stringPreferencesKey("themeMode")
        val dynamicColor = booleanPreferencesKey("dynamicColor")
        val reminderTime = stringPreferencesKey("reminderTime")
        val quietHours = booleanPreferencesKey("quietHours")
        val batteryGuideDismissed = booleanPreferencesKey("batteryGuideDismissed")
        val notificationsAsked = booleanPreferencesKey("notificationsAsked")
        val language = stringPreferencesKey("appLanguage")
        fun lastNotified(simId: Long, key: Int) = stringPreferencesKey("$LAST_NOTIFIED_PREFIX${simId}_$key")
        fun snoozedUntil(simId: Long) = stringPreferencesKey("snoozedUntil_$simId")
        fun belongsTo(key: Preferences.Key<*>, simId: Long) =
            key.name.startsWith("$LAST_NOTIFIED_PREFIX${simId}_") || key == snoozedUntil(simId)
    }

    private companion object {
        val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        fun parseTime(value: String): LocalTime? = runCatching { LocalTime.parse(value, timeFormat) }.getOrNull()
        fun parseDate(value: String): LocalDate? = runCatching { LocalDate.parse(value) }.getOrNull()
    }
}
