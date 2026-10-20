package com.simkeeper.app.navigation

import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

@Serializable
data object PermissionRoute

@Serializable
data object SimsDetectedRoute

/**
 * Manual add. [family] pre-sets "This SIM is in someone else's phone".
 * [finishSetup]: after saving, go to Home and mark onboarding done (setup flow without detection).
 */
@Serializable
data class AddSimRoute(val family: Boolean = false, val finishSetup: Boolean = false)

@Serializable
data object HomeRoute

@Serializable
data class SimDetailRoute(val simId: Long, val action: String? = null) {
    companion object {
        const val ACTION_RECHARGED = "recharged"
        const val DEEP_LINK = "simkeeper://sim/{simId}?action={action}"
    }
}

/** Full-screen "Recharged!" confirmation. */
@Serializable
data class RechargedRoute(val simId: Long)

@Serializable
data object SettingsRoute
