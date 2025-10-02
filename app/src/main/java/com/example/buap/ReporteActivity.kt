package com.example.buap

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReporteActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etFecha: EditText
    private lateinit var etHora: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etRiesgo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnEnviar: Button
    private lateinit var ivBack: ImageView

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_reporte) // Tu layout XML

        // Inicializar base de datos
        dbHelper = DatabaseHelper(this)

        // Referencias a elementos de UI
        etNombre = findViewById(R.id.etNombre)
        etFecha = findViewById(R.id.etFecha)
        etHora = findViewById(R.id.etHora)
        etDireccion = findViewById(R.id.etDireccion)
        etRiesgo = findViewById(R.id.etRiesgo)
        etDescripcion = findViewById(R.id.etDescripcion)
        btnEnviar = findViewById(R.id.btnEnviar)
        ivBack = findViewById(R.id.imageViewBack)

        // Botón regresar
        ivBack.setOnClickListener {
            finish()
        }

        // Botón enviar
        btnEnviar.setOnClickListener {
            enviarReporte()
        }
    }

    private fun enviarReporte() {
        val nombre = etNombre.text.toString()
        val fecha = etFecha.text.toString()
        val hora = etHora.text.toString()
        val direccion = etDireccion.text.toString()
        val riesgo = etRiesgo.text.toString()
        val descripcion = etDescripcion.text.toString()

        if (nombre.isEmpty() || fecha.isEmpty() || hora.isEmpty() ||
            direccion.isEmpty() || riesgo.isEmpty() || descripcion.isEmpty()
        ) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val reporte = Reporte(
            nombre = nombre,
            fecha = fecha,
            hora = hora,
            direccion = direccion,
            riesgo = riesgo,
            descripcion = descripcion
        )

        val id = dbHelper.insertReporte(reporte)
        if (id > 0) {
            Toast.makeText(this, "Reporte enviado correctamente", Toast.LENGTH_SHORT).show()
            limpiarCampos()
        } else {
            Toast.makeText(this, "Error al enviar reporte", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limpiarCampos() {
        etNombre.text.clear()
        etFecha.text.clear()
        etHora.text.clear()
        etDireccion.text.clear()
        etRiesgo.text.clear()
        etDescripcion.text.clear()
    }
}
