package bg.prices

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Leaf = Color(0xFFE5F3E5)
private val DeepLeaf = Color(0xFF356B43)
private val Orange = Color(0xFFFFE8D2)
private val DeepOrange = Color(0xFF9A541D)
private val Paper = Color(0xFFFFFCF8)
private val BgPricesColors = lightColorScheme(
    primary = DeepLeaf, onPrimary = Color.White, secondary = DeepOrange,
    onSecondary = Color.White, tertiary = Color(0xFF6C8C63),
    background = Paper, surface = Color.White, surfaceVariant = Leaf,
    secondaryContainer = Orange, onSecondaryContainer = DeepOrange
)

private val translations = mapOf(
    "bg" to mapOf(
        "tagline" to "Сравнявай цени в EUR", "location" to "Местоположение",
        "choose_location" to "Избери населено място", "all_locations" to "Всички локации",
        "categories" to "Категории", "pricing" to "Цени", "add" to "Добави",
        "basket" to "Количка", "profile" to "Профил", "settings" to "Настройки",
        "groceries" to "Хранителни стоки", "technology" to "Техника",
        "home" to "Дом и обзавеждане", "fuel" to "Бензиностанции",
        "groceries_sub" to "Ежедневни продукти и покупки", "technology_sub" to "Уреди и електроника",
        "home_sub" to "Мебели, ремонт и стоки за дома", "fuel_sub" to "Цени на горива по вид и локация",
        "select_category" to "Избери категория. Използвай стрелките за пренареждане.",
        "selected" to "Избрано", "view_prices" to "Виж цени  ›",
        "prices_for" to "Цени · ", "prices_promos_history" to "Цени, промоции и история на цените",
        "all_stores" to "Сравнение на всички магазини",
        "selected_store" to "Избран магазин: ", "choose_retailer_hint" to "Избери търговец в Категории, за да ограничиш този изглед. Без избран търговец се сравняват всички налични магазини.",
        "store_overview" to "ЦЕНИ ПО МАГАЗИНИ", "pending" to "Няма данни за цени",
        "no_live" to "Все още няма потвърдени актуални цени",
        "demo_prices" to "Демонстрационен изглед · Не се показват актуални цени от търговци.",
        "loading_prices" to "Зареждане на цени...", "no_verified_prices" to "Няма потвърдени актуални цени за избрания филтър.",
        "offer_disclaimer" to "Оферти от официални сайтове на търговци. Наличността и приложимостта може да се различават по магазин.",
        "offer_source" to "Оферта от официален сайт", "source_details" to "Отвори източника", "retry" to "Опитай отново", "was" to "Редовна цена:",
        "basket_sub" to "Списъци · сравнения · спестявания",
        "current_category" to "Текуща категория: ", "no_store_filter" to "Няма избран магазин в Категории: показват се всички магазини от категорията.",
        "store_filter" to "Филтър от Категории: ", "prices_by_store" to "ЦЕНИ ПО МАГАЗИНИ",
        "lists" to "Твоите списъци за пазаруване", "lists_desc" to "Създавай и управлявай списъци с продукти за следващото пазаруване.",
        "single_basket" to "Най-евтина количка в един магазин", "single_desc" to "Сравни общата стойност на количката във всеки магазин от избраната категория.",
        "combined_basket" to "Най-евтина комбинирана количка", "combined_desc" to "Сравни количка, разделена между няколко магазина.",
        "savings" to "Обобщение на спестяванията", "savings_desc" to "Очакваните спестявания ще се покажат при наличие на потвърдени цени.",
        "demo_basket" to "Демонстрационен изглед · Все още няма актуални цени или изчислени спестявания.",
        "profile_settings" to "Профил и настройки", "profile_sub" to "Акаунт · предложения · модерация · настройки",
        "data_sources" to "Източници на данни", "osm_attribution" to "Местоположения и обекти от OpenStreetMap. Данните за картата се обновяват на всеки 12 часа; покритието зависи от наличната информация в картата.",
        "display" to "Изглед на категориите", "grid" to "Мрежа", "list" to "Списък",
        "language" to "Език на приложението", "bulgarian" to "Български", "english" to "Английски",
        "account" to "Акаунт и вход", "submissions" to "Моите предложения",
        "moderation" to "Опашка за модерация", "app_settings" to "Настройки на приложението",
        "category_order" to "Подредба на категориите", "location_settings" to "Настройки на местоположението",
        "add_title" to "Добави в BG Prices", "suggestion" to "Предложение", "note" to "Бележка",
        "opinion" to "Мнение", "online_bulgaria" to "Онлайн · България", "describe" to "Опиши: ", "review_note" to "Предложенията ще бъдат прегледани преди да се добавят в каталога.",
        "submit" to "Изпрати", "cancel" to "Отказ", "location_filter" to "Филтър по местоположение",
        "city_town" to "Населено място (град, село и др.)", "coordinates" to "Координати", "current_location" to "Използвай текущото местоположение",
        "city_placeholder" to "Напр. Велико Търново, Арбанаси, Килифарево", "coordinates_placeholder" to "Ширина, дължина (напр. 43.0757, 25.6172)",
        "radius" to "Радиус", "save_location" to "Запази местоположението",
        "location_note" to "Можеш да избереш град, село или друго населено място, отбелязано на картата. Местоположението се използва във всички раздели. Намирането на близки обекти изисква потвърдени координати на търговските обекти.",
        "permission_denied" to "Няма разрешение за местоположение. Можеш да въведеш град или координати ръчно.",
        "location_unavailable" to "Няма налични координати от устройството. Провери дали услугите за местоположение са включени или въведи координати ръчно.",
        "invalid_location" to "Въведи град/село или валидни координати.",
        "no_stores_here" to "Няма потвърдени магазини от тази категория в избраното населено място.",
        "catalog_config_missing" to "Каталогът ще се зареди след свързване с BG Prices сървъра.",
        "choose_location_first" to "Избери населено място или текущо местоположение, за да видиш наличните магазини.",
        "catalog_error" to "Каталогът временно не е достъпен. Провери връзката и опитай отново.",
        "location_all" to "Всички локации", "nearby" to "Близо до мен",
        "location_saved" to "Местоположението е запазено", "choose_location" to "Избор на местоположение",
        "profile_preview_note" to "Профилите и личните данни не са налични в тази preview версия. Не се съхраняват данни за потребителски акаунти.",
        "add_sent" to "Предложението е изпратено за преглед.", "add_error" to "Изпращането не беше успешно. Провери връзката и опитай отново.",
        "basket_add" to "Добави продукт или артикул", "basket_item_placeholder" to "Име на продукт", "basket_empty" to "Списъкът е празен.",
        "basket_local_note" to "Списъкът се пази локално на това устройство. Ценово сравнение ще има при потвърдени цени.", "remove" to "Премахни"
    ),
    "en" to mapOf(
        "tagline" to "Compare prices in EUR", "location" to "Location",
        "choose_location" to "Choose a settlement", "all_locations" to "All locations",
        "categories" to "Categories", "pricing" to "Pricing", "add" to "Add",
        "basket" to "Basket", "profile" to "Profile", "settings" to "Settings",
        "groceries" to "Groceries", "technology" to "Technology",
        "home" to "Furniture & home", "fuel" to "Petrol stations",
        "groceries_sub" to "Everyday products and grocery baskets", "technology_sub" to "Devices and electronics",
        "home_sub" to "Furniture, DIY and home products", "fuel_sub" to "Fuel prices by type and location",
        "select_category" to "Select a category; use arrows to change its position.",
        "selected" to "Selected", "view_prices" to "View prices  ›",
        "prices_for" to "Pricing · ", "prices_promos_history" to "Prices, promotions and price history",
        "all_stores" to "All stores comparison",
        "selected_store" to "Selected store: ", "choose_retailer_hint" to "Choose a retailer in Categories to narrow this view. With no retailer selected, prices from all available stores will be compared.",
        "store_overview" to "STORE PRICE OVERVIEW", "pending" to "Price data pending",
        "no_live" to "No verified live prices yet",
        "demo_prices" to "Demo layout only · no live retailer prices are displayed.",
        "loading_prices" to "Loading prices...", "no_verified_prices" to "No verified current prices match this filter.",
        "offer_disclaimer" to "Offers from official retailer websites. Stock and branch availability may vary.",
        "offer_source" to "Official retailer offer", "source_details" to "Open source", "retry" to "Try again", "was" to "Regular price:",
        "basket_sub" to "Lists · comparisons · savings",
        "current_category" to "Current category: ", "no_store_filter" to "No store selected in Categories: showing all stores in this category.",
        "store_filter" to "Store filter from Categories: ", "prices_by_store" to "PRICES BY STORE",
        "lists" to "Your shopping lists", "lists_desc" to "Create and manage product lists for your next shop.",
        "single_basket" to "Cheapest single-store basket", "single_desc" to "Compare your basket total at every store in the selected category.",
        "combined_basket" to "Cheapest combined basket", "combined_desc" to "Compare a basket split across multiple stores.",
        "savings" to "Savings overview", "savings_desc" to "Estimated savings will appear when verified prices are available.",
        "demo_basket" to "Demo layout only · no live prices or calculated savings yet.",
        "profile_settings" to "Profile & Settings", "profile_sub" to "Account · submissions · moderation · settings",
        "data_sources" to "Data sources", "osm_attribution" to "Places and mapped locations from OpenStreetMap. Map data refreshes every 12 hours; coverage depends on information available in the map.",
        "display" to "Category display", "grid" to "Grid", "list" to "List",
        "language" to "App language", "bulgarian" to "Bulgarian", "english" to "English",
        "account" to "Account and sign in", "submissions" to "My submissions",
        "moderation" to "Moderation queue", "app_settings" to "App settings",
        "category_order" to "Category display order", "location_settings" to "Location settings",
        "add_title" to "Add to BG Prices", "suggestion" to "Suggestion", "note" to "Note",
        "opinion" to "Opinion", "online_bulgaria" to "Online · Bulgaria", "describe" to "Describe: ", "review_note" to "Submissions will be reviewed before appearing in the catalogue.",
        "submit" to "Submit", "cancel" to "Cancel", "location_filter" to "Location filter",
        "city_town" to "Inhabited place (city, town, village, etc.)", "coordinates" to "Coordinates", "current_location" to "Use current location",
        "city_placeholder" to "e.g. Veliko Tarnovo, Arbanasi, Kilifarevo", "coordinates_placeholder" to "Latitude, longitude (e.g. 43.0757, 25.6172)",
        "radius" to "Radius", "save_location" to "Save location",
        "location_note" to "Choose a city, town, village or another inhabited place shown on the map. The selection is shared across all tabs. Nearby branch results require verified retailer branch coordinates.",
        "permission_denied" to "Location permission was not granted. You can enter a city or coordinates manually.",
        "location_unavailable" to "No device coordinates are available. Check that location services are enabled or enter coordinates manually.",
        "invalid_location" to "Enter a city/town or valid coordinates.",
        "no_stores_here" to "No verified stores in this category are available at the selected location.",
        "catalog_config_missing" to "The catalogue will load once the BG Prices server is connected.",
        "choose_location_first" to "Choose an inhabited place or your current location to see available stores.",
        "catalog_error" to "The catalogue is temporarily unavailable. Check the connection and try again.",
        "location_all" to "All locations", "nearby" to "Near me",
        "location_saved" to "Location saved", "choose_location" to "Choose location",
        "profile_preview_note" to "Accounts and personal profile data are not available in this preview. No user account profile data is stored.",
        "add_sent" to "Your submission was sent for review.", "add_error" to "Submission failed. Check the connection and try again.",
        "basket_add" to "Add a product or item", "basket_item_placeholder" to "Product name", "basket_empty" to "Your list is empty.",
        "basket_local_note" to "This list is stored locally on this device. Price comparison requires verified prices.", "remove" to "Remove"
    )
)

private fun tr(language: String, key: String): String =
    translations[language]?.get(key) ?: translations["bg"]?.get(key) ?: key

private data class PriceCategory(
    val key: String,
    val subtitleKey: String,
    val retailers: List<String>,
    val nameBg: String? = null,
    val nameEn: String? = null,
    val descriptionBg: String? = null,
    val descriptionEn: String? = null
) {
    fun title(language: String) = if (language == "en") nameEn ?: tr(language, key) else nameBg ?: tr(language, key)
    fun subtitle(language: String) = if (language == "en") descriptionEn ?: tr(language, subtitleKey) else descriptionBg ?: tr(language, subtitleKey)
}

private val defaultCategories = listOf(
    PriceCategory("groceries", "groceries_sub", emptyList()),
    PriceCategory("technology", "technology_sub", emptyList()),
    PriceCategory("home", "home_sub", emptyList()),
    PriceCategory("fuel", "fuel_sub", emptyList())
)

private fun categoryEmoji(key: String): String = when (key) {
    "groceries" -> "🍐"
    "technology" -> "💻"
    "home" -> "🛋️"
    "fuel" -> "⛽"
    else -> "🛍️"
}


private data class CatalogPayload(val categories: List<CatalogCategoryDto> = emptyList())
private data class CatalogCategoryDto(
    val code: String,
    val nameBg: String,
    val nameEn: String,
    val descriptionBg: String,
    val descriptionEn: String,
    val retailers: List<CatalogRetailerDto> = emptyList()
)
private data class CatalogRetailerDto(val code: String, val name: String, val websiteUrl: String? = null)
private data class LocalitySuggestionDto(val id: String, val name: String, val nameBg: String?, val type: String, val municipality: String?, val region: String?)
private data class SubmissionRequestDto(val type: String, val content: String, val locationLabel: String?, val latitude: Double?, val longitude: Double?)
private data class SubmissionAcceptedDto(val id: String, val status: String)
private data class PriceCatalogItemDto(
    val productId: String,
    val categoryCode: String,
    val productName: String,
    val brand: String? = null,
    val variant: String? = null,
    val packageQuantity: Double? = null,
    val packageUnit: String? = null,
    val retailerCode: String,
    val retailerName: String,
    val branchName: String? = null,
    val city: String? = null,
    val settlement: String? = null,
    val address: String? = null,
    val priceEur: Double,
    val regularPriceEur: Double? = null,
    val currency: String = "EUR",
    val source: String,
    val sourceReference: String? = null,
    val priceNote: String? = null,
    val priceScope: String = "BRANCH",
    val verified: Boolean,
    val collectedAt: String,
    val validUntil: String? = null
)

private interface CatalogApi {
    @GET("api/v1/prices/catalog")
    suspend fun getPriceCatalog(
        @Query("categoryCode") categoryCode: String,
        @Query("locality") locality: String? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("radiusKm") radiusKm: Int = 10,
        @Query("retailerName") retailerName: String? = null,
        @Query("limit") limit: Int = 100
    ): List<PriceCatalogItemDto>

    @GET("api/v1/catalog")
    suspend fun getCatalog(
        @Query("locality") locality: String? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("radiusKm") radiusKm: Int = 10
    ): CatalogPayload

    @GET("api/v1/localities")
    suspend fun searchLocalities(@Query("query") query: String): List<LocalitySuggestionDto>

    @POST("api/v1/submissions")
    suspend fun submit(@Body request: SubmissionRequestDto): SubmissionAcceptedDto
}

private enum class Destination { CATEGORIES, PRICING, BASKET, PROFILE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = BgPricesColors) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { BgPricesApp() }
            }
        }
    }
}

@Composable
private fun BgPricesApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("bg_prices_settings", Context.MODE_PRIVATE) }
    val database = remember { BgPricesDatabase.get(context) }
    val scope = rememberCoroutineScope()
    var basketItems by remember { mutableStateOf(emptyList<BasketItemEntity>()) }
    LaunchedEffect(database) { database.basketDao().observeAll().collect { basketItems = it } }
    val catalogApi = remember(BuildConfig.API_BASE_URL) {
        val base = BuildConfig.API_BASE_URL.trim()
        if (base.isBlank()) null else runCatching {
            Retrofit.Builder()
                .baseUrl(if (base.endsWith("/")) base else "$base/")
                .client(OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(75, TimeUnit.SECONDS)
                    .callTimeout(90, TimeUnit.SECONDS)
                    .build())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(CatalogApi::class.java)
        }.getOrNull()
    }
    var language by remember { mutableStateOf(prefs.getString("language", "bg") ?: "bg") }
    var locationLabel by remember { mutableStateOf(prefs.getString("location_label", "") ?: "") }
    var locationCoordinates by remember { mutableStateOf(prefs.getString("location_coordinates", "") ?: "") }
    var radius by remember { mutableStateOf(prefs.getInt("location_radius", 10)) }
    val categories = remember { mutableStateListOf<PriceCategory>().apply { addAll(defaultCategories.map { it.copy(retailers = emptyList()) }) } }
    var catalogMessage by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableIntStateOf(0) }
    var destination by remember { mutableStateOf(Destination.CATEGORIES) }
    var selectedRetailer by remember { mutableStateOf<String?>(null) }
    var profileView by remember { mutableStateOf(prefs.getString("category_view", "Grid") ?: "Grid") }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf("suggestion") }
    var addText by remember { mutableStateOf("") }
    var addStatus by remember { mutableStateOf("") }

    LaunchedEffect(language, locationLabel, locationCoordinates, radius, profileView) {
        prefs.edit()
            .putString("language", language)
            .putString("location_label", locationLabel)
            .putString("location_coordinates", locationCoordinates)
            .putInt("location_radius", radius)
            .putString("category_view", profileView)
            .apply()
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = try {
                manager.getProviders(true).mapNotNull { provider ->
                    try { manager.getLastKnownLocation(provider) } catch (_: SecurityException) { null }
                }.maxByOrNull { it.time }
            } catch (_: Exception) { null }
            if (location != null) {
                locationCoordinates = String.format(java.util.Locale.US, "%.6f, %.6f", location.latitude, location.longitude)
                locationLabel = tr(language, "nearby")
            } else {
                locationLabel = tr(language, "location_unavailable")
            }
        } else {
            locationLabel = tr(language, "permission_denied")
        }
    }


    LaunchedEffect(locationLabel, locationCoordinates, radius, language) {
        val baseUrl = BuildConfig.API_BASE_URL.trim()
        if (catalogApi == null || baseUrl.isBlank()) {
            categories.indices.forEach { index -> categories[index] = categories[index].copy(retailers = emptyList()) }
            catalogMessage = tr(language, "catalog_config_missing")
        } else {
            try {
                val coords = locationCoordinates.split(",").mapNotNull { it.trim().toDoubleOrNull() }
                val hasCoordinates = coords.size == 2
                val ignoredLabels = listOf(tr(language, "nearby"), tr(language, "permission_denied"), tr(language, "location_unavailable"))
                val locality = locationLabel.takeIf { it.isNotBlank() && it !in ignoredLabels && !hasCoordinates }
                val response = catalogApi.getCatalog(
                    locality = locality,
                    latitude = if (hasCoordinates) coords[0] else null,
                    longitude = if (hasCoordinates) coords[1] else null,
                    radiusKm = radius
                )
                val loaded = response.categories.map { item ->
                    PriceCategory(
                        key = item.code,
                        subtitleKey = item.code + "_sub",
                        retailers = item.retailers.map { it.name },
                        nameBg = item.nameBg,
                        nameEn = item.nameEn,
                        descriptionBg = item.descriptionBg,
                        descriptionEn = item.descriptionEn
                    )
                }
                val savedOrder = prefs.getString("category_order", "")?.split("|")?.filter { it.isNotBlank() }.orEmpty()
                val ordered = loaded.sortedBy { item -> savedOrder.indexOf(item.key).let { if (it < 0) Int.MAX_VALUE else it } }
                categories.clear()
                categories.addAll(ordered)
                if (selectedCategory !in categories.indices) selectedCategory = 0
                if (selectedRetailer != null && categories.getOrNull(selectedCategory)?.retailers?.contains(selectedRetailer) != true) {
                    selectedRetailer = null
                }
                catalogMessage = if (locality == null && !hasCoordinates) tr(language, "choose_location_first") else if (categories.none { it.retailers.isNotEmpty() }) tr(language, "no_stores_here") else ""
            } catch (_: Exception) {
                categories.replaceAll { it.copy(retailers = emptyList()) }
                catalogMessage = tr(language, "catalog_error")
            }
        }
    }

    Scaffold(
        containerColor = Paper,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf(
                    Destination.CATEGORIES to "categories", Destination.PRICING to "pricing",
                    Destination.BASKET to "basket", Destination.PROFILE to "profile"
                ).forEach { (item, key) ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = { Text(when (item) { Destination.CATEGORIES -> "▦"; Destination.PRICING -> "€"; Destination.BASKET -> "🛒"; Destination.PROFILE -> "●" }, fontWeight = FontWeight.Bold) },
                        label = { Text(tr(language, key)) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("BG Prices", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = DeepLeaf)
                    Text(tr(language, "tagline"), style = MaterialTheme.typography.bodyMedium, color = DeepOrange)
                }
                Text("BETA", Modifier.background(Orange, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp), color = DeepOrange, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = { showLocationDialog = true }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)) {
                Text("📍")
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                    Text(locationLabel.ifBlank { tr(language, "choose_location") }, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(if (locationCoordinates.isNotBlank()) locationCoordinates + " · " + radius + " km" else tr(language, "location"), style = MaterialTheme.typography.labelSmall)
                }
                Text("⌄", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            when (destination) {
                Destination.CATEGORIES, Destination.PRICING -> {
                    CategoryRibbon(categories, selectedCategory, language, onSelect = { selectedCategory = it; selectedRetailer = null }, onMove = { from, to ->
                        if (to in categories.indices) {
                            val moved = categories.removeAt(from); categories.add(to, moved); selectedCategory = to
                            prefs.edit().putString("category_order", categories.joinToString("|") { it.key }).apply()
                        }
                    })
                    Spacer(Modifier.height(10.dp))
                    if (destination == Destination.CATEGORIES) {
                        CategoryContent(categories[selectedCategory], profileView, selectedRetailer, language, catalogMessage) { selectedRetailer = if (selectedRetailer == it) null else it }
                    } else {
                        PricingContent(categories[selectedCategory], selectedRetailer, language, locationLabel, catalogApi, locationCoordinates, radius)
                    }
                }
                Destination.BASKET -> BasketContent(
                    categories[selectedCategory], selectedRetailer, language, locationLabel, basketItems,
                    onAdd = { name -> scope.launch { database.basketDao().insert(BasketItemEntity(name = name, categoryCode = categories[selectedCategory].key)) } },
                    onQuantityChange = { id, delta -> scope.launch { database.basketDao().changeQuantity(id, delta) } },
                    onRemove = { id -> scope.launch { database.basketDao().delete(id) } }
                )
                Destination.PROFILE -> ProfileContent(profileView, language, onViewChange = { profileView = it }, onLanguageChange = { language = it }, onLocationClick = { showLocationDialog = true }, onAddClick = { addStatus = ""; showAddDialog = true })
            }
        }
    }

    if (showLocationDialog) {
        LocationDialog(
            language = language, initialLabel = locationLabel, initialCoordinates = locationCoordinates,
            initialRadius = radius, api = catalogApi, onCurrentLocation = {
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            onDismiss = { showLocationDialog = false },
            onSave = { label, coordinates, newRadius ->
                locationLabel = label
                locationCoordinates = coordinates
                radius = newRadius
                showLocationDialog = false
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(tr(language, "add_title")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("suggestion", "note", "opinion").forEach { key ->
                            FilterChip(selected = addType == key, onClick = { addType = key }, label = { Text(tr(language, key)) })
                        }
                    }
                    OutlinedTextField(value = addText, onValueChange = { addText = it }, label = { Text(tr(language, "describe") + tr(language, addType)) }, minLines = 3)
                    Text(tr(language, "review_note"), style = MaterialTheme.typography.bodySmall)
                    if (addStatus.isNotBlank()) Text(addStatus, color = if (addStatus == tr(language, "add_sent")) DeepLeaf else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { Button(onClick = {
                val api = catalogApi
                if (api == null) addStatus = tr(language, "catalog_config_missing")
                else scope.launch {
                    try {
                        val coords = locationCoordinates.split(",").mapNotNull { it.trim().toDoubleOrNull() }
                        api.submit(SubmissionRequestDto(
                            type = addType.uppercase(), content = addText.trim(),
                            locationLabel = locationLabel.takeIf { it.isNotBlank() },
                            latitude = coords.getOrNull(0), longitude = coords.getOrNull(1)
                        ))
                        addStatus = tr(language, "add_sent")
                        addText = ""
                    } catch (_: Exception) { addStatus = tr(language, "add_error") }
                }
            }, enabled = addText.isNotBlank()) { Text(tr(language, "submit")) } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text(tr(language, "cancel")) } }
        )
    }
}

@Composable
private fun CategoryRibbon(categories: List<PriceCategory>, selected: Int, language: String, onSelect: (Int) -> Unit, onMove: (Int, Int) -> Unit) {
    Text(tr(language, "categories").uppercase(), style = MaterialTheme.typography.labelLarge, color = DeepLeaf, fontWeight = FontWeight.Bold)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEachIndexed { index, category ->
                FilterChip(selected == index, onClick = { onSelect(index) }, label = { Text(categoryEmoji(category.key) + " " + category.title(language), maxLines = 1) })
            }
        }
        Column {
            TextButton(onClick = { onMove(selected, selected - 1) }, enabled = selected > 0, contentPadding = PaddingValues(2.dp)) { Text("←") }
            TextButton(onClick = { onMove(selected, selected + 1) }, enabled = selected < categories.lastIndex, contentPadding = PaddingValues(2.dp)) { Text("→") }
        }
    }
    Text(tr(language, "select_category"), style = MaterialTheme.typography.labelSmall, color = DeepOrange)
}

@Composable
private fun CategoryContent(category: PriceCategory, view: String, selectedRetailer: String?, language: String, emptyMessage: String, onRetailerSelect: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(categoryEmoji(category.key), style = MaterialTheme.typography.headlineMedium)
        Column {
            Text(category.title(language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(category.subtitle(language), style = MaterialTheme.typography.bodyMedium)
        }
    }
    Spacer(Modifier.height(10.dp))
if (category.retailers.isEmpty()) {
        Card(colors = CardDefaults.cardColors(containerColor = Orange)) {
            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Text(emptyMessage, color = DeepOrange)
            }
        }
    } else if (view == "Grid") {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
            items(category.retailers.chunked(2)) { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { retailer ->
                        Card(onClick = { onRetailerSelect(retailer) }, modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = if (selectedRetailer == retailer) Orange else Leaf)) {
                            Column(Modifier.padding(14.dp)) {
                                Text(retailer, fontWeight = FontWeight.Medium)
                                Text(if (selectedRetailer == retailer) tr(language, "selected") else tr(language, "view_prices"), color = DeepLeaf, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
            items(category.retailers.filter { selectedRetailer == null || it == selectedRetailer }) { retailer ->
                Card(onClick = { onRetailerSelect(retailer) }, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (selectedRetailer == retailer) Orange else Leaf)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(retailer, fontWeight = FontWeight.Medium)
                        Text(if (selectedRetailer == retailer) tr(language, "selected") else tr(language, "view_prices"), color = DeepLeaf)
                    }
                }
            }
        }
    }
}

@Composable
private fun PricingContent(
    category: PriceCategory,
    selectedRetailer: String?,
    language: String,
    location: String,
    api: CatalogApi?,
    coordinates: String,
    radius: Int
) {
    var priceItems by remember(category.key, selectedRetailer, location, coordinates) {
        mutableStateOf(emptyList<PriceCatalogItemDto>())
    }
    var loading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(category.key, selectedRetailer, language, location, coordinates, radius, api, refresh) {
        if (api == null) {
            priceItems = emptyList()
            failed = false
            return@LaunchedEffect
        }
        loading = true
        failed = false
        try {
            val parsed = coordinates.split(",").mapNotNull { it.trim().toDoubleOrNull() }
            val hasCoordinates = parsed.size == 2
            val ignoredLabels = listOf(
                tr(language, "nearby"),
                tr(language, "permission_denied"),
                tr(language, "location_unavailable")
            )
            val locality = location.takeIf { it.isNotBlank() && it !in ignoredLabels && !hasCoordinates }
            priceItems = api.getPriceCatalog(
                categoryCode = category.key,
                locality = locality,
                latitude = if (hasCoordinates) parsed[0] else null,
                longitude = if (hasCoordinates) parsed[1] else null,
                radiusKm = radius,
                retailerName = selectedRetailer,
                limit = 100
            )
        } catch (_: Exception) {
            priceItems = emptyList()
            failed = true
        } finally {
            loading = false
        }
    }

    Text(tr(language, "prices_for") + category.title(language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(tr(language, "prices_promos_history"), style = MaterialTheme.typography.bodyMedium)
    Text(tr(language, "location") + ": " + location.ifBlank { tr(language, "all_locations") }, style = MaterialTheme.typography.labelSmall, color = DeepOrange)
    Spacer(Modifier.height(8.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Orange)) {
        Column(Modifier.padding(12.dp)) {
            Text(if (selectedRetailer == null) tr(language, "all_stores") else tr(language, "selected_store") + selectedRetailer, fontWeight = FontWeight.Bold, color = DeepOrange)
            Text(tr(language, "choose_retailer_hint"))
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(tr(language, "offer_disclaimer"), style = MaterialTheme.typography.labelSmall, color = DeepOrange)
    Spacer(Modifier.height(8.dp))

    when {
        api == null -> Text(tr(language, "catalog_config_missing"), color = DeepOrange)
        loading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            Text(tr(language, "loading_prices"))
        }
        failed -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tr(language, "catalog_error"), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { refresh++ }) { Text(tr(language, "retry")) }
        }
        priceItems.isEmpty() -> Text(tr(language, "no_verified_prices"), color = DeepOrange)
        else -> LazyColumn(
            Modifier.fillMaxWidth().heightIn(max = 520.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(priceItems, key = { "${it.productId}-${it.retailerCode}-${it.city}-${it.branchName ?: ""}" }) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = Leaf)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            (item.brand?.let { "$it " } ?: "") + item.productName,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (item.priceScope == "NATIONAL_ONLINE") {
                                item.retailerName + " · " + tr(language, "online_bulgaria")
                            } else {
                                item.retailerName + " · " + (item.branchName?.takeIf { it.isNotBlank() }
                                    ?: item.settlement?.takeIf { it.isNotBlank() } ?: item.city.orEmpty())
                            },
                            color = DeepOrange,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (!item.address.isNullOrBlank()) {
                            Text(item.address, style = MaterialTheme.typography.labelSmall)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(String.format(Locale.US, "€ %.2f", item.priceEur), color = DeepLeaf, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            if (item.regularPriceEur != null && item.regularPriceEur > item.priceEur) {
                                Text(tr(language, "was") + " " + String.format(Locale.US, "€ %.2f", item.regularPriceEur), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (!item.priceNote.isNullOrBlank()) {
                            Text(item.priceNote, color = DeepOrange, style = MaterialTheme.typography.labelSmall)
                        }
                        Text(tr(language, "offer_source") + " · " + item.collectedAt.take(10), style = MaterialTheme.typography.labelSmall)
                        item.sourceReference?.takeIf { it.startsWith("https://") }?.let { source ->
                            Text(
                                tr(language, "source_details"),
                                color = DeepLeaf,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.clickable { runCatching { uriHandler.openUri(source) } }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun BasketContent(
    category: PriceCategory,
    selectedRetailer: String?,
    language: String,
    location: String,
    basketItems: List<BasketItemEntity>,
    onAdd: (String) -> Unit,
    onQuantityChange: (Long, Int) -> Unit,
    onRemove: (Long) -> Unit
) {
    var newItem by remember { mutableStateOf("") }
    Text(tr(language, "basket"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(tr(language, "basket_sub"), style = MaterialTheme.typography.bodyMedium)
    Text(tr(language, "location") + ": " + location.ifBlank { tr(language, "all_locations") }, style = MaterialTheme.typography.labelSmall, color = DeepOrange)
    Spacer(Modifier.height(8.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Orange)) {
        Column(Modifier.padding(12.dp)) {
            Text(tr(language, "current_category") + category.title(language), fontWeight = FontWeight.Bold, color = DeepOrange)
            Text(if (selectedRetailer == null) tr(language, "no_store_filter") else tr(language, "store_filter") + selectedRetailer)
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(tr(language, "lists"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = newItem, onValueChange = { newItem = it.take(160) },
            modifier = Modifier.weight(1f), singleLine = true,
            label = { Text(tr(language, "basket_item_placeholder")) }
        )
        Button(onClick = { onAdd(newItem.trim()); newItem = "" }, enabled = newItem.isNotBlank()) { Text("+") }
    }
    if (basketItems.isEmpty()) Text(tr(language, "basket_empty"), style = MaterialTheme.typography.bodySmall)
    else LazyColumn(Modifier.fillMaxWidth().heightIn(max = 230.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(basketItems, key = { it.id }) { item ->
            Card(colors = CardDefaults.cardColors(containerColor = Leaf)) {
                Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.SemiBold)
                        Text(item.quantity.toString() + " ×", style = MaterialTheme.typography.labelSmall, color = DeepOrange)
                    }
                    TextButton(onClick = { onQuantityChange(item.id, -1) }) { Text("−") }
                    Text(item.quantity.toString())
                    TextButton(onClick = { onQuantityChange(item.id, 1) }) { Text("+") }
                    TextButton(onClick = { onRemove(item.id) }) { Text("×", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
    Text(tr(language, "basket_local_note"), style = MaterialTheme.typography.labelSmall, color = DeepOrange)
    Spacer(Modifier.height(8.dp))
    Text(tr(language, "prices_by_store"), style = MaterialTheme.typography.labelLarge, color = DeepLeaf, fontWeight = FontWeight.Bold)
    Text(tr(language, "no_live"), style = MaterialTheme.typography.bodySmall)
    InfoCard(tr(language, "single_basket"), tr(language, "single_desc"))
    InfoCard(tr(language, "combined_basket"), tr(language, "combined_desc"))
    InfoCard(tr(language, "savings"), tr(language, "savings_desc"))
}
@Composable
private fun ProfileContent(view: String, language: String, onViewChange: (String) -> Unit, onLanguageChange: (String) -> Unit, onLocationClick: () -> Unit, onAddClick: () -> Unit) {
    Text(tr(language, "profile_settings"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(tr(language, "profile_sub"), style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(8.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Orange), onClick = onAddClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(tr(language, "opinion"), fontWeight = FontWeight.SemiBold)
                Text(tr(language, "suggestion") + " · " + tr(language, "note"), style = MaterialTheme.typography.bodySmall)
            }
            Text("›", color = DeepOrange)
        }
    }
    Spacer(Modifier.height(8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Leaf)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tr(language, "language"), fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(language == "bg", onClick = { onLanguageChange("bg") }, label = { Text(tr(language, "bulgarian")) })
                        FilterChip(language == "en", onClick = { onLanguageChange("en") }, label = { Text(tr(language, "english")) })
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Orange), onClick = onLocationClick) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(tr(language, "location_settings"), fontWeight = FontWeight.SemiBold)
                    Text("›", color = DeepOrange)
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Leaf)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tr(language, "display"), fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(view == "Grid", onClick = { onViewChange("Grid") }, label = { Text(tr(language, "grid")) })
                        FilterChip(view == "List", onClick = { onViewChange("List") }, label = { Text(tr(language, "list")) })
                    }
                }
            }
        }
        item { Text(tr(language, "profile_preview_note"), color = DeepOrange, style = MaterialTheme.typography.bodySmall) }
        items(listOf("account", "submissions", "moderation", "app_settings", "category_order")) { key ->
            InfoCard(tr(language, key), if (key == "account" || key == "submissions" || key == "moderation") tr(language, "profile_preview_note") else "")
        }
    }
}

@Composable
private fun LocationDialog(language: String, initialLabel: String, initialCoordinates: String, initialRadius: Int, api: CatalogApi?, onCurrentLocation: () -> Unit, onDismiss: () -> Unit, onSave: (String, String, Int) -> Unit) {
    var mode by remember { mutableStateOf(if (initialCoordinates.isNotBlank()) "coordinates" else "city") }
    var city by remember { mutableStateOf(if (initialLabel == tr(language, "nearby")) "" else initialLabel) }
    var coordinates by remember { mutableStateOf(initialCoordinates) }
    var radius by remember { mutableIntStateOf(initialRadius) }
    var error by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<LocalitySuggestionDto>>(emptyList()) }
    LaunchedEffect(city, mode, api) {
        if (mode != "city" || api == null || city.trim().length < 2) { suggestions = emptyList() }
        else {
            delay(350)
            suggestions = runCatching { api.searchLocalities(city.trim()) }.getOrDefault(emptyList())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr(language, "location_filter")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(mode == "city", onClick = { mode = "city"; error = "" }, label = { Text(tr(language, "city_town")) })
                    FilterChip(mode == "coordinates", onClick = { mode = "coordinates"; error = "" }, label = { Text(tr(language, "coordinates")) })
                }
                if (mode == "city") {
                    OutlinedTextField(value = city, onValueChange = { city = it; error = "" }, label = { Text(tr(language, "city_town")) }, placeholder = { Text(tr(language, "city_placeholder")) }, singleLine = true)
                    if (suggestions.isNotEmpty()) {
                        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 150.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            items(suggestions) { item ->
                                Column(Modifier.fillMaxWidth().clickable { city = item.nameBg?.takeIf { language == "bg" && it.isNotBlank() } ?: item.name; suggestions = emptyList() }.padding(8.dp)) {
                                    Text(item.nameBg?.takeIf { language == "bg" && it.isNotBlank() } ?: item.name, fontWeight = FontWeight.SemiBold)
                                    Text(listOf(item.type.lowercase(), item.municipality, item.region).filterNotNull().joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = DeepOrange)
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(value = coordinates, onValueChange = { coordinates = it; error = "" }, label = { Text(tr(language, "coordinates")) }, placeholder = { Text(tr(language, "coordinates_placeholder")) }, singleLine = true)
                }
                OutlinedButton(onClick = onCurrentLocation, modifier = Modifier.fillMaxWidth()) { Text(tr(language, "current_location")) }
                Text(tr(language, "radius"), fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf(5, 10, 25, 50).forEach { km ->
                        FilterChip(radius == km, onClick = { radius = km }, label = { Text(km.toString() + " km") })
                    }
                }
                Text(tr(language, "location_note"), style = MaterialTheme.typography.bodySmall, color = DeepOrange)
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (mode == "city" && city.isNotBlank()) onSave(city.trim(), "", radius)
                else {
                    val parts = coordinates.split(",").map { it.trim().toDoubleOrNull() }
                    val valid = parts.size == 2 && parts[0] != null && parts[1] != null &&
                        parts[0]!! in -90.0..90.0 && parts[1]!! in -180.0..180.0
                    if (valid) onSave(tr(language, "nearby"), String.format(java.util.Locale.US, "%.6f, %.6f", parts[0], parts[1]), radius)
                    else error = tr(language, "invalid_location")
                }
            }) { Text(tr(language, "save_location")) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr(language, "cancel")) } }
    )
}

@Composable
private fun InfoCard(title: String, description: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Leaf)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (description.isNotBlank()) Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

