package com.tembt.util

// Platform implementations use Log.d (Android) and NSLog (iOS).
expect fun logD(tag: String, message: String)
