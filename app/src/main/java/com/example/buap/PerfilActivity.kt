package com.example.buap

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class PerfilActivity : AppCompatActivity() {

    private lateinit var layoutHistorial: LinearLayout
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var btnBack: ImageView
    private lateinit var imgPerfil: ImageView

    // Datos usuario
    private lateinit var tvNombre: TextView
    private lateinit var tvEdad: TextView
    private lateinit var tvDireccion: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var btnEditar: Button

    private val PICK_IMAGE = 100
    private var imageUri: Uri? = null
    private var imagePath: String? = null // ✅ Ruta guardada del archivo interno
    private var editMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        dbHelper = DatabaseHelper(this)

        btnBack = findViewById(R.id.btnBack)
        imgPerfil = findViewById(R.id.imgPerfil)
        layoutHistorial = findViewById(R.id.layoutHistorial)

        tvNombre = findViewById(R.id.tvNombre)
        tvEdad = findViewById(R.id.tvEdad)
        tvDireccion = findViewById(R.id.tvDireccion)
        tvTelefono = findViewById(R.id.tvTelefono)
        btnEditar = findViewById(R.id.btnEditar)

        // 🔹 Regresar a pantalla anterior
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 🔹 Solo permitir cambiar imagen si está activado el modo edición
        imgPerfil.setOnClickListener {
            if (editMode) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, PICK_IMAGE)
            } else {
                Toast.makeText(this, "Activa el modo edición para cambiar la imagen", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔹 Botón Editar / Guardar
        btnEditar.setOnClickListener {
            if (!editMode) {
                activarEdicion(true)
            } else {
                guardarCambios()
                activarEdicion(false)
            }
        }

        cargarDatosUsuario()
        cargarHistorial()
    }

    private fun activarEdicion(habilitar: Boolean) {
        editMode = habilitar
        if (habilitar) {
            btnEditar.text = "Guardar"
            convertirTextViewAEditText(tvNombre)
            convertirTextViewAEditText(tvEdad)
            convertirTextViewAEditText(tvDireccion)
            convertirTextViewAEditText(tvTelefono)
        } else {
            btnEditar.text = "Editar"
        }
    }

    private fun convertirTextViewAEditText(tv: TextView) {
        val parent = tv.parent as LinearLayout
        val index = parent.indexOfChild(tv)
        parent.removeView(tv)
        val et = EditText(this)
        et.id = tv.id
        et.setText(tv.text.toString().substringAfter(": ").trim())
        et.setTextColor(tv.currentTextColor)
        et.textSize = tv.textSize / resources.displayMetrics.scaledDensity
        parent.addView(et, index)
    }

    private fun guardarCambios() {
        val parent = findViewById<LinearLayout>(R.id.datosUsuario)
        val etNombre = parent.findViewById<EditText>(R.id.tvNombre)
        val etEdad = parent.findViewById<EditText>(R.id.tvEdad)
        val etDireccion = parent.findViewById<EditText>(R.id.tvDireccion)
        val etTelefono = parent.findViewById<EditText>(R.id.tvTelefono)

        // 🔹 Guardar datos incluyendo imagen
        dbHelper.actualizarUsuario(
            etNombre.text.toString(),
            etEdad.text.toString(),
            etDireccion.text.toString(),
            etTelefono.text.toString(),
            imagePath // ✅ ahora guardamos la ruta absoluta
        )

        // 🔹 Reconvertir los campos a TextView
        parent.removeAllViews()
        parent.addView(tvNombre)
        parent.addView(tvEdad)
        parent.addView(tvDireccion)
        parent.addView(tvTelefono)
        parent.addView(btnEditar)

        tvNombre.text = "Nombre: ${etNombre.text}"
        tvEdad.text = "Edad: ${etEdad.text}"
        tvDireccion.text = "Dirección: ${etDireccion.text}"
        tvTelefono.text = "Tel: ${etTelefono.text}"

        Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
    }

    private fun cargarDatosUsuario() {
        val usuario = dbHelper.getUsuario()
        tvNombre.text = "Nombre: ${usuario.nombre}"
        tvEdad.text = "Edad: ${usuario.edad}"
        tvDireccion.text = "Dirección: ${usuario.direccion}"
        tvTelefono.text = "Tel: ${usuario.telefono}"

        if (!usuario.imagen.isNullOrEmpty()) {
            val archivo = File(usuario.imagen)
            if (archivo.exists()) {
                imgPerfil.setImageURI(Uri.fromFile(archivo))
                imagePath = usuario.imagen // ✅ mantener ruta actual para futuras ediciones
            }
        }
    }

    private fun cargarHistorial() {
        layoutHistorial.removeAllViews()
        val reportes = dbHelper.getAllReportes()

        if (reportes.isEmpty()) {
            val tvVacio = TextView(this)
            tvVacio.text = "No hay reportes aún"
            tvVacio.setPadding(20, 20, 20, 20)
            layoutHistorial.addView(tvVacio)
            return
        }

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
                intent.putExtra("imagenPath", reporte.foto)
                startActivity(intent)
            }

            layoutHistorial.addView(tvReporte)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarHistorial()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && editMode) {
            val uri = data.data ?: return
            val rutaGuardada = guardarImagenEnInterno(uri)

            if (rutaGuardada != null) {
                imagePath = rutaGuardada // ✅ guardar la ruta absoluta
                imageUri = Uri.fromFile(File(rutaGuardada))
                imgPerfil.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔹 Guarda la imagen seleccionada dentro del almacenamiento interno de la app
    private fun guardarImagenEnInterno(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val nombreArchivo = "perfil_${System.currentTimeMillis()}.jpg"
            val archivoDestino = File(filesDir, nombreArchivo)

            inputStream.use { input ->
                FileOutputStream(archivoDestino).use { output ->
                    input.copyTo(output)
                }
            }
            archivoDestino.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
