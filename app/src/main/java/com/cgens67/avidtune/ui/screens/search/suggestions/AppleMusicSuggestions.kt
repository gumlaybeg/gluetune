@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationApi::class
)

package com.cgens67.gluetune.ui.screens.search.suggestions

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cgens67.gluetune.LocalPlayerAwareWindowInsets
import com.cgens67.gluetune.LocalPlayerConnection
import com.cgens67.gluetune.R
import com.cgens67.gluetune.models.toMediaMetadata
import com.cgens67.gluetune.playback.PlayerConnection
import com.cgens67.gluetune.playback.queues.YouTubeQueue
import com.cgens67.innertube.YouTube
import com.cgens67.innertube.models.AlbumItem
import com.cgens67.innertube.models.ArtistItem
import com.cgens67.innertube.models.SongItem
import com.cgens67.innertube.models.WatchEndpoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// --- Data Models ---

data class SuggestionTrack(
    val rank: Int,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val appleMusicUrl: String? = null
)

data class SuggestionArtist(
    val rank: Int,
    val name: String,
    val thumbnailUrl: String?
)

data class SuggestionAlbum(
    val rank: Int,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val appleMusicUrl: String? = null
)

data class RegionInfo(
    val slug: String,
    val name: String,
    val flag: String
)

val SuggestionRegions = listOf(
    RegionInfo("us", "United States", "🇺🇸"),
    RegionInfo("gb", "United Kingdom", "🇬🇧"),
    RegionInfo("ca", "Canada", "🇨🇦"),
    RegionInfo("au", "Australia", "🇦🇺"),
    RegionInfo("jp", "Japan", "🇯🇵"),
    RegionInfo("kr", "South Korea", "🇰🇷"),
    RegionInfo("de", "Germany", "🇩🇪"),
    RegionInfo("fr", "France", "🇫🇷"),
    RegionInfo("in", "India", "🇮🇳"),
    RegionInfo("br", "Brazil", "🇧🇷"),
    RegionInfo("mx", "Mexico", "🇲🇽"),
    RegionInfo("es", "Spain", "🇪🇸"),
    RegionInfo("it", "Italy", "🇮🇹"),
    RegionInfo("nl", "Netherlands", "🇳🇱"),
    RegionInfo("se", "Sweden", "🇸🇪"),
    RegionInfo("id", "Indonesia", "🇮🇩"),
    RegionInfo("ph", "Philippines", "🇵🇭"),
    RegionInfo("tr", "Turkey", "🇹🇷"),
    RegionInfo("ar", "Argentina", "🇦🇷"),
    RegionInfo("co", "Colombia", "🇨🇴"),
    RegionInfo("ng", "Nigeria", "🇳🇬"),
    RegionInfo("za", "South Africa", "🇿🇦"),
    RegionInfo("sg", "Singapore", "🇸🇬"),
    RegionInfo("th", "Thailand", "🇹🇭")
)

val SuggestionRegionSlugToName = SuggestionRegions.associate { it.slug to it.name }

private enum class ChartCategory {
    ALL, SONGS, ALBUMS, ARTISTS
}

// --- Scraper ---

object AppleMusicScraper {
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private fun getJson(url: String): JSONArray? = try {
        val req = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
        client.newCall(req).execute().use {
            if (!it.isSuccessful) null
            else JSONObject(it.body?.string() ?: "").getJSONObject("feed").getJSONArray("results")
        }
    } catch (e: Exception) {
        null
    }

    fun fetchTopSongs(cc: String = "us"): List<SuggestionTrack> =
        getJson("https://rss.applemarketingtools.com/api/v2/$cc/music/most-played/100/songs.json")?.let {
            List(it.length()) { i ->
                val o = it.getJSONObject(i)
                val rawArt = o.optString("artworkUrl100", "")
                val highRes = rawArt.replace("100x100", "600x600")
                SuggestionTrack(
                    rank = i + 1,
                    title = o.getString("name"),
                    artist = o.getString("artistName"),
                    thumbnailUrl = highRes,
                    appleMusicUrl = o.optString("url", null)
                )
            }
        } ?: emptyList()

    fun fetchTopAlbums(cc: String = "us"): List<SuggestionAlbum> =
        getJson("https://rss.applemarketingtools.com/api/v2/$cc/music/most-played/50/albums.json")?.let {
            List(it.length()) { i ->
                val o = it.getJSONObject(i)
                val rawArt = o.optString("artworkUrl100", "")
                val highRes = rawArt.replace("100x100", "600x600")
                SuggestionAlbum(
                    rank = i + 1,
                    title = o.getString("name"),
                    artist = o.getString("artistName"),
                    thumbnailUrl = highRes,
                    appleMusicUrl = o.optString("url", null)
                )
            }
        } ?: emptyList()

    fun getTrendingArtists(tracks: List<SuggestionTrack>): List<SuggestionArtist> {
        val counts = mutableMapOf<String, Int>()
        val imgs = mutableMapOf<String, String?>()
        tracks.forEach { t ->
            val a = t.artist.split(",", "&", "feat.", "ft.").first().trim()
            counts[a] = (counts[a] ?: 0) + 1
            if (imgs[a] == null) imgs[a] = t.thumbnailUrl
        }
        return counts.toList()
            .sortedByDescending { it.second }
            .take(20)
            .mapIndexed { i, p ->
                SuggestionArtist(i + 1, p.first, imgs[p.first])
            }
    }
}

// --- ViewModel ---

@HiltViewModel
class SuggestionsViewModel @Inject constructor() : ViewModel() {
    val suggestionTracks = MutableStateFlow<List<SuggestionTrack>?>(null)
    val suggestionArtists = MutableStateFlow<List<SuggestionArtist>?>(null)
    val suggestionAlbums = MutableStateFlow<List<SuggestionAlbum>?>(null)
    val isLoading = MutableStateFlow(false)
    val errorState = MutableStateFlow<String?>(null)
    private var currentLoadedRegion: String? = null

    fun refresh(countryCode: String = "us", force: Boolean = false) {
        val cc = countryCode.lowercase()
        if (isLoading.value && !force && currentLoadedRegion == cc) return
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            errorState.value = null
            if (currentLoadedRegion != cc || force) {
                suggestionTracks.value = null
                suggestionArtists.value = null
                suggestionAlbums.value = null
            }
            try {
                coroutineScope {
                    launch {
                        val tracks = AppleMusicScraper.fetchTopSongs(cc)
                        if (tracks.isNotEmpty()) {
                            suggestionTracks.value = tracks
                            suggestionArtists.value = AppleMusicScraper.getTrendingArtists(tracks)
                        }
                    }
                    launch {
                        val albums = AppleMusicScraper.fetchTopAlbums(cc)
                        if (albums.isNotEmpty()) {
                            suggestionAlbums.value = albums
                        }
                    }
                }
                currentLoadedRegion = cc
            } catch (e: Exception) {
                e.printStackTrace()
                errorState.value = e.message ?: "Failed to fetch charts"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun playTrack(t: SuggestionTrack, p: PlayerConnection?) = viewModelScope.launch(Dispatchers.IO) {
        YouTube.search("${t.title} ${t.artist}", YouTube.SearchFilter.FILTER_SONG).onSuccess { res ->
            val songs = res.items.filterIsInstance<SongItem>()
            val best = songs.firstOrNull { s ->
                s.title.equals(t.title, true) && s.artists.any { a -> t.artist.contains(a.name, true) }
            } ?: songs.firstOrNull { s -> s.artists.any { a -> t.artist.contains(a.name, true) } }
            ?: songs.firstOrNull()

            best?.let {
                withContext(Dispatchers.Main) {
                    p?.playQueue(YouTubeQueue(WatchEndpoint(videoId = it.id), it.toMediaMetadata()))
                }
            }
        }
    }

    fun navigateToArtist(a: SuggestionArtist, nav: NavController) = viewModelScope.launch(Dispatchers.IO) {
        YouTube.search(a.name, YouTube.SearchFilter.FILTER_ARTIST).onSuccess { res ->
            res.items.filterIsInstance<ArtistItem>().firstOrNull()?.let { artist ->
                withContext(Dispatchers.Main) { nav.navigate("artist/${artist.id}") }
            }
        }
    }

    fun navigateToAlbum(a: SuggestionAlbum, nav: NavController) = viewModelScope.launch(Dispatchers.IO) {
        YouTube.search("${a.title} ${a.artist}", YouTube.SearchFilter.FILTER_ALBUM).onSuccess { res ->
            res.items.filterIsInstance<AlbumItem>().firstOrNull()?.let { album ->
                withContext(Dispatchers.Main) { nav.navigate("album/${album.id}") }
            }
        }
    }
}

// --- Main Composable Screen ---

@Composable
fun AppleMusicTrendingScreen(
    navController: NavController,
    viewModel: SuggestionsViewModel = hiltViewModel()
) {
    val tracks by viewModel.suggestionTracks.collectAsState()
    val artists by viewModel.suggestionArtists.collectAsState()
    val albums by viewModel.suggestionAlbums.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val playerConnection = LocalPlayerConnection.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var showRegionSheet by remember { mutableStateOf(false) }
    var regionCode by remember { mutableStateOf("us") }
    var selectedCategory by remember { mutableStateOf(ChartCategory.ALL) }

    LaunchedEffect(regionCode) { viewModel.refresh(regionCode) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pullToRefreshState = rememberPullToRefreshState()

    val scaleFraction = {
        if (isLoading) 1f
        else LinearOutSlowInEasing.transform(pullToRefreshState.distanceFraction).coerceIn(0f, 1f)
    }

    val regionInfo = remember(regionCode) {
        SuggestionRegions.find { it.slug == regionCode } ?: SuggestionRegions.first()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.apple),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.apple_music_trending),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(painterResource(R.drawable.arrow_back), contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    // Region selection pill
                    Surface(
                        onClick = { showRegionSheet = true },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = regionInfo.flag, fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = regionInfo.slug.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Refresh Button
                    IconButton(
                        onClick = {
                            viewModel.refresh(regionCode, force = true)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_retry)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullToRefresh(
                    state = pullToRefreshState,
                    isRefreshing = isLoading,
                    onRefresh = { viewModel.refresh(regionCode, force = true) }
                )
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Category Filter Pills
                CategoryFilterPills(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )

                // Shimmer Loading Skeleton
                if (isLoading && tracks == null) {
                    TrendingShimmerPlaceholder()
                } else if (errorState != null && tracks == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.something_went_wrong),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { viewModel.refresh(regionCode, force = true) }) {
                                Text(stringResource(R.string.action_retry))
                            }
                        }
                    }
                } else {
                    val currentTracks = tracks.orEmpty()
                    val currentAlbums = albums.orEmpty()
                    val currentArtists = artists.orEmpty()

                    // Top #1 Spotlight Banner (Visible in ALL or SONGS)
                    if (currentTracks.isNotEmpty() && (selectedCategory == ChartCategory.ALL || selectedCategory == ChartCategory.SONGS)) {
                        val topTrack = currentTracks.first()
                        HeroSpotlightCard(
                            track = topTrack,
                            regionName = regionInfo.name,
                            onPlay = {
                                Toast.makeText(context, context.getString(R.string.loading_item, topTrack.title), Toast.LENGTH_SHORT).show()
                                viewModel.playTrack(topTrack, playerConnection)
                            }
                        )
                    }

                    // Top Songs Carousel
                    if (currentTracks.isNotEmpty() && (selectedCategory == ChartCategory.ALL || selectedCategory == ChartCategory.SONGS)) {
                        val remainingTracks = if (selectedCategory == ChartCategory.ALL) currentTracks.drop(1).take(24) else currentTracks
                        AppleMusicSongsPager(
                            tracks = remainingTracks,
                            onTrackClick = { track ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                Toast.makeText(context, context.getString(R.string.loading_item, track.title), Toast.LENGTH_SHORT).show()
                                viewModel.playTrack(track, playerConnection)
                            }
                        )
                    }

                    // Top Albums Section
                    if (currentAlbums.isNotEmpty() && (selectedCategory == ChartCategory.ALL || selectedCategory == ChartCategory.ALBUMS)) {
                        AppleMusicAlbumsRow(
                            albums = currentAlbums,
                            onAlbumClick = { album ->
                                Toast.makeText(context, context.getString(R.string.loading_item, album.title), Toast.LENGTH_SHORT).show()
                                viewModel.navigateToAlbum(album, navController)
                            }
                        )
                    }

                    // Trending Artists Section
                    if (currentArtists.isNotEmpty() && (selectedCategory == ChartCategory.ALL || selectedCategory == ChartCategory.ARTISTS)) {
                        AppleMusicArtistsRow(
                            artists = currentArtists,
                            onArtistClick = { artist ->
                                Toast.makeText(context, context.getString(R.string.loading_item, artist.name), Toast.LENGTH_SHORT).show()
                                viewModel.navigateToArtist(artist, navController)
                            }
                        )
                    }

                    // Bottom Attribution Footer
                    Spacer(Modifier.height(32.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.apple),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.data_from_apple_music),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Pull to refresh indicator
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        scaleX = scaleFraction()
                        scaleY = scaleFraction()
                    }
            ) {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isLoading
                )
            }
        }
    }

    // Modern Region Selector BottomSheet
    if (showRegionSheet) {
        RegionSelectorBottomSheet(
            currentRegion = regionCode,
            onRegionSelected = { slug ->
                regionCode = slug
                showRegionSheet = false
            },
            onDismiss = { showRegionSheet = false }
        )
    }
}

// --- Category Filter Pills ---

@Composable
private fun CategoryFilterPills(
    selectedCategory: ChartCategory,
    onCategorySelected: (ChartCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == ChartCategory.ALL,
            onClick = { onCategorySelected(ChartCategory.ALL) },
            label = { Text(stringResource(R.string.filter_all)) },
            shape = RoundedCornerShape(16.dp),
            border = null,
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        FilterChip(
            selected = selectedCategory == ChartCategory.SONGS,
            onClick = { onCategorySelected(ChartCategory.SONGS) },
            label = { Text(stringResource(R.string.trending_songs)) },
            shape = RoundedCornerShape(16.dp),
            border = null,
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        FilterChip(
            selected = selectedCategory == ChartCategory.ALBUMS,
            onClick = { onCategorySelected(ChartCategory.ALBUMS) },
            label = { Text(stringResource(R.string.trending_albums)) },
            shape = RoundedCornerShape(16.dp),
            border = null,
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        FilterChip(
            selected = selectedCategory == ChartCategory.ARTISTS,
            onClick = { onCategorySelected(ChartCategory.ARTISTS) },
            label = { Text(stringResource(R.string.trending_artists)) },
            shape = RoundedCornerShape(16.dp),
            border = null,
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}

// --- Hero Spotlight Card ---

@Composable
private fun HeroSpotlightCard(
    track: SuggestionTrack,
    regionName: String,
    onPlay: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "hero_press"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .scale(cardScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onPlay
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle ambient backdrop art blur
            if (!track.thumbnailUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = track.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { alpha = 0.22f }
                )
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.98f)
                            )
                        )
                    )
            )

            Column(modifier = Modifier.padding(20.dp)) {
                // Top chart badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "👑 #1 IN $regionName",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Icon(
                        painter = painterResource(R.drawable.apple),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Artwork with glow
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .shadow(12.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        AsyncImage(
                            model = track.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.width(18.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Action row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPlay,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.play),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- Top Songs Carousel ---

@Composable
private fun AppleMusicSongsPager(
    tracks: List<SuggestionTrack>,
    onTrackClick: (SuggestionTrack) -> Unit
) {
    val itemsPerPage = 4
    val pageCount = remember(tracks) { (tracks.size + itemsPerPage - 1) / itemsPerPage }
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.trending_songs),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Top ${tracks.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    val startIdx = page * itemsPerPage
                    val endIdx = minOf(startIdx + itemsPerPage, tracks.size)

                    for (i in startIdx until endIdx) {
                        val track = tracks[i]
                        RankedTrackRow(
                            track = track,
                            onClick = { onTrackClick(track) }
                        )
                        if (i < endIdx - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            }
        }

        // Pager indicator dots
        if (pageCount > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount.coerceAtMost(8)) { index ->
                    val isSelected = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 22.dp else 6.dp,
                        label = "dot_width"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        label = "dot_color"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun RankedTrackRow(
    track: SuggestionTrack,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val rowScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "track_press"
    )

    val rankColor = when (track.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(rowScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Number
        Text(
            text = "${track.rank}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = rankColor,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.width(8.dp))

        // Thumbnail
        AsyncImage(
            model = track.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(Modifier.width(14.dp))

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        // Quick Play Icon
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.play),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// --- Trending Albums Row ---

@Composable
private fun AppleMusicAlbumsRow(
    albums: List<SuggestionAlbum>,
    onAlbumClick: (SuggestionAlbum) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.trending_albums),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(albums) { album ->
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.94f else 1f,
                    label = "album_scale"
                )

                Column(
                    modifier = Modifier
                        .width(136.dp)
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onAlbumClick(album) }
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .shadow(6.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        AsyncImage(
                            model = album.thumbnailUrl,
                            contentDescription = album.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Rank Tag Overlay
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.72f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${album.rank}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// --- Trending Artists Row ---

@Composable
private fun AppleMusicArtistsRow(
    artists: List<SuggestionArtist>,
    onArtistClick: (SuggestionArtist) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.trending_artists),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(artists) { artist ->
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.94f else 1f,
                    label = "artist_scale"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(96.dp)
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onArtistClick(artist) }
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        AsyncImage(
                            model = artist.thumbnailUrl,
                            contentDescription = artist.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Rank Tag Overlay
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${artist.rank}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// --- Region Selector BottomSheet ---

@Composable
private fun RegionSelectorBottomSheet(
    currentRegion: String,
    onRegionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current

    val filteredRegions = remember(searchQuery) {
        if (searchQuery.isBlank()) SuggestionRegions
        else SuggestionRegions.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.slug.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.select_region),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.search),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            inner()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredRegions, key = { it.slug }) { region ->
                    val isSelected = region.slug == currentRegion

                    Surface(
                        onClick = { onRegionSelected(region.slug) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = region.flag, fontSize = 24.sp)
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = region.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Loading Shimmer Skeleton ---

@Composable
private fun TrendingShimmerPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        )

        // Songs carousel placeholder
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            )
        }
    }
}
