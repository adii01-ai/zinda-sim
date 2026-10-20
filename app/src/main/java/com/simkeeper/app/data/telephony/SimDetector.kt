package com.simkeeper.app.data.telephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.simkeeper.app.domain.model.Carrier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** A SIM found in this phone. Only carrier name and slot: never the phone number. */
data class DetectedSim(
    val carrierName: String,
    val carrier: Carrier,
    /** 1-based slot. */
    val slot: Int,
    val subscriptionId: Int,
)

/** Thin wrapper over [SubscriptionManager]. Returns nothing without READ_PHONE_STATE. */
@Singleton
class SimDetector @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

    fun detect(): List<DetectedSim> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }
        val manager = context.getSystemService(SubscriptionManager::class.java) ?: return emptyList()
        val subscriptions = try {
            manager.activeSubscriptionInfoList.orEmpty()
        } catch (_: SecurityException) {
            emptyList()
        }
        return subscriptions
            .map { info ->
                val name = info.carrierName?.toString().orEmpty().ifBlank { info.displayName?.toString().orEmpty() }
                DetectedSim(
                    carrierName = name.trim(),
                    carrier = Carrier.fromCarrierName(name),
                    slot = info.simSlotIndex + 1,
                    subscriptionId = info.subscriptionId,
                )
            }
            .sortedBy { it.slot }
    }
}
