package ru.mirea.shumikhin.httpurlconnection

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import ru.mirea.shumikhin.httpurlconnection.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bthSendRequest.setOnClickListener(this::onClick)
    }

    fun onClick(view: View) {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var networkinfo: NetworkInfo? = null
        if (connectivityManager != null) {
            networkinfo = connectivityManager.activeNetworkInfo
        }
        if (networkinfo != null && networkinfo.isConnected) {
            lifecycleScope.launch {
                val result = downloadPageTask("https://ipinfo.io/json") ?: return@launch
                downloadWeatherTask("https://api.open-meteo.com/v1/forecast?latitude=${result.latitude}&longitude=${result.longitude}&current_weather=true")
            }
        } else {
            Toast.makeText(this, "Нет интернета", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun downloadPageTask(url: String): Coordinates? {
        binding.tvLoadState.text = "Загружаем..."
        val result = doInBackground(url)
        result?.let {
            val responseJson = onPostExecute(it)
            val ip = responseJson.getString("ip")
            binding.tvCity.text = responseJson.getString("city")
            binding.tvRegion.text = responseJson.getString("region")
            binding.tvTimeZone.text = responseJson.getString("timezone")
            Log.d(
                MainActivity::class.java.simpleName,
                "IP: $ip"
            )
            val latlng = responseJson.getString("loc").split(",")
            return Coordinates(
                latlng[0],
                latlng[1]
            )
        }
        return null
    }

    private suspend fun doInBackground(url: String) = withContext(Dispatchers.IO) {
        return@withContext try {
            downloadIpInfo(url)
        } catch (e: IOException) {
            e.printStackTrace()
            "error"
        }
    }
    private suspend fun onPostExecute(result: String) = withContext(Dispatchers.Main) {
        Log.d(MainActivity::class.java.simpleName, result)
        try {
            val responseJson = JSONObject(result)
            Log.d(
                MainActivity::class.java.simpleName,
                "Response: $responseJson"
            )
            return@withContext responseJson
        } catch (e: JSONException) {
            e.printStackTrace()
            JSONObject()
        }
    }

    @Throws(IOException::class)
    private fun downloadIpInfo(address: String): String? {
        var inputStream: InputStream? = null
        var data = ""
        try {
            val url = URL(address)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.readTimeout = 1000000
            connection.connectTimeout = 1000000
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = true
            connection.useCaches = false
            connection.doInput = true
            val responseCode: Int = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.inputStream
                val bos = ByteArrayOutputStream()
                var read = 0
                while (inputStream.read().also { read = it } != -1) {
                    bos.write(read)
                }
                bos.close()
                data = bos.toString()
            } else {
                data = connection.responseMessage + ". Error Code: " + responseCode
            }
            connection.disconnect()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return data
    }

    private suspend fun downloadWeatherTask(url: String) {
        binding.tvWeather.text = "Загружаем погоду..."
        val result = doInBackground(url)
        result?.let {
            val responseJson = onPostExecute(it)
            binding.tvWeather.text = responseJson.getJSONObject("current_weather").toString()
            binding.tvLoadState.text = "Done"
        }
    }

    private class Coordinates(
        val latitude: String,
        val longitude: String
    )
}