package co.onestep.kmp.uikit.testapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import co.onestep.kmp.uikit.testapp.ui.TestAppTheme
import kotlinx.coroutines.launch

/**
 * Root of the consolidated test app — the single shared UI both platform shells host. Owns the
 * login/clinician/identified top-level state that used to live in the Android `MainActivity` and
 * the iOS `AppState`/`ContentView`.
 */
@Composable
fun TestAppRoot(
    shell: TestAppShell,
    prefs: SettingsPrefs,
) {
    TestAppTheme {
        if (!shell.sdkAvailable) {
            InitFailedScreen()
            return@TestAppTheme
        }

        val scope = rememberCoroutineScope()
        var clinicianLoginState by remember { mutableStateOf<ClinicianLoginUiState>(ClinicianLoginUiState.Idle) }
        var clinicianSession by remember { mutableStateOf<ClinicianSession?>(null) }

        // Clinician web-login deep-link callback (`<scheme>://open/otp?uuid=&otp=`), pushed by the
        // platform shell. No-op for other URLs.
        val deepLink by TestAppDeepLinks.events.collectAsState()
        LaunchedEffect(deepLink) {
            val (uuid, otp) = ClinicianWebLogin.parseRedirect(deepLink) ?: return@LaunchedEffect
            TestAppDeepLinks.consume()
            clinicianLoginState = ClinicianLoginUiState.Exchanging
            clinicianLoginState = ClinicianWebLogin.exchangeOtp(
                environment = prefs.environment,
                customUrl = prefs.customUrl,
                uuid = uuid,
                otp = otp,
            ).fold(
                onSuccess = { ClinicianLoginUiState.Success(it) },
                onFailure = { ClinicianLoginUiState.Error(it.message ?: "Unknown error") },
            )
        }

        // Show the login-result screen while a clinician web login is in flight or just finished
        // (and the user hasn't yet chosen to enter the app).
        if (clinicianLoginState != ClinicianLoginUiState.Idle) {
            ClinicianLoginResultScreen(
                state = clinicianLoginState,
                onDone = { clinicianLoginState = ClinicianLoginUiState.Idle },
                onEnterApp = { session ->
                    // Clinician mode operates on the avatar patient: identify the SDK as the avatar
                    // so a real, authenticated MotionLab backs the flows. (The web-login JWT is not
                    // an SDK session, and OneStep.withPatient needs a clinician session we don't
                    // have here — so scoping to the avatar is the working path in this harness.)
                    clinicianSession = session
                    clinicianLoginState = ClinicianLoginUiState.Idle
                    val org = Organizations.find(prefs.orgName) ?: Organizations.default
                    scope.launch {
                        val error = shell.setPatient(org, AppConstants.AVATAR_AANG_DISTINCT_ID)
                        if (error != null) {
                            // Identify failed: drop back to the result screen with the error.
                            clinicianSession = null
                            clinicianLoginState =
                                ClinicianLoginUiState.Error("Could not start clinician session: $error")
                        }
                    }
                },
            )
            return@TestAppTheme
        }

        val identifiedPatientId by shell.identifiedPatientId.collectAsState()
        var isConnecting by remember { mutableStateOf(false) }
        var loginError by remember { mutableStateOf<String?>(null) }

        Box(Modifier.fillMaxSize()) {
            val identified = identifiedPatientId
            when {
                identified != null -> AuthenticatedContent(
                    shell = shell,
                    prefs = prefs,
                    userId = identified,
                    onLogout = {
                        shell.clearPatient()
                        clinicianSession = null
                    },
                )

                // Clinician mode: showing until the avatar identify completes (then the
                // `identified` branch above renders the full app). Prevents launching a flow
                // against an unauthenticated MotionLab.
                clinicianSession != null -> ClinicianConnectingScreen()

                else -> SettingsScreen(
                    initialEnvironment = prefs.environment,
                    initialCustomUrl = prefs.customUrl,
                    initialOrgName = prefs.orgName,
                    initialDistinctId = prefs.distinctId,
                    isConnecting = isConnecting,
                    errorMessage = loginError,
                    onIdentify = { org, distinctId, environment, customUrl ->
                        persist(prefs, org, distinctId, environment, customUrl)
                        isConnecting = true
                        loginError = null
                        scope.launch {
                            loginError = shell.setPatient(org, distinctId)
                            isConnecting = false
                        }
                    },
                    onClinicianWebLogin = { environment, customUrl ->
                        prefs.environment = environment
                        prefs.customUrl = customUrl
                        clinicianLoginState = ClinicianLoginUiState.InProgress
                        shell.openWebLogin(
                            url = ClinicianWebLogin.loginUrl(environment, customUrl),
                            callbackScheme = ClinicianWebLogin.redirectScheme(environment),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedContent(
    shell: TestAppShell,
    prefs: SettingsPrefs,
    userId: String,
    onLogout: () -> Unit,
) {
    var screen by remember { mutableStateOf<TestAppScreen>(TestAppScreen.Home) }
    var lastEvent by remember { mutableStateOf<String?>(null) }
    var isConnecting by remember { mutableStateOf(false) }
    var settingsError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // MotionLab is created per patient context, so the baseline mock must be (re-)applied after
    // identification. The baseline keeps quick-start Walk/TUG and Care Log recordings completing
    // on a stationary device; the Configure Flow "Mock recording" picker overrides it per run.
    LaunchedEffect(Unit) {
        shell.applyBaselineMock()
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
                persist(prefs, org, distinctId, environment, customUrl)
                isConnecting = true
                settingsError = null
                scope.launch {
                    val error = shell.setPatient(org, distinctId)
                    isConnecting = false
                    settingsError = error
                    if (error == null) screen = TestAppScreen.Home
                }
            },
            onLogout = onLogout,
            onClose = { screen = TestAppScreen.Home },
        )

        is TestAppScreen.ConfigureFlow -> ConfigureFlowScreen(
            mockOptions = shell.mockOptions,
            onStartFlow = { config, mock ->
                shell.setMock(mock)
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
                    returnToCareLog = true,
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
                // PHI-free label only: activity type + event name + a truncated measurement id.
                // `measurement_id` is present only on a real analyzed result. (HIPAA)
                val measurementId = event.properties["measurement_id"]
                val idSuffix = measurementId?.let { " (id:${it.take(8)})" }.orEmpty()
                lastEvent = "${current.config.activityType.name}: ${event.name}$idSuffix"
                screen = if (current.returnToCareLog) TestAppScreen.CareLog else TestAppScreen.Home
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
private fun ClinicianConnectingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = "Preparing clinician session (avatar)…",
                textAlign = TextAlign.Center,
            )
        }
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
            text = "OneStep SDK failed to initialize.\nCheck the platform logs for details.",
            color = MaterialTheme.colorScheme.error,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        )
    }
}

private fun persist(
    prefs: SettingsPrefs,
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
