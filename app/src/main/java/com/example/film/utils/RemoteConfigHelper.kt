package com.example.film.utils

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object RemoteConfigHelper {

    fun fetchTicketPrice(onResult: (Long) -> Unit) {

        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()

        remoteConfig.setConfigSettingsAsync(settings)

        remoteConfig.setDefaultsAsync(
            mapOf("ticket_price" to 70000L)
        )

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                val price = if (task.isSuccessful) {
                    remoteConfig.getLong("ticket_price")
                } else {
                    70000L
                }
                onResult(price)
            }
    }
}
