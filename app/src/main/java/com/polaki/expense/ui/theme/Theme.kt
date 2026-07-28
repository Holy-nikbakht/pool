package com.polaki.expense.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val PulakiDarkColorScheme = darkColorScheme(
    primary = PrimaryTeal,
    onPrimary = BackgroundDark,
    primaryContainer = PrimaryTealVariant,
    secondary = SecondaryRed,
    onSecondary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    error = SecondaryRed
)

// Rounded, friendly type scale (approximates a Vazirmatn-like rounded Persian look
// using the system default font family; swap in Vazirmatn font files under res/font
// for pixel-perfect branding).
val PulakiTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp)
)

val PulakiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp2()),
    small = RoundedCornerShape(12.dp2()),
    medium = RoundedCornerShape(18.dp2()),
    large = RoundedCornerShape(24.dp2()),
    extraLarge = RoundedCornerShape(28.dp2())
)

// small helper to avoid importing dp separately at top for constants above
private fun Int.dp2() = androidx.compose.ui.unit.Dp(this.toFloat())

@Composable
fun PulakiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PulakiDarkColorScheme,
        typography = PulakiTypography,
        shapes = PulakiShapes,
        content = content
    )
}
