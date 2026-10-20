package com.simkeeper.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performCustomAccessibilityActionWithLabel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simkeeper.app.R
import com.simkeeper.app.domain.model.ThemeMode
import com.simkeeper.app.ui.format.DateFormats
import com.simkeeper.app.ui.format.currentLocale
import com.simkeeper.app.ui.home.HomeActions
import com.simkeeper.app.ui.home.HomeScreen
import com.simkeeper.app.ui.home.HomeUiState
import com.simkeeper.app.ui.preview.SampleData
import com.simkeeper.app.ui.settings.AppLanguage
import com.simkeeper.app.ui.settings.SettingsActions
import com.simkeeper.app.ui.settings.SettingsScreen
import com.simkeeper.app.ui.settings.SettingsUiState
import com.simkeeper.app.ui.setup.SetValidityContent
import com.simkeeper.app.ui.setup.ValidityForm
import com.simkeeper.app.ui.theme.SimKeeperTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Compose UI tests from TESTING.md, on the stateless screens with the sample data (today = 2026-10-19).
 * Expected text comes from the test activity, so the tests pass in any app language.
 */
@RunWith(AndroidJUnit4::class)
class ScreenTests {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val context get() = rule.activity
    private fun date(y: Int, m: Int, d: Int) = DateFormats.date(LocalDate.of(y, m, d), context.currentLocale)

    @Test
    fun setValidityPreviewFollowsChips() {
        rule.setContent {
            SimKeeperTheme {
                var form by remember { mutableStateOf(ValidityForm()) }
                SetValidityContent(form = form, today = SampleData.today, onChange = { form = it })
            }
        }
        // 28 days + Today = ends 16 Nov 2026, 28 days left.
        rule.onNodeWithText(date(2026, 11, 16)).assertExists()
        rule.onAllNodesWithText("28").assertCountEquals(2) // plan chip + days left

        rule.onNodeWithText(context.getString(R.string.validity_3_weeks_ago)).performClick()
        rule.onNodeWithText(date(2026, 10, 26)).assertExists()
        rule.onNodeWithText("7").assertExists()

        rule.onNodeWithText("84").performClick()
        rule.onNodeWithText(date(2026, 12, 21)).assertExists()
        rule.onNodeWithText("63").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun homeShowsUrgentFirstAndRecharges() {
        var recharged: Long? = null
        var opened: Long? = null
        rule.setContent {
            SimKeeperTheme {
                HomeScreen(
                    state = HomeUiState(
                        loading = false,
                        today = SampleData.today,
                        phoneSims = listOf(SampleData.bankState, SampleData.dailyState),
                        familySims = listOf(SampleData.mummyState),
                    ),
                    actions = HomeActions(
                        onOpenSim = { opened = it },
                        onRecharged = { recharged = it },
                        onSettings = {},
                        onAddSim = {},
                        onOpenBatterySettings = {},
                        onDismissBatteryGuide = {},
                    ),
                )
            }
        }
        val bank = rule.onNodeWithContentDescription("Bank SIM,", substring = true)
        val daily = rule.onNodeWithContentDescription("Daily SIM,", substring = true)
        bank.assertIsDisplayed()
        assertThat(bank.fetchSemanticsNode().boundsInRoot.top).isLessThan(daily.fetchSemanticsNode().boundsInRoot.top)

        // The whole card reads as one sentence.
        val days = context.resources.getQuantityString(R.plurals.days_left, 2, 2)
        bank.assert(hasContentDescription(days, substring = true))

        bank.performCustomAccessibilityActionWithLabel(context.getString(R.string.i_recharged))
        assertThat(recharged).isEqualTo(SampleData.bankSim.id)

        daily.performClick()
        assertThat(opened).isEqualTo(SampleData.dailySim.id)

        rule.onNodeWithText(context.resources.getQuantityString(R.plurals.home_banner_needs_recharge, 1, 1)).assertIsDisplayed()
    }

    @Test
    fun settingsThemeSwitchAppliesImmediately() {
        rule.setContent {
            var mode by remember { mutableStateOf(ThemeMode.LIGHT) }
            SimKeeperTheme(themeMode = mode) {
                SettingsScreen(
                    state = SettingsUiState(rulesLastChecked = SampleData.today),
                    language = AppLanguage.ENGLISH,
                    actions = SettingsActions({}, {}, { mode = it }, {}, {}, {}, {}),
                )
                Text(if (SimKeeperTheme.isDark) "theme:dark" else "theme:light")
            }
        }
        rule.onNodeWithText("theme:light").assertExists()
        rule.onNodeWithText(context.getString(R.string.settings_theme_dark)).performClick()
        rule.onNodeWithText("theme:dark").assertExists()
    }
}
