package co.onestep.kmp.uikit.features.recordFlow.configurations

import platform.Foundation.NSUUID

internal actual fun randomUUID(): String = NSUUID().UUIDString()
