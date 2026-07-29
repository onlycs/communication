package page.angad.uicore

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

fun GoogleSansFlex(
    roundedness: Float = 0f,
    weight: FontWeight = FontWeight.Normal,
    width: Float = 100f,
    slant: Float = 0f,
    grade: Int = 0
): FontFamily {
    return FontFamily(
        Font(
            R.font.google_sans_flex,
            weight = weight,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight.weight),
                FontVariation.width(width),
                FontVariation.slant(slant),
                FontVariation.grade(grade),
                FontVariation.Setting("ROND", roundedness)
            ),
        )
    )
}