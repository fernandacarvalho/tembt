package com.tembt.platform

import android.content.Context
import java.util.UUID

class PlayerStorageImpl(context: Context) : PlayerStorage {

    private val prefs = context.getSharedPreferences("tembt_player", Context.MODE_PRIVATE)

    override fun isRegistered(): Boolean = prefs.getBoolean("is_registered", false)

    override fun getDeviceUuid(): String {
        var uuid = prefs.getString("device_uuid", null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            prefs.edit().putString("device_uuid", uuid).apply()
        }
        return uuid
    }

    override fun saveRegistration(name: String) {
        prefs.edit()
            .putBoolean("is_registered", true)
            .putString("player_name", name)
            .apply()
    }
}
