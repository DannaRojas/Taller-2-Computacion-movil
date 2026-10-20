package edu.javeriana.rutafoto.ui

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

private val RouteFont = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))

val RutaFotoTypography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = RouteFont),
        displayMedium = base.displayMedium.copy(fontFamily = RouteFont),
        displaySmall = base.displaySmall.copy(fontFamily = RouteFont),
        headlineLarge = base.headlineLarge.copy(fontFamily = RouteFont),
        headlineMedium = base.headlineMedium.copy(fontFamily = RouteFont),
        headlineSmall = base.headlineSmall.copy(fontFamily = RouteFont),
        titleLarge = base.titleLarge.copy(fontFamily = RouteFont),
        titleMedium = base.titleMedium.copy(fontFamily = RouteFont),
        titleSmall = base.titleSmall.copy(fontFamily = RouteFont),
        bodyLarge = base.bodyLarge.copy(fontFamily = RouteFont),
        bodyMedium = base.bodyMedium.copy(fontFamily = RouteFont),
        bodySmall = base.bodySmall.copy(fontFamily = RouteFont),
        labelLarge = base.labelLarge.copy(fontFamily = RouteFont),
        labelMedium = base.labelMedium.copy(fontFamily = RouteFont),
        labelSmall = base.labelSmall.copy(fontFamily = RouteFont)
    )
}
