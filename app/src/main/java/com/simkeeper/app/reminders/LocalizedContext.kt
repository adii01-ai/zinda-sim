package com.simkeeper.app.reminders

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList

/**
 * Workers, receivers and widgets have no Activity, so on Android 8-12 they would not see the
 * per-app language. Wrap the context in the stored language so notifications match the app.
 */
fun Context.withAppLanguage(tag: String?): Context {
    if (tag.isNullOrBlank()) return this
    val config = Configuration(resources.configuration)
    config.setLocales(LocaleList.forLanguageTags(tag))
    return createConfigurationContext(config)
}
