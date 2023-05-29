package ru.mirea.shumikhin.timeservice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mirea.shumikhin.timeservice.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.IOException
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private val TAG = this::class.simpleName
    private lateinit var binding: ActivityMainBinding

    private val host = "time-a.nist.gov"
    private val port = 13

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnGetDate.setOnClickListener {
            lifecycleScope.launch {
                updateDate()
            }
        }
    }

    private suspend fun getTimeTask() = withContext(Dispatchers.IO) {
        var timeResult = ""
        try {
            val socket = Socket(host, port)
            val reader: BufferedReader = SocketUtils.getReader(socket)
            reader.readLine() // игнорируем первую строку
            timeResult = reader.readLine() // считываем вторую строку
            Log.d(TAG, timeResult)
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return@withContext timeResult
    }
    private suspend fun updateDate() {
        val result = getTimeTask()
        binding.tvDate.text = result
    }
}