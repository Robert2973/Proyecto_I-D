package com.example.buap

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth // 🚨 NUEVO: Para obtener el UID
import com.google.firebase.firestore.FirebaseFirestore // 🚨 NUEVO: Para guardar datos
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage // 🚨 NUEVO: Para subir imágenes
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log // Para depuración
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import android.view.View


class ReporteActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etFecha: EditText
    private lateinit var etHora: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etRiesgo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnEnviar: Button
    private lateinit var layoutCarga: LinearLayout
    private lateinit var tvEstadoCarga: TextView
    private lateinit var ivBack: ImageView

    private lateinit var btnTomarFoto: Button
    private lateinit var imgFoto: ImageView
    private lateinit var tvPlaceholder: TextView

    // 🚨 INSTANCIAS DE FIREBASE
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var photoUri: Uri? = null // Usaremos Uri para subir a Storage

    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_reporte)

        // 🚨 Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val config: MutableMap<String, String> = HashMap()
        config["cloud_name"] = "dde3jjlyv"
        config["api_key"] = "569112689544813" // 🚨 Asegúrate de añadir esta línea
        config["api_secret"] = "rNaYSg1_J4psDjSqYNjukN2uv6" // 🚨 Y esta línea

        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            Log.e("Cloudinary", "Error al inicializar Cloudinary: ${e.message}")
        }

        // ❌ Eliminamos la inicialización de DatabaseHelper, ya no se usa para guardar reportes
        // dbHelper = DatabaseHelper(this)

        etNombre = findViewById(R.id.etNombre)
        etFecha = findViewById(R.id.etFecha)
        etHora = findViewById(R.id.etHora)
        etDireccion = findViewById(R.id.etDireccion)
        etRiesgo = findViewById(R.id.etRiesgo)
        etDescripcion = findViewById(R.id.etDescripcion)
        btnEnviar = findViewById(R.id.btnEnviar)
        ivBack = findViewById(R.id.imageViewBack)

        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        imgFoto = findViewById(R.id.imgFoto)
        tvPlaceholder = findViewById(R.id.tvPlaceholder)

        // ← Aquí recibe la dirección desde MapsActivity
        val direccionSeleccionada = intent.getStringExtra("direccionSeleccionada")
        if (!direccionSeleccionada.isNullOrEmpty()) {
            etDireccion.setText(direccionSeleccionada)
        }

        ivBack.setOnClickListener { finish() }

        // Fecha
        etFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                val mes = m + 1
                etFecha.setText(String.format("%02d/%02d/%04d", d, mes, y))
            }, year, month, day)

            datePicker.show()
        }

        // Hora
        etHora.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, { _, h, m ->
                etHora.setText(String.format("%02d:%02d", h, m))
            }, hour, minute, true)

            timePicker.show()
        }

        btnTomarFoto.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        // 🚨 Obtener referencias a los elementos de carga
        layoutCarga = findViewById(R.id.layoutCarga)
        tvEstadoCarga = findViewById(R.id.tvEstadoCarga)

        btnEnviar.setOnClickListener {
            enviarReporte()
        }
    }

    private fun mostrarCarga(mostrar: Boolean, mensaje: String? = null) {
        if (mostrar) {
            layoutCarga.visibility = View.VISIBLE // ⬅️ Mostrar
            btnEnviar.isEnabled = false // ⬅️ Desactivar botón
            if (mensaje != null) {
                tvEstadoCarga.text = mensaje
            }
        } else {
            layoutCarga.visibility = View.GONE // ⬅️ Ocultar
            btnEnviar.isEnabled = true // ⬅️ Reactivar botón
        }
    }
    private fun checkCameraPermissionAndOpen() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, "Error al crear el archivo", Toast.LENGTH_SHORT).show()
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraLauncher.launch(intent)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(null)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val file = File(currentPhotoPath ?: return@registerForActivityResult)

            // 🚨 Guardamos la Uri del archivo local para la subida a Storage
            photoUri = Uri.fromFile(file)

            imgFoto.setImageURI(photoUri)
            tvPlaceholder.visibility = TextView.GONE
        }
    }

    private fun enviarReporte() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Debe iniciar sesión para enviar un reporte.", Toast.LENGTH_SHORT).show()
            return
        }

        val nombre = etNombre.text.toString()
        val fecha = etFecha.text.toString()
        val hora = etHora.text.toString()
        val direccion = etDireccion.text.toString()
        val riesgo = etRiesgo.text.toString()
        val descripcion = etDescripcion.text.toString()

        // Usamos photoUri para chequear la foto, que es la Uri local
        if (nombre.isEmpty() || fecha.isEmpty() || hora.isEmpty() ||
            direccion.isEmpty() || riesgo.isEmpty() || descripcion.isEmpty() || photoUri == null
        ) {
            Toast.makeText(this, "Por favor completa todos los campos y toma una foto", Toast.LENGTH_SHORT).show()
            return
        }

        mostrarCarga(true, "Iniciando validación...")

        // 🚨 Iniciar el proceso de subida y guardado
        uploadImageAndSaveReport(user.uid, photoUri!!, nombre, fecha, hora, direccion, riesgo, descripcion)
    }

    private fun uploadImageAndSaveReport(
        userId: String,
        imagenUri: Uri,
        nombre: String,
        fecha: String,
        hora: String,
        direccion: String,
        riesgo: String,
        descripcion: String
    ) {
        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show()

        // 🚨 Usar el nombre del preset configurado en la consola
        val uploadPreset = "android_reports"

        // Opcionalmente, puedes seguir usando la carpeta del usuario
        val folderName = "app_reports/$userId"

        MediaManager.get().upload(imagenUri)
            .option("resource_type", "image")
            .option("folder", folderName)
            .unsigned(uploadPreset)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    mostrarCarga(true, "Subiendo imagen...")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progreso = (bytes.toFloat() / totalBytes.toFloat() * 100).toInt()
                    mostrarCarga(true, "Subiendo imagen... $progreso%")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as String

                    // 🚨 ACTUALIZAR MENSAJE: antes de guardar en Firestore
                    mostrarCarga(true, "Imagen subida. Guardando reporte...")
                    saveReportToFirestore(userId, nombre, fecha, hora, direccion, riesgo, descripcion, imageUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    // 🚨 OCULTAR CARGA EN CASO DE ERROR
                    mostrarCarga(false)
                    Toast.makeText(this@ReporteActivity, "Error al subir imagen: ${error.description}", Toast.LENGTH_LONG).show()
                    Log.e("Cloudinary", "Upload Error: ${error.description}")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun saveReportToFirestore(
        userId: String,
        nombre: String,
        fecha: String,
        hora: String,
        direccion: String,
        riesgo: String,
        descripcion: String,
        fotoURL: String?
    ) {
        // ... (La implementación de esta función se mantiene sin cambios)
        val reporteData = hashMapOf(
            "userId" to userId,
            "nombreReporte" to nombre,
            "fecha" to fecha,
            "hora" to hora,
            "direccion" to direccion,
            "riesgo" to riesgo,
            "descripcion" to descripcion,
            "fotoURL" to (fotoURL ?: ""), // Guarda la URL de Cloudinary
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("reportes").add(reporteData)
            .addOnSuccessListener {
                mostrarCarga(false)
                Toast.makeText(this, "Reporte enviado correctamente", Toast.LENGTH_SHORT).show()
                limpiarCampos()

                // 🚨 NAVEGACIÓN A MAIN ACTIVITY
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                finish()
            }
            .addOnFailureListener { e ->
                mostrarCarga(false)
                Toast.makeText(this, "Error al guardar el reporte: ${e.message}", Toast.LENGTH_LONG).show()
                // ...
            }
    }

    private fun limpiarCampos() {
        etNombre.text.clear()
        etFecha.text.clear()
        etHora.text.clear()
        etDireccion.text.clear()
        etRiesgo.text.clear()
        etDescripcion.text.clear()
        imgFoto.setImageDrawable(null)
        tvPlaceholder.visibility = TextView.VISIBLE
        currentPhotoPath = null
        photoUri = null // Limpiar también la Uri de la foto
    }
}
