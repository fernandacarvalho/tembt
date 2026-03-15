package com.tembt.platform

import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID
import platform.UIKit.UIDevice

class PlayerStorageImpl : PlayerStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun isRegistered(): Boolean = defaults.boolForKey("is_registered")

    override fun getDeviceUuid(): String {
        // Prefer identifierForVendor (stable across app installs on the same device)
        val vendorUuid = UIDevice.currentDevice.identifierForVendor?.UUIDString
        if (vendorUuid != null) return vendorUuid

        // Fallback: generate once and persist
        val stored = defaults.stringForKey("device_uuid")
        if (stored != null) return stored
        val generated = NSUUID().UUIDString
        defaults.setObject(generated, forKey = "device_uuid")
        return generated
    }

    override fun saveRegistration(name: String) {
        defaults.setBool(true, forKey = "is_registered")
        defaults.setObject(name, forKey = "player_name")
    }
}
