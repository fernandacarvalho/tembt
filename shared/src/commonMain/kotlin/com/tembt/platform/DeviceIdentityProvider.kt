package com.tembt.platform

/** Provides the stable device-level identifier used as the player UUID with the API. */
interface DeviceIdentityProvider {
    fun getDeviceUuid(): String
}
