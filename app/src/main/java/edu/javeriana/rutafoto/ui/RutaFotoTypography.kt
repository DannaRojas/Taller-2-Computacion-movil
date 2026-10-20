package edu.javeriana.rutafoto.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

// Fuente del propio dispositivo, sin descargar archivos ni añadir bibliotecas.
val RutaFotoTypography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = FontFamily.Serif),
        displayMedium = base.displayMedium.copy(fontFamily = FontFamily.Serif),
        displaySmall = base.displaySmall.copy(fontFamily = FontFamily.Serif),
        headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Serif),
        headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif),
        headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Serif),
        titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif),
        titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Serif),
        titleSmall = base.titleSmall.copy(fontFamily = FontFamily.Serif),
        bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.Serif),
        bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.Serif),
        bodySmall = base.bodySmall.copy(fontFamily = FontFamily.Serif),
        labelLarge = base.labelLarge.copy(fontFamily = FontFamily.Serif),
        labelMedium = base.labelMedium.copy(fontFamily = FontFamily.Serif),
        labelSmall = base.labelSmall.copy(fontFamily = FontFamily.Serif)
    )
}
