package com.example.buap

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Layout moderno con fondo animado

        // Animación de fondo
        val scrollView = findViewById<ScrollView>(R.id.scrollViewLogin)
        val animation = scrollView.background as AnimationDrawable
        animation.setEnterFadeDuration(3000)
        animation.setExitFadeDuration(3000)
        animation.start()

        // Referencias a los elementos de la vista
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        // Botón Sign In
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email == "admin@gmail.com" && password == "1234") {
                Toast.makeText(this, "Bienvenido $email", Toast.LENGTH_SHORT).show()

                // Aquí puedes abrir HomeActivity si quieres
                // val intent = Intent(this, HomeActivity::class.java)
                // startActivity(intent)
                // finish()
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        // Texto que manda a la pantalla de registro
        tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
