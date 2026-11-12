package bg.prices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Leaf = Color(0xFFE5F3E5)
private val DeepLeaf = Color(0xFF356B43)
private val Orange = Color(0xFFFFE8D2)
private val DeepOrange = Color(0xFF9A541D)
private val Paper = Color(0xFFFFFCF8)

private val BgPricesColors = lightColorScheme(
    primary = DeepLeaf,
    onPrimary = Color.White,
    secondary = DeepOrange,
    onSecondary = Color.White,
    tertiary = Color(0xFF6C8C63),
    background = Paper,
    surface = Color.White,
    surfaceVariant = Leaf,
    secondaryContainer = Orange,
    onSecondaryContainer = DeepOrange
)

private data class PriceCategory(
    val title: String,
    val subtitle: String,
    val retailers: List<String>
)

private val defaultCategories = listOf(
    PriceCategory("Groceries", "Everyday products and grocery baskets", listOf("Kaufland", "Lidl", "BILLA", "Fantastico", "T MARKET", "METRO", "Моят магазин", "CBA", "ProMarket", "HIT Max", "Burlex", "Klasiko", "ABC Market", "Mini Mart")),
    PriceCategory("Technology", "Devices and electronics", listOf("Technopolis", "Technomarket", "Zora", "Ardes", "Plesio", "Plasico", "Vali", "Jar Computers")),
    PriceCategory("Furniture & home", "Furniture, DIY and home products", listOf("HomeMax", "Bauhaus", "IKEA", "JYSK", "Mr. Bricolage", "Praktiker", "Praktis", "Videnov", "Mondo")),
    PriceCategory("Petrol stations", "Fuel prices by type and location", listOf("Fuel station listings will appear when verified data is available"))
)

private enum class Destination(val label: String, val symbol: String) {
    CATEGORIES("Categories", "▦"), PRICING("Pricing", "€"), ADD("Add", "+"),
    BASKET("Basket", "🛒"), USER("Profile", "●")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = BgPricesColors) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    BgPricesApp()
                }
            }
        }
    }
}

@Composable
private fun BgPricesApp() {
    val categories = remember { mutableStateListOf<PriceCategory>().apply { addAll(defaultCategories) } }
    var selectedCategory by remember { mutableStateOf(0) }
    var destination by remember { mutableStateOf(Destination.CATEGORIES) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf("Suggestion") }
    var addText by remember { mutableStateOf("") }
    var profileView by remember { mutableStateOf("Grid") }
    var selectedRetailer by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Paper,
        bottomBar = {
            NavigationBar(containerColor = Leaf) {
                Destination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = {
                            if (item == Destination.ADD) showAddDialog = true else destination = item
                        },
                        icon = { Text(item.symbol, fontWeight = FontWeight.Bold) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("BG Prices", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = DeepLeaf)
                    Text("Compare prices in EUR", style = MaterialTheme.typography.bodyMedium, color = DeepOrange)
                }
                Text("BETA", modifier = Modifier.background(Orange, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp), color = DeepOrange, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            when (destination) {
                Destination.CATEGORIES, Destination.PRICING -> {
                    CategoryRibbon(categories, selectedCategory, onSelect = { selectedCategory = it; selectedRetailer = null }, onMove = { from, to ->
                        if (to in categories.indices) {
                            val moved = categories.removeAt(from)
                            categories.add(to, moved)
                            selectedCategory = to
                        }
                    })
                    Spacer(Modifier.height(14.dp))
                    if (destination == Destination.CATEGORIES) {
                        CategoryContent(categories[selectedCategory], profileView, selectedRetailer, onRetailerSelect = { selectedRetailer = if (selectedRetailer == it) null else it })
                    } else {
                        PricingContent(categories[selectedCategory], selectedRetailer)
                    }
                }
                Destination.BASKET -> BasketContent(categories[selectedCategory], selectedRetailer)
                Destination.USER -> UserContent(profileView, onViewChange = { profileView = it })
                Destination.ADD -> Unit
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add to BG Prices") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Suggestion", "Note", "Feedback").forEach { type ->
                            FilterChip(selected = addType == type, onClick = { addType = type }, label = { Text(type) })
                        }
                    }
                    OutlinedTextField(value = addText, onValueChange = { addText = it }, label = { Text("Describe your $addType") }, minLines = 3)
                    Text("Submissions will be reviewed before appearing in the catalogue.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { Button(onClick = { showAddDialog = false; addText = "" }, enabled = addText.isNotBlank()) { Text("Submit") } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CategoryRibbon(
    categories: List<PriceCategory>,
    selected: Int,
    onSelect: (Int) -> Unit,
    onMove: (Int, Int) -> Unit
) {
    Text("CATEGORIES", style = MaterialTheme.typography.labelLarge, color = DeepLeaf, fontWeight = FontWeight.Bold)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Row(modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEachIndexed { index, category ->
                FilterChip(selected = selected == index, onClick = { onSelect(index) }, label = { Text(category.title, maxLines = 1) })
            }
        }
        Column {
            TextButton(onClick = { onMove(selected, selected - 1) }, enabled = selected > 0, contentPadding = PaddingValues(2.dp)) { Text("←") }
            TextButton(onClick = { onMove(selected, selected + 1) }, enabled = selected < categories.lastIndex, contentPadding = PaddingValues(2.dp)) { Text("→") }
        }
    }
    Text("Select a category; use arrows to change its position.", style = MaterialTheme.typography.labelSmall, color = DeepOrange)
}

@Composable
private fun CategoryContent(category: PriceCategory, view: String, selectedRetailer: String?, onRetailerSelect: (String) -> Unit) {
    Text(category.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(category.subtitle, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(12.dp))
    if (view == "Grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            category.retailers.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { retailer ->
                        Card(modifier = Modifier.weight(1f), onClick = { onRetailerSelect(retailer) }, colors = CardDefaults.cardColors(containerColor = if (selectedRetailer == retailer) Orange else Leaf)) {
                            Column(Modifier.padding(14.dp)) {
                                Text(retailer, fontWeight = FontWeight.Medium)
                                Text(if (selectedRetailer == retailer) "Selected" else "View prices  ›", color = DeepLeaf, style = MaterialTheme.typography.labelSmall)
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
                Card(modifier = Modifier.fillMaxWidth(), onClick = { onRetailerSelect(retailer) }, colors = CardDefaults.cardColors(containerColor = if (selectedRetailer == retailer) Orange else Leaf)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(retailer, fontWeight = FontWeight.Medium)
                        Text(if (selectedRetailer == retailer) "Selected" else "View prices  ›", color = DeepLeaf)
                    }
                }
            }
        }
    }
}

@Composable
private fun PricingContent(category: PriceCategory, selectedRetailer: String?) {
    Text("Pricing · ${category.title}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("Prices, promotions and price history", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(10.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Orange)) {
        Column(Modifier.padding(14.dp)) {
            Text(if (selectedRetailer == null) "All stores comparison" else "Selected store: $selectedRetailer", fontWeight = FontWeight.Bold, color = DeepOrange)
            Text("Choose a retailer in Categories to narrow this view. With no retailer selected, prices from all available stores will be compared.")
        }
    }
    Spacer(Modifier.height(10.dp))
    Text("STORE PRICE OVERVIEW", style = MaterialTheme.typography.labelLarge, color = DeepLeaf, fontWeight = FontWeight.Bold)
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(category.retailers.filter { selectedRetailer == null || it == selectedRetailer }) { retailer ->
            Card(colors = CardDefaults.cardColors(containerColor = Leaf)) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(retailer, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Price data pending", color = DeepOrange)
                        Text("EUR", color = DeepLeaf, fontWeight = FontWeight.Bold)
                    }
                    Text("No verified live prices yet", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
    Text("Demo layout only · no live retailer prices are displayed.", style = MaterialTheme.typography.labelSmall, color = DeepOrange)
}

@Composable
private fun BasketContent(category: PriceCategory, selectedRetailer: String?) {
    Text("Basket", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("Lists · comparisons · savings", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(8.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Orange)) {
        Column(Modifier.padding(12.dp)) {
            Text("Current category: ${category.title}", fontWeight = FontWeight.Bold, color = DeepOrange)
            Text(if (selectedRetailer == null) "No store selected in Categories: showing the category's full store comparison." else "Store filter from Categories: $selectedRetailer")
        }
    }
    Spacer(Modifier.height(10.dp))
    Text("PRICES BY STORE", style = MaterialTheme.typography.labelLarge, color = DeepLeaf, fontWeight = FontWeight.Bold)
    LazyColumn(modifier = Modifier.fillMaxWidth().height(180.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(category.retailers.filter { selectedRetailer == null || it == selectedRetailer }) { retailer ->
            Card(colors = CardDefaults.cardColors(containerColor = Leaf)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(retailer, fontWeight = FontWeight.SemiBold)
                    Text("Price data pending · EUR", color = DeepOrange, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    InfoCard("Your shopping lists", "Create and manage product lists for your next shop.")
    InfoCard("Cheapest single-store basket", "Compare your basket total at every store in the selected category.")
    InfoCard("Cheapest combined basket", "Compare a basket split across stores in the selected category.")
    InfoCard("Savings overview", "Estimated savings will appear when verified prices are available.")
    Text("Demo layout only · no live prices or calculated savings yet.", style = MaterialTheme.typography.labelSmall, color = DeepOrange)
}

@Composable
private fun UserContent(view: String, onViewChange: (String) -> Unit) {
    Text("User", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("Account · submissions · moderation · settings", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = view == "Grid", onClick = { onViewChange("Grid") }, label = { Text("Grid view") })
        FilterChip(selected = view == "List", onClick = { onViewChange("List") }, label = { Text("List view") })
    }
    val options = listOf("Account and sign in", "My submissions", "Moderation queue", "App settings", "Category display order")
    if (view == "Grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { option -> Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = if (option.contains("Moderation")) Orange else Leaf)) { Text(option, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Medium) } }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { InfoCard(it, "Open $it settings and details.") }
        }
    }
}

@Composable
private fun InfoCard(title: String, description: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Leaf)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
    Spacer(Modifier.height(8.dp))
}
