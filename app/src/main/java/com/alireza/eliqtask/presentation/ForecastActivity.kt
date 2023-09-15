package com.alireza.eliqtask.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alireza.eliqtask.data.local.entity.ViewPattern
import com.alireza.eliqtask.domian.model.weather.CurrentWeather
import com.alireza.eliqtask.domian.model.weather.Weather
import com.alireza.eliqtask.presentation.forecast.currentWeather.CurrentWeather
import com.alireza.eliqtask.presentation.ui.theme.EliqTaskTheme
import com.alireza.eliqtask.presentation.weather.WeatherViewModel
import com.alireza.eliqtask.presentation.weather.WeatherViewState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForecastActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EliqTaskTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Home()
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

@Composable
fun Home(modifier: Modifier = Modifier, viewModel: WeatherViewModel = hiltViewModel()) {

    val uiState: WeatherViewState by viewModel.uiWeatherState.collectAsStateWithLifecycle()
    when (uiState) {
        is WeatherViewState.WeatherData -> WeatherList(
            modifier = modifier,
            weather = (uiState as WeatherViewState.WeatherData)
        )

        else -> {}
    }

}

@Composable
fun WeatherList(modifier: Modifier=Modifier, weather: WeatherViewState.WeatherData){
    LazyColumn(modifier = modifier.fillMaxHeight().padding(16.dp)) {
        items(items = weather.uiPattern.pattern){item -> when(item.type){
            ViewPattern.CurrentWeather -> weather.weather.currentWeather?.let {
                CurrentWeather(
                    currentWeather = it
                )
            }
           else -> {}
        }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EliqTaskTheme {
        Greeting("Android")
    }
}