package com.example.buap

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Layout mínimo funcional

        val etEmailRegister = findViewById<EditText>(R.id.etEmailRegister)
        val etPasswordRegister = findViewById<EditText>(R.id.etPasswordRegister)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // Flecha para regresar
        val tvBack = findViewById<TextView>(R.id.tvBack)
        tvBack.setOnClickListener {
            finish() // Cierra RegisterActivity y vuelve a LoginActivity
        }

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
                    Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                    finish() // Vuelve al LoginActivity
                }
            }
        }
    }
}
