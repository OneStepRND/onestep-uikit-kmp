package co.onestep.kmp.uikit.testapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
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
import co.onestep.kmp.uikit.features.demo.OSTPushPopDemo
import co.onestep.kmp.uikit.features.permissions.OSTPermissionFlow
import co.onestep.kmp.uikit.features.permissions.OSTPermissionMode
import co.onestep.kmp.uikit.features.recordFlow.OSTRecordingFlow
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.summary.OSTMeasurementSummary
import co.onestep.kmp.uikit.testapp.ui.ClinicianLoginResultScreen
import co.onestep.kmp.uikit.testapp.ui.ConfigureFlowScreen
import co.onestep.kmp.uikit.testapp.ui.HomeScreen
import co.onestep.kmp.uikit.testapp.ui.MeasurementPickerScreen
import co.onestep.kmp.uikit.testapp.ui.SettingsScreen
import co.onestep.kmp.uikit.testapp.ui.SummaryLoadingScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val testApplication get() = application as TestApplication
    private lateinit var prefs: SettingsPrefs

    // Clinician web-login flow state (independent of SDK identification). Snapshot state so the
    // deep-link callback below can drive recomposition from outside the composition.
    private val clinicianLoginState = mutableStateOf<ClinicianLoginUiState>(ClinicianLoginUiState.Idle)

    // Set once the user enters the app via clinician web login. Acts as an app-level "signed in"
    // override so the main screen shows while the SDK identification stays UNIDENTIFIED (clinician
    // mode — see docs/patient-scope-clinician-mode-design.md).
    private val clinicianSession = mutableStateOf<ClinicianSession?>(null)

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = SettingsPrefs(applicationContext)
        handleClinicianRedirect(intent)
        val sdk = testApplication.oneStepSdk

        setContent {
            MaterialTheme {
                // Expose Compose `testTag`s as resource-ids so adb/UIAutomator (and Appium) can
                // find them by id — the Android analogue of iOS `accessibilityIdentifier`.
                Box(
                    Modifier
                        .fillMaxSize()
                        .semantics { testTagsAsResourceId = true },
                ) {
                    if (sdk == null) {
                        InitFailedScreen()
                    } else {
                        MainContent(sdk)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleClinicianRedirect(intent)
    }

    /**
     * Handle the clinician web-login deep-link callback (`<scheme>://open/otp?uuid=&otp=`). No-op
     * for any other intent (e.g. the LAUNCHER intent), so it is safe to call from onCreate.
     */
    private fun handleClinicianRedirect(intent: Intent?) {
        val (uuid, otp) = ClinicianWebLogin.parseRedirect(intent?.data) ?: return
        clinicianLoginState.value = ClinicianLoginUiState.Exchanging
        lifecycleScope.launch {
            clinicianLoginState.value = ClinicianWebLogin.exchangeOtp(
                environment = prefs.environment,
                customUrl = prefs.customUrl,
                uuid = uuid,
                otp = otp,
            ).fold(
                onSuccess = { ClinicianLoginUiState.Success(it) },
                onFailure = { ClinicianLoginUiState.Error(it.message ?: "Unknown error") },
            )
        }
    }

    private fun startClinicianWebLogin(environment: String, customUrl: String) {
        prefs.environment = environment
        prefs.customUrl = customUrl
        clinicianLoginState.value = ClinicianLoginUiState.InProgress
        ClinicianWebLogin.launch(this, environment, customUrl)
    }

    @Composable
    private fun MainContent(sdk: OneStep) {
        val clinicianState = clinicianLoginState.value
        val clinician = clinicianSession.value

        // Show the login-result screen while a clinician web login is in flight or just finished
        // (and the user hasn't yet chosen to enter the app).
        if (clinicianState != ClinicianLoginUiState.Idle) {
            ClinicianLoginResultScreen(
                state = clinicianState,
                onDone = { clinicianLoginState.value = ClinicianLoginUiState.Idle },
                onEnterApp = { session ->
                    // Enter the app in clinician mode. The SDK stays UNIDENTIFIED; per-flow
                    // patient scoping is threaded via a patientId argument (wired separately).
                    clinicianSession.value = session
                    clinicianLoginState.value = ClinicianLoginUiState.Idle
                },
            )
            return
        }

        val sdkState by sdk.identificationState.collectAsState()
        var isConnecting by remember { mutableStateOf(false) }
        var loginError by remember { mutableStateOf<String?>(null) }

        Box(Modifier.fillMaxSize()) {
            val identified = sdkState as? OSTIdentificationState.Identified
            when {
                identified != null -> AuthenticatedContent(
                    sdk = sdk,
                    userId = identified.patientId.value,
                )

                clinician != null -> AuthenticatedContent(
                    sdk = sdk,
                    userId = "clinician",
                )

                else -> SettingsScreen(
                    initialEnvironment = prefs.environment,
                    initialCustomUrl = prefs.customUrl,
                    initialOrgName = prefs.orgName,
                    initialDistinctId = prefs.distinctId,
                    isConnecting = isConnecting,
                    errorMessage = loginError,
                    onIdentify = { org, distinctId, environment, customUrl ->
                        persist(org, distinctId, environment, customUrl)
                        isConnecting = true
                        loginError = null
                        connect(sdk, org, distinctId) { error ->
                            isConnecting = false
                            loginError = error
                        }
                    },
                    onClinicianWebLogin = ::startClinicianWebLogin,
                )
            }
        }
    }

    @Composable
    private fun AuthenticatedContent(
        sdk: OneStep,
        userId: String,
    ) {
        var screen by remember { mutableStateOf<TestAppScreen>(TestAppScreen.Home) }
        var lastEvent by remember { mutableStateOf<String?>(null) }
        var isConnecting by remember { mutableStateOf(false) }
        var settingsError by remember { mutableStateOf<String?>(null) }

        // MotionLab is created per patient context, so the baseline mock must be (re-)applied after
        // identification. SUCCESSFUL keeps quick-start Walk/TUG and Care Log recordings completing
        // on an emulator; the Configure Flow "Mock recording" picker overrides it per run.
        LaunchedEffect(Unit) {
            testApplication.setMockIMU(OSTMockIMU.SUCCESSFUL)
        }

        when (val current = screen) {
            is TestAppScreen.Home -> HomeScreen(
                userId = userId,
                lastEvent = lastEvent,
                onClickConfigureAndRecord = { screen = TestAppScreen.ConfigureFlow },
                onClickWalkRecording = {
                    screen = TestAppScreen.Recording(OSTRecordingConfiguration.defaultWalk())
                },
                onClickTug = {
                    screen = TestAppScreen.Recording(OSTRecordingConfiguration.tug())
                },
                onClickPermissionInApp = { screen = TestAppScreen.PermissionInApp },
                onClickPermissionBackground = { screen = TestAppScreen.PermissionBackground },
                onClickMeasurementSummary = { screen = TestAppScreen.MeasurementPicker },
                onClickCareLog = { screen = TestAppScreen.CareLog },
                onClickPushPopDemo = { screen = TestAppScreen.PushPopDemo },
                onClickSettings = {
                    settingsError = null
                    screen = TestAppScreen.Settings
                },
            )

            is TestAppScreen.Settings -> SettingsScreen(
                initialEnvironment = prefs.environment,
                initialCustomUrl = prefs.customUrl,
                initialOrgName = prefs.orgName,
                initialDistinctId = prefs.distinctId,
                isConnecting = isConnecting,
                errorMessage = settingsError,
                onIdentify = { org, distinctId, environment, customUrl ->
                    persist(org, distinctId, environment, customUrl)
                    isConnecting = true
                    settingsError = null
                    connect(sdk, org, distinctId) { error ->
                        isConnecting = false
                        settingsError = error
                        if (error == null) screen = TestAppScreen.Home
                    }
                },
                onLogout = {
                    sdk.clearPatient()
                    clinicianSession.value = null
                },
                onClose = { screen = TestAppScreen.Home },
            )

            is TestAppScreen.ConfigureFlow -> ConfigureFlowScreen(
                onStartFlow = { config, mock ->
                    testApplication.setMockIMU(mock)
                    screen = TestAppScreen.Recording(config)
                },
                onBack = { screen = TestAppScreen.Home },
            )

            is TestAppScreen.MeasurementPicker -> MeasurementPickerScreen(
                onSelect = { measurement -> screen = TestAppScreen.Summary(measurement) },
                onBack = { screen = TestAppScreen.Home },
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

            is TestAppScreen.Recording -> OSTRecordingFlow(
                config = current.config,
                onResult = { event ->
                    // PHI-free label only: activity type + event name + a truncated measurement id
                    // (matches iOS ConfigureFlowView). `measurement_id` is present only on a real
                    // analyzed result. (HIPAA)
                    val measurementId = event.properties["measurement_id"]
                    val idSuffix = measurementId?.let { " (id:${it.take(8)})" }.orEmpty()
                    lastEvent = "${current.config.activityType.name}: ${event.name}$idSuffix"
                    screen = TestAppScreen.CareLog
                },
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

            // forceInteractiveBackGesture lets the Compose edge swipe be exercised on Android,
            // where it is normally off in favor of the system back gesture.
            is TestAppScreen.PushPopDemo -> OSTPushPopDemo(
                onDismiss = { screen = TestAppScreen.Home },
                forceInteractiveBackGesture = true,
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

    private fun persist(
        org: Organization,
        distinctId: String,
        environment: String,
        customUrl: String,
    ) {
        prefs.environment = environment
        prefs.customUrl = customUrl
        prefs.orgName = org.name
        prefs.distinctId = distinctId
    }

    private fun connect(
        sdk: OneStep,
        org: Organization,
        userId: String,
        onDone: (error: String?) -> Unit,
    ) {
        lifecycleScope.launch {
            sdk.setPatient(
                apiKey = org.apiKey,
                customerPatientId = userId,
                identityVerification = org.signIdentity(userId),
            ) {
                withFirstName("UIKit")
                withLastName("Tester")
            }.onSuccess {
                Log.i(TAG, "Connected as $userId (${org.name})")
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
