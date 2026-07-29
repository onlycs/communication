package page.angad.contacts.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import page.angad.uicore.GoogleSansFlex

internal fun Typography.withRoundedGoogleSans(): Typography {
    fun TextStyle.rounded() = copy(
        fontFamily = GoogleSansFlex(roundedness = 100f, weight = fontWeight ?: FontWeight.Normal)
    )

    return copy(
        displayLarge = displayLarge.rounded(),
        displayMedium = displayMedium.rounded(),
        displaySmall = displaySmall.rounded(),
        headlineLarge = headlineLarge.rounded(),
        headlineMedium = headlineMedium.rounded(),
        headlineSmall = headlineSmall.rounded(),
        titleLarge = titleLarge.rounded(),
        titleMedium = titleMedium.rounded(),
        titleSmall = titleSmall.rounded(),
        bodyLarge = bodyLarge.rounded(),
        bodyMedium = bodyMedium.rounded(),
        bodySmall = bodySmall.rounded(),
        labelLarge = labelLarge.rounded(),
        labelMedium = labelMedium.rounded(),
        labelSmall = labelSmall.rounded(),
    )
}

@Composable
fun ContactsTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val color = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

    MaterialExpressiveTheme(
        colorScheme = color,
        content = content,
        typography = Typography().withRoundedGoogleSans()
    )
}