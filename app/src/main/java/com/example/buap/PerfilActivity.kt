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
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

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
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val PICK_IMAGE = 100
    private var imageUri: Uri? = null
    private var imagePath: String? = null // ✅ Ruta guardada del archivo interno
    private var editMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        dbHelper = DatabaseHelper(this)
        db = FirebaseFirestore.getInstance() // 🚨 Inicializar Firestore
        auth = FirebaseAuth.getInstance()     // 🚨 Inicializar Auth

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

        cargarDatosUsuario() // 🚨 Llama a la nueva función

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

        val userId = auth.currentUser?.uid ?: return

        val newData = hashMapOf(
            "nombre" to etNombre.text.toString(),
            "edad" to etEdad.text.toString(),
            "direccion" to etDireccion.text.toString(), // Asumiendo que ahora guardas "dirección"
            "telefono" to etTelefono.text.toString(),
            "fotoPerfil" to imagePath // Guarda la ruta local o URL
        )

        db.collection("usuarios").document(userId).update(newData as Map<String, Any>)
            .addOnSuccessListener {
                // Actualizar la UI inmediatamente después de una subida exitosa
                tvNombre.text = "Nombre: ${etNombre.text}"
                tvEdad.text = "Edad: ${etEdad.text}"
                tvDireccion.text = "Correo/Dir: ${etDireccion.text}"
                tvTelefono.text = "Tel: ${etTelefono.text}"

                // 🔹 Reconvertir los campos a TextView (Lógica de tu UI)
                parent.removeAllViews()
                parent.addView(tvNombre)
                parent.addView(tvEdad)
                parent.addView(tvDireccion)
                parent.addView(tvTelefono)
                parent.addView(btnEditar)

                Toast.makeText(this, "Datos guardados y actualizados en Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("PerfilActivity", "Error updating document", e)
            }
    }

    private fun cargarDatosUsuario() {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "No hay sesión activa.", Toast.LENGTH_SHORT).show()
            // Considera redirigir a Login si el usuario es nulo
            return
        }

        val userId = user.uid
        val defaultPhotoUrl = user.photoUrl?.toString() // Foto por defecto de Google/GitHub/Firebase

        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // 1. Cargar datos de Firestore (fuente principal de verdad)
                    val nombre = document.getString("nombre") ?: user.displayName ?: "N/A"
                    val edad = document.getString("edad") ?: "N/A"
                    val direccion = document.getString("direccion") ?: user.email ?: "N/A"
                    val telefono = document.getString("telefono") ?: "N/A"
                    // Nota: Podrías usar el campo 'email' de Firestore o user.email para mostrar el correo
                    val userEmail = user.email // Guardar el email del proveedor

                    // 2. Cargar foto de Firestore (si está subida) o usar la URL del proveedor
                    val fotoPerfilFirestore = document.getString("fotoPerfil")
                    val fotoURL = if (!fotoPerfilFirestore.isNullOrEmpty()) {
                        fotoPerfilFirestore
                    } else {
                        defaultPhotoUrl
                    }

                    // 3. Actualizar la UI
                    tvNombre.text = "Nombre: $nombre"
                    tvEdad.text = "Edad: $edad"
                    // Usar 'Dirección' para mostrar el correo o la dirección guardada
                    tvDireccion.text = "Correo/Dir: $direccion"
                    tvTelefono.text = "Tel: $telefono"

                    // 4. Cargar Imagen con Glide
                    Glide.with(this)
                        .load(fotoURL)
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user) // Mostrar ícono por defecto si falla la carga
                        .into(imgPerfil)

                    // 5. Actualizar la ruta local/URL para el modo edición
                    imagePath = fotoURL

                } else {
                    // Si el documento NO existe, significa que algo falló en LoginActivity.
                    // Mostramos los datos básicos de Firebase Auth.
                    Log.w("PerfilActivity", "Documento de Firestore no encontrado. Usando datos de Auth.")
                    tvNombre.text = "Nombre: ${user.displayName ?: "Usuario Nuevo"}"
                    tvDireccion.text = "Correo: ${user.email ?: "Sin correo"}"
                    // ... el resto sigue como N/A o desconocido ...
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("PerfilActivity", "Error getting document", e)
            }
    }


    private fun cargarHistorial() {
        layoutHistorial.removeAllViews() // Limpiamos la vista

        val userId = auth.currentUser?.uid
        if (userId == null) {
            mostrarMensajeHistorial("Error: Usuario no autenticado.")
            return
        }

        db.collection("reportes")
            .whereEqualTo("userId", userId) // 🚨 FILTRO CLAVE: Solo reportes de este usuario
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    mostrarMensajeHistorial("Aún no has enviado ningún reporte.")
                    return@addOnSuccessListener
                }

                for (document in result) {
                    val nombreReporte = document.getString("nombreReporte") ?: "Reporte sin título"
                    val fecha = document.getString("fecha") ?: "Fecha N/A"
                    val hora = document.getString("hora") ?: "Hora N/A"
                    val documentoId = document.id // ID del documento para pasar a detalle

                    // Creamos y agregamos la vista del reporte
                    crearYAgregarReporteView(nombreReporte, fecha, hora, documentoId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("PerfilActivity", "Error al cargar historial: ", e)
                mostrarMensajeHistorial("Error al cargar tu historial de reportes.")
            }
    }

    // 🚨 Función auxiliar para crear la vista de cada reporte
    private fun crearYAgregarReporteView(nombre: String, fecha: String, hora: String, documentoId: String) {
        val tvReporte = TextView(this).apply {
            text = "$nombre - $fecha $hora"
            setBackgroundResource(R.drawable.rounded_card_light)
            setPadding(20, 20, 20, 20)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 12, 0, 0)
            }
            layoutParams = params

            setOnClickListener {
                // 🚨 MODIFICACIÓN CLAVE AQUÍ:
                val intent = Intent(this@PerfilActivity, DetalleReporteActivity::class.java)

                // Pasamos el ID del documento de Firestore
                intent.putExtra("reporteId", documentoId)

                // Opcional: También puedes pasar el nombre para un Toast rápido
                Toast.makeText(this@PerfilActivity, "Cargando reporte: $nombre", Toast.LENGTH_SHORT).show()

                startActivity(intent)
            }
        }

        layoutHistorial.addView(tvReporte)
    }

    private fun mostrarMensajeHistorial(mensaje: String) {
        val tvVacio = TextView(this)
        tvVacio.text = mensaje
        tvVacio.setPadding(20, 20, 20, 20)
        layoutHistorial.addView(tvVacio)
    }

    override fun onResume() {
        super.onResume()
        cargarHistorial()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && editMode) {
            val uri = data.data ?: return

            // 🚨 Guardar imagen localmente y obtener la ruta (si es una nueva imagen)
            val rutaGuardada = guardarImagenEnInterno(uri)

            if (rutaGuardada != null) {
                // Si la imagen se guardó localmente, esta es la nueva ruta
                imagePath = rutaGuardada
                imgPerfil.setImageURI(Uri.fromFile(File(rutaGuardada)))

                // NOTA: Si deseas que esta imagen de perfil local sea permanente y accesible desde
                // otros dispositivos, DEBERÍAS SUBIRLA A FIREBASE STORAGE aquí.
                // Por simplicidad, solo la guardamos localmente.

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
