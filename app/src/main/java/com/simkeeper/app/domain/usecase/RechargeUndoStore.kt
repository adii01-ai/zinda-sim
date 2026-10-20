package com.simkeeper.app.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the last "I recharged" until its snackbar closes, so the screen the user returns to
 * (Home or SIM detail) can offer Undo.
 */
@Singleton
class RechargeUndoStore @Inject constructor() {
    private val _pending = MutableStateFlow<RechargeReceipt?>(null)
    val pending: StateFlow<RechargeReceipt?> = _pending.asStateFlow()

    fun post(receipt: RechargeReceipt) {
        _pending.value = receipt
    }

    /** Called once the snackbar was shown and closed (with or without Undo). */
    fun consume(receipt: RechargeReceipt) {
        _pending.compareAndSet(receipt, null)
    }
}
