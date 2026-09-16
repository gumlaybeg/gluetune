@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
package com.cgens67.avidtune.ui.screens.settings

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cgens67.avidtune.LocalPlayerAwareWindowInsets
import com.cgens67.avidtune.R
import com.cgens67.avidtune.constants.CustomThemeColorKey
import com.cgens67.avidtune.ui.component.IconButton as AppIconButton
import com.cgens67.avidtune.ui.theme.DefaultThemeColor
import com.cgens67.avidtune.ui.theme.ThemeSeedPalette
import com.cgens67.avidtune.ui.theme.ThemeSeedPaletteCodec
import com.cgens67.avidtune.ui.utils.backToMain
import com.cgens67.avidtune.utils.rememberPreference
import com.google.material.color.hct.Hct
import com.google.material.color.scheme.SchemeTonalSpot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ThemePalette(
    val id: String,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val neutral: Color,
    val onPrimary: Color = if (primary.luminance() > 0.5f) Color.Black else Color.White
)

object ThemePalettes {

    private var lightPalettes: List<ThemePalette>? = null
    private var darkPalettes: List<ThemePalette>? = null

    val Default: ThemePalette
        get() = getPalettes(false).first()

    private fun generatePalette(id: String, name: String, primaryHex: Long, isDark: Boolean): ThemePalette {
        val primaryColor = Color(primaryHex)
        val scheme = SchemeTonalSpot(Hct.fromInt(primaryColor.toArgb()), isDark, 0.0)
        return ThemePalette(
            id = id,
            name = name,
            primary = primaryColor,
            secondary = Color(scheme.secondary),
            tertiary = Color(scheme.tertiary),
            neutral = Color(scheme.surfaceVariant),
            onPrimary = Color(scheme.onPrimary)
        )
    }

    fun getPalettes(isDark: Boolean = false): List<ThemePalette> {
        if (isDark && darkPalettes != null) return darkPalettes!!
        if (!isDark && lightPalettes != null) return lightPalettes!!

        val palettes = listOf(
            generatePalette("default", "Default", 0xFFED5564, isDark),
            generatePalette("ocean_blue", "Ocean Blue", 0xFF4A90D9, isDark),
            generatePalette("arctic_blue", "Arctic Blue", 0xFF00BFFF, isDark),
            generatePalette("midnight_navy", "Midnight Navy", 0xFF2C3E50, isDark),
            generatePalette("sky_blue", "Sky Blue", 0xFF87CEEB, isDark),
            generatePalette("cobalt_blue", "Cobalt Blue", 0xFF0047AB, isDark),
            generatePalette("electric_blue", "Electric Blue", 0xFF7DF9FF, isDark),
            generatePalette("emerald_green", "Emerald Green", 0xFF2ECC71, isDark),
            generatePalette("teal_wave", "Teal Wave", 0xFF1ABC9C, isDark),
            generatePalette("forest_green", "Forest Green", 0xFF228B22, isDark),
            generatePalette("spotify_green", "Spotify Green", 0xFF1DB954, isDark),
            generatePalette("mint_fresh", "Mint Fresh", 0xFF98FF98, isDark),
            generatePalette("olive_garden", "Olive Garden", 0xFF808000, isDark),
            generatePalette("sage_green", "Sage Green", 0xFF9CAF88, isDark),
            generatePalette("sunset_orange", "Sunset Orange", 0xFFE67E22, isDark),
            generatePalette("golden_hour", "Golden Hour", 0xFFF39C12, isDark),
            generatePalette("warm_amber", "Warm Amber", 0xFFFFBF00, isDark),
            generatePalette("tangerine_blast", "Tangerine Blast", 0xFFFF9800, isDark),
            generatePalette("peach", "Peach", 0xFFFFDAB9, isDark),
            generatePalette("mango", "Mango", 0xFFFF8243, isDark),
            generatePalette("royal_purple", "Royal Purple", 0xFF9B59B6, isDark),
            generatePalette("lavender_dream", "Lavender Dream", 0xFFB39DDB, isDark),
            generatePalette("grape_purple", "Grape Purple", 0xFF6B5B95, isDark),
            generatePalette("violet", "Violet", 0xFFEE82EE, isDark),
            generatePalette("amethyst", "Amethyst", 0xFF9966CC, isDark),
            generatePalette("ultra_violet", "Ultra Violet", 0xFF645394, isDark),
            generatePalette("cherry_blossom", "Cherry Blossom", 0xFFFFB7C5, isDark),
            generatePalette("rose_quartz", "Rose Quartz", 0xFFF7CAC9, isDark),
            generatePalette("magenta_pop", "Magenta Pop", 0xFFFF00FF, isDark),
            generatePalette("hot_pink", "Hot Pink", 0xFFFF69B4, isDark),
            generatePalette("blush", "Blush", 0xFFDE5D83, isDark),
            generatePalette("coral", "Coral", 0xFFFF7F50, isDark),
            generatePalette("bubblegum", "Bubblegum", 0xFFFFC1CC, isDark),
            generatePalette("crimson_red", "Crimson Red", 0xFFDC143C, isDark),
            generatePalette("youtube_red", "YouTube
