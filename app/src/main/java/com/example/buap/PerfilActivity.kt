package com.example.buap

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class PerfilActivity : AppCompatActivity() {

    private lateinit var layoutHistorial: LinearLayout
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var btnBack: ImageView
    private lateinit var imgPerfil: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Inicializar la base de datos
        dbHelper = DatabaseHelper(this)

        // Referencias a elementos del layout
        btnBack = findViewById(R.id.btnBack)
        imgPerfil = findViewById(R.id.imgPerfil)
        layoutHistorial = findViewById(R.id.layoutHistorial)

        // Flecha de regreso
        btnBack.setOnClickListener {
            finish()
        }

        // Click en la imagen del perfil
        imgPerfil.setOnClickListener {
            Toast.makeText(this, "Perfil presionado", Toast.LENGTH_SHORT).show()
            // Aquí podrías abrir un editor de perfil o foto
        }

        // Cargar historial desde la base de datos
        cargarHistorial()
    }

    private fun cargarHistorial() {
        layoutHistorial.removeAllViews() // Limpiar historial antes de cargar
        val reportes = dbHelper.getAllReportes() // Devuelve lista de reportes desde DB

        for (reporte in reportes) {
            val tvReporte = TextView(this)
            tvReporte.text = "${reporte.nombre} - ${reporte.fecha} ${reporte.hora}"
            tvReporte.setBackgroundResource(R.drawable.rounded_card_light)
            tvReporte.setPadding(20, 20, 20, 20)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 12, 0, 0)
            tvReporte.layoutParams = params

            tvReporte.setOnClickListener {
                val intent = Intent(this, DetalleReporteActivity::class.java)
                intent.putExtra("nombre", reporte.nombre)
                intent.putExtra("fecha", reporte.fecha)
                intent.putExtra("hora", reporte.hora)
                intent.putExtra("direccion", reporte.direccion)
                intent.putExtra("riesgo", reporte.riesgo)
                intent.putExtra("descripcion", reporte.descripcion)
                startActivity(intent)
            }


            layoutHistorial.addView(tvReporte)
        }

        if (reportes.isEmpty()) {
            val tvVacio = TextView(this)
            tvVacio.text = "No hay reportes aún"
            tvVacio.setPadding(20, 20, 20, 20)
            layoutHistorial.addView(tvVacio)
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar historial cada vez que regresa a esta pantalla
        cargarHistorial()
    }
}
