package com.example.buap

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DetalleReporteActivity : AppCompatActivity() {

    // Vistas
    private lateinit var imgReporte: ImageView
    private lateinit var tvNombre: TextView
    private lateinit var tvFechaHora: TextView
    private lateinit var tvDireccion: TextView
    private lateinit var tvRiesgo: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var layoutCarga: LinearLayout

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_reporte)

        db = Firebase.firestore

        // 1. Inicializar Vistas
        imgReporte = findViewById(R.id.imgReporte)
        tvNombre = findViewById(R.id.tvNombre)
        tvFechaHora = findViewById(R.id.tvFechaHora)
        tvDireccion = findViewById(R.id.tvDireccion)
        tvRiesgo = findViewById(R.id.tvRiesgo)
        tvDescripcion = findViewById(R.id.tvDescripcion)
        layoutCarga = findViewById(R.id.layoutCarga)

        // Botón regresar
        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            finish()
        }

        // 2. Obtener el ID del reporte
        val reporteId = intent.getStringExtra("reporteId")

        if (reporteId != null) {
            cargarDetalleReporte(reporteId)
        } else {
            Toast.makeText(this, "Error: ID de reporte no encontrado.", Toast.LENGTH_LONG).show()
            finish()
        }
    }
        private fun cargarDetalleReporte(reporteId: String) {
            layoutCarga.visibility = View.VISIBLE // Mostrar indicador de carga
            // Ocultar contenido mientras carga
            findViewById<LinearLayout>(R.id.cardReporte).visibility = View.GONE
            imgReporte.visibility = View.GONE

            db.collection("reportes").document(reporteId).get()
                .addOnSuccessListener { document ->
                    layoutCarga.visibility = View.GONE // Ocultar carga

                    if (document.exists()) {
                        // Mostrar contenido
                        findViewById<LinearLayout>(R.id.cardReporte).visibility = View.VISIBLE
                        imgReporte.visibility = View.VISIBLE

                        // 3. Extraer y mostrar datos
                        val nombre = document.getString("nombreReporte")
                        val fecha = document.getString("fecha")
                        val hora = document.getString("hora")
                        val direccion = document.getString("direccion")
                        val riesgo = document.getString("riesgo")
                        val descripcion = document.getString("descripcion")

                        tvNombre.text = "Nombre: ${nombre ?: "N/A"}"
                        tvFechaHora.text = "Fecha/Hora: ${fecha ?: "N/A"} ${hora ?: "N/A"}"
                        tvDireccion.text = "Dirección: ${direccion ?: "N/A"}"
                        tvRiesgo.text = "Tipo de riesgo: ${riesgo ?: "N/A"}"
                        tvDescripcion.text = descripcion ?: "Sin descripción"


                        // 4. Cargar Imagen desde Cloudinary (URL) usando Glide
                        val fotoURL = document.getString("fotoURL")
                        if (!fotoURL.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(fotoURL)
                                .centerCrop()
                                .placeholder(R.drawable.ic_image_placeholder) // Reemplaza por tu placeholder
                                .error(R.drawable.ic_image_error) // Reemplaza por tu error drawable
                                .into(imgReporte)
                        } else {
                            // Si no hay fotoURL, usar un drawable local para indicar que no hay imagen.
                            imgReporte.setImageResource(R.drawable.ic_no_photo)
                        }

                    } else {
                        Toast.makeText(this, "Reporte no encontrado.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    layoutCarga.visibility = View.GONE
                    Log.e("DetalleReporte", "Error al obtener reporte: ", e)
                    Toast.makeText(this, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
        }
}
