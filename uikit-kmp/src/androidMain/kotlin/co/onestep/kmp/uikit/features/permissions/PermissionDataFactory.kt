package co.onestep.kmp.uikit.features.permissions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SecondaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TertiaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.ui.components.InstructionContent
import co.onestep.kmp.uikit.ui.components.StyledSegment
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.allow
import co.onestep.kmp.uikit_kmp.generated.resources.change_to
import co.onestep.kmp.uikit_kmp.generated.resources.continue_camel_case
import co.onestep.kmp.uikit_kmp.generated.resources.get_better_assessments_with_real_life_walks
import co.onestep.kmp.uikit_kmp.generated.resources.go_to_settings
import co.onestep.kmp.uikit_kmp.generated.resources.go_to_your_device_settings
import co.onestep.kmp.uikit_kmp.generated.resources.how_is_my_data_used
import co.onestep.kmp.uikit_kmp.generated.resources.ic_run_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_trend_up_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_walk_stars
import co.onestep.kmp.uikit_kmp.generated.resources.in_order_to_monitor_your_measurements_access_to_your_physical_activity_is_required
import co.onestep.kmp.uikit_kmp.generated.resources.notification_permission_explanation_settings_redirect_description_background
import co.onestep.kmp.uikit_kmp.generated.resources.notification_permission_explanation_settings_redirect_description_in_app
import co.onestep.kmp.uikit_kmp.generated.resources.notifications_permission_explanation_request_description_background
import co.onestep.kmp.uikit_kmp.generated.resources.notifications_permission_explanation_request_description_inapp
import co.onestep.kmp.uikit_kmp.generated.resources.notifications_permission_explanation_request_title_background
import co.onestep.kmp.uikit_kmp.generated.resources.notifications_permission_explanation_request_title_inapp
import co.onestep.kmp.uikit_kmp.generated.resources.permissions
import co.onestep.kmp.uikit_kmp.generated.resources.physical_activity
import co.onestep.kmp.uikit_kmp.generated.resources.please_allow_access_you_physical_activity_when_prompted_tap
import co.onestep.kmp.uikit_kmp.generated.resources.please_allow_the_app_to_run_in_the_background_without_battery_restrictions
import co.onestep.kmp.uikit_kmp.generated.resources.tap
import org.jetbrains.compose.resources.stringResource

/**
 * Slim factory for building permission screen data.
 * Extracted from the original DataFactory (97KB) to only include permission-related methods.
 */
internal object PermissionDataFactory {

    @Composable
    fun physicalPermissionRequest(
        onSelection: () -> Unit,
        onSecondaryClick: (() -> Unit)? = null,
        toolBarData: ToolBarData,
    ) = PermissionScreenData(
        toolBarData = toolBarData,
        title =
            TextData(
                stringResource(Res.string.in_order_to_monitor_your_measurements_access_to_your_physical_activity_is_required),
                textSize = 28.sp,
                fontWeight = FontWeight.Bold,
            ),
        content =
            InstructionContent.Paragraph(
                listOf(
                    StyledSegment(stringResource(Res.string.please_allow_access_you_physical_activity_when_prompted_tap)),
                    StyledSegment(
                        " '${stringResource(Res.string.allow)}'",
                        color = Color(0xFF3E3D3B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                ),
            ),
        mainIcon = IconData(Res.drawable.ic_walk_stars),
        brandButton =
            PrimaryButtonData(
                text =
                    TextData(
                        stringResource(Res.string.allow),
                        18.sp,
                        W700,
                    ),
                action = { onSelection() },
            ),
        tertiaryButton =
            TertiaryButtonData(
                text =
                    TextData(
                        stringResource(Res.string.how_is_my_data_used),
                        18.sp,
                        W700,
                        color = Color(0xFF3E3D3B),
                    ),
                action = { onSecondaryClick?.invoke() },
            ),
    )

    @Composable
    fun permissionSettingsRequest(
        onSelection: () -> Unit,
        onSecondaryClick: () -> Unit,
        toolBarData: ToolBarData,
        customContent: @Composable () -> Unit = {},
    ) = PermissionScreenData(
        toolBarData = toolBarData,
        title =
            TextData(
                stringResource(Res.string.in_order_to_monitor_your_measurements_access_to_your_physical_activity_is_required),
                28.sp,
                FontWeight.Bold,
            ),
        content =
            InstructionContent.Bulleted(
                listOf(
                    listOf(
                        StyledSegment(
                            stringResource(Res.string.go_to_your_device_settings),
                            fontSize = 18.sp,
                            color = Color(0xFF716D69),
                        ),
                    ),
                    listOf(
                        StyledSegment(
                            stringResource(Res.string.tap),
                            fontSize = 18.sp,
                            color = Color(0xFF716D69),
                        ),
                        StyledSegment(
                            stringResource(Res.string.permissions),
                            fontSize = 18.sp,
                            color = Color(0xFF716D69),
                        ),
                    ),
                    listOf(
                        StyledSegment(
                            stringResource(Res.string.tap),
                            fontSize = 18.sp,
                            color = Color(0xFF716D69),
                        ),
                        StyledSegment(
                            "'${stringResource(Res.string.physical_activity)}'",
                            fontSize = 18.sp,
                            color = Color(0xFF716D69),
                        ),
                    ),
                    listOf(
                        StyledSegment(
                            stringResource(Res.string.change_to),
                            fontSize = 18.sp,
                            color = Color(0xFF716D69),
                        ),
                        StyledSegment(
                            "'${stringResource(Res.string.allow)}'",
                            color = Color(0xFF3E3D3B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        ),
                    ),
                ),
            ),
        mainIcon = IconData(Res.drawable.ic_walk_stars) {},
        customContent = customContent,
        brandButton =
            PrimaryButtonData(
                text =
                    TextData(
                        stringResource(Res.string.go_to_settings),
                        18.sp,
                        W700,
                    ),
                action = onSelection,
            ),
        tertiaryButton =
            TertiaryButtonData(
                text =
                    TextData(
                        stringResource(Res.string.how_is_my_data_used),
                        18.sp,
                        W700,
                        color = Color(0xFF3E3D3B),
                    ),
                action = { onSecondaryClick() },
            ),
    )

    @Composable
    fun notificationPermissionRequest(
        permissionMode: OSTPermissionMode,
        onSelection: () -> Unit,
        onSecondaryClick: (() -> Unit)? = null,
        toolBarData: ToolBarData,
    ) = PermissionScreenData(
        toolBarData = toolBarData,
        title =
            TextData(
                text =
                    when (permissionMode) {
                        OSTPermissionMode.BACKGROUND ->
                            stringResource(Res.string.notifications_permission_explanation_request_title_background)
                        OSTPermissionMode.IN_APP ->
                            stringResource(Res.string.notifications_permission_explanation_request_title_inapp)
                        else -> error("Permission mode $permissionMode is not supported on Android")
                    },
                textSize = 28.sp,
                fontWeight = FontWeight.Bold,
            ),
        content =
            when (permissionMode) {
                OSTPermissionMode.BACKGROUND ->
                    InstructionContent.Paragraph(
                        listOf(
                            StyledSegment(stringResource(Res.string.notifications_permission_explanation_request_description_background)),
                        ),
                    )
                OSTPermissionMode.IN_APP ->
                    InstructionContent.Paragraph(
                        listOf(
                            StyledSegment(stringResource(Res.string.notifications_permission_explanation_request_description_inapp)),
                        ),
                    )
                else -> error("Permission mode $permissionMode is not supported on Android")
            },
        mainIcon =
            when (permissionMode) {
                OSTPermissionMode.BACKGROUND -> IconData(Res.drawable.ic_run_stars)
                OSTPermissionMode.IN_APP -> IconData(Res.drawable.ic_walk_stars)
                else -> error("Permission mode $permissionMode is not supported on Android")
            },
        brandButton =
            PrimaryButtonData(
                text =
                    TextData(
                        stringResource(Res.string.continue_camel_case),
                        18.sp,
                        W700,
                    ),
                action = { onSelection() },
            ),
        tertiaryButton =
            TertiaryButtonData(
                text =
                    TextData(
                        stringResource(Res.string.how_is_my_data_used),
                        18.sp,
                        W700,
                        color = Color(0xFF3E3D3B),
                    ),
                action = { onSecondaryClick?.invoke() },
            ),
    )

    @Composable
    fun notificationSettingsRequest(
        permissionMode: OSTPermissionMode,
        onSelection: () -> Unit,
        toolBarData: ToolBarData,
    ) = PermissionScreenData(
        toolBarData = toolBarData,
        title =
            TextData(
                text =
                    when (permissionMode) {
                        OSTPermissionMode.BACKGROUND ->
                            stringResource(Res.string.notifications_permission_explanation_request_title_background)
                        OSTPermissionMode.IN_APP ->
                            stringResource(Res.string.notifications_permission_explanation_request_title_inapp)
                        else -> error("Permission mode $permissionMode is not supported on Android")
                    },
                textSize = 28.sp,
                fontWeight = FontWeight.Bold,
            ),
        content =
            when (permissionMode) {
                OSTPermissionMode.BACKGROUND ->
                    InstructionContent.Paragraph(
                        listOf(
                            StyledSegment(
                                stringResource(Res.string.notification_permission_explanation_settings_redirect_description_background),
                            ),
                        ),
                    )
                OSTPermissionMode.IN_APP ->
                    InstructionContent.Paragraph(
                        listOf(
                            StyledSegment(
                                stringResource(Res.string.notification_permission_explanation_settings_redirect_description_in_app),
                            ),
                        ),
                    )
                else -> error("Permission mode $permissionMode is not supported on Android")
            },
        mainIcon =
            when (permissionMode) {
                OSTPermissionMode.BACKGROUND -> IconData(Res.drawable.ic_run_stars)
                OSTPermissionMode.IN_APP -> IconData(Res.drawable.ic_walk_stars)
                else -> error("Permission mode $permissionMode is not supported on Android")
            },
        brandButton =
            PrimaryButtonData(
                text =
                    TextData(
                        stringResource(Res.string.go_to_settings),
                        18.sp,
                        W700,
                    ),
                action = { onSelection() },
            ),
        tertiaryButton =
            TertiaryButtonData(
                text =
                    TextData(
                        stringResource(Res.string.how_is_my_data_used),
                        18.sp,
                        W700,
                        color = Color(0xFF3E3D3B),
                    ),
                action = { },
            ),
    )

    @Composable
    fun batteryOptimizationPermissionRequest(
        onSelection: () -> Unit,
        onSecondaryClick: (() -> Unit)? = null,
        toolBarData: ToolBarData,
    ) = PermissionScreenData(
        toolBarData = toolBarData,
        title =
            TextData(
                stringResource(Res.string.get_better_assessments_with_real_life_walks),
                28.sp,
                FontWeight.Bold,
            ),
        content =
            InstructionContent.Paragraph(
                listOf(
                    StyledSegment(stringResource(Res.string.please_allow_the_app_to_run_in_the_background_without_battery_restrictions)),
                ),
            ),
        mainIcon = IconData(Res.drawable.ic_trend_up_stars),
        brandButton =
            PrimaryButtonData(
                text =
                    TextData(
                        stringResource(Res.string.allow),
                        18.sp,
                        W700,
                    ),
                action = { onSelection() },
            ),
        tertiaryButton =
            TertiaryButtonData(
                text =
                    TextData(
                        stringResource(Res.string.how_is_my_data_used),
                        18.sp,
                        W700,
                        color = Color(0xFF3E3D3B),
                    ),
                action = { onSecondaryClick?.invoke() },
            ),
    )
}
