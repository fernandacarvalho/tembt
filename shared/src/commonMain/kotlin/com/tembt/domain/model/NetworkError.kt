package com.tembt.domain.model

/**
 * Domain-level error signalling a connectivity/network failure (timeout, unreachable host, etc.).
 * The data layer maps platform/Ktor network exceptions to this type so the presentation layer can
 * show a "sem conexão" message without importing framework exception classes.
 */
class NetworkError(cause: Throwable) : Exception(cause)
