package com.example.buap

import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class Main : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_transport)

        // Header
        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)
        val tvSaludo = findViewById<TextView>(R.id.tvSaludo)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val locationBox = findViewById<LinearLayout>(R.id.locationBox)

        // Opciones de transporte
        val transportOptions = findViewById<GridLayout>(R.id.transportOptions)
        val btnOpenMap = findViewById<Button>(R.id.btnOpenMap)

        // Bottom Navigation
        val bottomNav = findViewById<LinearLayout>(R.id.bottomNav)

        // Click listeners para transporte
        for (i in 0 until transportOptions.childCount) {
            val option = transportOptions.getChildAt(i) as LinearLayout
            option.setOnClickListener {
                val textView = option.getChildAt(1) as TextView
                Toast.makeText(this, "Selected: ${textView.text}", Toast.LENGTH_SHORT).show()
            }
        }

        // Abrir mapa al pulsar botón
        btnOpenMap.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        // Click listeners para bottom nav
        val homeButton = bottomNav.getChildAt(0) as ImageView
        val addButton = bottomNav.getChildAt(1) as ImageView
        val filesButton = bottomNav.getChildAt(2) as ImageView

        homeButton.setOnClickListener {
            Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show()
        }

        addButton.setOnClickListener {
            Toast.makeText(this, "Add clicked", Toast.LENGTH_SHORT).show()
        }

        filesButton.setOnClickListener {
            Toast.makeText(this, "Files clicked", Toast.LENGTH_SHORT).show()
        }

        // Click listener para la imagen del perfil
        imgPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        // Ejemplo de saludo dinámico
        val userName = intent.getStringExtra("USER_NAME") ?: "Person"
        tvSaludo.text = "Hi $userName"
    }
}
