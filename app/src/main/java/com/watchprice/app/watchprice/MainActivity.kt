package com.watchprice.app.watchprice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.watchprice.app.watchprice.ui.theme.WatchPriceTheme
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class Prices(
    val cardPrice: Int,
    val price: Int,
    val originalPrice: Int,
)

fun extractPrices(inputString: String): Prices? {
    val (cardPrice, price, originalPrice) = listOf(
        "cardPrice",
        "price",
        "originalPrice"
    ).map {
        Regex("\"$it\\\\\":\\\\\"(\\d+).*₽\\\\\"")
            .find(inputString)
            ?.groupValues?.get(1)
            ?.toInt() ?: return null
    }
    return Prices(cardPrice, price, originalPrice)
}

class GreetingModel : ViewModel() {
    private val httpClient = HttpClient()
    private val prices = MutableStateFlow<Prices?>(null)
    val text: Flow<String> = prices.map { it.toString() }

    init {
        get()
    }

    fun get() {
        val productUrl =
//            "/product/avtomaticheskaya-kofemashina-inhouse-rozhkovaya-coffee-arte-icm1507-seryy-397529235/"
            "/product/stiralnyy-poroshok-tide-avtomat-2v1-lenor-color-20-stirok-3-kg-7436146/"
        val url = "https://www.ozon.ru/api/composer-api.bx/page/json/v2?url=$productUrl"
        viewModelScope.launch {
            val stringResponse = httpClient.get<String>(url)
            prices.value = extractPrices(stringResponse)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WatchPriceTheme {
                // A surface container using the 'background' color from the theme
                val viewModel = viewModel<GreetingModel>()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val text = viewModel.text.collectAsState("loading")
                    Greeting(text.value)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WatchPriceTheme {
        Greeting("Android")
    }
}