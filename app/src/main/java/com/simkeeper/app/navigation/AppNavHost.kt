package com.simkeeper.app.navigation

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.simkeeper.app.reminders.NotificationHelper
import com.simkeeper.app.ui.detail.RechargedRoute as RechargedScreenRoute
import com.simkeeper.app.ui.detail.SimDetailRoute as SimDetailScreenRoute
import com.simkeeper.app.ui.home.HomeRoute as HomeScreenRoute
import com.simkeeper.app.ui.onboarding.OnboardingScreen
import com.simkeeper.app.ui.onboarding.PermissionRoute as PermissionScreenRoute
import com.simkeeper.app.ui.settings.SettingsRoute as SettingsScreenRoute
import com.simkeeper.app.ui.setup.AddSimRoute as AddSimScreenRoute
import com.simkeeper.app.ui.setup.SimsDetectedRoute as SimsDetectedScreenRoute

/**
 * Onboarding → Permission → SimsDetected (→ SetValidity sheet) → Home.
 * Home → SimDetail, Settings, AddSim. Start destination comes from `onboardingDone`.
 */
@Composable
fun AppNavHost(onboardingDone: Boolean) {
    val navController = rememberNavController()
    val start: Any = remember { if (onboardingDone) HomeRoute else OnboardingRoute }
    ForwardNewIntents(navController)
    NavHost(navController = navController, startDestination = start) {
        composable<OnboardingRoute> { OnboardingScreen(onDone = { navController.navigate(PermissionRoute) }) }
        composable<PermissionRoute> {
            PermissionScreenRoute(
                onGranted = { navController.navigate(SimsDetectedRoute) },
                onManual = { navController.navigate(AddSimRoute(finishSetup = true)) },
            )
        }
        composable<SimsDetectedRoute> {
            SimsDetectedScreenRoute(
                onBack = { navController.popBackStack() },
                onAddFamily = { navController.navigate(AddSimRoute(family = true)) },
                onAddByHand = { navController.navigate(AddSimRoute()) },
                onFinished = { navController.goHome() },
            )
        }
        composable<AddSimRoute> { entry ->
            val finishSetup = entry.toRoute<AddSimRoute>().finishSetup
            AddSimScreenRoute(
                onBack = { navController.popBackStack() },
                onSaved = { if (finishSetup) navController.goHome() else navController.popBackStack() },
            )
        }
        composable<HomeRoute> {
            HomeScreenRoute(
                onOpenSim = { navController.navigate(SimDetailRoute(it)) },
                onRecharged = { navController.navigate(RechargedRoute(it)) },
                onSettings = { navController.navigate(SettingsRoute) },
                onAddSim = { navController.navigate(AddSimRoute()) },
            )
        }
        composable<SimDetailRoute>(
            deepLinks = listOf(navDeepLink<SimDetailRoute>(basePath = NotificationHelper.DEEP_LINK_BASE)),
        ) {
            SimDetailScreenRoute(
                onBack = { navController.popBackStack() },
                onRecharged = { navController.navigate(RechargedRoute(it)) },
            )
        }
        // Full-screen confirmation. A plain destination (not a window dialog) so the
        // system bars stay edge-to-edge instead of being dimmed.
        composable<RechargedRoute> {
            RechargedScreenRoute(onClose = { navController.popBackStack() })
        }
        composable<SettingsRoute> { SettingsScreenRoute(onBack = { navController.popBackStack() }) }
    }
}

/** Setup is done: Home becomes the only screen in the back stack. */
private fun NavHostController.goHome() {
    navigate(HomeRoute) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

/** MainActivity is singleTop: notification and widget taps arrive here while the app is open. */
@Composable
private fun ForwardNewIntents(navController: NavHostController) {
    val activity = LocalContext.current as? ComponentActivity ?: return
    DisposableEffect(activity, navController) {
        val listener = Consumer<Intent> { intent -> navController.handleDeepLink(intent) }
        activity.addOnNewIntentListener(listener)
        onDispose { activity.removeOnNewIntentListener(listener) }
    }
}
