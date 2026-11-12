package bg.prices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AssistChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class PriceCategory(
    val title: String,
    val subtitle: String,
    val retailers: List<String>
)

private val categories = listOf(
    PriceCategory(
        "Groceries",
        "Compare everyday products and baskets",
        listOf("Kaufland", "Lidl", "BILLA", "Fantastico", "T MARKET", "METRO", "Моят магазин", "CBA", "ProMarket", "HIT Max", "Burlex", "Klasiko", "ABC Market", "Mini Mart")
    ),
    PriceCategory(
        "Technology",
        "Compare matching devices and electronics",
        listOf("Technopolis", "Technomarket", "Zora", "Ardes", "Plesio", "Plasico", "Vali", "Jar Computers")
    ),
    PriceCategory(
        "Furniture & home improvement",
        "Compare furniture, DIY and home products",
        listOf("HomeMax", "Bauhaus", "IKEA", "JYSK", "Mr. Bricolage", "Praktiker", "Praktis", "Videnov", "Mondo")
    ),
    PriceCategory(
        "Petrol stations",
        "Compare fuel prices by fuel type and location",
        listOf("Fuel price listings will be added in the next data milestone")
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
private fun HomeScreen() {
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("BG Prices", style = MaterialTheme.typography.headlineLarge)
        Text("Price comparison for Bulgaria · EUR", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(18.dp))

        Text("Categories", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { selectedCategory = category },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedCategory == category) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(category.title, style = MaterialTheme.typography.titleMedium)
                        Text(category.subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Text(selectedCategory.title, style = MaterialTheme.typography.titleLarge)
        Text(selectedCategory.subtitle, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(selectedCategory.retailers) { retailer ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    AssistChip(onClick = { }, label = { Text(retailer) })
                }
            }
        }
        Text("Testing prototype · retailer prices are not live yet", style = MaterialTheme.typography.labelSmall)
    }
}
