package com.tembt.util

import platform.Foundation.NSLog

actual fun logD(tag: String, message: String) {
    NSLog("[$tag] $message")
}
