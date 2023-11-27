package com.example.seld.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.seld.R
import com.example.seld.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.fragment_home.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException
import java.util.*
import okhttp3.RequestBody
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import kotlinx.coroutines.*
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

class HomeFragment : Fragment() {
    private var timer: Timer? = null
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        spinner()
        //таймер для отслеживания инфы с сервера
        var sharedPreferences = requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val key = "key"
        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if(sharedPreferences.contains(key)) {
                    trackChanges()
                }
            }
       }, 0, 2000)
        //анимация фоток открытия-закрытия
        val imageClose= binding.imageClose
        val imageOpen= binding.imageOpen
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.image_click_animation)
        imageClose.setOnClickListener {
            imageClose.startAnimation(animation)
            openclose()
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            val paint = Paint()
            paint.colorFilter = ColorMatrixColorFilter(matrix)
            imageClose.setColorFilter(ColorMatrixColorFilter(matrix))
            imageOpen.setColorFilter(ColorMatrixColorFilter(matrix))
            imageClose.isEnabled = false
            imageOpen.isEnabled = false
            Handler().postDelayed({
                imageClose.setColorFilter(null)
                imageOpen.setColorFilter(null)
                imageOpen.isEnabled = true
                imageClose.isEnabled = true
            }, 3000)
        }
        imageOpen.setOnClickListener {
            imageOpen.startAnimation(animation)
            openclose()
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            val paint = Paint()
            paint.colorFilter = ColorMatrixColorFilter(matrix)
            imageClose.setColorFilter(ColorMatrixColorFilter(matrix))
            imageOpen.setColorFilter(ColorMatrixColorFilter(matrix))
            imageOpen.isEnabled = false
            imageClose.isEnabled = false
            Handler().postDelayed({
                imageClose.setColorFilter(null)
                imageOpen.setColorFilter(null)
                imageOpen.isEnabled = true
                imageClose.isEnabled = true
            }, 3000)
        }
        return root
    }
    //открытие-закрытие замка
    fun openclose(){
        val newData = getres()
        var sharedPreferences = requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        var Token = sharedPreferences.getString("key", "default_value").toString()
        Token="Token $Token"
        val number: Int = sharedPreferences.getString("number", "default_value")?.toIntOrNull() ?: 0
        val jsonArray = JSONArray(newData)
        val jsonObject = jsonArray.getJSONObject(number)
        val serial_num = jsonObject.getString("serial_num")
        val status = jsonObject.getString("status")
        val url = "https://seld-lock.ru/api/v1.0/devices/$serial_num/"
        val client = OkHttpClient()
        var status1=""
        if(status=="Open") {
            status1="Close"
        }
        else if(status=="Close") {
            status1="Open"
        }
        val requestBody = RequestBody.create( "application/json"?.toMediaTypeOrNull(), "{\"status\":\"$status1\"}")
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", Token)
            .patch(requestBody)
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                println(responseData)
            }
        })
    }
    //функция для получения данных с сервера по устройствам
fun getres(): String = runBlocking {
    return@runBlocking withContext(Dispatchers.IO) {
        var sharedPreferences = requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        var Token = sharedPreferences.getString("key", "default_value").toString()
        Token="Token $Token"
        val apiUrl = "https" +
                "://seld-lock.ru/api/v1.0/devices/"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", Token)
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                return@withContext response.body?.string() ?: "0000"
            } else {
                return@withContext "0000"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext "0000"
        }
    }
}
//обновление картинки и текста(открытие и закрытие)
var previousData: String = ""
var number1: Int = 0
var opt: Int? = null
    @SuppressLint("SuspiciousIndentation")
    fun trackChanges() {
        val newData = getres()
            if (newData == "0000") {
                if (imageClose != null && imageOpen!= null &&  textcloseopen!= null) {
                    requireActivity().runOnUiThread {
                        imageClose.visibility = View.GONE
                        imageOpen.visibility = View.GONE
                        textcloseopen.text = "Ошибка сервера"
                    }
                }
            }
            var sharedPreferences = requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
            val number: Int = sharedPreferences.getString("number", "default_value")?.toIntOrNull() ?: 0
            if (previousData != newData && newData.contains("status") ||number!=number1 ) {
                println(newData)///////
                    val jsonArray = JSONArray(newData)
                    if (jsonArray.length() > 0) {
                        val jsonObject = jsonArray.getJSONObject(number)
                        val status = jsonObject.getString("status")
                        if (status == "Open" ) {
                            if (imageClose != null && imageOpen!= null &&  textcloseopen!= null) {
                                requireActivity().runOnUiThread {
                                    imageClose.visibility = View.GONE
                                    imageOpen.visibility = View.VISIBLE
                                    textcloseopen.text = "Замок открыт!"
                                }
                                opt=0
                            }
                        }
                        if (status == "Close") {
                            if (imageClose != null && imageOpen != null &&  textcloseopen!= null) {
                                requireActivity().runOnUiThread {
                                    imageOpen.visibility = View.GONE
                                    imageClose.visibility = View.VISIBLE
                                    textcloseopen.text = "Замок закрыт!"
                                }
                                opt=1
                            }
                        }
                    }
                    previousData = newData
                   number1 = number
                }
    }
    //спиннер устройств
    fun spinner() {
        val spinner = binding.choosedevice
        val newData = getres()
        if (newData != "0000") {
            val jsonArray = JSONArray(newData)
            val i = jsonArray.length() - 1
            val items = mutableListOf<String>()
            for (j in 0..i) {
                val jsonObject = jsonArray.getJSONObject(j)
                val device_name = jsonObject.getString("device_name")
                items.add(device_name)
            }
            requireActivity().runOnUiThread {
                val adapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedPosition = (position).toString()
                var sharedPreferences =
                    requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("number", selectedPosition)
                editor.apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    override fun onStop() {
        super.onStop()
        _binding = null
        timer?.cancel()
    }
    override fun onResume() {
        if(opt!=null) {
            requireActivity().runOnUiThread {
                if (opt == 0) {
                    if (imageClose != null && imageOpen != null && textcloseopen != null) {
                        imageClose.visibility = View.GONE
                        imageOpen.visibility = View.VISIBLE
                        textcloseopen.text = "Замок открыт!"
                        opt = 0
                    }
                }
                if (opt == 1) {
                    if (imageClose != null && imageOpen != null && textcloseopen != null) {
                        imageOpen.visibility = View.GONE
                        imageClose.visibility = View.VISIBLE
                        textcloseopen.text = "Замок закрыт!"
                        opt = 1
                    }
                }
            }
        }
        super.onResume()
        previousData =""
    }
}