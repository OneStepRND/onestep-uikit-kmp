package co.onestep.kmp.uikit.features.permissions.ios.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.PrimaryButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors

/**
 * Reusable settings redirect content that shows instructions for enabling
 * a permission via iOS Settings app.
 *
 * @param title The screen title (e.g., "Motion & Fitness Access")
 * @param description Explanation of why the permission is needed
 * @param steps Numbered instructions for the user to follow in Settings
 * @param onOpenSettings Callback to open the Settings app
 * @param onSkip Callback when user wants to skip this permission
 */
@Composable
internal fun IosSettingsRedirectContent(
    title: String,
    description: String,
    steps: List<String>,
    onOpenSettings: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalOSColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.neutral_m5)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Close/Skip button at top-right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onSkip) {
                OSText(
                    text = "Skip",
                    color = colors.primary_0,
                    fontSize = 17.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OSText(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.neutral_p3,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OSText(
            text = description,
            fontSize = 16.sp,
            color = colors.neutral_p2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Step-by-step instructions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colors.neutral_m2,
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OSText(
                text = "How to enable:",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.neutral_p3,
            )
            steps.forEachIndexed { index, step ->
                Row(verticalAlignment = Alignment.Top) {
                    OSText(
                        text = "${index + 1}.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primary_0,
                        modifier = Modifier.size(24.dp),
                    )
                    OSText(
                        text = step,
                        fontSize = 15.sp,
                        color = colors.neutral_p3,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Go to Settings button
        PrimaryButton(
            text = "Go to Settings",
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(),
            size = OSButtonSize.Big,
        )
    }
}
