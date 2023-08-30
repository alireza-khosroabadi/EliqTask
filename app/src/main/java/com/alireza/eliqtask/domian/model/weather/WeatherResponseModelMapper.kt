package com.alireza.eliqtask.domian.model.weather

import com.alireza.eliqtask.base.constant.DEGREE_SYMBOL
import com.alireza.eliqtask.data.remote.entity.weather.CurrentWeatherResponse
import com.alireza.eliqtask.data.remote.entity.weather.DailyResponse
import com.alireza.eliqtask.data.remote.entity.weather.DailyUnitsResponse
import com.alireza.eliqtask.data.remote.entity.weather.HourlyResponse
import com.alireza.eliqtask.data.remote.entity.weather.HourlyUnitsResponse
import com.alireza.eliqtask.data.remote.entity.weather.WeatherResponse
import com.alireza.eliqtask.utils.extention.toFormattedDate
import com.alireza.eliqtask.utils.extention.toFormattedDateTime
import com.alireza.eliqtask.utils.extention.toFormattedTime
import java.util.Calendar
import java.util.Date

class WeatherResponseModelMapper(private val weatherResponse: WeatherResponse) {
    private val currentWeather: CurrentWeather by lazy {
        generateCurrentWeather(
            weatherResponse.timezone?:"",
            weatherResponse.currentWeather,
            weatherResponse.hourlyUnits
        )
    }
    private val daily: Daily
    private val hourly: Hourly

    init {
        daily = generateDaily(weatherResponse.daily, weatherResponse.dailyUnits)
        hourly = generateHourly(weatherResponse.currentWeather?.time,weatherResponse.hourly, weatherResponse.hourlyUnits)
    }

    fun toDomainModel(): Weather {
        return Weather(
            time = weatherResponse.generationTimeMs,
            utcOffsetSeconds = weatherResponse.utcOffsetSeconds,
            currentWeather = currentWeather,
            daily = daily,
            hourly = hourly
        )
    }


    private fun generateCurrentWeather(
        location: String,
        weatherResponse: CurrentWeatherResponse?,
        units: HourlyUnitsResponse?
    ): CurrentWeather =
        CurrentWeather(
            temperature = "${weatherResponse?.temperature}$DEGREE_SYMBOL",
            windSpeed = "${weatherResponse?.windspeed} ${units?.windspeed10m}",
            windDirection = weatherResponse?.winddirection,
            weatherCode = weatherStatusFromInt(weatherResponse?.weathercode),
            isDay = weatherResponse?.isDay,
            time = weatherResponse?.time.toFormattedDateTime(),
            location = location
        )

    private fun generateHourly(
        date: Date?,
        hourly: HourlyResponse?,
        hourlyUnits: HourlyUnitsResponse?
    ): Hourly {
        val todayDay = today()
        val hourlyList = hourly?.time?.map { date -> getTodayDate(date) }
            ?.filter { it.get(Calendar.DAY_OF_YEAR) == todayDay }?.mapIndexed { index, time ->
            HourlyData(
                time = time.time.toFormattedTime(),
                temperature2m = "${hourly.temperature_2m[index]}$DEGREE_SYMBOL",
                weatherCode = weatherStatusFromInt(hourly.weathercode[index]),
                precipitationProbability = "${hourly.precipitationProbability[index]} ${hourlyUnits?.precipitationProbability}"
            ).apply {
                isDay = if (time.get(Calendar.HOUR_OF_DAY) < 6 || time.get(Calendar.HOUR_OF_DAY) > 18) 1 else 0
            }
        } ?: listOf()
        return Hourly(
            timeUnit = date.toFormattedDate(),
            data = hourlyList
        )
    }

    private fun getTodayDate(date: Date):Calendar =
        Calendar.getInstance().apply { time = date }

    private fun today(): Int {
        val today = Calendar.getInstance()
        val todayDay = today.get(Calendar.DAY_OF_YEAR)
        return todayDay
    }

    private fun generateDaily(daily: DailyResponse?, dailyUnits: DailyUnitsResponse?): Daily {
        val dailyList = daily?.time?.mapIndexed { index, time ->
            DailyData(
                time = time,
                weatherCode = weatherStatusFromInt(daily.weathercode[index]),
                temperature = "${daily.temperature_2m_min[index]}/${daily.temperature_2m_max[index]}${dailyUnits?.temperature2mMax}",
                windSpeed10mMax = "${daily.windspeed10mMax[index]} ${dailyUnits?.windspeed10mMax}"
            )
        } ?: listOf()
        return Daily(
            timeUnit = dailyUnits?.time ?: "",
            data = dailyList
        )
    }


}