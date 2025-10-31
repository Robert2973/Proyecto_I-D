package com.example.buap

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

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

        btnBack.setOnClickListener { finish() }

        imgPerfil.setOnClickListener {
            // Abrir galería para seleccionar imagen
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        btnEditar.setOnClickListener {
            if (!editMode) {
                // Cambiar a modo edición
                activarEdicion(true)
            } else {
                // Guardar cambios
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

    private fun convertirEditTextATextView(et: EditText, label: String): TextView {
        val tv = TextView(this)
        tv.id = et.id
        tv.text = "$label: ${et.text}"
        tv.setTextColor(et.currentTextColor)
        tv.textSize = et.textSize / resources.displayMetrics.scaledDensity
        return tv
    }

    private fun guardarCambios() {
        val parent = findViewById<LinearLayout>(R.id.datosUsuario)
        val etNombre = parent.findViewById<EditText>(R.id.tvNombre)
        val etEdad = parent.findViewById<EditText>(R.id.tvEdad)
        val etDireccion = parent.findViewById<EditText>(R.id.tvDireccion)
        val etTelefono = parent.findViewById<EditText>(R.id.tvTelefono)

        // Actualizar base de datos
        dbHelper.actualizarUsuario(
            etNombre.text.toString(),
            etEdad.text.toString(),
            etDireccion.text.toString(),
            etTelefono.text.toString(),
            imageUri?.toString()
        )

        // Reconvertir a TextView
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

        Toast.makeText(this, "Datos guardados", Toast.LENGTH_SHORT).show()
    }

    private fun cargarDatosUsuario() {
        val usuario = dbHelper.getUsuario()
        tvNombre.text = "Nombre: ${usuario.nombre}"
        tvEdad.text = "Edad: ${usuario.edad}"
        tvDireccion.text = "Dirección: ${usuario.direccion}"
        tvTelefono.text = "Tel: ${usuario.telefono}"
        if (usuario.imagen != null) {
            imgPerfil.setImageURI(Uri.parse(usuario.imagen))
        }
    }

    private fun cargarHistorial() {
        layoutHistorial.removeAllViews()
        val reportes = dbHelper.getAllReportes()
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

        if (reportes.isEmpty()) {
            val tvVacio = TextView(this)
            tvVacio.text = "No hay reportes aún"
            tvVacio.setPadding(20, 20, 20, 20)
            layoutHistorial.addView(tvVacio)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarHistorial()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imgPerfil.setImageURI(imageUri)
        }
    }
}
