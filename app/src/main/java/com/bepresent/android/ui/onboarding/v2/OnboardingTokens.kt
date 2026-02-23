package com.bepresent.android.ui.onboarding.v2

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bepresent.android.R

/** Custom font family for onboarding — FFF Acid Grotesk Soft variable font. */
@OptIn(ExperimentalTextApi::class)
val AcidGroteskFontFamily = FontFamily(
    Font(
        R.font.fff_acid_grotesk_soft,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.fff_acid_grotesk_soft,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.fff_acid_grotesk_soft,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.fff_acid_grotesk_soft,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

/**
 * Onboarding-specific design tokens matching the iOS theme.
 */
object OnboardingTokens {

    // ── Brand Colors ──
    val BrandPrimary = Color(0xFF003BFF)
    val Brand100 = Color(0xFFD9EEFE)
    val Brand200 = Color(0xFFABDDFF)
    val Brand300 = Color(0xFF0091FF)
    val Brand400 = Color(0xFF6093B5)
    val BrandDropShadow = Color(0xFF00249B)

    // ── Neutrals ──
    val NeutralWhite = Color(0xFFFFFFFF)
    val Neutral100 = Color(0xFFF9F9F9)
    val Neutral200 = Color(0xFFE6E6E6)
    val Neutral300 = Color(0xFFD7D7D7)
    val Neutral400 = Color(0xFFCBCBCB)
    val Neutral800 = Color(0xFF777777)
    val Neutral900 = Color(0xFF393939)
    val NeutralBlack = Color(0xFF000000)

    // ── Status Colors ──
    val RedPrimary = Color(0xFFEF2424)
    val GreenPrimary = Color(0xFF32BC00)
    val OrangePrimary = Color(0xFFFF6A00)
    val YellowPrimary = Color(0xFFFFD400)

    // ── Fill Colors ──
    val GreenFill = Color(0xFFE8FFDF)
    val OrangeFill = Color(0xFFFFEDE5)

    // ── Gradient Colors ──
    val BlueGradientTop = Color(0xFFE4F4FF)
    val BlueGradientBottom = Color(0xFF92D2FF)
    val OrangeGradientBottom = Color(0xFFFF6A00)

    // ── Dimensions ──
    val ScreenHorizontalPadding = 20.dp
    val ButtonHeight = 56.dp
    val ProgressBarHeight = 6.dp
}

/**
 * Typography scale matching iOS onboarding theme — uses FFF Acid Grotesk Soft.
 */
object OnboardingTypography {
    private val fontFamily = AcidGroteskFontFamily

    val extraExtraLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 80.sp
    )

    val extraLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp
    )

    val title = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp
    )

    val title2 = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp
    )

    val h1 = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    )

    val h2 = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp
    )

    val p1 = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp
    )

    val p2 = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    )

    val p3 = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    )

    val p4 = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    )

    val label = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    )

    val label2 = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )

    val subLabel = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )

    val subLabel2 = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )

    val caption = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )

    val caption2 = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
}
