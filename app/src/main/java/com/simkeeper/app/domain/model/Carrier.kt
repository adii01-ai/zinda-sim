package com.simkeeper.app.domain.model

enum class Carrier {
    JIO, AIRTEL, VI, BSNL, OTHER;

    companion object {
        private val viWord = Regex("\\bvi\\b")

        /**
         * Maps a `SubscriptionInfo.carrierName` to a known carrier (case-insensitive).
         * "vi" must be a whole word so names like "Movistar" do not match.
         */
        fun fromCarrierName(name: String?): Carrier {
            val n = name?.trim()?.lowercase().orEmpty()
            return when {
                n.isEmpty() -> OTHER
                "jio" in n -> JIO
                "airtel" in n -> AIRTEL
                "bsnl" in n || "cellone" in n -> BSNL
                "vodafone" in n || "idea" in n || viWord.containsMatchIn(n) -> VI
                else -> OTHER
            }
        }
    }
}
