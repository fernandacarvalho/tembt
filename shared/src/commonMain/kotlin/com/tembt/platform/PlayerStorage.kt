package com.tembt.platform

interface PlayerStorage : DeviceIdentityProvider {
    /** True if the player has previously completed registration. */
    fun isRegistered(): Boolean

    /** Stable device identifier used as the player UUID with the API. */
    override fun getDeviceUuid(): String

    /** Persists the registration locally so the Welcome screen is never shown again. */
    fun saveRegistration(name: String)
}
