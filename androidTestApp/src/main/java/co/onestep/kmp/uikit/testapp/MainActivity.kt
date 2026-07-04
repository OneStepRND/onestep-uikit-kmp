package co.onestep.kmp.uikit.testapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import co.onestep.android.core.OSTIdentificationState
import co.onestep.android.core.OneStep
import co.onestep.android.core.motionLab.OSTMockIMU
import co.onestep.android.core.onError
import co.onestep.android.core.onSuccess
import co.onestep.kmp.uikit.features.carlog.OSTCareLog
import co.onestep.kmp.uikit.features.permissions.OSTPermissionFlow
import co.onestep.kmp.uikit.features.permissions.OSTPermissionMode
import co.onestep.kmp.uikit.features.recordFlow.OSTRecordingFlow
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.summary.OSTMeasurementSummary
import co.onestep.kmp.uikit.testapp.ui.ConfigureFlowScreen
import co.onestep.kmp.uikit.testapp.ui.HomeScreen
import co.onestep.kmp.uikit.testapp.ui.LoginScreen
import co.onestep.kmp.uikit.testapp.ui.SummaryLoadingScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val testApplication get() = application as TestApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sdk = testApplication.oneStepSdk

        setContent {
            MaterialTheme {
                if (sdk == null) {
                    InitFailedScreen()
                } else {
                    MainContent(sdk)
                }
            }
        }
    }

    @Composable
    private fun MainContent(sdk: OneStep) {
        val sdkState by sdk.identificationState.collectAsState()
        var isConnecting by remember { mutableStateOf(false) }
        var loginError by remember { mutableStateOf<String?>(null) }

        Scaffold { paddingValues ->
            Box(Modifier.padding(paddingValues)) {
                when (val state = sdkState) {
                    is OSTIdentificationState.Identified -> AuthenticatedContent(
                        sdk = sdk,
                        userId = state.patientId.value,
                    )

                    else -> LoginScreen(
                        isConnecting = isConnecting,
                        errorMessage = loginError,
                        onConnect = { userId ->
                            isConnecting = true
                            loginError = null
                            connect(
                                sdk = sdk,
                                userId = userId,
                                onDone = { error ->
                                    isConnecting = false
                                    loginError = error
                                },
                            )
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun AuthenticatedContent(
        sdk: OneStep,
        userId: String,
    ) {
        var screen by remember { mutableStateOf<TestAppScreen>(TestAppScreen.Home) }
        var mockImu by remember { mutableStateOf(OSTMockIMU.SUCCESSFUL) }

        // MotionLab is created per patient context, so the mock IMU flag must be
        // (re-)applied after identification, not at SDK init.
        LaunchedEffect(Unit) {
            testApplication.setMockIMU(mockImu)
        }

        when (val current = screen) {
            is TestAppScreen.Home -> HomeScreen(
                userId = userId,
                mockImuName = mockImu.name,
                mockImuOptions = remember { OSTMockIMU.entries.map { it.name } },
                onMockImuSelected = { name ->
                    mockImu = OSTMockIMU.valueOf(name)
                    testApplication.setMockIMU(mockImu)
                },
                onClickCareLog = { screen = TestAppScreen.CareLog },
                onClickRecordingFlow = { screen = TestAppScreen.ConfigureFlow },
                onClickPermissionInApp = { screen = TestAppScreen.PermissionInApp },
                onClickPermissionBackground = { screen = TestAppScreen.PermissionBackground },
                onClickLogout = { sdk.clearPatient() },
            )

            is TestAppScreen.CareLog -> OSTCareLog(
                onClose = { screen = TestAppScreen.Home },
                onNavigateToRecording = { config ->
                    screen = TestAppScreen.Recording(
                        config ?: OSTRecordingConfiguration.defaultWalk(),
                    )
                },
                onNavigateToSummary = { measurementId ->
                    screen = TestAppScreen.SummaryLoading(measurementId)
                },
                onNavigateToPermissions = { screen = TestAppScreen.PermissionInApp },
            )

            is TestAppScreen.ConfigureFlow -> ConfigureFlowScreen(
                onStartFlow = { config -> screen = TestAppScreen.Recording(config) },
                onBack = { screen = TestAppScreen.Home },
            )

            is TestAppScreen.Recording -> OSTRecordingFlow(
                config = current.config,
                onResult = { screen = TestAppScreen.CareLog },
                onDismiss = { screen = TestAppScreen.Home },
            )

            is TestAppScreen.SummaryLoading -> SummaryLoadingScreen(
                measurementId = current.measurementId,
                onLoaded = { measurement -> screen = TestAppScreen.Summary(measurement) },
                onError = { screen = TestAppScreen.CareLog },
            )

            is TestAppScreen.Summary -> OSTMeasurementSummary(
                measurement = current.measurement,
                onDismiss = { screen = TestAppScreen.CareLog },
            )

            is TestAppScreen.PermissionInApp -> OSTPermissionFlow(
                mode = OSTPermissionMode.IN_APP,
                onComplete = { screen = TestAppScreen.Home },
            )

            is TestAppScreen.PermissionBackground -> OSTPermissionFlow(
                mode = OSTPermissionMode.BACKGROUND,
                onComplete = { screen = TestAppScreen.Home },
            )
        }
    }

    @Composable
    private fun InitFailedScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "OneStep SDK failed to initialize.\nCheck logcat for details.",
                color = Color.Red,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
            )
        }
    }

    private fun connect(
        sdk: OneStep,
        userId: String,
        onDone: (error: String?) -> Unit,
    ) {
        lifecycleScope.launch {
            sdk.setPatient(
                apiKey = TestCredentials.CLIENT_TOKEN,
                customerPatientId = userId,
                identityVerification = TestCredentials.signIdentity(userId),
            ) {
                withFirstName("UIKit")
                withLastName("Tester")
            }.onSuccess {
                Log.i(TAG, "Connected as $userId")
                onDone(null)
            }.onError { error ->
                Log.e(TAG, "Connect failed: ${error.cause}")
                onDone(error.cause.message)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
