package com.example.buap

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetalleReporteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_reporte)

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        val tvNombre = findViewById<TextView>(R.id.tvNombre)
        val tvFechaHora = findViewById<TextView>(R.id.tvFechaHora)
        val tvDireccion = findViewById<TextView>(R.id.tvDireccion)
        val tvRiesgo = findViewById<TextView>(R.id.tvRiesgo)
        val tvDescripcion = findViewById<TextView>(R.id.tvDescripcion)

        // Botón regresar
        ivBack.setOnClickListener {
            finish()
        }

        // Recibir datos del Intent
        val nombre = intent.getStringExtra("nombre")
        val fecha = intent.getStringExtra("fecha")
        val hora = intent.getStringExtra("hora")
        val direccion = intent.getStringExtra("direccion")
        val riesgo = intent.getStringExtra("riesgo")
        val descripcion = intent.getStringExtra("descripcion")

        // Mostrar datos
        tvNombre.text = "Nombre: $nombre"
        tvFechaHora.text = "Fecha/Hora: $fecha $hora"
        tvDireccion.text = "Dirección: $direccion"
        tvRiesgo.text = "Tipo de riesgo: $riesgo"
        tvDescripcion.text = descripcion
    }
}
