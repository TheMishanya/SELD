package com.example.seld.ui.notifications

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.seld.R
import com.example.seld.databinding.FragmentNotificationsBinding
import kotlinx.android.synthetic.main.registrationback.view.*
import kotlinx.android.synthetic.main.registrstion.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import android.widget.Toast
import kotlinx.android.synthetic.main.about.*
import kotlinx.android.synthetic.main.about.view.*
import kotlinx.android.synthetic.main.forregistr2.view.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import org.json.JSONObject
import java.io.IOException


class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val changeuser = binding.changeuser
        var sharedPreferences =  requireContext().getSharedPreferences("my_preferences", MODE_PRIVATE)
        val value = sharedPreferences.getString("username", "")
        println(value)
        val nickname = binding.nickname
        val about = binding.aboutus
        about.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            val inflater = LayoutInflater.from(requireContext())
            val view = inflater.inflate(R.layout.about, null)
            dialogBuilder.setView(view)
            val dialog3 = dialogBuilder.create()
            dialog3.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog3.show()
            val nobutton5= view.nobutton5
            nobutton5.setOnClickListener {
                dialog3.dismiss()
            }
        }
        nickname.text = "Логин: $value"
        //смена пользователя
        changeuser.setOnClickListener {

            var sharedPreferences = requireContext().getSharedPreferences("my_preferences", MODE_PRIVATE)
            val dialogBuilder = AlertDialog.Builder(requireContext())
            val inflater = LayoutInflater.from(requireContext())
            val view = inflater.inflate(R.layout.forregistr2, null)
            dialogBuilder.setView(view)
            val dialog = dialogBuilder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            val yesbutton = view.yesButton
            val noobutton = view.noobutton
            val nonoobutton = view.nonobutton1
            nonoobutton.setOnClickListener {
                dialog.dismiss()
            }
            //вход в акк
            yesbutton.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(requireContext())
                val inflater = LayoutInflater.from(requireContext())
                val view = inflater.inflate(R.layout.registrationback, null)
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
                                println(responseBody)
                                response.close()
                                val jsonString = responseBody
                                if (jsonString != null) {
                                    val jsonObject = JSONObject(responseBody)
                                    val token = jsonObject.getString("auth_token")
                                    deletetoken()
                                    nickname.text = "Логин: $username"
                                    editor.putString("key", token)
                                    editor.putString("username", username)
                                    editor.putString("password", password)
                                    editor.apply()
                                    requireActivity().runOnUiThread {
                                        texterror.text = ""
                                        val text = "Вы зашли в аккаунт"
                                        val duration = Toast.LENGTH_SHORT
                                        val toast =
                                            Toast.makeText(requireContext(), text, duration)
                                        toast.show()
                                    }
                                    dialog.dismiss()
                                    dialog1.dismiss()
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                                requireActivity().runOnUiThread {
                                    val text = "Отсутствует подключение к интернету"
                                    val duration = Toast.LENGTH_SHORT
                                    val toast = Toast.makeText(requireContext(), text, duration)
                                    toast.show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                    requireActivity().runOnUiThread {
                                        val text = "Логин или пароль неверный"
                                        val duration = Toast.LENGTH_SHORT
                                        val toast =
                                            Toast.makeText(requireContext(), text, duration)
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
            //регистрация
            noobutton.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(requireContext())
                val inflater = LayoutInflater.from(requireContext())
                val view = inflater.inflate(R.layout.registrstion, null)
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
                    }
                    else if(password!=password2)
                    {
                        texterror.text = "Пароли не совпадают"
                    }
                    else {
                        val client = OkHttpClient()
                        GlobalScope.launch(Dispatchers.IO) {
                            val requestBody = """
            {
            "username": "$username",
            "password": "$password"
            }
            """.trimIndent()
                            try {
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
                                    requireActivity().runOnUiThread {
                                        texterror.text = ""
                                        val text = "Вы успешно зарегестрировались"
                                        val duration = Toast.LENGTH_SHORT
                                        val toast =
                                            Toast.makeText(requireContext(), text, duration)
                                        toast.show()
                                    }
                                    dialog2.dismiss()
                                    dialog.dismiss()
                                    requireActivity().runOnUiThread {
                                        registrationin()
                                    }
                                }
                                if (jsonString != null && jsonString.contains("A user with that username already exists.")) {
                                    usernameEditText.text.clear()
                                    passwordEditText.text.clear()
                                    requireActivity().runOnUiThread {
                                        texterror.text = ""
                                        val text = "Пользователь с таким логином уже существует"
                                        val duration = Toast.LENGTH_SHORT
                                        val toast = Toast.makeText(requireContext(), text, duration)
                                        toast.show()
                                        texterror.text = "Пользователь с таким логином уже существует"
                                    }
                                }
                               else if (jsonString != null && jsonString.contains("This password is too short. It must contain at least 8 characters.")) {
                                    passwordEditText.text.clear()
                                    requireActivity().runOnUiThread {
                                        texterror.text = ""
                                        val text = "Слишком короткий пароль"
                                        val duration = Toast.LENGTH_SHORT
                                        val toast =
                                            Toast.makeText(requireContext(), text, duration)
                                        toast.show()
                                        texterror.text = "Слишком короткий пароль"
                                    }
                                }
                                else if (jsonString != null && jsonString.contains("This password is entirely numeric.")) {
                                    passwordEditText.text.clear()
                                    requireActivity().runOnUiThread {
                                        texterror.text = ""
                                        val text = "Слишком простой пароль"
                                        val duration = Toast.LENGTH_SHORT
                                        val toast =
                                            Toast.makeText(requireContext(), text, duration)
                                        toast.show()
                                        texterror.text = "Слишком простой пароль"
                                    }
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                               requireActivity().runOnUiThread {
                                    val text = "Отсутствует подключение к интернету"
                                    val duration = Toast.LENGTH_SHORT
                                    val toast = Toast.makeText(requireContext(), text, duration)
                                    toast.show()
                                }
                            }
                        }
                    }
                }
            }

        }
        return root
    }
    //удаление токена
    fun deletetoken() {
        var sharedPreferences =
            requireContext().getSharedPreferences("my_preferences", MODE_PRIVATE)
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
                requireActivity().runOnUiThread {
                    val text = "Отсутствует подключение к интернету"
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(requireContext(), text, duration)
                    toast.show()
                }
            }
        }
    }
    //вход в аккаунт
    fun registrationin() {
        var sharedPreferences = requireActivity().getSharedPreferences("my_preferences", MODE_PRIVATE)
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.popup_registration, null)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setView(view)
        val dialog1 = dialogBuilder.create()
        dialog1.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog1.show()
        val usernameEditText = view.Loginin
        val passwordEditText = view.Passwordin
        val registerButton = view.registerButtonin
        val texterror = view.texterrorin
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (username.equals("") || password.equals("")) {
                texterror.text = "Введите логин и пароль"
            } else {
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
                            deletetoken()
                            val nickname = binding.nickname
                            nickname.text = username
                            editor.putString("key", token)
                            editor.putString("username", username)
                            editor.putString("password", password)
                            editor.apply()
                            requireActivity().runOnUiThread {
                                texterror.text = ""
                                val text = "Вы зашли в аккаунт"
                                val duration = Toast.LENGTH_SHORT
                                val toast = Toast.makeText(requireContext(), text, duration)
                                toast.show()
                            }
                            dialog1.dismiss()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        requireActivity().runOnUiThread {
                            val text = "Отсутствует подключение к интернету"
                            val duration = Toast.LENGTH_SHORT
                            val toast = Toast.makeText(requireContext(), text, duration)
                            toast.show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                            requireActivity().runOnUiThread {
                                val text = "Логин или пароль неверный"
                                val duration = Toast.LENGTH_SHORT
                                val toast =
                                    Toast.makeText(requireContext(), text, duration)
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}