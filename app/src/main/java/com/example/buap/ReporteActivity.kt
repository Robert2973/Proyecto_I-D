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


class ReporteActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etFecha: EditText
    private lateinit var etHora: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etRiesgo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnEnviar: Button
    private lateinit var ivBack: ImageView

    private lateinit var btnTomarFoto: Button
    private lateinit var imgFoto: ImageView
    private lateinit var tvPlaceholder: TextView

    // 🚨 INSTANCIAS DE FIREBASE
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var photoUri: Uri? = null // Usaremos Uri para subir a Storage

    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_reporte)

        // 🚨 Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

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

        btnEnviar.setOnClickListener {
            enviarReporte()
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
        Toast.makeText(this, "Enviando reporte...", Toast.LENGTH_SHORT).show()

        // Referencia de Storage: reportes/UID/timestamp.jpg
        val imageFileName = "${System.currentTimeMillis()}.jpg"
        val imageRef = storage.reference.child("reportes/$userId/$imageFileName")

        imageRef.putFile(imagenUri)
            .addOnSuccessListener {
                // Obtener la URL pública de la imagen
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveReportToFirestore(userId, nombre, fecha, hora, direccion, riesgo, descripcion, uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ReporteActivity", "Error al subir imagen", e)
            }
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
        val reporteData = hashMapOf(
            "userId" to userId, // 🚨 ESTO ASOCIA EL REPORTE AL USUARIO
            "nombreReporte" to nombre,
            "fecha" to fecha,
            "hora" to hora,
            "direccion" to direccion,
            "riesgo" to riesgo,
            "descripcion" to descripcion,
            "fotoURL" to (fotoURL ?: ""),
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("reportes").add(reporteData)
            .addOnSuccessListener {
                Toast.makeText(this, "Reporte enviado correctamente", Toast.LENGTH_SHORT).show()
                limpiarCampos()
                finish() // Cierra la activity después de enviar
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el reporte: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ReporteActivity", "Error al guardar en Firestore", e)
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
