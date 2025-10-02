package com.example.buap

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReporteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_reporte) // Asegúrate de que el layout se llame así

        // Referencias a los elementos
        val ivBack = findViewById<ImageView>(R.id.imageViewBack)
        ivBack.setOnClickListener {
            finish()
        }
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etFecha = findViewById<EditText>(R.id.etFecha)
        val etHora = findViewById<EditText>(R.id.etHora)
        val etDireccion = findViewById<EditText>(R.id.etDireccion)
        val etRiesgo = findViewById<EditText>(R.id.etRiesgo)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcion)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)

        // Botón regresar
        ivBack.setOnClickListener {
            finish() // Cierra la actividad y regresa a la anterior
        }

        // Botón enviar
        btnEnviar.setOnClickListener {
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
            } else {
                // Aquí puedes guardar en la base de datos o enviar al servidor
                Toast.makeText(this, "Reporte enviado correctamente", Toast.LENGTH_SHORT).show()
                finish() // Opcional: cerrar la actividad después de enviar
            }
        }
    }
}
