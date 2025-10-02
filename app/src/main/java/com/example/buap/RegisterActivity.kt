package com.example.buap

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Se eliminó la animación de fondo
        // val scrollView = findViewById<ScrollView>(R.id.scrollViewRegister)
        // val animation = scrollView.background as AnimationDrawable
        // animation.setEnterFadeDuration(3000)
        // animation.setExitFadeDuration(3000)
        // animation.start()

        val etEmailRegister = findViewById<EditText>(R.id.etEmailRegister)
        val etPasswordRegister = findViewById<EditText>(R.id.etPasswordRegister)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBack = findViewById<TextView>(R.id.tvBack)

        val db = AppDatabase.getDatabase(this)

        tvBack.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val email = etEmailRegister.text.toString()
            val password = etPasswordRegister.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            when {
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val hashed = hashPassword(password)
                    lifecycleScope.launch {
                        val existingUser = db.userDao().getUserByEmail(email)
                        runOnUiThread {
                            if (existingUser != null) {
                                Toast.makeText(this@RegisterActivity, "El correo ya existe", Toast.LENGTH_SHORT).show()
                            } else {
                                lifecycleScope.launch {
                                    db.userDao().insert(User(email = email, password = hashed))
                                }
                                Toast.makeText(this@RegisterActivity, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}
