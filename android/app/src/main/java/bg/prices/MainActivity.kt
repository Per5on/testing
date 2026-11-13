package bg.prices

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

private val Leaf = Color(0xFFEAF5E1)
private val DeepLeaf = Color(0xFF14532D)
private val Orange = Color(0xFFFF9800)
private val DeepOrange = Color(0xFFFF9800)
private val Paper = Color(0xFFFFFCF8)
private val BgPricesColors = lightColorScheme(
    primary = DeepLeaf, onPrimary = Color.White, secondary = Orange,
    onSecondary = DeepLeaf, tertiary = Color(0xFF6C8C63),
    background = Paper, surface = Color.White, surfaceVariant = Leaf,
    secondaryContainer = Orange, onSecondaryContainer = DeepLeaf
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
        "data_sources" to "Източници на данни",
        "display" to "Изглед на категориите", "grid" to "Мрежа", "list" to "Списък",
        "language" to "Език на приложението", "bulgarian" to "Български", "english" to "Английски",
        "account" to "Акаунт и вход", "submissions" to "Моите предложения",
        "moderation" to "Опашка за модерация", "app_settings" to "Настройки на приложението",
        "category_order" to "Подредба на категориите", "location_settings" to "Настройки на местоположението",
        "add_title" to "Добави в Пестеливко", "suggestion" to "Предложение", "note" to "Бележка",
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
        "catalog_config_missing" to "Каталогът ще се зареди след свързване с Пестеливко сървъра.",
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
        "data_sources" to "Data sources",
        "display" to "Category display", "grid" to "Grid", "list" to "List",
        "language" to "App language", "bulgarian" to "Bulgarian", "english" to "English",
        "account" to "Account and sign in", "submissions" to "My submissions",
        "moderation" to "Moderation queue", "app_settings" to "App settings",
        "category_order" to "Category display order", "location_settings" to "Location settings",
        "add_title" to "Add to Пестеливко", "suggestion" to "Suggestion", "note" to "Note",
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
        "catalog_config_missing" to "The catalogue will load once the Пестеливко server is connected.",
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
private data class ProductDto(val id: String, val categoryCode: String, val barcode: String? = null, val brand: String? = null, val name: String, val variant: String? = null, val packageQuantity: Double? = null, val packageUnit: String? = null)
private data class ProductPageDto(val items: List<ProductDto> = emptyList(), val total: Long = 0, val limit: Int = 30, val offset: Int = 0)
private data class CatalogCategoryDto(
    val code: String,
    val nameBg: String,
    val nameEn: String,
    val descriptionBg: String,
    val descriptionEn: String,
    val retailers: List<CatalogRetailerDto> = emptyList()
)
private data class CatalogRetailerDto(val code: String, val name: String, val websiteUrl: String? = null)
private data class LocalitySuggestionDto(val id: String, val name: String, val nameBg: String?, val type: String, val municipality: String?, val region: String?, val postalCode: String? = null)
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
    @GET("api/v1/products")
    suspend fun searchProducts(
        @Query("categoryCode") categoryCode: String,
        @Query("query") query: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): ProductPageDto

    @GET("api/v1/prices/catalog")
    suspend fun getPriceCatalog(
        @Query("categoryCode") categoryCode: String,
        @Query("locality") locality: String? = null,
        @Query("postalCode") postalCode: String? = null,
        @Query("retailerName") retailerName: String? = null,
        @Query("limit") limit: Int = 100
    ): List<PriceCatalogItemDto>

    @GET("api/v1/catalog")
    suspend fun getCatalog(
        @Query("locality") locality: String? = null,
        @Query("postalCode") postalCode: String? = null
    ): CatalogPayload

    @GET("api/v1/localities")
    suspend fun searchLocalities(@Query("query") query: String, @Query("mode") mode: String): List<LocalitySuggestionDto>

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
    var locationMode by remember { mutableStateOf(prefs.getString("location_mode", "locality") ?: "locality") }
    var postalCode by remember { mutableStateOf(prefs.getString("postal_code", "") ?: "") }
    val categories = remember { mutableStateListOf<PriceCategory>().apply { addAll(defaultCategories.map { it.copy(retailers = emptyList()) }) } }
    var catalogMessage by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableIntStateOf(0) }
    var destination by remember { mutableStateOf(Destination.CATEGORIES) }
    var selectedRetailer by remember { mutableStateOf<String?>(null) }
    val selectedProductIds = remember { mutableStateListOf<String>() }
    val appliedProductIds = remember { mutableStateListOf<String>() }
    var profileView by remember { mutableStateOf(prefs.getString("category_view", "Grid") ?: "Grid") }
    var showAddDialog by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf("suggestion") }
    var addText by remember { mutableStateOf("") }
    var addStatus by remember { mutableStateOf("") }

    LaunchedEffect(language, locationLabel, locationMode, postalCode, profileView) {
        prefs.edit()
            .putString("language", language)
            .putString("location_label", locationLabel)
            .putString("location_mode", locationMode)
            .putString("postal_code", postalCode)
            .putString("category_view", profileView)
            .apply()
    }

    LaunchedEffect(locationLabel, locationMode, postalCode, language) {
        val baseUrl = BuildConfig.API_BASE_URL.trim()
        if (catalogApi == null || baseUrl.isBlank()) {
            categories.indices.forEach { index -> categories[index] = categories[index].copy(retailers = emptyList()) }
            catalogMessage = tr(language, "catalog_config_missing")
        } else {
            try {
                val locality = locationLabel.takeIf { locationMode == "locality" && it.isNotBlank() }
                val selectedPostalCode = postalCode.takeIf { locationMode == "postalCode" && it.isNotBlank() }
                val response = catalogApi.getCatalog(locality = locality, postalCode = selectedPostalCode)
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
                catalogMessage = if (locality == null && selectedPostalCode == null) tr(language, "choose_location_first") else if (categories.none { it.retailers.isNotEmpty() }) tr(language, "no_stores_here") else ""
            } catch (_: Exception) {
                categories.replaceAll { it.copy(retailers = emptyList()) }
                catalogMessage = tr(language, "catalog_error")
            }
        }
    }

    Scaffold(
        containerColor = Paper,
        bottomBar = {
            NavigationBar(containerColor = Color.White, contentColor = DeepLeaf) {
                listOf(
                    Destination.CATEGORIES to "categories", Destination.PRICING to "pricing",
                    Destination.BASKET to "basket", Destination.PROFILE to "profile"
                ).forEach { (item, key) ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = DeepLeaf, selectedTextColor = DeepLeaf, indicatorColor = Orange, unselectedIconColor = DeepLeaf.copy(alpha = 0.60f), unselectedTextColor = DeepLeaf.copy(alpha = 0.70f)),
                        icon = { Text(when (item) { Destination.CATEGORIES -> "▦"; Destination.PRICING -> "€"; Destination.BASKET -> "🛒"; Destination.PROFILE -> "●" }, fontWeight = FontWeight.Bold) },
                        label = { Text(tr(language, key)) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(
                Modifier.fillMaxWidth().background(DeepLeaf).padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🐷  Пестеливко", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("BETA", Modifier.background(Orange, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp), color = DeepLeaf, fontWeight = FontWeight.Bold)
                }
                Text(tr(language, "tagline"), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.88f))
            }
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    LocationSelector(
                language = language, api = catalogApi, mode = locationMode,
                selectedLocality = locationLabel, selectedPostalCode = postalCode,
                onModeChange = { mode -> locationMode = mode; locationLabel = ""; postalCode = "" },
                onLocalitySelected = { name -> locationMode = "locality"; locationLabel = name; postalCode = "" },
                onPostalCodeSelected = { code -> locationMode = "postalCode"; postalCode = code; locationLabel = "" }
                    )
                }
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
                        CategoryContent(
                            categories[selectedCategory], profileView, selectedRetailer, language, catalogMessage, catalogApi,
                            selectedProductIds.toSet(),
                            onProductToggle = { id, checked -> if (checked) { if (id !in selectedProductIds) selectedProductIds.add(id) } else selectedProductIds.remove(id) },
                            onApplyProducts = { ids -> appliedProductIds.clear(); appliedProductIds.addAll(ids) },
                            appliedCount = appliedProductIds.size
                        ) { selectedRetailer = if (selectedRetailer == it) null else it }
                    } else {
                        PricingContent(
                            categories[selectedCategory], selectedRetailer, language,
                            if (locationMode == "postalCode") postalCode else locationLabel,
                            locationMode, postalCode, catalogApi, if (categories[selectedCategory].key == "groceries") appliedProductIds.toSet() else emptySet()
                        )
                    }
                }
                Destination.BASKET -> BasketContent(
                    categories[selectedCategory], selectedRetailer, language,
                    if (locationMode == "postalCode") postalCode else locationLabel, basketItems,
                    onAdd = { name -> scope.launch { database.basketDao().insert(BasketItemEntity(name = name, categoryCode = categories[selectedCategory].key)) } },
                    onQuantityChange = { id, delta -> scope.launch { database.basketDao().changeQuantity(id, delta) } },
                    onRemove = { id -> scope.launch { database.basketDao().delete(id) } }
                )
                Destination.PROFILE -> ProfileContent(profileView, language, onViewChange = { profileView = it }, onLanguageChange = { language = it }, onAddClick = { addStatus = ""; showAddDialog = true })
            }
            }
        }
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

                        api.submit(SubmissionRequestDto(
                            type = addType.uppercase(), content = addText.trim(),
                            locationLabel = locationLabel.takeIf { it.isNotBlank() },
                            latitude = null, longitude = null
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
}

@Composable
private fun CategoryContent(
    category: PriceCategory, view: String, selectedRetailer: String?, language: String, emptyMessage: String,
    api: CatalogApi?, selectedProductIds: Set<String>, onProductToggle: (String, Boolean) -> Unit,
    onApplyProducts: (Set<String>) -> Unit, appliedCount: Int, onRetailerSelect: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(categoryEmoji(category.key), style = MaterialTheme.typography.headlineMedium)
        Column {
            Text(category.title(language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(category.subtitle(language), style = MaterialTheme.typography.bodyMedium)
        }
    }
    Spacer(Modifier.height(10.dp))
    if (category.key == "groceries") {
        ProductMultiSelect(language, api, selectedProductIds, appliedCount, onProductToggle, onApplyProducts)
        Spacer(Modifier.height(10.dp))
    }
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
    locationMode: String,
    postalCode: String,
    api: CatalogApi?,
    appliedProductIds: Set<String>
) {
    var priceItems by remember(category.key, selectedRetailer, location, locationMode, postalCode) {
        mutableStateOf(emptyList<PriceCatalogItemDto>())
    }
    var loading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(category.key, selectedRetailer, language, location, locationMode, postalCode, api, appliedProductIds, refresh) {
        if (api == null) {
            priceItems = emptyList()
            failed = false
            return@LaunchedEffect
        }
        loading = true
        failed = false
        try {
            val locality = location.takeIf { locationMode == "locality" && it.isNotBlank() }
            val selectedPostalCode = postalCode.takeIf { locationMode == "postalCode" && it.isNotBlank() }
            val loadedPrices = api.getPriceCatalog(
                categoryCode = category.key, locality = locality, postalCode = selectedPostalCode,
                retailerName = selectedRetailer, limit = 100
            )
            priceItems = if (appliedProductIds.isEmpty()) loadedPrices else loadedPrices.filter { it.productId in appliedProductIds }
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
private fun ProfileContent(view: String, language: String, onViewChange: (String) -> Unit, onLanguageChange: (String) -> Unit, onAddClick: () -> Unit) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSelector(
    language: String, api: CatalogApi?, mode: String, selectedLocality: String, selectedPostalCode: String,
    onModeChange: (String) -> Unit, onLocalitySelected: (String) -> Unit, onPostalCodeSelected: (String) -> Unit
) {
    var query by remember(mode, selectedLocality, selectedPostalCode) { mutableStateOf(if (mode == "postalCode") selectedPostalCode else selectedLocality) }
    var suggestions by remember { mutableStateOf<List<LocalitySuggestionDto>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(query, mode, api) {
        val term = query.trim()
        val minimum = if (mode == "postalCode") 1 else 2
        if (api == null || term.length < minimum) { suggestions = emptyList(); loading = false; failed = false }
        else { delay(250); loading = true; failed = false
            try { suggestions = api.searchLocalities(term, mode) }
            catch (_: Exception) { suggestions = emptyList(); failed = true }
            finally { loading = false }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = mode == "locality", onClick = { onModeChange("locality"); query = ""; expanded = false }, label = { Text(if (language == "bg") "Населено място" else "Settlement") })
            FilterChip(selected = mode == "postalCode", onClick = { onModeChange("postalCode"); query = ""; expanded = false }, label = { Text(if (language == "bg") "Пощ. код" else "Postal code") })
        }
        ExposedDropdownMenuBox(expanded = expanded && suggestions.isNotEmpty(), onExpandedChange = { expanded = it }) {
            OutlinedTextField(value = query, onValueChange = { query = it.take(if (mode == "postalCode") 4 else 100); expanded = true },
                modifier = Modifier.menuAnchor().fillMaxWidth(), singleLine = true,
                placeholder = { Text(if (mode == "postalCode") { if (language == "bg") "Въведи пощенски код" else "Enter postal code" } else { if (language == "bg") "Търсете населено място..." else "Search settlement..." }) },
                trailingIcon = { IconButton(onClick = { expanded = !expanded }) { Text(if (expanded) "⌃" else "⌄", color = DeepLeaf, fontWeight = FontWeight.Bold) } }
            )
            ExposedDropdownMenu(expanded = expanded && suggestions.isNotEmpty(), onDismissRequest = { expanded = false }) {
                suggestions.forEach { item ->
                    val label = if (mode == "postalCode") listOfNotNull(item.postalCode, item.nameBg ?: item.name).joinToString(" · ") else item.nameBg?.takeIf { language == "bg" && it.isNotBlank() } ?: item.name
                    DropdownMenuItem(text = { Column { Text(label, fontWeight = FontWeight.SemiBold); val detail = listOf(item.type.takeIf { it.isNotBlank() }, item.municipality, item.region).filterNotNull().joinToString(" · "); if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.labelSmall, color = DeepOrange) } },
                        onClick = { if (mode == "postalCode") { item.postalCode?.let(onPostalCodeSelected); query = item.postalCode.orEmpty() } else { onLocalitySelected(label); query = label }; expanded = false; suggestions = emptyList() })
                }
            }
        }
        when {
            loading -> Text(if (language == "bg") "Търсене..." else "Searching...", color = DeepOrange, style = MaterialTheme.typography.labelSmall)
            failed -> Text(if (language == "bg") "Неуспешно зареждане. Провери връзката." else "Could not load suggestions. Check your connection.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            expanded && query.isNotBlank() && suggestions.isEmpty() -> Text(if (language == "bg") "Няма намерени предложения." else "No suggestions found.", color = DeepOrange, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ProductMultiSelect(language: String, api: CatalogApi?, selectedIds: Set<String>, appliedCount: Int, onToggle: (String, Boolean) -> Unit, onApply: (Set<String>) -> Unit) {
    var query by remember { mutableStateOf("") }
    var products by remember { mutableStateOf<List<ProductDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(api, query) {
        if (api == null) { products = emptyList(); return@LaunchedEffect }
        if (query.isNotBlank() && query.trim().length < 2) { products = emptyList(); return@LaunchedEffect }
        delay(220); loading = true; failed = false
        try { products = api.searchProducts("groceries", query.trim().takeIf { it.isNotBlank() }, 60, 0).items }
        catch (_: Exception) { products = emptyList(); failed = true }
        finally { loading = false }
    }
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text(if (language == "bg") "Търси продукт..." else "Search products...") })
            when {
                loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                failed -> Text(if (language == "bg") "Продуктите не се заредиха." else "Products could not be loaded.", color = MaterialTheme.colorScheme.error)
                products.isEmpty() -> Text(if (language == "bg") "Няма налични продукти в каталога за този филтър." else "No products are available in the catalogue for this filter.", color = DeepOrange)
                else -> Column(verticalArrangement = Arrangement.spacedBy(2.dp)) { products.forEach { product ->
                    Row(Modifier.fillMaxWidth().clickable { onToggle(product.id, product.id !in selectedIds) }.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = product.id in selectedIds, onCheckedChange = { onToggle(product.id, it) })
                        Column(Modifier.weight(1f)) { Text((product.brand?.let { "$it " } ?: "") + product.name, fontWeight = FontWeight.Medium); product.variant?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = DeepOrange) } }
                    }
                } }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { selectedIds.forEach { onToggle(it, false) }; onApply(emptySet()) }) { Text(if (language == "bg") "Изчисти избора" else "Clear selection") }
                Button(onClick = { onApply(selectedIds) }) { Text(if (language == "bg") "Приложи (${selectedIds.size})" else "Apply (${selectedIds.size})") }
            }
            if (appliedCount > 0) Text(if (language == "bg") "Активен филтър: $appliedCount продукта" else "Active filter: $appliedCount products", style = MaterialTheme.typography.labelSmall, color = DeepLeaf)
        }
    }
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

