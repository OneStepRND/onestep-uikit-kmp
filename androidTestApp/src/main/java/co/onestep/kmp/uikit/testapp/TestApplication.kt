package co.onestep.kmp.uikit.testapp

import android.app.Application
import android.util.Log
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.motionLab.OSTMockIMU
import co.onestep.android.core.motionLab.getMotionLab
import co.onestep.android.core.onError
import co.onestep.android.core.onSuccess
import co.onestep.kmp.uikit.bridge.android.configureWithAndroidSDK
import co.onestep.kmp.uikit.di.UIKitServiceLocator

class TestApplication : Application() {

    var oneStepSdk: OneStep? = null
        private set

    override fun onCreate() {
        super.onCreate()

        // OneStep SDK must be initialized from the application class
        OneStep.initialize(
            application = this,
            onAuthLost = { Log.e(TAG, "Auth lost: $it") },
        ) {
            additionalConfiguration("Logging" to true)
        }.onSuccess { oneStep ->
            oneStepSdk = oneStep
            // Mock IMU feeds simulated sensor data into the recording pipeline,
            // so flows complete on emulators without physical movement.
            oneStep.getMotionLab().getOr(null)?.setMockIMU(OSTMockIMU.SUCCESSFUL)
            UIKitServiceLocator.configureWithAndroidSDK(applicationContext, oneStep)
            Log.i(TAG, "[OneStep SDK] initialized, mock IMU = SUCCESSFUL")
        }.onError { error ->
            Log.e(TAG, "[OneStep SDK] initialization failed: ${error.cause}")
        }
    }

    fun setMockIMU(mock: OSTMockIMU) {
        val motionLab = oneStepSdk?.getMotionLab()?.getOr(null)
        if (motionLab == null) {
            // MotionLab needs a patient context — callers must re-apply after login.
            Log.w(TAG, "setMockIMU($mock) skipped, motionLab unavailable")
            return
        }
        motionLab.setMockIMU(mock)
        Log.i(TAG, "Mock IMU set to $mock")
    }

    companion object {
        private const val TAG = "TestApplication"
    }
}
