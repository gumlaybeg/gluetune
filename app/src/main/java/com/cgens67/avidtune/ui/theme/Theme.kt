package com.cgens67.gluetune.ui.theme

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import com.google.material.color.dynamiccolor.DynamicScheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.cgens67.gluetune.constants.AppFont
import com.google.material.color.dynamiccolor.DynamicScheme
import com.google.material.color.dynamiccolor.MaterialDynamicColors
import com.google.material.color.hct.Hct
import com.google.material.color.scheme.SchemeTonalSpot
import com.google.material.color.score.Score

val DefaultThemeColor = Color(0xFFED5564)
val LocalGlueTuneFont = staticCompositionLocalOf { AppFont.SYSTEM }
val LocalGlueTuneFontFamily = staticCompositionLocalOf { AppFontFamily }

@Composable
fun rememberGlueTuneLyricsFontFamily(): FontFamily {
    val appFont = LocalGlueTuneFont.current
    val fontFamily = LocalGlueTuneFontFamily.current
    return remember(appFont, fontFamily) {
        if (appFont == AppFont.SYSTEM) FontFamily.Default else fontFamily
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GlueTuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    seedPalette: ThemeSeedPalette? = null,
    disableAnimations: Boolean = false,
    appFont: AppFont = AppFont.SYSTEM,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val useSystemDynamicColor =
        (seedPalette == null && themeColor == DefaultThemeColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

    val resolvedFontFamily = remember(appFont) {
        when (appFont) {
            AppFont.SYSTEM -> FontFamily.Default
            AppFont.SF_PRO -> sfProDisplayBold
            AppFont.GOOGLE_SANS -> googleSansBold
            AppFont.SPACE_GROTESK -> spaceGroteskBold
        }
    }
        
    val typography = remember(resolvedFontFamily) {
        when (resolvedFontFamily) {
            AppFontFamily -> AppTypography
            FontFamily.Default -> SystemTypography
            else -> typographyFor(resolvedFontFamily)
        }
    }
        
    val motionScheme = remember(disableAnimations) {
        if (disableAnimations) DisabledMotionScheme else MotionScheme.expressive()
    }

    val appColorScheme = remember(seedPalette, themeColor, darkTheme) {
        if (seedPalette != null) {
            mergedSeedColorScheme(
                primarySeed = seedPalette.primary,
                secondarySeed = seedPalette.secondary,
                tertiarySeed = seedPalette.tertiary,
                neutralSeed = seedPalette.neutral,
                isDark = darkTheme,
            )
        } else {
            val scheme = SchemeTonalSpot(Hct.fromInt(themeColor.toArgb()), darkTheme, 0.0)
            scheme.toColorScheme()
        }
    }

    val baseColorScheme = if (useSystemDynamicColor) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        appColorScheme
    }

    val colorScheme = remember(baseColorScheme, pureBlack, darkTheme) {
        if (darkTheme && pureBlack) baseColorScheme.pureBlack(true, darkTheme) else baseColorScheme
    }

    val animatedColorScheme = if (disableAnimations) {
        colorScheme
    } else {
        animateColorScheme(
            targetColorScheme = colorScheme,
            animationSpec = motionScheme.defaultEffectsSpec(),
        )
    }

    val expressiveShapes = remember {
        Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
        )
    }

    CompositionLocalProvider(
        LocalGlueTuneFont provides appFont,
        LocalGlueTuneFontFamily provides resolvedFontFamily,
        LocalOverscrollFactory provides null
    ) {
        MaterialExpressiveTheme(
            colorScheme = animatedColorScheme,
            motionScheme = motionScheme,
            typography = typography,
            shapes = expressiveShapes,
            content = content,
        )
    }
}

@Composable
private fun animateColorScheme(
    targetColorScheme: ColorScheme,
    animationSpec: FiniteAnimationSpec<Color>,
): ColorScheme =
    ColorScheme(
        primary = animateColorAsState(targetColorScheme.primary, animationSpec, label = "primary").value,
        onPrimary = animateColorAsState(targetColorScheme.onPrimary, animationSpec, label = "onPrimary").value,
        primaryContainer = animateColorAsState(targetColorScheme.primaryContainer, animationSpec, label = "primaryContainer").value,
        onPrimaryContainer = animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec, label = "onPrimaryContainer").value,
        inversePrimary = animateColorAsState(targetColorScheme.inversePrimary, animationSpec, label = "inversePrimary").value,
        secondary = animateColorAsState(targetColorScheme.secondary, animationSpec, label = "secondary").value,
        onSecondary = animateColorAsState(targetColorScheme.onSecondary, animationSpec, label = "onSecondary").value,
        secondaryContainer = animateColorAsState(targetColorScheme.secondaryContainer, animationSpec, label = "secondaryContainer").value,
        onSecondaryContainer = animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec, label = "onSecondaryContainer").value,
        tertiary = animateColorAsState(targetColorScheme.tertiary, animationSpec, label = "tertiary").value,
        onTertiary = animateColorAsState(targetColorScheme.onTertiary, animationSpec, label = "onTertiary").value,
        tertiaryContainer = animateColorAsState(targetColorScheme.tertiaryContainer, animationSpec, label = "tertiaryContainer").value,
        onTertiaryContainer = animateColorAsState(targetColorScheme.onTertiaryContainer, animationSpec, label = "onTertiaryContainer").value,
        background = animateColorAsState(targetColorScheme.background, animationSpec, label = "background").value,
        onBackground = animateColorAsState(targetColorScheme.onBackground, animationSpec, label = "onBackground").value,
        surface = animateColorAsState(targetColorScheme.surface, animationSpec, label = "surface").value,
        onSurface = animateColorAsState(targetColorScheme.onSurface, animationSpec, label = "onSurface").value,
        surfaceVariant = animateColorAsState(targetColorScheme.surfaceVariant, animationSpec, label = "surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec, label = "onSurfaceVariant").value,
        surfaceTint = animateColorAsState(targetColorScheme.surfaceTint, animationSpec, label = "surfaceTint").value,
        inverseSurface = animateColorAsState(targetColorScheme.inverseSurface, animationSpec, label = "inverseSurface").value,
        inverseOnSurface = animateColorAsState(targetColorScheme.inverseOnSurface, animationSpec, label = "inverseOnSurface").value,
        error = animateColorAsState(targetColorScheme.error, animationSpec, label = "error").value,
        onError = animateColorAsState(targetColorScheme.onError, animationSpec, label = "onError").value,
        errorContainer = animateColorAsState(targetColorScheme.errorContainer, animationSpec, label = "errorContainer").value,
        onErrorContainer = animateColorAsState(targetColorScheme.onErrorContainer, animationSpec, label = "onErrorContainer").value,
        outline = animateColorAsState(targetColorScheme.outline, animationSpec, label = "outline").value,
        outlineVariant = animateColorAsState(targetColorScheme.outlineVariant, animationSpec, label = "outlineVariant").value,
        scrim = animateColorAsState(targetColorScheme.scrim, animationSpec, label = "scrim").value,
        surfaceBright = animateColorAsState(targetColorScheme.surfaceBright, animationSpec, label = "surfaceBright").value,
        surfaceDim = animateColorAsState(targetColorScheme.surfaceDim, animationSpec, label = "surfaceDim").value,
        surfaceContainer = animateColorAsState(targetColorScheme.surfaceContainer, animationSpec, label = "surfaceContainer").value,
        surfaceContainerLow = animateColorAsState(targetColorScheme.surfaceContainerLow, animationSpec, label = "surfaceContainerLow").value,
        surfaceContainerLowest = animateColorAsState(targetColorScheme.surfaceContainerLowest, animationSpec, label = "surfaceContainerLowest").value,
        surfaceContainerHigh = animateColorAsState(targetColorScheme.surfaceContainerHigh, animationSpec, label = "surfaceContainerHigh").value,
        surfaceContainerHighest = animateColorAsState(targetColorScheme.surfaceContainerHighest, animationSpec, label = "surfaceContainerHighest").value,
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private object DisabledMotionScheme : MotionScheme {
    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = snap()
}

private fun mergedSeedColorScheme(
    primarySeed: Color,
    secondarySeed: Color,
    tertiarySeed: Color,
    neutralSeed: Color,
    isDark: Boolean
): ColorScheme {
    val primaryScheme = SchemeTonalSpot(Hct.fromInt(primarySeed.toArgb()), isDark, 0.0).toColorScheme()
    val secondaryScheme = SchemeTonalSpot(Hct.fromInt(secondarySeed.toArgb()), isDark, 0.0).toColorScheme()
    val tertiaryScheme = SchemeTonalSpot(Hct.fromInt(tertiarySeed.toArgb()), isDark, 0.0).toColorScheme()
    val neutralScheme = SchemeTonalSpot(Hct.fromInt(neutralSeed.toArgb()), isDark, 0.0).toColorScheme()

    return ColorScheme(
        primary = primaryScheme.primary,
        onPrimary = primaryScheme.onPrimary,
        primaryContainer = primaryScheme.primaryContainer,
        onPrimaryContainer = primaryScheme.onPrimaryContainer,
        inversePrimary = primaryScheme.inversePrimary,
        secondary = secondaryScheme.primary,
        onSecondary = secondaryScheme.onPrimary,
        secondaryContainer = secondaryScheme.primaryContainer,
        onSecondaryContainer = secondaryScheme.onPrimaryContainer,
        tertiary = tertiaryScheme.primary,
        onTertiary = tertiaryScheme.onPrimary,
        tertiaryContainer = tertiaryScheme.primaryContainer,
        onTertiaryContainer = tertiaryScheme.onPrimaryContainer,
        background = neutralScheme.background,
        onBackground = neutralScheme.onBackground,
        surface = neutralScheme.surface,
        onSurface = neutralScheme.onSurface,
        surfaceVariant = neutralScheme.surfaceVariant,
        onSurfaceVariant = neutralScheme.onSurfaceVariant,
        inverseSurface = neutralScheme.inverseSurface,
        inverseOnSurface = neutralScheme.inverseOnSurface,
        surfaceBright = neutralScheme.surfaceBright,
        surfaceDim = neutralScheme.surfaceDim,
        surfaceContainer = neutralScheme.surfaceContainer,
        surfaceContainerHigh = neutralScheme.surfaceContainerHigh,
        surfaceContainerHighest = neutralScheme.surfaceContainerHighest,
        surfaceContainerLow = neutralScheme.surfaceContainerLow,
        surfaceContainerLowest = neutralScheme.surfaceContainerLowest,
        outline = neutralScheme.outline,
        outlineVariant = neutralScheme.outlineVariant,
        error = primaryScheme.error,
        onError = primaryScheme.onError,
        errorContainer = primaryScheme.errorContainer,
        onErrorContainer = primaryScheme.onErrorContainer,
        scrim = neutralScheme.scrim,
        surfaceTint = primaryScheme.surfaceTint,
    )
}

fun DynamicScheme.toColorScheme(): ColorScheme {
    val dc = MaterialDynamicColors()
    return ColorScheme(
        primary = Color(dc.primary().getArgb(this)),
        onPrimary = Color(dc.onPrimary().getArgb(this)),
        primaryContainer = Color(dc.primaryContainer().getArgb(this)),
        onPrimaryContainer = Color(dc.onPrimaryContainer().getArgb(this)),
        inversePrimary = Color(dc.inversePrimary().getArgb(this)),
        secondary = Color(dc.secondary().getArgb(this)),
        onSecondary = Color(dc.onSecondary().getArgb(this)),
        secondaryContainer = Color(dc.secondaryContainer().getArgb(this)),
        onSecondaryContainer = Color(dc.onSecondaryContainer().getArgb(this)),
        tertiary = Color(dc.tertiary().getArgb(this)),
        onTertiary = Color(dc.onTertiary().getArgb(this)),
        tertiaryContainer = Color(dc.tertiaryContainer().getArgb(this)),
        onTertiaryContainer = Color(dc.onTertiaryContainer().getArgb(this)),
        background = Color(dc.background().getArgb(this)),
        onBackground = Color(dc.onBackground().getArgb(this)),
        surface = Color(dc.surface().getArgb(this)),
        onSurface = Color(dc.onSurface().getArgb(this)),
        surfaceVariant = Color(dc.surfaceVariant().getArgb(this)),
        onSurfaceVariant = Color(dc.onSurfaceVariant().getArgb(this)),
        surfaceTint = Color(dc.primary().getArgb(this)),
        inverseSurface = Color(dc.inverseSurface().getArgb(this)),
        inverseOnSurface = Color(dc.inverseOnSurface().getArgb(this)),
        error = Color(dc.error().getArgb(this)),
        onError = Color(dc.onError().getArgb(this)),
        errorContainer = Color(dc.errorContainer().getArgb(this)),
        onErrorContainer = Color(dc.onErrorContainer().getArgb(this)),
        outline = Color(dc.outline().getArgb(this)),
        outlineVariant = Color(dc.outlineVariant().getArgb(this)),
        scrim = Color(dc.scrim().getArgb(this)),
        surfaceBright = Color(dc.surfaceBright().getArgb(this)),
        surfaceDim = Color(dc.surfaceDim().getArgb(this)),
        surfaceContainer = Color(dc.surfaceContainer().getArgb(this)),
        surfaceContainerHigh = Color(dc.surfaceContainerHigh().getArgb(this)),
        surfaceContainerHighest = Color(dc.surfaceContainerHighest().getArgb(this)),
        surfaceContainerLow = Color(dc.surfaceContainerLow().getArgb(this)),
        surfaceContainerLowest = Color(dc.surfaceContainerLowest().getArgb(this)),
    )
}

fun ColorScheme.pureBlack(apply: Boolean, isDarkTheme: Boolean) =
    if (apply && isDarkTheme) {
        copy(
            surface = Color.Black,
            background = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainerLowest = Color.Black,
        )
    } else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}

fun Bitmap.extractThemeColor(): Color {
    val colorsToPopulation = Palette.from(this).maximumColorCount(8).generate().swatches.associate { it.rgb to it.population }
    val rankedColors = Score.score(colorsToPopulation)
    return Color(rankedColors.firstOrNull() ?: DefaultThemeColor.toArgb())
}
