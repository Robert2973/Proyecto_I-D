package com.example.buap

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil) // Asegúrate de que coincida con tu XML

        // Referencias a elementos
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)
        val layoutHistorial = findViewById<LinearLayout>(R.id.layoutHistorial)
        val tvHistorial = findViewById<TextView>(R.id.tvHistorial)

        // Flecha de regreso
        btnBack.setOnClickListener {
            finish() // Regresa a la actividad anterior
        }

        // Click en la imagen del perfil
        imgPerfil.setOnClickListener {
            Toast.makeText(this, "Perfil presionado", Toast.LENGTH_SHORT).show()
            // Aquí podrías abrir un editor de perfil o foto
        }

        // Ejemplo: click en cada item del historial
        for (i in 0 until layoutHistorial.childCount) {
            val reporte = layoutHistorial.getChildAt(i) as TextView
            reporte.setOnClickListener {
                Toast.makeText(this, "Abriendo ${reporte.text}", Toast.LENGTH_SHORT).show()
                // Aquí podrías abrir detalles del reporte
            }
        }

        // Opcional: click en botones sociales
        val socialButtons = findViewById<LinearLayout>(R.id.socialButtons)
        for (i in 0 until socialButtons.childCount) {
            val socialIcon = socialButtons.getChildAt(i) as ImageView
            socialIcon.setOnClickListener {
                when (i) {
                    0 -> Toast.makeText(this, "Google clicked", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(this, "Facebook clicked", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(this, "X clicked", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
