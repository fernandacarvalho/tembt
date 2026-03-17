package com.tembt.fake

import com.tembt.platform.PlayerStorage

class FakePlayerStorage(
    var uuid: String = "test-uuid-123"
) : PlayerStorage {

    var registered = false
    var savedName: String? = null

    override fun isRegistered() = registered
    override fun getDeviceUuid() = uuid
    override fun saveRegistration(name: String) {
        savedName = name
        registered = true
    }
}
