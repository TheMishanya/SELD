package com.example.seld

//import kotlinx.android.synthetic.main.fragment_home.*
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.seld.databinding.ActivityMainBinding
import com.example.seld.ui.dashboard.DashboardFragment
import com.example.seld.ui.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.forregistr.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.popup_registration.view.*
import kotlinx.android.synthetic.main.popup_registration.view.Login
import kotlinx.android.synthetic.main.popup_registration.view.Password
import kotlinx.android.synthetic.main.popup_registration.view.registerButton
import kotlinx.android.synthetic.main.popup_registration.view.texterror
import kotlinx.android.synthetic.main.registrationback.view.*
import kotlinx.android.synthetic.main.registrstion.view.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        var sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
        //вход или регистрация
        if (!sharedPreferences.contains("key")) {
            registrationin()
        }
        //получение токена каждый вход
        else {
            //удаление токена
            var sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
            var Token = sharedPreferences.getString("key", "default_value").toString()
            Token = "Token $Token"
            val client = OkHttpClient()
            GlobalScope.launch(Dispatchers.IO) {
                val requestBody = """
            {
            }
            """.trimIndent()
                try {
                    println(requestBody)
                    val request = Request.Builder()
                        .url("https://seld-lock.ru/auth/token/logout/")
                        .post(
                            RequestBody.create(
                                "application/json".toMediaTypeOrNull(),
                                requestBody
                            )
                        )
                        .addHeader("Authorization", Token)
                        .build()
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()
                    response.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        val text = "Отсутствует подключение к интернету"
                        val duration = Toast.LENGTH_SHORT
                        val toast = Toast.makeText(applicationContext, text, duration)
                        toast.show()
                    }
                }
            }
            //получение нового токена
            val username = sharedPreferences.getString("username", "default_value").toString()
            val password = sharedPreferences.getString("password", "default_value").toString()
            val editor = sharedPreferences.edit()
            GlobalScope.launch(Dispatchers.IO) {
                val requestBody = """
                {
                "username": "$username",
                "password": "$password"
                }
                """.trimIndent()
                try {
                    println(requestBody)
                    val request = Request.Builder()
                        .url("https://seld-lock.ru/auth/token/login")
                        .post(
                            RequestBody.create(
                                "application/json".toMediaTypeOrNull(),
                                requestBody
                            )
                        )
                        .build()
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()
                    println(responseBody)
                    response.close()
                    val jsonString = responseBody
                    if (jsonString != null) {
                        val jsonObject = JSONObject(responseBody)
                        val token = jsonObject.getString("auth_token")
                        editor.putString("key", token)
                        editor.apply()
                        println(token)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        val text = "Отсутствует подключение к интернету"
                        val duration = Toast.LENGTH_SHORT
                        val toast = Toast.makeText(applicationContext, text, duration)
                        toast.show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        runOnUiThread {
                            val text = "Логин или пароль неверный"
                            val duration = Toast.LENGTH_SHORT
                            val toast = Toast.makeText(applicationContext, text, duration)
                            toast.show()
                            registrationin()
                        }
                    }
                }
            }
        }
    }
    //вход в акк или регистрация
    fun registrationin() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.forregistr, null)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setView(view)
        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val yesbutton = view.yesButton
        val noobutton = view.noobutton
        //вход в акк
        yesbutton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val view = inflater.inflate(R.layout.registrationback, null)
            dialogBuilder.setCancelable(false)
            dialogBuilder.setView(view)
            val dialog1 = dialogBuilder.create()
            dialog1.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog1.show()
            val usernameEditText = view.Loginin
            val passwordEditText = view.Passwordin
            val registerButton = view.registerButtonin
            val nobutton = view.nobuttonin
            val texterror = view.texterrorin
            nobutton.setOnClickListener {
                dialog1.dismiss()
            }
            registerButton.setOnClickListener {
                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                if (username.equals("") || password.equals("")) {
                    texterror.text = "Введите логин и пароль"
                } else {
                    var sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    val client = OkHttpClient()
                    GlobalScope.launch(Dispatchers.IO) {
                        val requestBody = """
                {
                "username": "$username",
                "password": "$password"
                }
                """.trimIndent()
                        try {
                            println(requestBody)
                            val request = Request.Builder()
                                .url("https://seld-lock.ru/auth/token/login")
                                .post(
                                    RequestBody.create(
                                        "application/json".toMediaTypeOrNull(),
                                        requestBody
                                    )
                                )
                                .build()
                            val response = client.newCall(request).execute()
                            val responseBody = response.body?.string()
                            response.close()
                            val jsonString = responseBody
                            if (jsonString != null) {
                                val jsonObject = JSONObject(responseBody)
                                val token = jsonObject.getString("auth_token")
                                println(token)
                                editor.putString("key", token)
                                editor.putString("username", username)
                                editor.putString("password", password)
                                editor.apply()
                                runOnUiThread {
                                    texterror.text = ""
                                    val text = "Вы зашли в аккаунт"
                                    val duration = Toast.LENGTH_SHORT
                                    val toast =
                                        Toast.makeText(applicationContext, text, duration)
                                    toast.show()
                                }
                                dialog1.dismiss()
                                dialog.dismiss()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            runOnUiThread {
                                val text = "Отсутствует подключение к интернету"
                                val duration = Toast.LENGTH_SHORT
                                val toast = Toast.makeText(applicationContext, text, duration)
                                toast.show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUiThread {
                                runOnUiThread {
                                    val text = "Логин или пароль неверный"
                                    val duration = Toast.LENGTH_SHORT
                                    val toast =
                                        Toast.makeText(applicationContext, text, duration)
                                    toast.show()
                                    texterror.text = "Логин или пароль неверный"
                                    usernameEditText.text.clear()
                                    passwordEditText.text.clear()
                                }
                            }
                        }
                    }
                }
            }
        }
        //регистрация в акке
        noobutton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val view = inflater.inflate(R.layout.registrstion, null)
            dialogBuilder.setCancelable(false)
            dialogBuilder.setView(view)
            val dialog2 = dialogBuilder.create()
            dialog2.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog2.show()
            val usernameEditText = view.Loginreg
            val passwordEditText = view.Passwordreg
            val passwordEditTextnext = view.Passwordnextreg
            val nobutton = view.nobuttonreg
            val registerButton = view.registerButtonreg
            val texterror = view.texterrorreg
            nobutton.setOnClickListener {
                dialog2.dismiss()
            }
            registerButton.setOnClickListener {
                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                val password2 = passwordEditTextnext.text.toString()
                if (username.equals("") || password.equals("")) {
                    texterror.text = "Введите логин и пароль"
                } else if (password != password2) {
                    texterror.text = "Пароли не совпадают"
                } else {
                    val client = OkHttpClient()
                    GlobalScope.launch(Dispatchers.IO) {
                        val requestBody = """
            {
            "username": "$username",
            "password": "$password"
            }
            """.trimIndent()
                        try {
                            println(requestBody)
                            val request = Request.Builder()
                                .url("https://seld-lock.ru/api/v1.0/auth/users/")
                                .post(
                                    RequestBody.create(
                                        "application/json".toMediaTypeOrNull(),
                                        requestBody
                                    )
                                )
                                .build()
                            val response = client.newCall(request).execute()
                            val responseBody = response.body?.string()
                            response.close()
                            println(responseBody)
                            val jsonString = responseBody
                            if (jsonString != null && jsonString.contains(username)) {
                                runOnUiThread {
                                    texterror.text = ""
                                    val text = "Вы успешно зарегестрировались"
                                    val duration = Toast.LENGTH_SHORT
                                    val toast =
                                        Toast.makeText(applicationContext, text, duration)
                                    toast.show()
                                }
                                dialog2.dismiss()
                                dialog.dismiss()
                                runOnUiThread {
                                    registrationin()
                                }
                            }
                            if (jsonString != null && jsonString.contains("A user with that username already exists.")) {
                                usernameEditText.text.clear()
                                passwordEditText.text.clear()
                                runOnUiThread {
                                    texterror.text = ""
                                    val text = "Пользователь с таким логином уже существует"
                                    val duration = Toast.LENGTH_SHORT
                                    val toast =
                                        Toast.makeText(applicationContext, text, duration)
                                    toast.show()
                                    texterror.text =
                                        "Пользователь с таким логином уже существует"
                                }
                            }
                            if (jsonString != null && jsonString.contains("This password is too short. It must contain at least 8 characters.")) {
                                passwordEditText.text.clear()
                                runOnUiThread {
                                    texterror.text = ""
                                    val text = "Слишком короткий пароль"
                                    val duration = Toast.LENGTH_SHORT
                                    val toast =
                                        Toast.makeText(applicationContext, text, duration)
                                    toast.show()
                                    texterror.text = "Слишком короткий пароль"
                                }
                            }
                            if (jsonString != null && jsonString.contains("This password is entirely numeric.")) {
                                passwordEditText.text.clear()
                                runOnUiThread {
                                    texterror.text = ""
                                    val text = "Слишком простой пароль"
                                    val duration = Toast.LENGTH_SHORT
                                    val toast =
                                        Toast.makeText(applicationContext, text, duration)
                                    toast.show()
                                    texterror.text = "Слишком простой пароль"
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            runOnUiThread {
                                val text = "Отсутствует подключение к интернету"
                                val duration = Toast.LENGTH_SHORT
                                val toast = Toast.makeText(applicationContext, text, duration)
                                toast.show()
                            }
                        }
                    }
                }
            }
        }
    }


}





