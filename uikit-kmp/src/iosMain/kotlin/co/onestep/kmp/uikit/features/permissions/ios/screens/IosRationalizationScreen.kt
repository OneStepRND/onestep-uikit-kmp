package co.onestep.kmp.uikit.features.permissions.ios.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.PrimaryButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionFlowCoordinator
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_shield_stars
import org.jetbrains.compose.resources.painterResource

/**
 * Rationalization screen — "Your data is safe with us".
 * Shown when 2+ permissions will be requested.
 * Displays a shield icon and two description paragraphs about data safety.
 */
@Composable
internal fun IosRationalizationScreen(
    coordinator: IosPermissionFlowCoordinator,
) {
    val colors = LocalOSColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.neutral_m5)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PermissionCloseButton { coordinator.onDismiss() }

        Spacer(modifier = Modifier.height(40.dp))

        // Shield icon
        Image(
            painter = painterResource(Res.drawable.ic_shield_stars),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        OSText(
            text = "Your data is safe with us",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.neutral_p3,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OSText(
            text = "Please allow OneStep to analyze your movement and measure your progress.",
            fontSize = 16.sp,
            color = colors.neutral_p2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        OSText(
            text = "Your data is securely stored and shared only with your healthcare provider.",
            fontSize = 16.sp,
            color = colors.neutral_p2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "Continue",
            onClick = { coordinator.nextScreen() },
            modifier = Modifier.fillMaxWidth(),
            size = OSButtonSize.Big,
        )
    }
}
