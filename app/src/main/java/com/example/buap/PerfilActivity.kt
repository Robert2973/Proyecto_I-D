package com.example.buap

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import com.google.firebase.auth.FirebaseAuth
import com.bumptech.glide.Glide

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

        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            mostrarDialogoCerrarSesion()
        }

    }

    private fun mostrarDialogoCerrarSesion() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cerrar sesión")
        builder.setMessage("¿Estás seguro de que quieres cerrar sesión?")

        builder.setPositiveButton("Sí, salir") { dialog, _ ->
            // Cerrar sesión de Firebase
            FirebaseAuth.getInstance().signOut()

            // Cerrar sesión de Google (si es el caso)
            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
                this,
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                ).build()
            )
            googleSignInClient.signOut()

            // 🔹 Eliminar los datos del usuario local
            val dbHelper = DatabaseHelper(this)
            dbHelper.limpiarUsuario()

            // 🔹 Redirigir al login y limpiar el back stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

        // Personalización opcional de los botones
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(resources.getColor(android.R.color.holo_red_dark))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(resources.getColor(android.R.color.darker_gray))
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
            imagePath // ✅ ruta o URL según caso
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

        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val nombre = user.displayName ?: "Usuario Google"
            val correo = user.email ?: "Sin correo"
            val foto = user.photoUrl?.toString()

            dbHelper.limpiarUsuario()

            dbHelper.registrarUsuarioDesdeGoogle(nombre, correo, foto)

            tvNombre.text = "Nombre: $nombre"
            tvEdad.text = "Edad: Desconocida"
            tvDireccion.text = "Correo: $correo"
            tvTelefono.text = "Tel: No registrado"

            // ✅ Guardar en variable global para cargar en Main luego
            imagePath = foto

            Glide.with(this)
                .load(foto)
                .placeholder(R.drawable.ic_user)
                .into(imgPerfil)

        } else {
            // Si no hay sesión de Google, cargar datos locales
            val usuario = dbHelper.getUsuario()
            tvNombre.text = "Nombre: ${usuario.nombre}"
            tvEdad.text = "Edad: ${usuario.edad}"
            tvDireccion.text = "Dirección: ${usuario.direccion}"
            tvTelefono.text = "Tel: ${usuario.telefono}"

            if (!usuario.imagen.isNullOrEmpty()) {
                val archivo = File(usuario.imagen)
                if (archivo.exists()) {
                    imgPerfil.setImageURI(Uri.fromFile(archivo))
                    imagePath = usuario.imagen
                }
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
